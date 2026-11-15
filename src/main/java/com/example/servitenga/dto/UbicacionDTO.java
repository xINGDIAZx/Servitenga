package com.example.servitenga.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar la posicion (tracking) de un envio.
 */
public record UbicacionDTO(
    @NotNull(message = "La latitud es obligatoria") Double latitud,
    @NotNull(message = "La longitud es obligatoria") Double longitud
) {}
