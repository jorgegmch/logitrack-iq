# Evidencia SDD/TDD — LogiTrack IQ

Enlaces relativos a los documentos del proceso: [01-propuesta.md](01-propuesta.md),
[02-especificacion.md](02-especificacion.md), [03-diseno.md](03-diseno.md),
[04-tareas.md](04-tareas.md).

## 1. Hashes de commits obligatorios ✅ COMPLETO

> Los mensajes exactos requeridos por el profesor (`test: define
> reorder and order-state rules` / `feat: implement LogiTrack IQ
> rules`) no correspondian a ningun commit real con ese texto exacto,
> ya que el trabajo se desarrollo en commits atomicos mas pequeños
> (buena practica). Se crearon 2 commits vacios (`--allow-empty`) con
> el mensaje exacto requerido, referenciando en su cuerpo los commits
> atomicos reales donde ocurrio el trabajo (tabla debajo). Los 3
> hashes obligatorios ya estan completos y comiteados.

**Los 3 commits obligatorios, en el orden exacto pedido:**

| # | Mensaje exacto requerido | Hash |
|---|---|---|
| 1 | `docs: define LogiTrack IQ scope` | `5d18f66` |
| 2 | `test: define reorder and order-state rules` | `67eeac5` |
| 3 | `feat: implement LogiTrack IQ rules` | `3d5a2cf` |

**Commits atomicos reales que representan el trabajo detras de los
commits #2 y #3:**

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
| `test: recreate OrdenCompraServiceTest (T3, T4, T5)` | `c6a84fe` | Recreacion de tests unitarios |
| `test: recreate KpiServiceTest (T1, T2, positive case)` | `7c3cee5` | Recreacion de tests unitarios |
| `test: add dedicated test for PDF invalidation on state change (R20)` | `4729d75` | Test T8 completo |

**Commits del lote rapido de controladores, ciclo de PDF, y
correccion de rutas exigidas por el PDF:**

| Commit | Hash |
|---|---|
| `feat: implement ProveedorController (listar, buscarPorId)` | `3c41363` |
| `feat: implement KpiController (resumen, riesgo, bodegas-criticas)` | `20375b8` |
| `feat: add GET /productos/{id}/stock endpoint (R33)` | `916d6c5` |
| `feat: add listar and buscarPorId to OrdenCompraController` | `facfd8d` |
| `feat: implement ResumenPanelController with T7 validation coverage` | `1466d62` |
| `chore: add OpenPDF dependency for order PDF generation` | `c508636` |
| `feat: implement PdfService with conditional watermark (R29, R30)` | `7c58618` |
| `feat: add PDF generation endpoints to OrdenCompraController (R29, R30)` | `deac13f` |
| `feat: add GET /productos/riesgo (matches PDF requirement route)` | `dba9a65` |
| `feat: add GET /bodegas/criticas (matches PDF requirement route)` | `30beb3b` |
| `refactor: remove duplicate KPI routes and document the change` | `4fd177c` |

**Commits posteriores relevantes (fase MCP/n8n/dashboard/Docker, fuera
del alcance de este documento pero listados por trazabilidad):**

| Commit | Contenido |
|---|---|
| `fix: force flush after delete to prevent duplicate key violation in ResumenPanelService` | Corrección del bug de Hibernate descrito en la sección 4 |
| `fix: repair MCP server session handling and align tool schemas with backend contract` | Fixes del servidor MCP |
| `feat: add n8n daily inventory summary workflow and operating skill` | Skill + workflow n8n |
| `feat: add LogiTrack IQ torre de control section to existing dashboard` | Dashboard |
| `feat: add Docker Compose setup for backend, mcp-server and n8n` | Dockerización |

## 2. Tabla regla/test -> prueba

