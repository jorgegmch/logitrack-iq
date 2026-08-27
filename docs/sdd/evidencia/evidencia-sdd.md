# Evidencia SDD/TDD — LogiTrack IQ

> Documento en construccion. Se actualiza a medida que avanza el proceso
> de implementacion. Cada seccion nueva se agrega al final del ciclo
> rojo->verde correspondiente.

## 1. Hashes de commits obligatorios

> NOTA: los mensajes exactos que pide el profesor (`test: define
> reorder and order-state rules` / `feat: implement LogiTrack IQ
> rules`) no existen como commits reales — el trabajo se dividio
> naturalmente en varios commits pequeños y atomicos a medida que
> avanzaba el proyecto (buena practica de desarrollo, pero distinta
> del plan inicial de 3 commits grandes). La solucion: se crearan 2
> commits vacios (`--allow-empty`) con el mensaje exacto requerido,
> marcando el punto del historial donde ese trabajo ya estaba
> completo, y referenciando en su cuerpo los commits atomicos reales
> donde ocurrio el trabajo (tabla debajo).

**Los 3 commits obligatorios, en el orden exacto pedido:**

| # | Mensaje exacto requerido | Hash |
|---|---|---|
| 1 | `docs: define LogiTrack IQ scope` | `5d18f66` |
| 2 | `test: define reorder and order-state rules` | `PENDIENTE — crear commit vacio marcador` |
| 3 | `feat: implement LogiTrack IQ rules` | `PENDIENTE — crear commit vacio marcador` |

**Commits atomicos reales que representan el trabajo detras de los
commits #2 y #3** (tests y logica de reglas de reorden/maquina de
estados, entremezclados en cada commit siguiendo la practica real del
proyecto — no se separaron en "solo test" y "solo feat" porque cada
commit atomico incluyo ambas cosas juntas):

| Commit | Hash | Contenido |
|---|---|---|
| `feat: add domain entities for orders, providers and panel summary` | `6fed3b1` | Entidades `OrdenCompra`, `Proveedor`, `ResumenPanel` |
| `docs: document byte array mapping decision for transaction pooler compatibility` | `00ce706` | Decision tecnica sobre `@Lob` |
| `feat: add repositories for orders, providers and panel summary` | `d4ef07a` | Repositorios + calculos de stock (R33) |
| `feat: add provider and purchase order services with state machine` | `3774018` | `OrdenCompraService` (maquina de estados R17-R19) |
| `feat: add panel summary service with contract validation` | `25850ee` | `ResumenPanelService` (R21-R26) |
| `feat: add KPI calculation, risk detection and stock computation from movements` | `eddef1c` | `KpiService`, `StockCalculadoService` (R14, punto de reorden) |
| `feat: implement POST /ordenes endpoint with red-green evidence` | `c301023` | `OrdenCompraController.crear` + test (ciclo TDD documentado en 3.1) |
| `feat: implement PATCH /ordenes/{id}/estado with role-based authorization` | `54ecc3b` | `OrdenCompraController.cambiarEstado` + T6 + test (ciclo TDD documentado en 3.2) |

**Commits del lote rapido de controladores** (posteriores a los 3
obligatorios, no forman parte de ese requisito, pero siguen el mismo
ciclo TDD rojo->verde documentado en la seccion 3):

| Commit | Hash |
|---|---|
| `feat: implement ProveedorController (listar, buscarPorId)` | `3c41363` |
| `feat: implement KpiController (resumen, riesgo, bodegas-criticas)` | `20375b8` |
| `feat: add GET /productos/{id}/stock endpoint (R33)` | `916d6c5` |
| `feat: add listar and buscarPorId to OrdenCompraController` | `facfd8d` |

## 2. Tabla regla/test -> prueba

