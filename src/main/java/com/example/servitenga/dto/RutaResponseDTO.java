package com.example.servitenga.dto;

import java.util.List;
import java.util.Map;

/**
 * Respuesta simplificada con los datos de ruta para el conductor.
 */
public record RutaResponseDTO(
        Long envioId,
        String ciudadOrigen,
        String ciudadDestino,
        double distanciaMetros,
        double duracionSegundos,
        List<Map<String, Object>> pasos,
        String geometriaGeoJson
) {
}