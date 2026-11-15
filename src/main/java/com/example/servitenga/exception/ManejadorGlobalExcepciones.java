package com.example.servitenga.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido",
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("marcaTiempo", LocalDateTime.now().toString());
        cuerpo.put("estado", HttpStatus.BAD_REQUEST.value());
        cuerpo.put("error", "Solicitud inválida");
        cuerpo.put("mensaje", "Error de validación en los datos enviados");
        cuerpo.put("errores", errores);
        cuerpo.put("ruta", request.getRequestURI());
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarEstado(ResponseStatusException ex,
                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String mensaje = ex.getReason() != null ? ex.getReason() : "Ocurrió un error al procesar la solicitud";

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("marcaTiempo", LocalDateTime.now().toString());
        cuerpo.put("estado", status.value());
        cuerpo.put("error", traducirError(status));
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("ruta", request.getRequestURI());
        return ResponseEntity.status(status).body(cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarGeneral(Exception ex, HttpServletRequest request) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("marcaTiempo", LocalDateTime.now().toString());
        cuerpo.put("estado", HttpStatus.INTERNAL_SERVER_ERROR.value());
        cuerpo.put("error", "Error interno del servidor");
        cuerpo.put("mensaje", "Ocurrió un error inesperado al procesar la solicitud");
        cuerpo.put("ruta", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cuerpo);
    }

    private String traducirError(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Solicitud inválida";
            case NOT_FOUND -> "No encontrado";
            case BAD_GATEWAY -> "Error de puerta de enlace";
            case SERVICE_UNAVAILABLE -> "Servicio no disponible";
            case INTERNAL_SERVER_ERROR -> "Error interno del servidor";
            default -> status.getReasonPhrase();
        };
    }
}