package com.escrims.controller.dto;

import com.escrims.domain.entities.Juego;
import com.escrims.domain.entities.Rango;
import com.escrims.domain.entities.Region;
import com.escrims.domain.entities.RolJuego;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO de entrada para registrar una nueva cuenta de usuario y su perfil inicial en la plataforma (CU1, RN-02).
 *
 * @param username        Nombre de usuario único dentro de la plataforma (entre 3 y 30 caracteres).
 * @param email           Correo electrónico de contacto y verificación de la cuenta.
 * @param password        Contraseña ingresada para el alta del usuario (entre 6 y 64 caracteres).
 * @param juegoPrincipal  Videojuego principal configurado en el perfil del jugador.
 * @param rangoActual     Rango o nivel competitivo actual del usuario.
 * @param region          Servidor o región geográfica habitual del jugador.
 * @param rolesPreferidos Conjunto opcional de roles preferidos dentro del juego.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio.") @Size(min = 3, max = 30, message = "El username debe tener entre 3 y 30 caracteres.") String username,

        @NotBlank(message = "El email es obligatorio.") @Email(message = "El formato del email no es válido.") String email,

        @NotBlank(message = "La contraseña es obligatoria.") @Size(min = 6, max = 64, message = "La contraseña debe tener al menos 6 caracteres.") String password,

        @NotNull(message = "El juego principal es obligatorio.") Juego juegoPrincipal,

        @NotNull(message = "El rango actual es obligatorio.") Rango rangoActual,

        @NotNull(message = "La región es obligatoria.") Region region,

        Set<RolJuego> rolesPreferidos) {
}