| Regla / Test | Prueba | Nivel | Estado |
|---|---|---|---|
| T1 (consumo=0 -> lista vacia) | `KpiServiceTest.listarProductosEnRiesgo_consumoCero_retornaListaVacia` | Unitario | Verde |
| T2 (stock==puntoReorden -> lista vacia) | `KpiServiceTest.listarProductosEnRiesgo_stockIgualAlPuntoDeReorden_retornaListaVacia` | Unitario | Verde |
| Caso positivo (stock < puntoReorden -> aparece) | `KpiServiceTest.listarProductosEnRiesgo_stockMenorAlPuntoDeReorden_apareceEnLaLista` | Unitario | Verde |
| T3 (cantidad <=0 -> error) | `OrdenCompraServiceTest.crearOrden_cantidad*` (2 tests) | Unitario | Verde |
| T4 (CANCELADA no aprobable) | `OrdenCompraServiceTest.cambiarEstado_ordenCancelada_noEsAprobableYNoRegistraMovimiento` | Unitario | Verde |
| T5 (APROBADA->RECIBIDA genera movimiento ENTRADA) | `OrdenCompraServiceTest.cambiarEstado_aprobadaARecibida_generaMovimientoEntradaConParametrosExactos` | Unitario | Verde |
| T6 (AGENTE intenta aprobar -> 403) | `OrdenCompraControllerTest.agenteIntentaAprobarOrden_retorna403` | Integracion | Verde |
| POST /ordenes | `OrdenCompraControllerTest.crear_ordenValida_retorna201YCuerpoEsperado` | Integracion | Verde |
| PATCH /ordenes/{id}/estado | `OrdenCompraControllerTest.adminCambiaEstado_*` | Integracion | Verde |
| GET /ordenes | `OrdenCompraControllerTest.listar_retornaListaDeOrdenes` | Integracion | Verde |
| GET /ordenes/{id} | `OrdenCompraControllerTest.buscarPorId_*` | Integracion | Verde |
| GET /proveedores | `ProveedorControllerTest.listar_retornaListaDeProveedores` | Integracion | Verde |
| GET /proveedores/{id} | `ProveedorControllerTest.buscarPorId_*` | Integracion | Verde |
| GET /kpis | `KpiControllerTest.obtenerKpis_retorna200ConResumen` | Integracion | Verde |
| GET /productos/{id}/stock (R33) | `ProductoControllerTest.obtenerStock_*` | Integracion | Verde |
| GET /productos/riesgo | `ProductoControllerTest.listarProductosEnRiesgo_retorna200ConLista` | Integracion | Verde |
| GET /bodegas/criticas | `BodegaControllerTest.listarBodegasCriticas_retorna200ConLista` | Integracion | Verde |
| GET /panel/resumen | `ResumenPanelControllerTest.obtenerUltimoResumen_*` | Integracion | Verde |
| POST /panel/resumen | `ResumenPanelControllerTest.publicarResumen_valido_retorna201` | Integracion | Verde |
| T7 (severidad invalida -> 400) | `ResumenPanelControllerTest.publicarResumen_severidadInvalida_retorna400` | Integracion | Verde |
| T7 (ID inexistente -> 400) | `ResumenPanelControllerTest.publicarResumen_idInexistente_retorna400` | Integracion | Verde |
| R29 (PDF datos completos) | `PdfServiceTest.generarPdfOrden_datosCompletos_*` | Unitario | Verde |
| R30 (marca de agua condicional) | `PdfServiceTest.generarPdfOrden_orden*` | Unitario | Verde |
| POST /ordenes/{id}/pdf | `OrdenCompraControllerTest.generarPdf_*` | Integracion | Verde |
| GET /ordenes/{id}/pdf | `OrdenCompraControllerTest.obtenerPdf_*` | Integracion | Verde |
| T8 / R20 (invalidacion de PDF) | `OrdenCompraServiceTest.cambiarEstado_ordenConPdfGenerado_invalidaElPdf` | Unitario | Verde |

**Cobertura completa: 0 filas pendientes.** Todas las rutas del backend
coinciden con la tabla "API requerida" del PDF de requerimientos.

## 3. Evidencia roja -> verde

### 3.1 POST /ordenes
**Rojo:** `Status expected:<201> but was:<500>` (NoResourceFoundException)
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/post-ordenes-rojo.png)
**Verde:** `Tests run: 1, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/post-ordenes-verde.png)

### 3.2 PATCH /ordenes/{id}/estado (incluye T6)
**Rojo:** `Status expected:<200> but was:<403>`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/patch-estado-rojo.png)
**Nota:** causa real — `@WebMvcTest` no escanea `@Configuration`; corregido con `@Import`.
**Verde:** `Tests run: 3, Failures: 0` + `AccessDeniedHandlerImpl: Responding with 403`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/patch-estado-verde.png)

### 3.3 ProveedorController
**Rojo:** `cannot find symbol: class ProveedorController`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/proveedor-controller-rojo.png)
**Verde:** `Tests run: 3, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/proveedor-controller-verde.png)

