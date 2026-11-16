package com.escrims.service;

import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.RolJuego;
import com.escrims.domain.entities.RolSistema;
import com.escrims.domain.entities.Usuario;
import com.escrims.domain.repository.UsuarioRepository;
import com.escrims.service.exception.AccesoDenegadoException;
import com.escrims.service.exception.NoAutenticadoException;
import com.escrims.service.exception.NoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para el alta de cuentas, la verificación de email
 * y el inicio de sesión con tokens opacos (CU1, CU2, RN-01).
 *
 * <p>Las contraseñas se guardan como hash PBKDF2 con sal por usuario y las
 * sesiones viven en memoria: se pierden al reiniciar la aplicación.</p>
 */
@Service
public class AuthService {

    private static final String PREFIJO_BEARER = "Bearer ";
    private static final Duration DURACION_SESION = Duration.ofHours(12);
    private static final int ITERACIONES = 120_000;
    private static final int BITS_HASH = 256;

    private record Credencial(byte[] sal, byte[] hash) {
    }

    private record SesionActiva(UUID usuarioId, LocalDateTime expira) {
    }

    /**
     * Resultado del alta de una cuenta.
     *
     * @param usuario            usuario recién creado, todavía sin verificar
     * @param codigoVerificacion código que debe presentarse para verificar el email
     */
    public record Registro(Usuario usuario, String codigoVerificacion) {
    }

    /**
     * Sesión emitida tras un inicio de sesión correcto.
     *
     * @param token   token opaco a enviar en {@code Authorization: Bearer <token>}
     * @param usuario usuario autenticado
     * @param rol     rol de sistema del usuario
     * @param expira  momento en que el token deja de ser válido
     */
    public record Sesion(String token, Usuario usuario, RolSistema rol, LocalDateTime expira) {
    }

    private final UsuarioRepository usuarios;
    private final Set<String> emailsModeradores;
    private final SecureRandom random = new SecureRandom();
    private final Map<UUID, Credencial> credenciales = new ConcurrentHashMap<>();
    private final Map<UUID, RolSistema> roles = new ConcurrentHashMap<>();
    private final Map<UUID, String> codigosVerificacion = new ConcurrentHashMap<>();
    private final Map<String, SesionActiva> sesiones = new ConcurrentHashMap<>();

    /**
     * Inicializa el servicio.
     *
     * @param usuarios          repositorio de usuarios
     * @param emailsModeradores lista separada por comas de los emails que reciben
     *                          el rol {@link RolSistema#MOD} al registrarse
     */
    public AuthService(UsuarioRepository usuarios,
                       @Value("${escrims.auth.moderadores:}") String emailsModeradores) {
        this.usuarios = usuarios;
        this.emailsModeradores = Arrays.stream((emailsModeradores == null ? "" : emailsModeradores).split(","))
                .map(e -> e.trim().toLowerCase(Locale.ROOT))
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Da de alta una cuenta con su perfil competitivo inicial (CU1, RN-02).
     * El email queda sin verificar hasta que se presente el código devuelto.
     *
     * @return el usuario creado y su código de verificación
     * @throws IllegalStateException si el email o el nombre de usuario ya están registrados
     */
    public synchronized Registro registrar(String username, String email, String password,
                                           Juego juegoPrincipal, Rango rangoActual, Region region,
                                           Set<RolJuego> rolesPreferidos) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (usuarios.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new IllegalStateException("Ya existe una cuenta registrada con ese email.");
        }
        if (usuarios.existePorUsername(username.trim())) {
            throw new IllegalStateException("El nombre de usuario ya está en uso.");
        }

        int mmr = (rangoActual.getMmrMin() + rangoActual.getMmrMax()) / 2;
        Usuario usuario = new Usuario(username.trim(), mmr, 0, region);
        usuario.setEmail(emailNormalizado);
        usuario.asignarRango(juegoPrincipal, rangoActual);
        usuario.setRolesPreferidos(rolesPreferidos);

        byte[] sal = new byte[16];
        random.nextBytes(sal);
        credenciales.put(usuario.getId(), new Credencial(sal, hash(password, sal)));
        roles.put(usuario.getId(),
                emailsModeradores.contains(emailNormalizado) ? RolSistema.MOD : RolSistema.USER);

        String codigo = tokenAleatorio(9);
        codigosVerificacion.put(usuario.getId(), codigo);
        usuarios.guardar(usuario);
        return new Registro(usuario, codigo);
    }

