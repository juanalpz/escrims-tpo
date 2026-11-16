package com.escrims.controller.dto;

/**
 * DTO de salida del alta de una cuenta (CU1).
 *
 * @param usuario            Cuenta creada, todavía sin verificar.
 * @param codigoVerificacion Código para {@code POST /api/auth/verificar-email}. Se devuelve en la
 *                           respuesta porque el entorno no tiene un proveedor de email real.
 */
public record RegistroResponse(UsuarioResponse usuario, String codigoVerificacion) {
}
