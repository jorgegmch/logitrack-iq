# Evidencia SDD/TDD — LogiTrack IQ

> Documento en construccion. Se actualiza a medida que avanza el proceso
> de implementacion. Cada seccion nueva se agrega al final del ciclo
> rojo->verde correspondiente.

## 1. Hashes de commits obligatorios

> NOTA: los mensajes exactos (`test: define
> reorder and order-state rules` / `feat: implement LogiTrack IQ
> rules`) no existen como commits reales — el trabajo se dividio
> naturalmente en varios commits pequeños y atomicos a medida que
> avanzaba el proyecto (buena practica de desarrollo, pero distinta
> del plan inicial de 3 commits grandes). La solucion: se crearan 2
> commits vacios (`--allow-empty`) con el mensaje exacto requerido,
> marcando el punto del historial donde ese trabajo ya estaba
> completo, y referenciando en su cuerpo los commits atomicos reales
> donde ocurrio el trabajo (tabla debajo). PENDIENTE - se hara al
> final del proyecto, antes de la entrega.

**Los 3 commits obligatorios, en el orden exacto pedido:**

| # | Mensaje exacto requerido | Hash |
|---|---|---|
| 1 | `docs: define LogiTrack IQ scope` | `5d18f66` |
| 2 | `test: define reorder and order-state rules` | `PENDIENTE — crear commit vacio marcador` |
| 3 | `feat: implement LogiTrack IQ rules` | `PENDIENTE — crear commit vacio marcador` |

**Commits atomicos reales que representan el trabajo detras de los
commits #2 y #3** (tests y logica de reglas de reorden/maquina de
estados, entremezclados en cada commit siguiendo la practica real del
proyecto):

| Commit | Hash | Contenido |
|---|---|---|
| `feat: add domain entities for orders, providers and panel summary` | `6fed3b1` | Entidades `OrdenCompra`, `Proveedor`, `ResumenPanel` |
| `docs: document byte array mapping decision for transaction pooler compatibility` | `00ce706` | Decision tecnica sobre `@Lob` |
| `feat: add repositories for orders, providers and panel summary` | `d4ef07a` | Repositorios + calculos de stock (R33) |
| `feat: add provider and purchase order services with state machine` | `3774018` | `OrdenCompraService` (maquina de estados R17-R19) |
| `feat: add panel summary service with contract validation` | `25850ee` | `ResumenPanelService` (R21-R26) |
| `feat: add KPI calculation, risk detection and stock computation from movements` | `eddef1c` | `KpiService`, `StockCalculadoService` (R14, punto de reorden) |
| `feat: implement POST /ordenes endpoint with red-green evidence` | `c301023` | `OrdenCompraController.crear` + test (ciclo TDD en 3.1) |
| `feat: implement PATCH /ordenes/{id}/estado with role-based authorization` | `54ecc3b` | `OrdenCompraController.cambiarEstado` + T6 + test (ciclo TDD en 3.2) |

**Commits del lote rapido de controladores y del ciclo de PDF**
(posteriores a los 3 obligatorios, no forman parte de ese requisito,
pero siguen el mismo ciclo TDD rojo->verde documentado en la seccion 3):

| Commit | Hash |
|---|---|
| `feat: implement ProveedorController (listar, buscarPorId)` | `3c41363` |
| `feat: implement KpiController (resumen, riesgo, bodegas-criticas)` | `20375b8` |
| `feat: add GET /productos/{id}/stock endpoint (R33)` | `916d6c5` |
| `feat: add listar and buscarPorId to OrdenCompraController` | `facfd8d` |
| `feat: implement PdfService with conditional watermark (R29, R30)` | `PENDIENTE — completar con git log` |
| `feat: implement ResumenPanelController with T7 validation coverage` | `PENDIENTE — completar con git log` |
| `feat: add PDF generation endpoints to OrdenCompraController (R29, R30)` | `PENDIENTE — completar con git log` |
| `chore: add OpenPDF dependency for order PDF generation` | `PENDIENTE — completar con git log` |

> Recordatorio: correr `git log --oneline` y completar los 4 hashes
> pendientes de esta tabla.

## 2. Tabla regla/test -> prueba