| Regla / Test | Prueba | Nivel | Estado |
|---|---|---|---|
| T1 (consumo=0 -> lista vacia) | `KpiServiceTest` | Unitario | Verde |
| T2 (stock==puntoReorden -> lista vacia) | `KpiServiceTest` | Unitario | Verde |
| T3 (cantidad <=0 -> error) | `OrdenCompraServiceTest` | Unitario | Verde |
| T4 (CANCELADA no aprobable) | `OrdenCompraServiceTest` | Unitario | Verde |
| T5 (APROBADA->RECIBIDA genera movimiento ENTRADA) | `OrdenCompraServiceTest` | Unitario | Verde |
| T6 (AGENTE intenta aprobar -> 403) | `OrdenCompraControllerTest.agenteIntentaAprobarOrden_retorna403` | Integracion | Verde |
| POST /ordenes (creacion valida -> 201) | `OrdenCompraControllerTest.crear_ordenValida_retorna201YCuerpoEsperado` | Integracion | Verde |
| PATCH /ordenes/{id}/estado (ADMIN aprueba -> 200) | `OrdenCompraControllerTest.adminCambiaEstado_ordenValida_retorna200ConEstadoActualizado` | Integracion | Verde |
| GET /ordenes (listar) | `OrdenCompraControllerTest.listar_retornaListaDeOrdenes` | Integracion | Verde |
| GET /ordenes/{id} (encontrada/no encontrada) | `OrdenCompraControllerTest.buscarPorId_*` | Integracion | Verde |
| GET /proveedores (listar) | `ProveedorControllerTest.listar_retornaListaDeProveedores` | Integracion | Verde |
| GET /proveedores/{id} (encontrado/no encontrado) | `ProveedorControllerTest.buscarPorId_*` | Integracion | Verde |
| GET /kpis, /kpis/riesgo, /kpis/bodegas-criticas | `KpiControllerTest` (3 tests) | Integracion | Verde |
| GET /productos/{id}/stock (R33, encontrado/no encontrado) | `ProductoControllerTest` (2 tests) | Integracion | Verde |
| T7 (resumen severidad/ID invalido -> 400) | Pendiente | Integracion | Pendiente |
| T8 (PDF BORRADOR con marca de agua) | Pendiente | Integracion | Pendiente |

## 3. Evidencia roja -> verde

### 3.1 POST /ordenes

**Contexto:** `OrdenCompraController` existia como esqueleto sin metodo
`@PostMapping`. El test ya estaba completo, esperando `201 Created`.

**Rojo:**
```
Resolved Exception: NoResourceFoundException
MockHttpServletResponse:
    Status = 500
    Body = {"status":500,"error":"Error interno del servidor", ...}

[ERROR] com.jorgegmch.logitrack.controller.OrdenCompraControllerTest.crear_ordenValida_retorna201YCuerpoEsperado -- FAILURE!
java.lang.AssertionError: Status expected:<201> but was:<500>
```
![Rojo POST /ordenes](/docs/sdd/evidencia/capturas-evidencia-sdd/post-ordenes-rojo.png)

**Explicacion del codigo (500 en vez de 404):** al no existir el
`@PostMapping`, Spring intento resolver `/ordenes` como recurso
estatico, lanzando `NoResourceFoundException`. El `GlobalExceptionHandler`
del proyecto tiene un manejador generico (`@ExceptionHandler(Exception.class)`)
que convierte esa excepcion en `500` con el formato de error propio del
proyecto en vez del `404` por defecto de Spring. El resultado sigue
siendo evidencia roja valida: confirma que el endpoint no funcionaba.

**Verde:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde POST /ordenes](/docs/sdd/evidencia/capturas-evidencia-sdd/post-ordenes-verde.png)

---

### 3.2 PATCH /ordenes/{id}/estado (incluye T6)

**Contexto:** se agrego el metodo `cambiarEstado` al controlador, el DTO
`CambiarEstadoRequest`, y las reglas de `SecurityConfig` para este
endpoint (`hasRole("ADMIN")`). El test cubre dos escenarios: ADMIN
aprueba correctamente (200) y AGENTE intenta aprobar sin permiso (403,
T6).

**Rojo (antes de implementar `cambiarEstado` + reglas de seguridad):**
```
[ERROR] Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
[ERROR]   adminCambiaEstado_ordenValida_retorna200ConEstadoActualizado:115 Status expected:<200> but was:<403>
[ERROR]   crear_ordenValida_retorna201YCuerpoEsperado:90 Status expected:<201> but was:<403>
```
![Rojo PATCH estado](/docs/sdd/evidencia/capturas-evidencia-sdd/patch-estado-rojo.png)

**Nota de proceso (relevante para la reflexion):** este ciclo tuvo una
complicacion real que vale la pena documentar. El primer intento de
implementacion parecia correcto (controlador + reglas de seguridad con
patrones `/ordenes/*/estado`), pero el test seguia fallando: el rol
`AGENTE` lograba pasar la seguridad y llegar al controlador cuando no
deberia. Tras varias hipotesis descartadas (sintaxis de comodines,
motor de coincidencia de rutas de Spring Security 7), la causa real
resulto ser que `@WebMvcTest` no escanea automaticamente clases
`@Configuration` genericas como `SecurityConfig` — sin un `@Import`
explicito, el test corria con la cadena de seguridad por defecto de
Spring Boot (sin las reglas de rol del proyecto). La correccion fue
agregar `@Import({ SecurityConfig.class, PasswordEncoderConfig.class })`
al test.

**Verde:**
```
DEBUG o.s.s.w.access.AccessDeniedHandlerImpl : Responding with 403 status code
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde PATCH estado](/docs/sdd/evidencia/capturas-evidencia-sdd/patch-estado-verde.png)

---

### 3.3 ProveedorController (listar, buscarPorId)

**Contexto:** controlador de solo lectura, sin reglas de rol
especificas (requiere solo autenticacion, via el catch-all de
`SecurityConfig`).

**Rojo:**
```
[ERROR] COMPILATION ERROR :
[ERROR] ProveedorControllerTest.java:[35,27] cannot find symbol
  symbol: class ProveedorController
