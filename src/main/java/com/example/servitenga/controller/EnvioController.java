package com.example.servitenga.controller;

import com.example.servitenga.dto.EnvioRequestDTO;
import com.example.servitenga.model.Envio;
import com.example.servitenga.service.EnvioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService service;

    public EnvioController(EnvioService service) {
        this.service = service;
    }

    // GET - listar todos
    @GetMapping
    public ResponseEntity<List<Envio>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // GET con PathVariable - buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Envio> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET con RequestParam - filtrar por ciudad o estado
    @GetMapping("/buscar")
    public ResponseEntity<List<Envio>> buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String estado) {

        if (ciudad != null) {
            return ResponseEntity.ok(service.buscarPorCiudad(ciudad));
        }
        if (estado != null) {
            return ResponseEntity.ok(service.buscarPorEstado(estado));
        }
        return ResponseEntity.badRequest().build();
    }

    // POST con RequestBody + DTO
    @PostMapping
    public ResponseEntity<Envio> crear(@Valid @RequestBody EnvioRequestDTO dto) {
        Envio creado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}