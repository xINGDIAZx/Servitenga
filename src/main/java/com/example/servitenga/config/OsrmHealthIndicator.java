package com.example.servitenga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Indicador de salud personalizado para verificar disponibilidad del servicio externo OSRM.
 */
@Component("osrm")
public class OsrmHealthIndicator implements HealthIndicator {

    private final RestClient restClient;

    public OsrmHealthIndicator(RestClient.Builder builder,
                               @Value("${osrm.base-url:https://router.project-osrm.org}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Health health() {
        try {
            // Consulta ligera de ruta conocida para validar disponibilidad del servicio
            String result = restClient.get()
                    .uri("/route/v1/driving/-75.6961,4.8133;-75.5812,6.2442?overview=false")
                    .retrieve()
                    .body(String.class);
            if (result != null && result.contains("\"code\":\"Ok\"")) {
                return Health.up().withDetail("service", "OSRM").withDetail("status", "reachable").build();
            }
            return Health.down().withDetail("service", "OSRM").withDetail("reason", "respuesta no valida").build();
        } catch (Exception e) {
            return Health.down().withDetail("service", "OSRM").withDetail("error", e.getMessage()).build();
        }
    }
}