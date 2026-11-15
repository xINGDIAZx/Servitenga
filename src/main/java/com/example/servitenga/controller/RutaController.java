package com.example.servitenga.controller;

import com.example.servitenga.dto.RutaResponseDTO;
import com.example.servitenga.service.EnvioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios/{id}/ruta")
public class RutaController {

    private final EnvioService envioService;

    public RutaController(EnvioService envioService) {
        this.envioService = envioService;
    }

    // GET - Calcula y devuelve la mejor ruta para el conductor (origen -> destino)
    @GetMapping
    public ResponseEntity<RutaResponseDTO> obtenerRuta(@PathVariable Long id) {
        return ResponseEntity.ok(envioService.calcularRuta(id));
    }
}