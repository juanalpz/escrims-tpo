package com.escrims.controller;

import com.escrims.controller.dto.CrearScrimRequest;
import com.escrims.controller.dto.PostulacionRequest;
import com.escrims.controller.exception.ErrorResponse;
import com.escrims.controller.exception.GlobalExceptionHandler;
import com.escrims.controller.exception.RecursoNoEncontradoException;
import com.escrims.domain.entities.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;
    private Validator validator;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/scrims");
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("DTO CrearScrimRequest: detecta latencia fuera de rango, fecha pasada y campos nulos")
    void deberiaValidarRestriccionesEnCrearScrimRequest() {
        CrearScrimRequest dtoInvalido = new CrearScrimRequest(
                null, // organizadorId nulo
                Juego.VALORANT,
                Formato.CINCO_VS_CINCO,
                Region.LAS,
                Rango.ORO,
                Rango.PLATINO,
                850, // latencia > 500 ms (RN-04)
                LocalDateTime.now().minusHours(1), // fecha pasada
                60,
                Modalidad.RANKED_LIKE,
                Map.of());

        Set<ConstraintViolation<CrearScrimRequest>> violaciones = validator.validate(dtoInvalido);
        assertEquals(3, violaciones.size(), "Debe detectar 3 errores de validación en el DTO.");
    }

    @Test
    @DisplayName("DTO PostulacionRequest: pasa validación cuando usuarioId y rolDeseado son válidos")
    void deberiaAceptarPostulacionRequestValida() {
        PostulacionRequest dtoValido = new PostulacionRequest(UUID.randomUUID(), RolJuego.DUELIST);
        assertTrue(validator.validate(dtoValido).isEmpty());
    }

    @Test
    @DisplayName("GlobalExceptionHandler: mapea IllegalArgumentException a 400 Bad Request")
    void deberiaDevolver400ParaIllegalArgumentException() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("El rango del jugador está fuera del límite permitido (RN-03)."),
                request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertTrue(response.getBody().mensaje().contains("RN-03"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: mapea RecursoNoEncontradoException a 404 Not Found")
    void deberiaDevolver404ParaRecursoNoEncontrado() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new RecursoNoEncontradoException("No se encontró el scrim solicitado."),
                request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
    }

    @Test
    @DisplayName("GlobalExceptionHandler: mapea IllegalStateException a 409 Conflict")
    void deberiaDevolver409ParaIllegalStateException() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(
                new IllegalStateException("No se puede cancelar un scrim que ya está en juego."),
                request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
    }

    @Test
    @DisplayName("GlobalExceptionHandler: mapea Exception no controlada a 500 Internal Server Error")
    void deberiaDevolver500ParaErrorInesperado() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new RuntimeException("Fallo inesperado de conexión"),
                request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
    }
}
