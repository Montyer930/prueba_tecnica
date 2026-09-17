# Módulo 2 – Prueba Técnica Práctica

## Hands-on Code Challenge

**Duración:** 90 minutos  
**Evalúa:** Estilo de codificación, lógica, buenas prácticas y arquitectura limpia.

## Caso técnico

### API de Gestión de Pólizas

Implementar únicamente lo esencial de una API para la gestión de pólizas.

## Requerimientos

### 1. Listar pólizas

**Endpoint:**
```http
GET /polizas
```

Debe permitir listar pólizas filtrando por:

- `tipo`
- `estado`

---

### 2. Consultar riesgos de una póliza

**Endpoint:**
```http
GET /polizas/{id}/riesgos
```

Debe retornar los riesgos asociados a una póliza.

---

### 3. Renovar una póliza

**Endpoint:**
```http
POST /polizas/{id}/renovar
```

Al renovar una póliza se debe:

1. Incrementar el canon en el porcentaje de **IPC**.
2. Incrementar la prima en el porcentaje de **IPC**.
3. Cambiar el estado de la póliza a `RENOVADA`.

**Regla de negocio:**

- No se puede renovar una póliza que se encuentre cancelada.

---

### 4. Cancelar una póliza

**Endpoint:**
```http
POST /polizas/{id}/cancelar
```

La cancelación debe aplicar la regla de negocio correspondiente a sus riesgos.

**Regla de negocio:**

- La cancelación de una póliza cancela todos sus riesgos.

---

### 5. Agregar un riesgo

**Endpoint:**
```http
POST /polizas/{id}/riesgos
```

Solo debe permitirse cuando la póliza sea de tipo `COLECTIVA`.

**Reglas de negocio:**

- Una póliza individual solo puede tener **1 riesgo**.
- Agregar un riesgo exige validar el tipo de póliza.
- Las pólizas individuales no deben permitir agregar riesgos adicionales si ya cuentan con uno.

---

### 6. Cancelar un riesgo

**Endpoint:**
```http
POST /riesgos/{id}/cancelar
```

Debe cambiar el estado del riesgo a cancelado y aplicar las validaciones correspondientes.

---

# Reglas de negocio esenciales

La implementación debe contemplar como mínimo:

| Regla | Comportamiento |
|---|---|
| Póliza individual | Solo puede tener 1 riesgo |
| Renovación | No se puede renovar una póliza cancelada |
| Cancelación de póliza | Cancela todos sus riesgos |
| Agregar riesgo | Solo permitido para pólizas colectivas |
| Validación | Las reglas deben ejecutarse en la capa de servicio |

---

# Mock externo obligatorio

Se debe implementar un endpoint que simule la comunicación con el CORE.

**Endpoint:**
```http
POST /core-mock/evento
```

### Payload

```json
{
  "evento": "ACTUALIZACION",
  "polizaId": 555
}
```

### Propósito

El endpoint únicamente debe:

- Recibir el evento.
- Registrar en logs que la operación intentó ser enviada al CORE.

No se requiere implementar una integración real con el CORE.

---

# Seguridad mínima

Todos los endpoints deben requerir el siguiente header:

```http
x-api-key: 123456
```

La API debe validar que el header esté presente y tenga el valor esperado.

Ejemplo:

```http
x-api-key: 123456
```

Una solicitud sin una API Key válida debe ser rechazada.

---

# Stack y estructura requerida

El proyecto debe utilizar:

**Spring Boot**

Se solicita una estructura basada en capas:

```text
src/
└── main/
    └── java/
        └── ...
            ├── controller/
            ├── service/
            ├── repository/
            └── ...
```

Como mínimo deben existir:

- `controller`
- `service`
- `repository`

---

# Entidades básicas

Se deben implementar las entidades:

## Poliza

Debe representar como mínimo la información necesaria para:

- Identificar la póliza.
- Determinar su tipo.
- Determinar su estado.
- Gestionar el canon mensual.
- Gestionar la prima.
- Asociar sus riesgos.

## Riesgo

Debe representar como mínimo la información necesaria para:

- Identificar el riesgo.
- Asociarlo a una póliza.
- Gestionar su estado.

La implementación concreta de los atributos queda a criterio del desarrollador, siempre que permita cumplir los requerimientos y reglas de negocio.

---

# Entregables

El resultado debe incluir:

1. **Estructura del proyecto**
   - Spring Boot.
   - Capas `controller`, `service` y `repository`.

2. **Entidades**
   - `Poliza`.
   - `Riesgo`.

3. **Endpoints funcionales**
   - `GET /polizas`
   - `GET /polizas/{id}/riesgos`
   - `POST /polizas/{id}/renovar`
   - `POST /polizas/{id}/cancelar`
   - `POST /polizas/{id}/riesgos`
   - `POST /riesgos/{id}/cancelar`
   - `POST /core-mock/evento`

4. **Validaciones de negocio**
   - Restricción de riesgos para pólizas individuales.
   - Validación de tipo para agregar riesgos.
   - Restricción de renovación de pólizas canceladas.
   - Cancelación de riesgos al cancelar una póliza.

5. **Seguridad mínima**
   - Validación del header `x-api-key`.

6. **README**
   - Instrucciones para ejecutar el proyecto.
   - Configuración necesaria.
   - Forma de probar los endpoints.

---

# Criterios técnicos recomendados para la implementación

Aunque la prueba solicita únicamente lo esencial, la implementación debería mantener una separación clara de responsabilidades:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Persistencia
```

La lógica de negocio debe permanecer principalmente en la capa `service`, evitando colocar reglas de negocio directamente en los controllers.

Una posible organización sería:

```text
controller/
├── PolizaController
├── RiesgoController
└── CoreMockController

service/
├── PolizaService
├── RiesgoService
└── CoreMockService

repository/
├── PolizaRepository
└── RiesgoRepository

entity/
├── Poliza
└── Riesgo

dto/
├── ...
```

La estructura exacta puede adaptarse mientras se mantengan las responsabilidades separadas y el código sea claro y mantenible.

---

# Flujo esperado de operaciones

## Renovación

```text
Cliente
   ↓
POST /polizas/{id}/renovar
   ↓
Controller
   ↓
PolizaService
   ↓
Validar que exista
   ↓
Validar que no esté CANCELADA
   ↓
Aplicar incremento IPC
   ↓
Cambiar estado → RENOVADA
   ↓
Registrar/realizar evento hacia CORE
   ↓
Respuesta HTTP
```

## Cancelación de póliza

```text
Cliente
   ↓
POST /polizas/{id}/cancelar
   ↓
Controller
   ↓
PolizaService
   ↓
Cancelar póliza
   ↓
Cancelar todos sus riesgos
   ↓
Registrar/realizar evento hacia CORE
   ↓
Respuesta HTTP
```

## Agregar riesgo

```text
Cliente
   ↓
POST /polizas/{id}/riesgos
   ↓
Controller
   ↓
PolizaService / RiesgoService
   ↓
Validar existencia de póliza
   ↓
Validar tipo = COLECTIVA
   ↓
Crear riesgo
   ↓
Registrar/realizar evento hacia CORE
   ↓
Respuesta HTTP
```

---

# Consideraciones para la implementación

Durante los 90 minutos se debe priorizar:

1. Código funcional.
2. Cumplimiento de las reglas de negocio.
3. Separación de responsabilidades.
4. Validaciones y manejo de errores.
5. Seguridad mediante `x-api-key`.
6. README claro.
7. Código limpio y fácil de mantener.

No es necesario implementar funcionalidades adicionales que no estén solicitadas en el módulo.
