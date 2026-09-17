# API de gestión de pólizas

Aplicación Spring Boot con almacenamiento en memoria para la prueba técnica del módulo 2.

La solución usa datos iniciales de demostración y no requiere base de datos externa. Al recrear el contenedor, los datos regresan a su estado inicial.

## Despliegue y ejecución

### Opción 1: Maven

Requiere Java 17 y Maven instalado:

```bash
mvn spring-boot:run
```

La aplicación quedará disponible en `http://localhost:8080`.

Para generar el artefacto ejecutable:

```bash
mvn clean package
java -jar target/polizas-api-1.0.0.jar
```

### Opción 2: Docker

Requiere Docker instalado. Desde la raíz del proyecto:

```bash
docker build -t polizas-api:local .
docker run -d --name polizas-api -p 8080:8080 polizas-api:local
```

Verificar que el contenedor esté ejecutándose:

```bash
docker ps
curl -H 'x-api-key: 123456' http://localhost:8080/polizas
```

Comandos para administrar el contenedor:

```bash
docker logs -f polizas-api
docker stop polizas-api
docker start polizas-api
docker rm -f polizas-api
```

### Opción 3: Docker Compose (recomendada)

Requiere Docker Compose. Ejecutar desde la raíz del proyecto:

```bash
docker compose up --build -d
```

Consultar los logs:

```bash
docker compose logs -f
```

Detener y eliminar el servicio:

```bash
docker compose down
```

El IPC puede cambiarse al iniciar el contenedor mediante una variable de entorno de Spring:

```bash
docker run -d --name polizas-api -p 8080:8080 \
  -e APP_IPC=0.12 polizas-api:local
```

### Pruebas con Postman

Importar `postman/Modulo_2_Polizas.postman_collection.json` en Postman. La colección usa estas variables:

- `baseUrl`: `http://localhost:8080`
- `apiKey`: `123456`
- `polizaId`: `555`
- `riesgoId`: `1`

Todas las solicitudes agregan automáticamente el header `x-api-key` mediante un script de colección.

### Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/polizas?tipo=COLECTIVA&estado=ACTIVA` | Lista y filtra pólizas |
| GET | `/polizas/{id}/riesgos` | Consulta riesgos de una póliza |
| POST | `/polizas/{id}/renovar` | Renueva aplicando IPC |
| POST | `/polizas/{id}/cancelar` | Cancela póliza y sus riesgos |
| POST | `/polizas/{id}/riesgos` | Agrega riesgo a una póliza colectiva |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo |
| POST | `/core-mock/evento` | Registra un evento simulado del CORE |

### Flujo rápido de validación

1. Listar pólizas y consultar los riesgos iniciales.
2. Renovar la póliza `555` y verificar el incremento del 10%.
3. Agregar un riesgo a la póliza colectiva `555`.
4. Intentar agregar un riesgo a la póliza individual `556` y verificar respuesta `409`.
5. Cancelar la póliza `555` y verificar que sus riesgos quedan cancelados.
6. Intentar renovarla nuevamente y verificar respuesta `409`.

Todos los endpoints requieren `x-api-key: 123456`. El IPC se configura en `app.ipc` (por defecto `0.10`).

Ejemplos:

```bash
curl -H 'x-api-key: 123456' 'http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA'
curl -H 'x-api-key: 123456' http://localhost:8080/polizas/555/riesgos
curl -X POST -H 'x-api-key: 123456' http://localhost:8080/polizas/555/renovar
curl -X POST -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' http://localhost:8080/core-mock/evento
```

La aplicación inicia con las pólizas `555` (colectiva) y `556` (individual), cada una con un riesgo.
