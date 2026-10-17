package com.example.servitenga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record EnvioRequestDTO(
    @NotBlank String remitente,
    @NotBlank String destinatario,
    @NotBlank String ciudadOrigen,
    @NotBlank String ciudadDestino,
    @Positive double pesoKg
) {}