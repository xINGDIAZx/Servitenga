package com.example.servitenga.repository;

import com.example.servitenga.model.Envio;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class EnvioRepository {
    private final List<Envio> envios = new ArrayList<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    public EnvioRepository() {
        // Datos de ejemplo
        guardar(new Envio(null, "LOG-2026-001", "Juan Pérez", "María López", "Bogotá", "Medellín", 2.5, "EN_TRANSITO", LocalDate.now().minusDays(2)));
        guardar(new Envio(null, "LOG-2026-002", "Carlos Ruiz", "Ana Gómez", "Cali", "Barranquilla", 1.2, "REGISTRADO", LocalDate.now()));
    }

    public Envio guardar(Envio envio) {
        if (envio.getId() == null) {
            envio.setId(contadorId.getAndIncrement());
        }
        envios.add(envio);
        return envio;
    }

    public List<Envio> findAll() { return envios; }

    public Optional<Envio> findById(Long id) {
        return envios.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public List<Envio> findByCiudadDestino(String ciudad) {
        return envios.stream()
                .filter(e -> e.getCiudadDestino().equalsIgnoreCase(ciudad))
                .toList();
    }

    public List<Envio> findByEstado(String estado) {
        return envios.stream()
                .filter(e -> e.getEstado().equalsIgnoreCase(estado))
                .toList();
    }
}