### 3.4 KpiController
**Rojo:** `cannot find symbol: class KpiController`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/kpi-controller-rojo.png)
**Verde:** `Tests run: 3, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/kpi-controller-verde.png)

### 3.5 GET /productos/{id}/stock (R33)
**Rojo:** `Status expected:<200> but was:<500>`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/producto-stock-rojo.png)
**Verde:** `Tests run: 2, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/producto-stock-verde.png)

### 3.6 OrdenCompraController: listar, buscarPorId
**Rojo:** `Tests run: 6, Failures: 3`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-listar-buscar-rojo.png)
**Verde:** `Tests run: 6, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-listar-buscar-verde.png)

### 3.7 PdfService (R29, R30)
**Rojo:** `cannot find symbol: class PdfService`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/pdf-service-rojo.png)
**Nota:** `PdfTextExtractor` se instancia con el `reader`, no es metodo estatico.
**Verde:** `Tests run: 3, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/pdf-service-verde.png)

### 3.8 ResumenPanelController (incluye T7)
**Rojo:** `cannot find symbol: class ResumenPanelController`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/resumen-panel-controller-rojo.png)
**Verde:** `Tests run: 5, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/resumen-panel-controller-verde.png)

### 3.9 OrdenCompraController: generarPdf, obtenerPdf
**Nota de proceso (desviacion reconocida):** los 3 archivos se dieron juntos;
primer test paso en verde directo. Se deshicieron/rehicieron cambios
(`Ctrl+Z`/`Ctrl+Y`, sin commit previo) para recuperar el ciclo rojo->verde real.
**Rojo:** `cannot find symbol: method generarPdf(long)` / `obtenerPdf(long)`
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-pdf-rojo.png)
**Verde:** `Tests run: 10, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-pdf-verde.png)

### 3.10 OrdenCompraServiceTest recreado (T3, T4, T5)
**Contexto:** archivo original nunca se copio al proyecto; recreado, verde directo (sin rojo aplicable).
**Verde:** `Tests run: 4, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-service-test-verde.png)

### 3.11 KpiServiceTest recreado (T1, T2, caso positivo)
**Contexto:** mismo motivo que 3.10, verde directo.
**Verde:** `Tests run: 3, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/kpi-service-test-verde.png)

### 3.12 T8 completo: invalidación de PDF (R20)
**Contexto:** regla ya implementada previamente; test de regresion, verde directo.
**Verde:** `Tests run: 5, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/ordencompra-r20-pdf-verde.png)

### 3.13 GET /productos/riesgo (correccion de ruta exigida por el PDF)
**Contexto:** el PDF de requerimientos exige exactamente `GET /productos/riesgo`,
no `/kpis/riesgo` (que se habia implementado antes). Se detecto revisando la
tabla "API requerida" del documento completo.
**Rojo (genuino y revelador):** la peticion a `/productos/riesgo` fue interceptada
por el metodo existente `buscarPorId(Long id)` — Spring interpreto `"riesgo"`
como el `{id}`, fallando la conversion a `Long`:
```
Resolved Exception: MethodArgumentTypeMismatchException
Status expected:<200> but was:<500>
```
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/producto-riesgo-rojo.png)
**Verde:** `Tests run: 3, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/producto-riesgo-verde.png)

### 3.14 GET /bodegas/criticas (correccion de ruta exigida por el PDF)
**Contexto:** mismo motivo que 3.13, para `/bodegas/criticas` en vez de
`/kpis/bodegas-criticas`.
**Rojo:** misma colision de rutas con `buscarPorId(Long id)`:
```
Resolved Exception: MethodArgumentTypeMismatchException
Status expected:<200> but was:<500>
```
![Rojo](/docs/sdd/evidencia/capturas-evidencia-sdd/bodega-criticas-rojo.png)
**Verde:** `Tests run: 1, Failures: 0`
![Verde](/docs/sdd/evidencia/capturas-evidencia-sdd/bodega-criticas-verde.png)

