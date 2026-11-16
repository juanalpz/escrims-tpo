package com.escrims.controller.dto;

import com.escrims.domain.entities.RolSistema;
import com.escrims.domain.entities.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida con los datos públicos de una cuenta y su situación disciplinaria.
 *
 * @param id              Identificador del usuario.
 * @param username        Nombre de usuario.
 * @param email           Correo electrónico registrado.
 * @param emailVerificado Indica si el email fue verificado (RN-01).
 * @param rol             Rol de sistema (USER, MOD, ADMIN).
 * @param strikes         Strikes acumulados.
 * @param cooldownHasta   Fin del cooldown vigente, o null si puede participar (RN-06).
 */
public record UsuarioResponse(
        UUID id,
        String username,
        String email,
        boolean emailVerificado,
        RolSistema rol,
        int strikes,
        LocalDateTime cooldownHasta) {

    public static UsuarioResponse fromDomain(Usuario usuario, RolSistema rol) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(),
                usuario.isEmailVerificado(), rol, usuario.getStrikes(),
                usuario.getCooldownHasta(LocalDateTime.now()));
    }
}
