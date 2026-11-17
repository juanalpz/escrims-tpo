package com.escrims.controller;

import com.escrims.controller.dto.LoginRequest;
import com.escrims.controller.dto.LoginResponse;
import com.escrims.controller.dto.RegisterRequest;
import com.escrims.controller.dto.RegistroResponse;
import com.escrims.controller.dto.UsuarioResponse;
import com.escrims.controller.dto.VerificarEmailRequest;
import com.escrims.domain.entities.Usuario;
import com.escrims.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de alta de cuentas, verificación de email e inicio de sesión (CU1, CU2).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /**
     * Registra una cuenta nueva. El email queda pendiente de verificación.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse register(@Valid @RequestBody RegisterRequest request) {
        AuthService.Registro registro = auth.registrar(request.username(), request.email(), request.password(),
                request.juegoPrincipal(), request.rangoActual(), request.region(), request.rolesPreferidos());
        return new RegistroResponse(respuesta(registro.usuario()), registro.codigoVerificacion());
    }

    /**
     * Verifica el email de una cuenta con el código entregado en el registro (RN-01).
     */
    @PostMapping("/verificar-email")
    public UsuarioResponse verificarEmail(@Valid @RequestBody VerificarEmailRequest request) {
        return respuesta(auth.verificarEmail(request.email(), request.codigo()));
    }

    /**
     * Inicia sesión y devuelve el token para el encabezado {@code Authorization}.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.Sesion sesion = auth.login(request.email(), request.password());
        return new LoginResponse(sesion.token(), sesion.expira(), respuesta(sesion.usuario()));
    }

    /**
     * Devuelve la cuenta dueña del token enviado.
     */
    @GetMapping("/me")
    public UsuarioResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return respuesta(auth.autenticar(authorization));
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        return UsuarioResponse.fromDomain(usuario, auth.rolDe(usuario));
    }
}
