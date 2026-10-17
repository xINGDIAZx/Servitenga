# Servitenga API

API REST de seguimiento de envios construida con Spring Boot 3 (Java 17).

## Estructura

```
servitenga/
├── src/main/java/com/example/servitenga/
│   ├── ServitengaApiApplication.java
│   ├── controller/
│   │   └── EnvioController.java
│   ├── service/
│   │   └── EnvioService.java
│   ├── model/
│   │   └── Envio.java
│   ├── dto/
│   │   └── EnvioRequestDTO.java
│   └── repository/
│       └── EnvioRepository.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── README.md
```

El repositorio almacena los envios en memoria (`ArrayList`) por simplicidad y precarga dos envios de ejemplo (`LOG-2026-001` y `LOG-2026-002`).

## Modelo de datos

| Campo | Tipo | Descripcion |
| --- | --- | --- |
| `id` | `Long` | Generado automaticamente por el repositorio |
| `numeroGuia` | `String` | Generado automaticamente con el formato `LOG-2026-NNN` |
| `remitente` | `String` | Obligatorio (`@NotBlank`) |
| `destinatario` | `String` | Obligatorio (`@NotBlank`) |
| `ciudadOrigen` | `String` | Obligatorio (`@NotBlank`) |
| `ciudadDestino` | `String` | Obligatorio (`@NotBlank`) |
| `pesoKg` | `double` | Obligatorio y mayor que 0 (`@Positive`) |
| `estado` | `String` | `REGISTRADO` al crear |
| `fechaCreacion` | `LocalDate` | Fecha del registro |

## Endpoints

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| GET | `/api/envios` | Lista todos los envios |
| GET | `/api/envios/{id}` | Obtiene un envio por id (`404` si no existe) |
| GET | `/api/envios/buscar?ciudad=` | Filtra por ciudad de destino (ignora mayusculas) |
| GET | `/api/envios/buscar?estado=` | Filtra por estado (ignora mayusculas) |
| POST | `/api/envios` | Crea un nuevo envio (`201`, o `400` si el body es invalido) |

## Ejecucion

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Ejemplos

### GET `/api/envios`

```bash
curl http://localhost:8080/api/envios
```

```json
[
  {
    "id": 1,
    "numeroGuia": "LOG-2026-001",
    "remitente": "Juan Pérez",
    "destinatario": "María López",
    "ciudadOrigen": "Bogotá",
    "ciudadDestino": "Medellín",
    "pesoKg": 2.5,
    "estado": "EN_TRANSITO",
    "fechaCreacion": "2026-10-12"
  },
  {
    "id": 2,
    "numeroGuia": "LOG-2026-002",
    "remitente": "Carlos Ruiz",
    "destinatario": "Ana Gómez",
    "ciudadOrigen": "Cali",
    "ciudadDestino": "Barranquilla",
    "pesoKg": 1.2,
    "estado": "REGISTRADO",
    "fechaCreacion": "2026-10-14"
  }
]
```

### GET `/api/envios/{id}`

```bash
curl http://localhost:8080/api/envios/1
```

```json
{
  "id": 1,
  "numeroGuia": "LOG-2026-001",
  "remitente": "Juan Pérez",
  "destinatario": "María López",
  "ciudadOrigen": "Bogotá",
  "ciudadDestino": "Medellín",
  "pesoKg": 2.5,
  "estado": "EN_TRANSITO",
  "fechaCreacion": "2026-10-12"
}
```

Si el id no existe responde `404 Not Found`.

### GET `/api/envios/buscar`

Filtra por ciudad de destino o por estado (los parametros no distinguen mayusculas).

```bash
curl -G http://localhost:8080/api/envios/buscar --data-urlencode "ciudad=Medellín"
curl -G http://localhost:8080/api/envios/buscar --data-urlencode "estado=REGISTRADO"
```

```json
[{ "id": 1, "numeroGuia": "LOG-2026-001", "ciudadDestino": "Medellín", "estado": "EN_TRANSITO" }]
```

Si no se envia ningun parametro responde `400 Bad Request`.

### POST `/api/envios`

Crea un envio. Todos los campos del body son obligatorios y `pesoKg` debe ser mayor que 0.

```bash
curl -X POST http://localhost:8080/api/envios \
  -H "Content-Type: application/json" \
  -d '{"remitente":"Juan Perez","destinatario":"Ana Gomez","ciudadOrigen":"Bogota","ciudadDestino":"Cali","pesoKg":3.4}'
```

Respuesta `201 Created` (el `id`, `numeroGuia`, `estado` y `fechaCreacion` los genera el servidor):

```json
{
  "id": 3,
  "numeroGuia": "LOG-2026-003",
  "remitente": "Juan Perez",
  "destinatario": "Ana Gomez",
  "ciudadOrigen": "Bogota",
  "ciudadDestino": "Cali",
  "pesoKg": 3.4,
  "estado": "REGISTRADO",
  "fechaCreacion": "2026-10-14"
}
```

Body incompleto (por ejemplo, sin `ciudadDestino`) responde `400 Bad Request`.