package com.escrims.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para verificar el email de una cuenta recién registrada (RN-01).
 *
 * @param email  Correo electrónico con el que se registró la cuenta.
 * @param codigo Código de verificación devuelto por el registro.
 */
public record VerificarEmailRequest(
        @NotBlank(message = "El email es obligatorio.") @Email(message = "El formato del email no es válido.") String email,

        @NotBlank(message = "El código de verificación es obligatorio.") String codigo) {
}
