package com.escrims.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para autenticar las credenciales de un usuario en el sistema (CU2).
 *
 * @param email    Correo electrónico registrado del usuario.
 * @param password Contraseña ingresada para validar el inicio de sesión.
 */
public record LoginRequest(
        @NotBlank(message = "El email es obligatorio.") @Email(message = "El formato del email no es válido.") String email,

        @NotBlank(message = "La contraseña es obligatoria.") String password) {
}
