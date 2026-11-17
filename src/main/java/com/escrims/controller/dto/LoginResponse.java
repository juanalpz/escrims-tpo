package com.escrims.controller.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida de un inicio de sesión correcto (CU2).
 *
 * @param token   Token a enviar en el encabezado {@code Authorization: Bearer <token>}.
 * @param expira  Momento en que el token deja de ser válido.
 * @param usuario Cuenta autenticada.
 */
public record LoginResponse(String token, LocalDateTime expira, UsuarioResponse usuario) {
}
