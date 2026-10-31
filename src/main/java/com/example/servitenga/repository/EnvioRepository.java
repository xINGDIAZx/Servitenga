package com.example.servitenga.repository;

import com.example.servitenga.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {

    // Consulta personalizada: buscar envios por ciudad de destino (ignora mayusculas)
    List<Envio> findByCiudadDestinoIgnoreCase(String ciudadDestino);

    // Consulta personalizada: buscar envios por estado (ignora mayusculas)
    List<Envio> findByEstadoIgnoreCase(String estado);

    // Consulta personalizada: buscar un envio por su numero de guia
    Optional<Envio> findByNumeroGuia(String numeroGuia);

    // Consulta personalizada: buscar envios por ciudad de origen (ignora mayusculas)
    List<Envio> findByCiudadOrigenIgnoreCase(String ciudadOrigen);
}