```
![Rojo ProveedorController](/docs/sdd/evidencia/capturas-evidencia-sdd/proveedor-controller-rojo.png)

**Verde:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde ProveedorController](/docs/sdd/evidencia/capturas-evidencia-sdd/proveedor-controller-verde.png)

---

### 3.4 KpiController (resumen, riesgo, bodegas criticas)

**Contexto:** tres endpoints de solo lectura que exponen `KpiService`,
ya implementado y probado a nivel de servicio previamente.

**Rojo:**
```
[ERROR] COMPILATION ERROR :
[ERROR] KpiControllerTest.java:[33,27] cannot find symbol
  symbol: class KpiController
```
![Rojo KpiController](/docs/sdd/evidencia/capturas-evidencia-sdd/kpi-controller-rojo.png)

**Verde:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde KpiController](/docs/sdd/evidencia/capturas-evidencia-sdd/kpi-controller-verde.png)

---

### 3.5 GET /productos/{id}/stock (R33)

**Contexto:** se extendio el `ProductoController` ya existente (sin
tests previos) con un endpoint nuevo que expone
`StockCalculadoService.calcularStockTotalProducto`. Se valida primero
la existencia del producto (404 si no existe) antes de calcular el
stock.

**Rojo:**
```
Resolved Exception: NoResourceFoundException
MockHttpServletResponse: Status = 500

[ERROR] obtenerStock_productoExistente_retorna200ConStockTotal -- FAILURE!
java.lang.AssertionError: Status expected:<200> but was:<500>
[ERROR] obtenerStock_productoNoExistente_retorna404 -- FAILURE!
java.lang.AssertionError: Status expected:<404> but was:<500>
```
![Rojo GET productos stock](/docs/sdd/evidencia/capturas-evidencia-sdd/producto-stock-rojo.png)

**Verde:**
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde GET productos stock](/docs/sdd/evidencia/capturas-evidencia-sdd/producto-stock-verde.png)

---

### 3.6 OrdenCompraController: listar, buscarPorId

**Contexto:** se completaron los dos metodos de solo lectura que
faltaban en `OrdenCompraController`, agregando los tests al archivo de
test ya existente (sin modificar los 3 tests previos, que siguieron
pasando durante todo el ciclo).

**Rojo:**
```
[ERROR] Tests run: 6, Failures: 3, Errors: 0, Skipped: 0
[ERROR]   listar_retornaListaDeOrdenes:164 Status expected:<200> but was:<500>
[ERROR]   buscarPorId_ordenExistente_retorna200:179 Status expected:<200> but was:<500>
[ERROR]   buscarPorId_ordenNoExistente_retorna404:190 Status expected:<404> but was:<500>
```
![Rojo listar/buscarPorId](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-listar-buscar-rojo.png)

**Verde:**
```
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde listar/buscarPorId](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-listar-buscar-verde.png)

## 4. Reflexion (borrador, maximo 150 palabras — completar/ajustar antes de entregar)

> El desarrollo de LogiTrack IQ evidencio friccion real al trabajar con
> Spring Boot 4.1 y Spring Security 7, versiones tan recientes que
> incluso la documentacion y las herramientas de asistencia disponibles
> tenian informacion desactualizada (paquetes reubicados, `@MockBean`
> removido en favor de `@MockitoBean`). El caso mas representativo fue
> el ciclo de `PATCH /ordenes/{id}/estado`: una prueba de integracion
> que fallaba silenciosamente por una causa no evidente (`@WebMvcTest`
> no importa clases `@Configuration` por defecto), lo que llevo a
> descartar varias hipotesis tecnicas razonables antes de llegar a la
> causa real mediante un experimento de diagnostico controlado
> (`denyAll()` como prueba binaria). Tambien se detecto y corrigio un
> desvio del proceso TDD: los servicios se implementaron antes que los
> tests unitarios en las primeras semanas, error reconocido y corregido
> a partir de `OrdenCompraController` en adelante, donde el ciclo
> rojo->verde se siguio de forma estricta en cada uno de los siete
> endpoints implementados desde entonces.

## 5. Pendiente antes de la entrega final

- [ ] Correr los 2 commits vacios marcador (`test: define reorder and
      order-state rules` / `feat: implement LogiTrack IQ rules`) y
      completar sus hashes en la seccion 1
- [ ] Agregar evidencia de T7, T8
- [ ] Agregar evidencia de `ResumenPanelController` y PDF de orden
- [ ] Revisar y ajustar la reflexion final (tono propio, verificar
      limite de 150 palabras)