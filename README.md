# Servitenga API

API REST de seguimiento de envíos (mensajería y logística) desarrollada con **Java y Spring Boot**. La solución gestiona envíos y su recorrido geográfico (tracking), persiste información en **MySQL**, implementa una **relación entre entidades con JPA**, consume la **API externa OSRM** para calcular rutas para conductores e incorpora mecanismos básicos de **observabilidad** (Actuator, métricas, Prometheus, logs y trazabilidad).

## Integrantes
- Hector Mauricio

> Actividad desarrollada de forma individual.

## Descripción de la solución
Servitenga permite registrar envíos, consultar su información y almacenar puntos de seguimiento (latitud/longitud) asociados a cada envío (monitoreo del recorrido). La ruta de conducción origen→destino se calcula a partir de las **ciudades de origen y destino** del envío: la aplicación geocodifica ambas ciudades (Nominatim/OpenStreetMap) y consulta el servicio externo **OSRM** para calcular la mejor ruta, exponiéndola en un endpoint orientado al conductor del camión. Además, expone información operativa mediante Spring Boot Actuator y Micrometer.

## Tecnologías utilizadas
- Java 17
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA + Hibernate
- Spring Boot Validation (Jakarta Validation)
- Spring Boot Actuator
- Micrometer + Prometheus Registry
- MySQL (`mysql-connector-j`)
- Maven

## Estructura del proyecto
```text
servitenga/
├── src/main/java/com/example/servitenga/
│   ├── ServitengaApiApplication.java
│   ├── config/
│   │   ├── OsrmHealthIndicator.java      # Indicador de salud personalizado (OSRM)
│   │   └── TracingFilter.java             # Trazabilidad básica (X-Request-Id / MDC)
│   ├── controller/
│   │   ├── EnvioController.java           # CRUD y búsqueda de envíos
│   │   ├── TrackEnvioController.java      # Registro y consulta de tracking
│   │   └── RutaController.java            # Ruta para conductor (consume OSRM)
│   ├── dto/
│   │   ├── EnvioRequestDTO.java
│   │   ├── RutaResponseDTO.java
│   │   └── UbicacionDTO.java
│   ├── model/
│   │   ├── Envio.java                     # Entidad JPA (1)
│   │   └── TrackEnvio.java                # Entidad JPA (N), relacionada con Envio
│   ├── repository/
│   │   ├── EnvioRepository.java
│   │   └── TrackEnvioRepository.java
│   └── service/
│       ├── EnvioService.java              # Lógica de negocio, métricas y logs
│       └── OsrmRouteService.java          # Cliente/consumo de API externa OSRM
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── servitenga.sql                         # Esquema y datos de ejemplo (Pereira -> Medellín)
└── README.md
```

## Configuración de MySQL (sin credenciales en el repositorio)
La conexión se configura mediante variables de entorno (con valores por defecto para desarrollo local). **No publiques contraseñas, tokens ni credenciales en GitHub.**

Variables soportadas:
- `DB_URL` (por defecto: `jdbc:mysql://localhost:3306/servitenga?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true`)
- `DB_USERNAME` (por defecto: `root`)
- `DB_PASSWORD` (por defecto: vacío)

Ejemplo en macOS/Linux:
```bash
export DB_URL="jdbc:mysql://localhost:3306/servitenga?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
export DB_USERNAME="root"
export DB_PASSWORD="tu_password"
```

Ejemplo en PowerShell (Windows):
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/servitenga?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_password"
```

> Requisitos: JDK 17+, Maven 3.8+ y MySQL 8.x en ejecución. La base de datos se crea automáticamente si no existe.

## Instrucciones básicas de ejecución
1. Configura las variables de entorno de MySQL (o usa los valores por defecto si aplican en tu entorno local).
2. (Opcional) Ejecuta el script `servitenga.sql` para cargar el esquema y el ejemplo de ruta **Pereira -> Medellín**.
3. Inicia la aplicación:

```bash
mvn spring-boot:run
```

4. La API quedará disponible en: `http://localhost:8080`

