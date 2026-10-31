package com.example.servitenga.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "track_envios")
public class TrackEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al envio (columna simple, sin relacion JPA)
    @Column(name = "envio_id", nullable = false)
    private Long envioId;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    // Constructor sin argumentos (requerido por JPA)
    public TrackEnvio() {
    }

    public TrackEnvio(Long id, Long envioId, Double latitud, Double longitud, LocalDateTime fechaRegistro) {
        this.id = id;
        this.envioId = envioId;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaRegistro = fechaRegistro;
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEnvioId() { return envioId; }
    public void setEnvioId(Long envioId) { this.envioId = envioId; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
