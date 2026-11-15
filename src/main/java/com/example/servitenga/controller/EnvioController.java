package com.example.servitenga.controller;

import com.example.servitenga.dto.EnvioRequestDTO;
import com.example.servitenga.model.Envio;
import com.example.servitenga.service.EnvioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService service;

    public EnvioController(EnvioService service) {
        this.service = service;
    }

    // POST - Crear un nuevo envio (201 Created)
    @PostMapping
    public ResponseEntity<Envio> crear(@Valid @RequestBody EnvioRequestDTO dto) {
        Envio creado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // GET - Listar todos los envios (200 OK)
    @GetMapping
    public ResponseEntity<List<Envio>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // GET - Buscar por id (200 OK o 404 Not Found)
    @GetMapping("/{id}")
    public ResponseEntity<Envio> buscarPorId(@PathVariable Long id) {
        Envio envio = service.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado"));
        return ResponseEntity.ok(envio);
    }

    // GET - Busqueda personalizada por ciudad o estado
    @GetMapping("/buscar")
    public ResponseEntity<List<Envio>> buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String estado) {

        if (ciudad != null && !ciudad.isBlank()) {
            return ResponseEntity.ok(service.buscarPorCiudad(ciudad));
        }
        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(service.buscarPorEstado(estado));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe proporcionar el parámetro 'ciudad' o 'estado'");
    }

    // PUT - Actualizar un envio existente (200 OK o 404 Not Found)
    @PutMapping("/{id}")
    public ResponseEntity<Envio> actualizar(@PathVariable Long id,
                                            @Valid @RequestBody EnvioRequestDTO dto) {
        Envio actualizado = service.actualizar(id, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado"));
        return ResponseEntity.ok(actualizado);
    }

    // DELETE - Eliminar un envio (204 No Content o 404 Not Found)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Envío no encontrado");
    }
}