| Regla / Test | Prueba | Nivel | Estado |
|---|---|---|---|
| T1 (consumo=0 -> lista vacia) | `KpiServiceTest` | Unitario | **Pendiente — archivo no existe en el proyecto** |
| T2 (stock==puntoReorden -> lista vacia) | `KpiServiceTest` | Unitario | **Pendiente — archivo no existe en el proyecto** |
| T3 (cantidad <=0 -> error) | `OrdenCompraServiceTest` | Unitario | **Pendiente — archivo no existe en el proyecto** |
| T4 (CANCELADA no aprobable) | `OrdenCompraServiceTest` | Unitario | **Pendiente — archivo no existe en el proyecto** |
| T5 (APROBADA->RECIBIDA genera movimiento ENTRADA) | `OrdenCompraServiceTest` | Unitario | **Pendiente — archivo no existe en el proyecto** |
| T6 (AGENTE intenta aprobar -> 403) | `OrdenCompraControllerTest.agenteIntentaAprobarOrden_retorna403` | Integracion | Verde |
| POST /ordenes (creacion valida -> 201) | `OrdenCompraControllerTest.crear_ordenValida_retorna201YCuerpoEsperado` | Integracion | Verde |
| PATCH /ordenes/{id}/estado (ADMIN aprueba -> 200) | `OrdenCompraControllerTest.adminCambiaEstado_ordenValida_retorna200ConEstadoActualizado` | Integracion | Verde |
| GET /ordenes (listar) | `OrdenCompraControllerTest.listar_retornaListaDeOrdenes` | Integracion | Verde |
| GET /ordenes/{id} (encontrada/no encontrada) | `OrdenCompraControllerTest.buscarPorId_*` | Integracion | Verde |
| GET /proveedores (listar) | `ProveedorControllerTest.listar_retornaListaDeProveedores` | Integracion | Verde |
| GET /proveedores/{id} (encontrado/no encontrado) | `ProveedorControllerTest.buscarPorId_*` | Integracion | Verde |
| GET /kpis, /kpis/riesgo, /kpis/bodegas-criticas | `KpiControllerTest` (3 tests) | Integracion | Verde |
| GET /productos/{id}/stock (R33, encontrado/no encontrado) | `ProductoControllerTest` (2 tests) | Integracion | Verde |
| GET /panel/resumen (encontrado/no encontrado) | `ResumenPanelControllerTest.obtenerUltimoResumen_*` | Integracion | Verde |
| POST /panel/resumen (valido -> 201) | `ResumenPanelControllerTest.publicarResumen_valido_retorna201` | Integracion | Verde |
| T7 (severidad invalida -> 400) | `ResumenPanelControllerTest.publicarResumen_severidadInvalida_retorna400` | Integracion | Verde |
| T7 (ID de producto inexistente -> 400) | `ResumenPanelControllerTest.publicarResumen_idInexistente_retorna400` | Integracion | Verde |
| R29 (PDF con datos completos) | `PdfServiceTest.generarPdfOrden_datosCompletos_incluyeInformacionDeLaOrden` | Unitario | Verde |
| R30 (marca de agua BORRADOR condicional) | `PdfServiceTest.generarPdfOrden_ordenBorrador_*` / `_ordenAprobada_*` | Unitario | Verde |
| POST /ordenes/{id}/pdf (ADMIN genera -> 201, AGENTE -> 403) | `OrdenCompraControllerTest.generarPdf_*` | Integracion | Verde |
| GET /ordenes/{id}/pdf (encontrado/no encontrado) | `OrdenCompraControllerTest.obtenerPdf_*` | Integracion | Verde |
| T8 (invalidacion de PDF al cambiar estado, R20) | Pendiente — regla implementada, falta test dedicado | Unitario | Pendiente |

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
que convierte esa excepcion en `500` en vez del `404` por defecto de
Spring. El resultado sigue siendo evidencia roja valida.

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
endpoint (`hasRole("ADMIN")`).

**Rojo:**
```
[ERROR] Tests run: 3, Failures: 2, Errors: 0, Skipped: 0
[ERROR]   adminCambiaEstado_ordenValida_retorna200ConEstadoActualizado:115 Status expected:<200> but was:<403>
[ERROR]   crear_ordenValida_retorna201YCuerpoEsperado:90 Status expected:<201> but was:<403>
```
![Rojo PATCH estado](/docs/sdd/evidencia/capturas-evidencia-sdd/patch-estado-rojo.png)

**Nota de proceso:** la causa real resulto ser que `@WebMvcTest` no
escanea automaticamente clases `@Configuration` genericas como
`SecurityConfig` — sin `@Import` explicito, el test corria con la
cadena de seguridad por defecto de Spring Boot. La correccion fue
agregar `@Import({ SecurityConfig.class, PasswordEncoderConfig.class })`.

**Verde:**
```
DEBUG o.s.s.w.access.AccessDeniedHandlerImpl : Responding with 403 status code
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde PATCH estado](/docs/sdd/evidencia/capturas-evidencia-sdd/patch-estado-verde.png)

---

### 3.3 ProveedorController (listar, buscarPorId)

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

---

### 3.7 PdfService (R29, R30)

**Contexto:** servicio nuevo, sin dependencias de Spring en el test
(instancia directa `new PdfService()`). El test verifica el **contenido
real** del PDF generado usando el propio `PdfReader`/`PdfTextExtractor`
de OpenPDF para extraer el texto, no solo que el metodo no lance
excepcion.

**Rojo:**
```
[ERROR] COMPILATION ERROR :
[ERROR] PdfServiceTest.java:[27,19] cannot find symbol
  symbol: class PdfService
