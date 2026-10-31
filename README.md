# Servitenga API

API REST de seguimiento de envios con persistencia en MySQL y sistema de geolocalizacion (tracking de envios por latitud y longitud). Construida con Spring Boot 3, JPA e Hibernate.

## Contexto

Servitenga es una empresa de mensajeria y logistica. Esta API permite gestionar envios, persistirlos en una base de datos MySQL y realizar seguimiento (tracking) de cada envio mediante coordenadas geograficas (latitud y longitud) de origen, destino y posicion actual.

## Integrantes

- Hector Mauricio

Actividad desarrollada de forma individual.

## Tecnologias utilizadas

- Java 17
- Spring Boot 3.3.4
- Spring Data JPA + Hibernate
- MySQL (conector `mysql-connector-j`)
- Jakarta Validation (Bean Validation)
- Maven

## Estructura del proyecto

```
servitenga/
├── src/main/java/com/example/servitenga/
│   ├── ServitengaApiApplication.java
│   ├── controller/
│   │   ├── EnvioController.java
│   │   └── TrackEnvioController.java
│   ├── service/
│   │   └── EnvioService.java
│   ├── model/
│   │   ├── Envio.java            <- Entidad JPA (el envio)
│   │   └── TrackEnvio.java       <- Entidad JPA (el recorrido)
│   ├── dto/
│   │   ├── EnvioRequestDTO.java
│   │   └── UbicacionDTO.java
│   └── repository/
│       ├── EnvioRepository.java       <- Extiende JpaRepository
│       └── TrackEnvioRepository.java  <- Extiende JpaRepository
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── servitenga.sql
└── README.md
```

## Persistencia de datos

La persistencia se gestiona mediante **JPA e Hibernate**. La entidad `Envio` se mapea a la tabla `envios` y la entidad `TrackEnvio` a la tabla `track_envios` en MySQL. Al arrancar la aplicacion, Hibernate crea/actualiza automaticamente las tablas (`ddl-auto=update`), por lo que no es necesario ejecutar scripts SQL manualmente.

## Modelo de datos

El modelo esta separado en dos entidades: **`Envio`** (el envio en si) y **`TrackEnvio`** (el recorrido del envio).

### Entidad `Envio` (tabla `envios`)

| Campo | Tipo | Descripcion |
| --- | --- | --- |
| `id` | `Long` | Identificador autogenerado (`@Id`, `@GeneratedValue IDENTITY`) |
| `numeroGuia` | `String` | Generado automaticamente: `LOG-2026-NNN` (unico) |
| `remitente` | `String` | Obligatorio |
| `destinatario` | `String` | Obligatorio |
| `ciudadOrigen` | `String` | Obligatorio |
| `ciudadDestino` | `String` | Obligatorio |
| `pesoKg` | `double` | Obligatorio y mayor que 0 |
| `estado` | `String` | `REGISTRADO` al crear |
| `fechaCreacion` | `LocalDate` | Fecha del registro |
| `fechaActualizacion` | `LocalDateTime` | Ultima actualizacion del registro |

> El envio **no** guarda coordenadas. Toda la geolocalizacion vive en `TrackEnvio`.

### Entidad `TrackEnvio` (tabla `track_envios`)

Guarda el **recorrido** del envio: cada punto (latitud, longitud) con su fecha. El **primer** punto corresponde al origen y el **ultimo** al destino.

| Campo | Tipo | Descripcion |
| --- | --- | --- |
| `id` | `Long` | Identificador autogenerado (`@Id`, `@GeneratedValue IDENTITY`) |
| `envioId` | `Long` | Referencia al envio (columna simple, sin relacion JPA) |
| `latitud` | `Double` | Latitud del punto del recorrido |
| `longitud` | `Double` | Longitud del punto del recorrido |
| `fechaRegistro` | `LocalDateTime` | Fecha y hora en que se registro el punto |

## Repositorios

Ambos repositorios extienden `JpaRepository`, heredando automaticamente los metodos CRUD (`save`, `findById`, `findAll`, `deleteById`, `existsById`, `count`).

- `EnvioRepository extends JpaRepository<Envio, Long>` — incluye **consultas personalizadas** por derivacion de nombre: `findByCiudadDestinoIgnoreCase(String)`, `findByEstadoIgnoreCase(String)`, `findByNumeroGuia(String)`, `findByCiudadOrigenIgnoreCase(String)`.
- `TrackEnvioRepository extends JpaRepository<TrackEnvio, Long>` — incluye `findByEnvioIdOrderByFechaRegistroAsc(Long)` (recorrido ordenado) y `deleteByEnvioId(Long)` (limpieza al borrar el envio).

## Configuracion de MySQL

Edita `src/main/resources/application.properties` con tus credenciales:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/servitenga?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

La base de datos `servitenga` se crea automaticamente si no existe (`createDatabaseIfNotExist=true`).

## Prerequisitos

- JDK 17 o superior
- Maven 3.8 o superior
- MySQL 8.x en ejecucion en `localhost:3306`

