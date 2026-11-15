package com.example.servitenga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record EnvioRequestDTO(
    @NotBlank(message = "El remitente es obligatorio") String remitente,
    @NotBlank(message = "El destinatario es obligatorio") String destinatario,
    @NotBlank(message = "La ciudad de origen es obligatoria") String ciudadOrigen,
    @NotBlank(message = "La ciudad de destino es obligatoria") String ciudadDestino,
    @Positive(message = "El peso debe ser mayor que 0") double pesoKg
) {}
