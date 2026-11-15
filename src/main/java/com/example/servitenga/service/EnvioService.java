package com.example.servitenga.service;

import com.example.servitenga.dto.EnvioRequestDTO;
import com.example.servitenga.dto.RutaResponseDTO;
import com.example.servitenga.dto.UbicacionDTO;
import com.example.servitenga.model.Envio;
import com.example.servitenga.model.TrackEnvio;
import com.example.servitenga.repository.EnvioRepository;
import com.example.servitenga.repository.TrackEnvioRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnvioService {

    private static final Logger log = LoggerFactory.getLogger(EnvioService.class);

    private final EnvioRepository repository;
    private final TrackEnvioRepository trackRepository;
    private final OsrmRouteService osrmRouteService;
    private final Counter enviosCreadosCounter;
    private final Counter tracksRegistradosCounter;
    private final Counter rutasCalculadasCounter;

    public EnvioService(EnvioRepository repository,
                        TrackEnvioRepository trackRepository,
                        OsrmRouteService osrmRouteService,
                        MeterRegistry meterRegistry) {
        this.repository = repository;
        this.trackRepository = trackRepository;
        this.osrmRouteService = osrmRouteService;
        this.enviosCreadosCounter = Counter.builder("servitenga.envios.creados")
                .description("Cantidad total de envios creados")
                .register(meterRegistry);
        this.tracksRegistradosCounter = Counter.builder("servitenga.tracks.registrados")
                .description("Cantidad total de puntos de tracking registrados")
                .register(meterRegistry);
        this.rutasCalculadasCounter = Counter.builder("servitenga.rutas.calculadas")
                .description("Cantidad total de rutas calculadas mediante OSRM")
                .register(meterRegistry);
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
        Envio guardado = repository.save(nuevo);
        enviosCreadosCounter.increment();
        log.info("Envio creado: id={}, guia={}, origen={}, destino={}", guardado.getId(), guardado.getNumeroGuia(),
                guardado.getCiudadOrigen(), guardado.getCiudadDestino());
        return guardado;
    }

    // --- READ: todos ---
    public List<Envio> listarTodos() {
        log.info("Consultando listado de envios");
        return repository.findAll();
    }

    // --- READ: por id ---
    public Optional<Envio> buscarPorId(Long id) {
        log.info("Consultando envio por id={}", id);
        return repository.findById(id);
    }

    // --- READ: por ciudad de destino ---
    public List<Envio> buscarPorCiudad(String ciudad) {
        log.info("Buscando envios por ciudad destino={}", ciudad);
        return repository.findByCiudadDestinoIgnoreCase(ciudad);
    }

    // --- READ: por estado ---
    public List<Envio> buscarPorEstado(String estado) {
        log.info("Buscando envios por estado={}", estado);
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
            Envio actualizado = repository.save(existente);
            log.info("Envio actualizado: id={}", actualizado.getId());
            return actualizado;
        });
    }

    // --- DELETE (el cascade/orphanRemoval elimina su track asociado) ---
    @Transactional
    public boolean eliminar(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            log.warn("Envio eliminado: id={} (incluye su recorrido)", id);
            return true;
        }
        log.warn("Intento de eliminar envio inexistente: id={}", id);
        return false;
    }

    // ================= TRACK DEL ENVIO =================

    // Registra un nuevo punto (latitud, longitud) en el recorrido del envio
    public Optional<TrackEnvio> registrarTrack(Long envioId, UbicacionDTO dto) {
        Optional<Envio> envioOpt = repository.findById(envioId);
        if (envioOpt.isEmpty()) {
            log.warn("Intento de registrar track para envio inexistente: id={}", envioId);
            return Optional.empty();
        }
        TrackEnvio punto = new TrackEnvio();
        punto.setEnvio(envioOpt.get());
        punto.setLatitud(dto.latitud());
        punto.setLongitud(dto.longitud());
        punto.setFechaRegistro(LocalDateTime.now());
        TrackEnvio guardado = trackRepository.save(punto);
        tracksRegistradosCounter.increment();
        log.info("Track registrado: envioId={}, puntoId={}, lat={}, lon={}", envioId, guardado.getId(),
                guardado.getLatitud(), guardado.getLongitud());
        return Optional.of(guardado);
    }

    // Devuelve el recorrido completo de un envio
    public Optional<List<TrackEnvio>> listarTrack(Long envioId) {
        if (!repository.existsById(envioId)) {
            log.warn("Intento de consultar track para envio inexistente: id={}", envioId);
            return Optional.empty();
        }
        log.info("Consultando recorrido del envio id={}", envioId);
        return Optional.of(trackRepository.findByEnvio_IdOrderByFechaRegistroAsc(envioId));
    }

    // ================= RUTA (API EXTERNA OSRM) =================

    public RutaResponseDTO calcularRuta(Long envioId) {
        Envio envio = repository.findById(envioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Envio no encontrado"));
        RutaResponseDTO ruta = osrmRouteService.calcularRuta(envio);
        rutasCalculadasCounter.increment();
        return ruta;
    }
}
