package com.example.servitenga.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar la posicion actual (tracking) de un envio.
 */
public record UbicacionDTO(
    @NotNull Double latitud,
    @NotNull Double longitud
) {}