## Ejecucion

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

| Metodo | Ruta | Descripcion | Respuesta |
| --- | --- | --- | --- |
| POST | `/api/envios` | Crea un nuevo envio | `201 Created` |
| GET | `/api/envios` | Lista todos los envios | `200 OK` |
| GET | `/api/envios/{id}` | Obtiene un envio por id | `200 OK` / `404 Not Found` |
| GET | `/api/envios/buscar?ciudad=` | Filtra por ciudad de destino | `200 OK` |
| GET | `/api/envios/buscar?estado=` | Filtra por estado | `200 OK` |
| PUT | `/api/envios/{id}` | Actualiza un envio existente | `200 OK` / `404 Not Found` |
| DELETE | `/api/envios/{id}` | Elimina un envio (y su recorrido) | `204 No Content` / `404 Not Found` |
| POST | `/api/envios/{id}/track` | Registra un punto del recorrido del envio | `201 Created` / `404 Not Found` |
| GET | `/api/envios/{id}/track` | Devuelve el recorrido completo del envio | `200 OK` / `404 Not Found` |

## Ejemplos

### POST `/api/envios`

```bash
curl -X POST http://localhost:8080/api/envios \
  -H "Content-Type: application/json" \
  -d '{
    "remitente": "Juan Perez",
    "destinatario": "Ana Gomez",
    "ciudadOrigen": "Bogota",
    "ciudadDestino": "Cali",
    "pesoKg": 3.4
  }'
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "numeroGuia": "LOG-2026-001",
  "remitente": "Juan Perez",
  "destinatario": "Ana Gomez",
  "ciudadOrigen": "Bogota",
  "ciudadDestino": "Cali",
  "pesoKg": 3.4,
  "estado": "REGISTRADO",
  "fechaCreacion": "2026-10-30",
  "fechaActualizacion": "2026-10-30T10:30:00"
}
```

### POST `/api/envios/{id}/track` (Registrar punto del recorrido)

Registra un punto (latitud, longitud) en el recorrido del envio. Se puede llamar varias veces para ir construyendo el track.

```bash
curl -X POST http://localhost:8080/api/envios/1/track \
  -H "Content-Type: application/json" \
  -d '{"latitud": 4.5709, "longitud": -74.2973}'
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "envioId": 1,
  "latitud": 4.5709,
  "longitud": -74.2973,
  "fechaRegistro": "2026-10-30T11:05:00"
}
```

Si el envio no existe responde `404 Not Found`, y si falta `latitud` o `longitud` responde `400 Bad Request`.

### GET `/api/envios/{id}/track` (Consultar el recorrido)

```bash
curl http://localhost:8080/api/envios/1/track
```

Respuesta `200 OK` con el recorrido ordenado por fecha:

```json
[
  { "id": 1, "envioId": 1, "latitud": 4.5709, "longitud": -74.2973, "fechaRegistro": "2026-10-30T11:05:00" },
  { "id": 2, "envioId": 1, "latitud": 4.15, "longitud": -75.1, "fechaRegistro": "2026-10-30T12:20:00" }
]
```

Si el envio no existe responde `404 Not Found`.

### GET `/api/envios/buscar?ciudad=Cali`

```bash
curl "http://localhost:8080/api/envios/buscar?ciudad=Cali"
```

### PUT `/api/envios/{id}`

```bash
curl -X PUT http://localhost:8080/api/envios/1 \
  -H "Content-Type: application/json" \
  -d '{
    "remitente": "Juan Perez",
    "destinatario": "Ana Gomez",
    "ciudadOrigen": "Bogota",
    "ciudadDestino": "Medellin",
    "pesoKg": 3.4
  }'
```

### DELETE `/api/envios/{id}`

```bash
curl -X DELETE http://localhost:8080/api/envios/1
```

Respuesta `204 No Content`.

## Evidencia de persistencia

1. Crea un envio con `POST /api/envios`.
2. Consultalo luego con `GET /api/envios/{id}`: el registro sigue disponible porque esta almacenado en MySQL.
3. Reinicia la aplicacion y vuelve a consultar: los datos persisten.

## Uso de inteligencia artificial

Se utilizo inteligencia artificial (asistente de codigo) como apoyo durante el desarrollo:

- **Para que se utilizo:** revisar la estructura de las consultas personalizadas en Spring Data JPA, verificar las anotaciones JPA necesarias para la entidad y revisar la configuracion del datasource de MySQL.
- **Tipo de ayuda solicitada:** explicaciones sobre el funcionamiento de `JpaRepository` y sugerencias de implementacion para el mapeo de la entidad y el tracking de ubicacion.
- **Adaptacion:** las sugerencias fueron adaptadas al contexto del proyecto (envios y geolocalizacion) y no se copiaron literalmente.
- **Verificacion:** cada respuesta se comprobo ejecutando los endpoints con `curl` y consultando directamente la tabla `envios` en MySQL para confirmar que los datos persistian correctamente.