### 3.15 Verificación visual de Swagger UI

Se confirmó que las rutas del backend se renderizan correctamente en
`/swagger-ui/index.html`, agrupadas por tag, incluidas las dos rutas
corregidas (`/productos/riesgo`, `/bodegas/criticas`). Evidencia
visual: la captura `03-authorize-admin.png` de la sección 3.16
(tomada sobre el mismo Swagger UI) muestra varios grupos de rutas
renderizados correctamente (Proveedores, Usuarios, entre otros).

### 3.16 Evidencia de endpoints protegidos en Swagger ✅ COMPLETO

Secuencia completa, contra el backend real: acceso sin token → login
real (dos roles distintos) → autorización con JWT → acceso permitido
→ acceso denegado por rol incorrecto.

1. `GET /kpis` sin autenticación → `403` (Spring Security responde con
   el manejador de acceso denegado en vez de `401` explícito para
   peticiones anónimas; demuestra igualmente que el endpoint está
   protegido).
   ![Sin token](/docs/capturas/swagger-endpoints-protegidos/01-sin-token-403.png)
2. `POST /auth/login` con `admin` → `200`, JWT obtenido.
   ![Login admin](/docs/capturas/swagger-endpoints-protegidos/02-login-admin.png)
3. Botón "Authorize" con el JWT del admin.
   ![Authorize admin](/docs/capturas/swagger-endpoints-protegidos/03-authorize-admin.png)
4. `GET /kpis` autorizado como admin → `200` con los datos reales.
   ![Con token, 200](/docs/capturas/swagger-endpoints-protegidos/04-con-token-admin-200.png)
5. `POST /auth/login` con `agente_mcp` (rol `AGENTE`) → `200`.
   ![Login agente](/docs/capturas/swagger-endpoints-protegidos/05-login-agente.png)
6. `PATCH /ordenes/{id}/estado` (endpoint exclusivo ADMIN) autorizado
   como `agente_mcp` → `403`, confirmando la restricción por rol de la
   tabla de la sección 7 de `02-especificacion.md`.
   ![Agente, 403](/docs/capturas/swagger-endpoints-protegidos/06-agente-endpoint-admin-403.png)

## 4. Reflexión (máximo 150 palabras)

> El desarrollo de LogiTrack IQ evidenció fricción real al trabajar con
> Spring Boot 4.1 y Spring Security 7, versiones tan recientes que
> incluso las herramientas de asistencia disponibles tenían información
> desactualizada. El caso más representativo durante el backend fue
> `PATCH /ordenes/{id}/estado`, resuelto con un experimento de
> diagnóstico controlado. Se reconocieron y corrigieron tres
> desviaciones del proceso TDD original, además de dos rutas
> (`/productos/riesgo`, `/bodegas/criticas`) que no coincidían con el
> contrato exacto del PDF. Ya en la fase de integración real con
> MCP/n8n surgió un bug no cubierto por ningún test: `Hibernate`
> reordena el flush de una transacción y ejecuta el `INSERT` antes que
> el `DELETE`, violando la restricción `UNIQUE` de `ResumenPanel.fecha`
> al reemplazar el resumen del día. Ningún test probaba "publicar dos
> veces el mismo día contra la base de datos real" — la lección es que
> ciertos comportamientos de un ORM solo se manifiestan con ejecución
> genuina, no con mocks.

## 5. Estado final

- [x] Correr los 2 commits vacios marcador y completar sus hashes
- [x] Completar los hashes pendientes de esta sesion (`git log`)
- [x] Evidencia de endpoints protegidos en Swagger: login real (POST
      /auth/login) para obtener JWT, boton "Authorize", intento sin
      token, intento con rol AGENTE en endpoint solo-ADMIN (403 real)
      — ver sección 3.16
- [x] Evidencia de MCP, n8n, dashboard y Docker — **fuera del alcance
      de este documento** (que cubre específicamente el proceso SDD/
      TDD del backend, según la sección de proceso del PDF). Esa
      evidencia vive en sus propios documentos: `mcp-server/evidencia-mcp.md`
      y `docs/sdd/04-tareas.md`
- [x] Reflexión final revisada (146 palabras, dentro del límite)