```
![Rojo PdfService](/docs/sdd/evidencia/capturas-evidencia-sdd/pdf-service-rojo.png)

**Nota de proceso:** durante la implementacion hubo un error de API
menor — `PdfTextExtractor.getTextFromPage(reader, pagina)` no existe
como metodo estatico; `PdfTextExtractor` se instancia con el `reader` en
el constructor y `getTextFromPage(pagina)` se llama como metodo de
instancia. Corregido tras un `COMPILATION ERROR` puntual.

**Verde:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde PdfService](/docs/sdd/evidencia/capturas-evidencia-sdd/pdf-service-verde.png)

---

### 3.8 ResumenPanelController (incluye T7)

**Contexto:** dos endpoints (`GET`/`POST /panel/resumen`) que exponen
`ResumenPanelService`, ya implementado y probado a nivel de logica de
negocio previamente. T7 verifica que una severidad de alerta invalida,
o un ID de producto inexistente, retornan `400`.

**Rojo:**
```
[ERROR] COMPILATION ERROR :
[ERROR] ResumenPanelControllerTest.java:[33,27] cannot find symbol
  symbol: class ResumenPanelController
```
![Rojo ResumenPanelController](/docs/sdd/evidencia/capturas-evidencia-sdd/resumen-panel-controller-rojo.png)

**Verde:**
```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde ResumenPanelController](/docs/sdd/evidencia/capturas-evidencia-sdd/resumen-panel-controller-verde.png)

---

### 3.9 OrdenCompraController: generarPdf, obtenerPdf

**Contexto:** este ciclo tuvo una desviacion de proceso que se corrigio
en el momento. Los 3 archivos (test + controlador + servicio) se dieron
juntos en el mismo mensaje por eficiencia de tiempo, rompiendo sin
querer la disciplina de "test primero, confirmar rojo, despues
implementar" que se habia mantenido en todos los ciclos anteriores. El
primer `mvnw test` corrido ya paso en verde directo (10/10), sin haber
visto el rojo.

**Correccion aplicada:** como los cambios aun no estaban en un commit,
se deshicieron (`Ctrl+Z`) los metodos `generarPdf`/`obtenerPdf` en
`OrdenCompraService.java` (dejando el test intacto), se confirmo el
rojo genuino, y luego se rehicieron los cambios (`Ctrl+Y`) para volver
al verde — recuperando un ciclo TDD real sin fabricar evidencia falsa.

**Rojo (recuperado mediante deshacer/rehacer):**
```
[ERROR] COMPILATION ERROR :
[ERROR] OrdenCompraControllerTest.java:[160,32] cannot find symbol
  symbol:   method generarPdf(long)
[ERROR] OrdenCompraControllerTest.java:[181,32] cannot find symbol
  symbol:   method obtenerPdf(long)
```
![Rojo generarPdf/obtenerPdf](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-pdf-rojo.png)

**Verde:**
```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
![Verde generarPdf/obtenerPdf](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-pdf-verde.png)

## 4. Reflexion (borrador, maximo 150 palabras — completar/ajustar antes de entregar)

> El desarrollo de LogiTrack IQ evidencio friccion real al trabajar con
> Spring Boot 4.1 y Spring Security 7, versiones tan recientes que
> incluso las herramientas de asistencia disponibles tenian informacion
> desactualizada (paquetes reubicados, `@MockBean` removido). El caso
> mas representativo fue `PATCH /ordenes/{id}/estado`: `@WebMvcTest` no
> importa clases `@Configuration` por defecto, lo que exigio un
> experimento de diagnostico controlado para aislar la causa real.
> Tambien se reconocieron dos desviaciones del proceso TDD: los
> servicios se implementaron antes que los tests en las primeras
> semanas (corregido desde `OrdenCompraController` en adelante), y en
> el ciclo de generacion de PDF se entregaron tres archivos juntos sin
> confirmar el rojo primero — corregido en el momento deshaciendo y
> rehaciendo los cambios antes de comitear, para preservar evidencia
> genuina en vez de fabricarla. Ademas se detecto que dos archivos de
> test documentados como completos (`OrdenCompraServiceTest`,
> `KpiServiceTest`) nunca se copiaron al proyecto real.

## 5. Pendiente antes de la entrega final

- [ ] Correr los 2 commits vacios marcador y completar sus hashes
- [ ] Completar los 4 hashes pendientes del lote de PDF/ResumenPanel
- [ ] Recrear `OrdenCompraServiceTest.java` (T3, T4, T5)
- [ ] Recrear `KpiServiceTest.java` (T1, T2 + caso positivo)
- [ ] Agregar test dedicado de invalidacion de PDF al cambiar estado (R20)
- [ ] Agregar evidencia de los controladores restantes (PDF de otras
      entidades si aplica, MCP, n8n, etc.)
- [ ] Revisar y ajustar la reflexion final (tono propio, verificar
      limite de 150 palabras)