## Entidades y relación (JPA)
La solución implementa **dos entidades relacionadas** con anotaciones JPA:

### 1. `Envio` (`envios`)
Representa el envío.
- `id` (PK, autogenerado)
- `numeroGuia` (único)
- `remitente`, `destinatario`
- `ciudadOrigen`, `ciudadDestino`
- `pesoKg`, `estado`
- `fechaCreacion`, `fechaActualizacion`
- Relación: `@OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)` hacia `TrackEnvio`

### 2. `TrackEnvio` (`track_envios`)
Representa un punto del recorrido del envío.
- `id` (PK, autogenerado)
- `latitud`, `longitud`
- `fechaRegistro`
- Relación: `@ManyToOne(fetch = LAZY, optional = false)` + `@JoinColumn(name = "envio_id")` hacia `Envio`

**Tipo de relación:** `Envio (1) ─── (N) TrackEnvio`  
Un envío puede tener múltiples puntos de tracking (recorrido) y cada punto pertenece a un único envío. La relación se implementa mediante anotaciones JPA (`@OneToMany`, `@ManyToOne`, `@JoinColumn`) y se refleja en la base de datos mediante la clave foránea `envio_id` en `track_envios`.

## API externa utilizada: OSRM (Open Source Routing Machine)
Se consume la API pública de ruteo **OSRM** para calcular la mejor ruta de conducción entre la ciudad de origen y la ciudad de destino del envío. Las ciudades se geocodifican previamente con **Nominatim (OpenStreetMap)** para obtener sus coordenadas, y luego se consulta OSRM; el tracking (puntos lat/lon) se mantiene como monitoreo del recorrido y no es requisito para calcular la ruta.

- **Servicio:** OSRM Route Service (servidor demo público)
- **Base URL configurable:** `OSRM_BASE_URL` (por defecto: `https://router.project-osrm.org`)
- **Endpoint consumido:** `GET /route/v1/driving/{lon_origen},{lat_origen};{lon_destino},{lat_destino}`
- **Parámetros usados:** `overview=full`, `geometries=geojson`, `steps=true`
- **Datos aprovechados en la aplicación:** distancia, duración estimada, pasos/instrucciones y geometría de la ruta (respuesta adaptada en `RutaResponseDTO`).
- **Autenticación/pago:** no requiere API key ni pago (API pública de demostración).
- **Manejo controlado de errores:** si no se pueden geocodificar las ciudades de origen/destino, si OSRM no devuelve una ruta válida (`code != Ok` o sin resultados), o si ocurre un fallo de red/servicio, la aplicación responde de forma controlada (`400`/`502`/`503` según el caso) y registra eventos `WARN`/`ERROR` en logs.

> Nota: OSRM calcula rutas sobre datos de OpenStreetMap; es una estimación de ruta (no tráfico en tiempo real tipo Waze).

## Principales endpoints

### Envíos (`/api/envios`)
| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| POST | `/api/envios` | Crea un envío | `201 Created` |
| GET | `/api/envios` | Lista envíos | `200 OK` |
| GET | `/api/envios/{id}` | Obtiene envío por id | `200 OK` / `404` |
| GET | `/api/envios/buscar?ciudad=` | Filtra por ciudad de destino | `200 OK` |
| GET | `/api/envios/buscar?estado=` | Filtra por estado | `200 OK` |
| PUT | `/api/envios/{id}` | Actualiza envío | `200 OK` / `404` |
| DELETE | `/api/envios/{id}` | Elimina envío (y su recorrido) | `204 No Content` / `404` |

### Tracking (`/api/envios/{id}/track`)
| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| POST | `/api/envios/{id}/track` | Registra un punto (lat, lon) del recorrido | `201 Created` / `404` / `400` |
| GET | `/api/envios/{id}/track` | Lista el recorrido completo (orden cronológico) | `200 OK` / `404` |

