package com.example.servitenga.service;

import com.example.servitenga.dto.EnvioRequestDTO;
import com.example.servitenga.model.Envio;
import com.example.servitenga.repository.EnvioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EnvioService {
    private final EnvioRepository repository;

    public EnvioService(EnvioRepository repository) {
        this.repository = repository;
    }

    public List<Envio> listarTodos() { 
        return repository.findAll(); 
    }

    public Optional<Envio> buscarPorId(Long id) { return repository.findById(id); }

    public List<Envio> buscarPorCiudad(String ciudad) {
        return repository.findByCiudadDestino(ciudad);
    }

    public List<Envio> buscarPorEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public Envio crear(EnvioRequestDTO dto) {
        String guia = "LOG-2026-" + String.format("%03d", repository.findAll().size() + 1);
        Envio nuevo = new Envio(
                null, guia, dto.remitente(), dto.destinatario(),
                dto.ciudadOrigen(), dto.ciudadDestino(), dto.pesoKg(),
                "REGISTRADO", LocalDate.now()
        );
        return repository.guardar(nuevo);
    }
}