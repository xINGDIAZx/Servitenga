package com.example.servitenga.service;

import com.example.servitenga.dto.EnvioRequestDTO;
import com.example.servitenga.dto.UbicacionDTO;
import com.example.servitenga.model.Envio;
import com.example.servitenga.model.TrackEnvio;
import com.example.servitenga.repository.EnvioRepository;
import com.example.servitenga.repository.TrackEnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnvioService {

    private final EnvioRepository repository;
    private final TrackEnvioRepository trackRepository;

    public EnvioService(EnvioRepository repository, TrackEnvioRepository trackRepository) {
        this.repository = repository;
        this.trackRepository = trackRepository;
    }

    // ================= ENVIO (CRUD) =================

    // --- CREATE ---
    public Envio crear(EnvioRequestDTO dto) {
        String guia = "LOG-2026-" + String.format("%03d", repository.count() + 1);
        Envio nuevo = new Envio();
        nuevo.setNumeroGuia(guia);
        nuevo.setRemitente(dto.remitente());
        nuevo.setDestinatario(dto.destinatario());
        nuevo.setCiudadOrigen(dto.ciudadOrigen());
        nuevo.setCiudadDestino(dto.ciudadDestino());
        nuevo.setPesoKg(dto.pesoKg());
        nuevo.setEstado("REGISTRADO");
        nuevo.setFechaCreacion(LocalDate.now());
        nuevo.setFechaActualizacion(LocalDateTime.now());
        return repository.save(nuevo);
    }

    // --- READ: todos ---
    public List<Envio> listarTodos() {
        return repository.findAll();
    }

    // --- READ: por id ---
    public Optional<Envio> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // --- READ: por ciudad de destino ---
    public List<Envio> buscarPorCiudad(String ciudad) {
        return repository.findByCiudadDestinoIgnoreCase(ciudad);
    }

    // --- READ: por estado ---
    public List<Envio> buscarPorEstado(String estado) {
        return repository.findByEstadoIgnoreCase(estado);
    }

    // --- UPDATE ---
    public Optional<Envio> actualizar(Long id, EnvioRequestDTO dto) {
        return repository.findById(id).map(existente -> {
            existente.setRemitente(dto.remitente());
            existente.setDestinatario(dto.destinatario());
            existente.setCiudadOrigen(dto.ciudadOrigen());
            existente.setCiudadDestino(dto.ciudadDestino());
            existente.setPesoKg(dto.pesoKg());
            existente.setFechaActualizacion(LocalDateTime.now());
            return repository.save(existente);
        });
    }

    // --- DELETE (tambien elimina su track) ---
    @Transactional
    public boolean eliminar(Long id) {
        if (repository.existsById(id)) {
            trackRepository.deleteByEnvioId(id);
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // ================= TRACK DEL ENVIO =================

    // Registra un nuevo punto (latitud, longitud) en el recorrido del envio
    public Optional<TrackEnvio> registrarTrack(Long envioId, UbicacionDTO dto) {
        if (!repository.existsById(envioId)) {
            return Optional.empty();
        }
        TrackEnvio punto = new TrackEnvio();
        punto.setEnvioId(envioId);
        punto.setLatitud(dto.latitud());
        punto.setLongitud(dto.longitud());
        punto.setFechaRegistro(LocalDateTime.now());
        return Optional.of(trackRepository.save(punto));
    }

    // Devuelve el recorrido completo de un envio
    public Optional<List<TrackEnvio>> listarTrack(Long envioId) {
        if (!repository.existsById(envioId)) {
            return Optional.empty();
        }
        return Optional.of(trackRepository.findByEnvioIdOrderByFechaRegistroAsc(envioId));
    }
}
