package com.escrims.domain.repository;

import com.escrims.domain.entities.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Define el contrato de persistencia y consulta de usuarios
 * mediante el patrón Repository.
 */
public interface UsuarioRepository {

    /**
     * Guarda o actualiza un usuario en el repositorio.
     *
     * @param usuario usuario que se desea persistir
     */
    void guardar(Usuario usuario);

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<Usuario> buscarPorId(UUID id);

    /**
     * Busca un usuario por su correo electrónico, sin distinguir mayúsculas.
     *
     * @param email correo electrónico registrado
     * @return Optional con el usuario encontrado o vacío si no existe
     */
    Optional<Usuario> buscarPorEmail(String email);

    /**
     * Verifica si un nombre de usuario ya está en uso, sin distinguir mayúsculas.
     *
     * @param username nombre de usuario que se desea consultar
     * @return true si ya existe un usuario con ese nombre
     */
    boolean existePorUsername(String username);
}
