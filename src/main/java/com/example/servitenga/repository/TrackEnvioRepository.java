package com.example.servitenga.repository;

import com.example.servitenga.model.TrackEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackEnvioRepository extends JpaRepository<TrackEnvio, Long> {

    // Consulta personalizada: recorrido completo de un envio, en orden cronologico
    List<TrackEnvio> findByEnvio_IdOrderByFechaRegistroAsc(Long envioId);

    // Eliminar todos los puntos de un envio (al borrar el envio)
    void deleteByEnvio_Id(Long envioId);
}
