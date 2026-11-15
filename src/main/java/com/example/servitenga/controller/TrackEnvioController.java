package com.example.servitenga.controller;

import com.example.servitenga.dto.UbicacionDTO;
import com.example.servitenga.model.TrackEnvio;
import com.example.servitenga.service.EnvioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/envios/{id}/track")
public class TrackEnvioController {

    private final EnvioService service;

    public TrackEnvioController(EnvioService service) {
        this.service = service;
    }

    // POST - Registra un punto (latitud, longitud) en el recorrido del envio (201 Created)
    @PostMapping
    public ResponseEntity<TrackEnvio> registrar(@PathVariable Long id,
                                                @Valid @RequestBody UbicacionDTO dto) {
        TrackEnvio punto = service.registrarTrack(id, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado"));
        return ResponseEntity.status(HttpStatus.CREATED).body(punto);
    }

    // GET - Devuelve el recorrido completo del envio, en orden cronologico (200 OK o 404 Not Found)
    @GetMapping
    public ResponseEntity<List<TrackEnvio>> listar(@PathVariable Long id) {
        List<TrackEnvio> track = service.listarTrack(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado"));
        return ResponseEntity.ok(track);
    }
}