### Ruta para conductor (API externa OSRM) (`/api/envios/{id}/ruta`)
| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| GET | `/api/envios/{id}/ruta` | Calcula y devuelve la ruta de conducción origen→destino usando OSRM | `200 OK` / `400` / `404` / `502` / `503` |

**Ejemplo (ruta Pereira -> Medellín, usando el dato de ejemplo):**
```bash
curl http://localhost:8080/api/envios/1/ruta
```

## Ejemplo de datos (Pereira -> Medellín)
El archivo `servitenga.sql` incluye un envío de ejemplo:
- **Origen:** Pereira
- **Destino:** Medellín

La ruta se calcula a partir de las ciudades de origen y destino (geocodificadas automáticamente). Los puntos de tracking se mantienen como monitoreo del recorrido y no son requisito para calcular la ruta.

## Elementos de observabilidad implementados

### Spring Boot Actuator
Endpoints expuestos en configuración: `health`, `info`, `metrics`, `prometheus`.
- `GET /actuator/health` — estado de la aplicación (incluye detalles e indicador personalizado)
- `GET /actuator/metrics` — listado de métricas disponibles

### Métricas personalizadas (Micrometer)
- `servitenga.envios.creados` — cantidad total de envíos creados
- `servitenga.tracks.registrados` — cantidad total de puntos de tracking registrados
- `servitenga.rutas.calculadas` — cantidad total de rutas calculadas mediante OSRM

### Exposición para Prometheus
- `GET /actuator/prometheus` — métricas en formato compatible con Prometheus (incluye las métricas personalizadas)

### Logs (SLF4J)
Se registran eventos relevantes en servicio, integración externa y trazabilidad:
- `INFO`: creación/consulta/actualización de envíos, registro/consulta de tracking, cálculo de rutas y solicitudes entrantes/completadas
- `WARN`: intentos sobre recursos inexistentes, datos insuficientes para ruta o respuestas no válidas del servicio externo
- `ERROR`: fallos al consumir la API externa OSRM

### Indicador de salud personalizado y trazabilidad
- **Health Indicator personalizado (`osrm`)**: verifica la disponibilidad del servicio externo OSRM y se refleja en `/actuator/health`.
- **Trazabilidad básica (`TracingFilter`)**: asigna o propaga el encabezado `X-Request-Id`, lo incluye en la respuesta y en el contexto de logs (MDC), registrando inicio/fin y duración de cada solicitud.

## Instrucciones para consultar Actuator
Con la aplicación en ejecución:

```bash
# Estado de salud (incluye indicador personalizado OSRM y detalles)
curl http://localhost:8080/actuator/health

# Listado de métricas
curl http://localhost:8080/actuator/metrics

# Métrica personalizada específica (ejemplos)
curl http://localhost:8080/actuator/metrics/servitenga.envios.creados
curl http://localhost:8080/actuator/metrics/servitenga.tracks.registrados
curl http://localhost:8080/actuator/metrics/servitenga.rutas.calculadas

# Métricas en formato Prometheus
curl http://localhost:8080/actuator/prometheus
```

## Uso de inteligencia artificial
Se utilizó inteligencia artificial como apoyo durante el desarrollo de la actividad (consulta y verificación técnica), incluyendo orientación para:
- Modelar la relación JPA `@OneToMany/@ManyToOne` entre `Envio` y `TrackEnvio`.
- Integrar el consumo de la API externa OSRM con `RestClient` y definir manejo controlado de errores.
- Configurar observabilidad con Spring Boot Actuator, Micrometer/Prometheus, métricas personalizadas, logs y mecanismos de salud/trazabilidad.

Las sugerencias fueron revisadas, adaptadas al contexto del proyecto y verificadas ejecutando la aplicación, probando endpoints (`curl`) y consultando los endpoints de Actuator y la base de datos MySQL.
