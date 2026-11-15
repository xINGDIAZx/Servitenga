package com.example.servitenga.service;

import com.example.servitenga.dto.RutaResponseDTO;
import com.example.servitenga.model.Envio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OsrmRouteService {

    private static final Logger log = LoggerFactory.getLogger(OsrmRouteService.class);

    private final RestClient osrmClient;
    private final RestClient nominatimClient;

    public OsrmRouteService(RestClient.Builder builder,
                            @Value("${osrm.base-url:https://router.project-osrm.org}") String baseUrl) {
        this.osrmClient = builder.baseUrl(baseUrl).build();
        this.nominatimClient = builder.baseUrl("https://nominatim.openstreetmap.org").build();
    }

    public RutaResponseDTO calcularRuta(Envio envio) {
        double[] origen = geocodificar(envio.getCiudadOrigen());
        double[] destino = geocodificar(envio.getCiudadDestino());

        if (origen == null || destino == null) {
            log.warn("No se pudo geocodificar origen/destino para envio {}", envio.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo obtener coordenadas para la ciudad de origen o destino");
        }

        String coordinates = origen[1] + "," + origen[0] + ";" + destino[1] + "," + destino[0];

        log.info("Consultando ruta OSRM para envio {} ({} -> {})", envio.getId(), envio.getCiudadOrigen(), envio.getCiudadDestino());
        try {
            Map<String, Object> response = osrmClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/route/v1/driving/{coordinates}")
                            .queryParam("overview", "full")
                            .queryParam("geometries", "geojson")
                            .queryParam("steps", "true")
                            .build(coordinates))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response == null || !"Ok".equals(response.get("code"))) {
                log.warn("OSRM respondio sin ruta valida para envio {}: {}", envio.getId(), response);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "El servicio de rutas no pudo calcular una ruta para el envio");
            }

            List<Map<String, Object>> routes = castListOfMaps(response.get("routes"));
            if (routes.isEmpty()) {
                log.warn("OSRM no devolvio rutas para envio {}", envio.getId());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "El servicio de rutas no devolvio resultados");
            }

            Map<String, Object> route = routes.get(0);
            double distance = toDouble(route.get("distance"));
            double duration = toDouble(route.get("duration"));
            String geometry = Objects.toString(route.get("geometry"), null);

            List<Map<String, Object>> legs = castListOfMaps(route.get("legs"));
            List<Map<String, Object>> steps = Collections.emptyList();
            if (!legs.isEmpty()) {
                steps = castListOfMaps(legs.get(0).get("steps"));
            }

            log.info("Ruta OSRM calculada para envio {}: {} m, {} s", envio.getId(), distance, duration);
            return new RutaResponseDTO(
                    envio.getId(),
                    envio.getCiudadOrigen(),
                    envio.getCiudadDestino(),
                    distance,
                    duration,
                    steps,
                    geometry
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al consumir la API externa OSRM para el envio {}", envio.getId(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible obtener la ruta desde el servicio externo", e);
        }
    }

    @SuppressWarnings("unchecked")
    private double[] geocodificar(String ciudad) {
        try {
            List<Map<String, Object>> response = nominatimClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", ciudad + ", Colombia")
                            .queryParam("format", "json")
                            .queryParam("limit", "1")
                            .build())
                    .header("User-Agent", "Servitenga/1.0")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response == null || response.isEmpty()) {
                return null;
            }
            Map<String, Object> result = response.get(0);
            double lat = toDouble(result.get("lat"));
            double lon = toDouble(result.get("lon"));
            return new double[]{lat, lon};
        } catch (Exception e) {
            log.warn("No se pudo geocodificar {}: {}", ciudad, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castListOfMaps(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return Collections.emptyList();
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0d;
    }
}