# API de Gestión de Pólizas — Módulo 2

API REST desarrollada con **Spring Boot 3.3.5** y **Java 17** para la gestión de pólizas
de arrendamiento (individuales y colectivas), riesgos, renovaciones y cancelaciones.

Esta es la entrega del **Módulo 2 – Prueba Técnica Práctica** de la prueba de
conocimientos. La aplicación usa **almacenamiento en memoria**, por lo que no requiere
base de datos externa. Al reiniciar el proceso, los datos vuelven a su estado inicial de
demostración.

---

## Tabla de contenido

1. [Tecnologías](#tecnologías)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Requisitos previos](#requisitos-previos)
4. [Cómo ejecutar](#cómo-ejecutar)
5. [Configuración](#configuración)
6. [Seguridad](#seguridad)
7. [Endpoints](#endpoints)
8. [Ejemplos con cURL](#ejemplos-con-curl)
9. [Reglas de negocio](#reglas-de-negocio)
10. [Modelo de datos](#modelo-de-datos)
11. [Datos de demostración](#datos-de-demostración)
12. [Manejo de errores](#manejo-de-errores)
13. [Pruebas con Postman](#pruebas-con-postman)
14. [Flujo de validación sugerido](#flujo-de-validación-sugerido)
15. [Decisiones de diseño](#decisiones-de-diseño)

---

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.3.5 |
| Spring Web (REST) | incluido en Spring Boot |
| Spring Validation (Bean Validation) | incluido en Spring Boot |
| Maven | 3.9+ |
| Docker / Docker Compose | opcional |

No se utilizan librerías adicionales fuera del ecosistema de Spring Boot. El
almacenamiento es en memoria mediante repositorios implementados con `ConcurrentHashMap`.

---

## Estructura del proyecto

```
.
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── postman/
│   └── Modulo_2_Polizas.postman_collection.json
└── src/main/java/co/com/segurosbolivar/
    ├── PolizasApplication.java        # Punto de entrada Spring Boot
    ├── config/
    │   └── SecurityConfig.java        # Filtro de x-api-key
    ├── controller/
    │   ├── PolizaController.java      # Endpoints de pólizas
    │   ├── RiesgoController.java      # Endpoints de riesgos
    │   ├── CoreMockController.java    # Mock del CORE (registro de eventos)
    │   └── ApiExceptionHandler.java   # Manejo centralizado de errores
    ├── model/
    │   ├── Poliza.java                # Entidad Póliza
    │   ├── Riesgo.java                # Entidad Riesgo
    │   ├── TipoPoliza.java            # INDIVIDUAL | COLECTIVA
    │   └── Estado.java                # ACTIVA | RENOVADA | CANCELADA
    ├── repository/
    │   ├── PolizaRepository.java      # Interfaz
    │   ├── RiesgoRepository.java      # Interfaz
    │   ├── InMemoryPolizaRepository.java
    │   └── InMemoryRiesgoRepository.java
    └── service/
        ├── PolizaService.java         # Lógica de negocio
        └── BusinessException.java     # Excepción con código HTTP
```

La arquitectura sigue una separación por capas:

- **Controller**: expone los endpoints REST y traduce HTTP ↔ dominio.
- **Service**: contiene las reglas de negocio y las validaciones.
- **Repository**: abstrae el almacenamiento (hoy en memoria, fácilmente reemplazable por
  JPA/una base de datos sin tocar el servicio).
- **Model**: entidades del dominio.

---

## Requisitos previos

Para ejecutar sin Docker necesita:

- **Java 17** o superior (`java -version`).
- **Maven 3.9** o superior (`mvn -version`).

Para ejecutar con Docker solo necesita Docker y, opcionalmente, Docker Compose.

---

## Cómo ejecutar

### Opción 1 — Maven

```bash
# Ejecutar en modo desarrollo
mvn spring-boot:run

# O empaquetar y ejecutar el JAR
mvn clean package
java -jar target/polizas-api-1.0.0.jar
```

La API queda disponible en `http://localhost:8080`.

### Opción 2 — Docker

```bash
# Construir la imagen
docker build -t polizas-api:local .

# Ejecutar el contenedor
docker run -d --name polizas-api -p 8080:8080 polizas-api:local

# Verificar que responde
curl -H 'x-api-key: 123456' http://localhost:8080/polizas
```

Comandos de administración:

```bash
docker logs -f polizas-api     # Ver logs en vivo
docker stop polizas-api        # Detener
docker start polizas-api       # Iniciar
docker rm -f polizas-api       # Eliminar
```

### Opción 3 — Docker Compose (recomendada)

```bash
docker compose up --build -d   # Construir y levantar en segundo plano
docker compose logs -f         # Ver logs
docker compose down            # Detener y eliminar
```

---

## Configuración

Las propiedades se encuentran en `src/main/resources/application.properties`:

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `server.port` | `8080` | Puerto HTTP del servicio |
| `app.api-key` | `123456` | API Key requerida en el header `x-api-key` |
| `app.ipc` | `0.10` | Incremento por IPC aplicado en las renovaciones (10%) |

Cualquiera de estas propiedades puede sobrescribirse con variables de entorno de Spring o
argumentos de la JVM, sin necesidad de recompilar:

```bash
# Con Docker: cambiar el IPC a 12%
docker run -d --name polizas-api -p 8080:8080 -e APP_IPC=0.12 polizas-api:local

# Con el JAR: cambiar el puerto y el IPC
java -Dserver.port=9090 -Dapp.ipc=0.12 -jar target/polizas-api-1.0.0.jar
```

---

## Seguridad

**Todos** los endpoints requieren el header:

```
x-api-key: 123456
```

La validación la realiza un filtro de servlet (`SecurityConfig.ApiKeyFilter`) que se
ejecuta para todas las rutas. Si el header falta o es incorrecto, la API responde:

```json
HTTP/1.1 401 Unauthorized
{ "error": "API Key inválida o ausente" }
```

---

## Endpoints

| # | Método | Endpoint | Descripción | Requerimiento |
|---|---|---|---|---|
| 1 | GET | `/polizas?tipo=&estado=` | Lista y filtra pólizas por tipo y/o estado | RF-1 |
| 2 | GET | `/polizas/{id}/riesgos` | Consulta los riesgos de una póliza | RF-2 |
| 3 | POST | `/polizas/{id}/renovar` | Renueva aplicando IPC y pasa a `RENOVADA` | RF-3 |
| 4 | POST | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos | RF-4 |
| 5 | POST | `/polizas/{id}/riesgos` | Agrega un riesgo (solo pólizas `COLECTIVA`) | RF-5 |
| 6 | POST | `/riesgos/{id}/cancelar` | Cancela un riesgo individualmente | RF-6 |
| — | POST | `/core-mock/evento` | Registra en logs el intento de envío al CORE | Mock externo |

Parámetros de filtrado de `/polizas` (ambos opcionales, combinables):

- `tipo`: `INDIVIDUAL` o `COLECTIVA`.
- `estado`: `ACTIVA`, `RENOVADA` o `CANCELADA`.

---

## Ejemplos con cURL

```bash
# 1. Listar todas las pólizas
curl -H 'x-api-key: 123456' 'http://localhost:8080/polizas'

# 1b. Filtrar colectivas activas
curl -H 'x-api-key: 123456' 'http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA'

# 2. Consultar riesgos de una póliza
curl -H 'x-api-key: 123456' http://localhost:8080/polizas/555/riesgos

# 3. Renovar una póliza (incrementa canon y prima en el IPC configurado)
curl -X POST -H 'x-api-key: 123456' http://localhost:8080/polizas/555/renovar

# 4. Cancelar una póliza (cancela también sus riesgos)
curl -X POST -H 'x-api-key: 123456' http://localhost:8080/polizas/556/cancelar

# 5. Agregar un riesgo a una póliza colectiva
curl -X POST -H 'x-api-key: 123456' http://localhost:8080/polizas/555/riesgos

# 6. Cancelar un riesgo
curl -X POST -H 'x-api-key: 123456' http://localhost:8080/riesgos/1/cancelar

# Mock del CORE: solo escribe en los logs del servidor
curl -X POST -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' \
  http://localhost:8080/core-mock/evento
```

---

## Reglas de negocio

Implementadas en `PolizaService` y `Poliza`:

1. **Una póliza individual solo puede tener 1 riesgo.**
   Como los riesgos solo se pueden agregar a pólizas `COLECTIVA`, una póliza `INDIVIDUAL`
   queda limitada a su riesgo inicial.
2. **No se puede renovar una póliza cancelada.** → `409 Conflict`.
3. **La cancelación de una póliza cancela todos sus riesgos.**
4. **Agregar un riesgo exige validación del tipo de póliza.** Solo `COLECTIVA`;
   para `INDIVIDUAL` responde `409 Conflict`.
5. **No se pueden agregar riesgos a una póliza cancelada.** → `409 Conflict`.
6. **La renovación incrementa canon y prima en el IPC** y cambia el estado a `RENOVADA`.
7. **Los identificadores de riesgo son únicos a nivel global**, se generan de forma
   incremental y segura (`AtomicLong`) en `InMemoryRiesgoRepository.nextId()`.

### Mock del CORE

El endpoint `POST /core-mock/evento` cumple el propósito requerido: registrar en los logs
que la operación se intentó enviar al CORE. Devuelve `202 Accepted` y su cuerpo se valida
(`evento` obligatorio, `polizaId` obligatorio).

---

## Modelo de datos

### Poliza

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Identificador único |
| `tipo` | `TipoPoliza` | `INDIVIDUAL` o `COLECTIVA` |
| `estado` | `Estado` | `ACTIVA`, `RENOVADA` o `CANCELADA` |
| `canonMensual` | `BigDecimal` | Valor del canon mensual de arrendamiento |
| `prima` | `BigDecimal` | Valor de la prima |
| `riesgos` | `List<Riesgo>` | Riesgos asociados |

### Riesgo

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `Long` | Identificador único global |
| `polizaId` | `Long` | Póliza a la que pertenece |
| `estado` | `Estado` | `ACTIVA` o `CANCELADA` |

> La prima se modela como valor calculado (canon × meses de vigencia), pero en esta
> versión simplificada se almacena directamente y se incrementa con el IPC en la
> renovación.

---

## Datos de demostración

La aplicación arranca con dos pólizas de ejemplo:

| Póliza | Tipo | Canon mensual | Prima | Riesgos |
|---|---|---|---|---|
| `555` | `COLECTIVA` | 1.000.000 | 500.000 | Riesgo `1` |
| `556` | `INDIVIDUAL` | 800.000 | 300.000 | Riesgo `2` |

Tras renovar la póliza `555` con IPC del 10%, el canon pasa a `1.100.000` y la prima a
`550.000`, con estado `RENOVADA`.

---

## Manejo de errores

`ApiExceptionHandler` centraliza las respuestas de error:

| Situación | Código HTTP | Cuerpo |
|---|---|---|
| Póliza o riesgo no encontrado | `404` | `{"error":"Póliza no encontrada"}` |
| Regla de negocio violada (ej. renovar cancelada) | `409` | `{"error":"..."}` |
| Parámetro inválido / tipo incorrecto | `400` | `{"error":"Parámetro inválido"}` |
| Validación de body fallida | `400` | Error de Bean Validation |
| API Key ausente o incorrecta | `401` | `{"error":"API Key inválida o ausente"}` |

Ejemplo de error de negocio:

```json
HTTP/1.1 409 Conflict
{ "error": "No se puede renovar una póliza cancelada" }
```

---

## Pruebas con Postman

1. Importe `postman/Modulo_2_Polizas.postman_collection.json` en Postman.
2. La colección define estas variables:

   | Variable | Valor por defecto |
   |---|---|
   | `baseUrl` | `http://localhost:8080` |
   | `apiKey` | `123456` |
   | `polizaId` | `555` |
   | `riesgoId` | `1` |

3. Un script a nivel de colección (`prerequest`) agrega automáticamente el header
   `x-api-key` a todas las solicitudes.

---

## Flujo de validación sugerido

Ejecute en orden para comprobar las reglas de negocio:

1. `GET /polizas` — ver las pólizas `555` y `556` y sus riesgos.
2. `POST /polizas/555/renovar` — verificar el incremento del 10% y estado `RENOVADA`.
3. `POST /polizas/555/riesgos` — agregar un riesgo (se genera un id nuevo y único).
4. `POST /polizas/556/riesgos` — debe responder `409` (es individual).
5. `POST /polizas/555/cancelar` — verificar que sus riesgos quedan `CANCELADA`.
6. `POST /polizas/555/renovar` — debe responder `409` (está cancelada).
7. Llamar un endpoint **sin** `x-api-key` — debe responder `401`.

---

## Decisiones de diseño

- **Repositorios como interfaces** (`PolizaRepository`, `RiesgoRepository`): permiten
  sustituir la implementación en memoria por JPA sin cambiar el servicio.
- **IDs de riesgo con `AtomicLong`**: garantizan unicidad global y son seguros ante
  concurrencia, evitando colisiones entre pólizas.
- **Excepción de negocio con código HTTP** (`BusinessException`): mantiene el controlador
  libre de lógica de validación y unifica la respuesta de errores.
- **Configuración externalizada** (`app.ipc`, `app.api-key`): facilita cambiar el IPC o la
  clave sin recompilar.
- **Contenerización** (Dockerfile multi-stage + Compose): despliegue reproducible y rápido.

> **Nota:** esta versión usa almacenamiento en memoria para centrarse en el alcance del
> Módulo 2. La persistencia real, las notificaciones y la integración con el CORE legado se
> describen en el diseño del Módulo 1.
