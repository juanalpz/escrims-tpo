package com.escrims.infrastructure.persistence.memory;

import com.escrims.domain.entities.Usuario;
import com.escrims.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de usuarios. Conserva las
 * entidades de dominio con su historial de sanciones.
 */
@Repository
public class InMemoryUsuarioRepository implements UsuarioRepository {

    private final Map<UUID, Usuario> usuarios = new ConcurrentHashMap<>();

    @Override
    public void guardar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return usuarios.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public boolean existePorUsername(String username) {
        return username != null && usuarios.values().stream()
                .anyMatch(u -> username.equalsIgnoreCase(u.getUsername()));
    }
}
