package com.example.servitenga.model;

import java.time.LocalDate;

public class Envio {
    private Long id;
    private String numeroGuia;
    private String remitente;
    private String destinatario;
    private String ciudadOrigen;
    private String ciudadDestino;
    private double pesoKg;
    private String estado;
    private LocalDate fechaCreacion;

    // Constructor
    public Envio(Long id, String numeroGuia, String remitente, String destinatario, String ciudadOrigen, String ciudadDestino, double pesoKg, String estado, LocalDate fechaCreacion) {
        this.id = id;
        this.numeroGuia = numeroGuia;
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.ciudadOrigen = ciudadOrigen;
        this.ciudadDestino = ciudadDestino;
        this.pesoKg = pesoKg;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }
    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getCiudadOrigen() { return ciudadOrigen; }
    public void setCiudadOrigen(String ciudadOrigen) { this.ciudadOrigen = ciudadOrigen; }
    public String getCiudadDestino() { return ciudadDestino; }
    public void setCiudadDestino(String ciudadDestino) { this.ciudadDestino = ciudadDestino; }
    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}