    /**
     * Marca el email de una cuenta como verificado (RN-01).
     *
     * @throws IllegalArgumentException si el email no existe o el código no coincide
     */
    public synchronized Usuario verificarEmail(String email, String codigo) {
        Usuario usuario = usuarios.buscarPorEmail(email == null ? null : email.trim()).orElse(null);
        String esperado = usuario == null ? null : codigosVerificacion.get(usuario.getId());
        if (usuario != null && usuario.isEmailVerificado()) {
            return usuario;
        }
        if (esperado == null || codigo == null
                || !MessageDigest.isEqual(esperado.getBytes(), codigo.trim().getBytes())) {
            throw new IllegalArgumentException("El email o el código de verificación no son válidos.");
        }
        usuario.setEmailVerificado(true);
        codigosVerificacion.remove(usuario.getId());
        usuarios.guardar(usuario);
        return usuario;
    }

    /**
     * Valida las credenciales y emite un token de sesión (CU2).
     *
     * @throws NoAutenticadoException si el email o la contraseña son incorrectos
     */
    public Sesion login(String email, String password) {
        Usuario usuario = usuarios.buscarPorEmail(email == null ? null : email.trim()).orElse(null);
        Credencial credencial = usuario == null ? null : credenciales.get(usuario.getId());
        if (credencial == null || !MessageDigest.isEqual(credencial.hash(), hash(password, credencial.sal()))) {
            throw new NoAutenticadoException("Email o contraseña incorrectos.");
        }
        String token = tokenAleatorio(32);
        LocalDateTime expira = LocalDateTime.now().plus(DURACION_SESION);
        sesiones.put(token, new SesionActiva(usuario.getId(), expira));
        return new Sesion(token, usuario, rolDe(usuario), expira);
    }

    /**
     * Resuelve el usuario dueño del token recibido en el encabezado {@code Authorization}.
     *
     * @param encabezadoAuthorization valor completo del encabezado ({@code Bearer <token>})
     * @return usuario autenticado
     * @throws NoAutenticadoException si falta el encabezado o el token es inválido o expiró
     */
    public Usuario autenticar(String encabezadoAuthorization) {
        if (encabezadoAuthorization == null || !encabezadoAuthorization.startsWith(PREFIJO_BEARER)) {
            throw new NoAutenticadoException("Falta el encabezado Authorization: Bearer <token>.");
        }
        String token = encabezadoAuthorization.substring(PREFIJO_BEARER.length()).trim();
        SesionActiva sesion = sesiones.get(token);
        if (sesion == null || sesion.expira().isBefore(LocalDateTime.now())) {
            sesiones.remove(token);
            throw new NoAutenticadoException("La sesión no es válida o expiró.");
        }
        return usuarios.buscarPorId(sesion.usuarioId())
                .orElseThrow(() -> new NoAutenticadoException("La sesión no es válida o expiró."));
    }

    /**
     * Busca un usuario registrado por su identificador.
     *
     * @throws NoEncontradoException si no existe
     */
    public Usuario buscarUsuario(UUID id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado: " + id));
    }

    /**
     * Obtiene el rol de sistema de un usuario.
     */
    public RolSistema rolDe(Usuario usuario) {
        return roles.getOrDefault(usuario.getId(), RolSistema.USER);
    }

    /**
     * Indica si el usuario puede moderar: rol MOD o ADMIN con email verificado.
     */
    public boolean esModerador(Usuario usuario) {
        return usuario != null && usuario.isEmailVerificado() && rolDe(usuario) != RolSistema.USER;
    }

    /**
     * @throws AccesoDenegadoException si el usuario no es moderador
     */
    public void exigirModerador(Usuario usuario) {
        if (!esModerador(usuario)) {
            throw new AccesoDenegadoException("La operación requiere rol de moderador.");
        }
    }

    /**
     * Verifica que el usuario autenticado sea el mismo que figura en la petición.
     *
     * @throws AccesoDenegadoException si los identificadores no coinciden
     */
    public void exigirIdentidad(Usuario autenticado, UUID idDeclarado) {
        if (!autenticado.getId().equals(idDeclarado)) {
            throw new AccesoDenegadoException("No se puede operar en nombre de otro usuario.");
        }
    }

    private String tokenAleatorio(int bytes) {
        byte[] buffer = new byte[bytes];
        random.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private static byte[] hash(String password, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), sal, ITERACIONES, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
