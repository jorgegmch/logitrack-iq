# 02. Especificación — LogiTrack IQ

Este documento traduce los requerimientos del proyecto a reglas verificables.
Cada regla listada aquí debe tener al menos una prueba asociada en
`evidencia-sdd.md` (tabla regla → prueba).

## 1. Convenciones generales

- **Zona horaria:** `America/Bogota` para backend, n8n y datos de prueba.
- **Fuente de verdad:** el backend y su base de datos (PostgreSQL/Supabase)
  son la única fuente de información. Dashboard, MCP y n8n consultan o usan
  la API; no calculan ni modifican datos directamente en la base de datos.
- **Códigos de error reutilizados** del backend anterior:
  - `400` — validaciones y transiciones inválidas.
  - `404` — recursos inexistentes.
  - `403` — acciones prohibidas por rol.
  - `401` — sesión no válida.
- **Corrección de motor de base de datos:** el documento de requerimientos
  original menciona MySQL. El motor real usado es **PostgreSQL vía
  Supabase**, heredado del proyecto base LogiTrack API.

## 2. Reglas base de inventario

| # | Regla |
|---|---|
| R1 | El stock se calcula a partir de los movimientos (`Movimiento` + `DetalleMovimiento`); `Producto.stock` no es fuente para estos cálculos. |
| R2 | Un movimiento puede tener uno o varios detalles de producto; cada cálculo recorre `DetalleMovimiento`. |
| R3 | `ENTRADA` suma unidades a la bodega destino. |
| R4 | `SALIDA` resta unidades a la bodega origen. |
| R5 | `TRANSFERENCIA` resta en origen y suma la misma cantidad en destino. |
| R6 | No se permite una `SALIDA` o `TRANSFERENCIA` que deje una bodega con stock negativo → `400`. |
| R7 | El stock total de un producto es la suma de sus existencias (`InventarioBodega`) en todas las bodegas. |
| R8 | La capacidad de una bodega debe ser mayor que 0. |
| R33 | Los endpoints que reporten stock (`GET /productos/{id}/stock`, `GET /productos/riesgo`, `GET /kpis`) deben calcularlo agregando `DetalleMovimiento` en tiempo real — nunca leyendo `InventarioBodega.stock` directamente, incluso si ambos valores coinciden en los datos de prueba actuales. (Ver hallazgo y justificación en `03-diseno.md`, sección 5.) |

## 3. Modelo de datos nuevo

### 3.1 `Proveedor`
| Campo | Tipo / regla |
|---|---|
| `id` | PK |
| `nombre` | obligatorio |
| `contacto` | obligatorio |
| `diasEntrega` | entero, entre 1 y 90 |

Precargado vía `data.sql`.

### 3.2 `Producto` (extensión)
| Campo | Tipo / regla |
|---|---|
| `proveedorPrincipal` | `ManyToOne` opcional a `Proveedor` |

**R9:** un producto sin `proveedorPrincipal` no puede aparecer como producto
en riesgo ni generar una orden automática.

### 3.3 `OrdenCompra`
| Campo | Tipo / regla |
|---|---|
| `id` | PK |
| `producto` | obligatorio, exactamente uno |
| `proveedor` | obligatorio |
| `bodegaDestino` | obligatoria |
| `cantidad` | entero, mayor que 0 |
| `precioUnitario` | obligatorio |
| `total` | calculado en servidor (`cantidad × precioUnitario`) |
| `fechaCreacion` | autogenerada |
| `estado` | `BORRADOR` \| `APROBADA` \| `RECIBIDA` \| `CANCELADA` |
| `creadoPor` | resuelto desde el usuario autenticado (JWT), no editable por cliente |
| `pdfGenerado` | opcional, blob/ruta del PDF guardado |
| `fechaGeneracionPdf` | opcional |

**R10:** cantidad ≤ 0 → `400`.

### 3.4 `ResumenPanel`
| Campo | Tipo / regla |
|---|---|
| `id` | PK |
| `fecha` | `YYYY-MM-DD`, única por fecha |
| `contenidoJson` | contrato validado (ver sección 6) |
| `autor` | resuelto desde el usuario autenticado |

**R11:** una nueva publicación para la misma fecha reemplaza el contenido
anterior y queda registrada en auditoría.

## 4. Indicadores (KPIs)

| Indicador | Fórmula exacta |
|---|---|
| Ocupación por bodega | `(unidades almacenadas / capacidad) × 100`, por cada bodega |
| Productos en quiebre | cantidad de productos con stock total `= 0` |
| Productos en riesgo | cantidad de productos **con proveedor principal** cuyo stock total `<` punto de reorden |
| Órdenes por aprobar | cantidad de órdenes en `BORRADOR` + suma de sus totales |

**R12:** bodega crítica cuando ocupación `≥ 90%`.

### 4.1 Datos por producto en riesgo

| Dato | Fórmula |
|---|---|
| Consumo diario promedio | unidades en `SALIDA` de los últimos 30 días calendario (incluida la fecha de consulta) `/ 30` |
| Punto de reorden | `consumoDiarioPromedio × diasEntrega × 1.5` |
| Días de cobertura | `stockTotal / consumoDiarioPromedio` |

**R13:** si consumo = 0 → cobertura = `null`, estado = `SIN_CONSUMO`.

**R14:** si stock = punto de reorden exactamente → el producto **no** está
en riesgo (debe ser estrictamente menor).

**R15:** movimientos de ayer = conteo separado de `ENTRADA`, `SALIDA`,
`TRANSFERENCIA` del día calendario anterior en `America/Bogota` (bloque
informativo, no tarjeta principal).

**R16:** `bodegaDestinoId` sugerida en producto en riesgo = bodega con menor
stock de ese producto; empate → menor `id`.

## 5. Estados de la orden y recepción

| Estado actual | Siguiente permitido |
|---|---|
| `BORRADOR` | `APROBADA`, `CANCELADA` |
| `APROBADA` | `RECIBIDA`, `CANCELADA` |
| `RECIBIDA` | ninguno |
| `CANCELADA` | ninguno |

**R17:** transición no listada → `400 Bad Request` con mensaje claro.

**R18:** al pasar de `APROBADA` a `RECIBIDA`, el sistema crea automáticamente
un movimiento `ENTRADA` (producto, cantidad, bodegaDestino de la orden).
Actualización de la orden + creación del movimiento ocurren en **una sola
transacción** (`@Transactional`): ambas se completan o ninguna se guarda.

**R19:** una orden `CANCELADA` no puede aprobarse → `400`.

**R20:** al cambiar el estado de una orden, el PDF guardado se elimina;
debe generarse nuevamente para reflejar el estado actual.

## 6. Contrato del resumen del panel

`POST /panel/resumen` acepta **solo** esta estructura (sin propiedades
adicionales):

```json
{
  "fecha": "YYYY-MM-DD",
  "narrativa": "string, 20–500 caracteres",
  "alertas": [
    {
      "severidad": "BAJA | MEDIA | ALTA",
      "titulo": "string",
      "detalle": "string",
      "productoId": "number | null",
      "ordenId": "number | null",
      "bodegaId": "number | null"
    }
  ],
  "accionesSugeridas": [
    {
      "tipo": "REVISAR_ORDEN | REVISAR_PRODUCTO | REVISAR_BODEGA",
      "descripcion": "string",
      "ordenId": "number | null",
      "productoId": "number | null",
      "bodegaId": "number | null"
    }
  ]
}
```

Reglas:

| # | Regla |
|---|---|
| R21 | `fecha` = fecha actual en `America/Bogota`, formato `YYYY-MM-DD`. |
| R22 | `narrativa` entre 20 y 500 caracteres. |
| R23 | `alertas` y `accionesSugeridas` son arreglos, aunque estén vacíos. |
| R24 | cada identificador informado debe existir (`productoId`, `ordenId`, `bodegaId`). |
| R25 | una alerta enlaza **al menos un** identificador; una acción enlaza **exactamente uno**. |
| R26 | JSON inválido (estructura, longitud, enum, ID inexistente) → `400`, y el último resumen válido permanece disponible. |

No se exige validar el significado semántico de la narrativa; solo
estructura, longitud, enumeraciones y existencia de IDs.

## 7. Seguridad y roles

Rol nuevo: `AGENTE`.

| Acción | AGENTE | ADMIN |
|---|---|---|
| Consultar KPIs, stock, riesgos, bodegas críticas | ✅ | ✅ |
| Crear orden en BORRADOR | ✅ | ✅ |
| Publicar resumen | ✅ | ✅ |
| Aprobar, recibir o cancelar orden | ❌ (403) | ✅ |
| Registrar movimientos manualmente | ❌ (403) | ✅ |

**R27:** auditoría obligatoria sobre: creación de orden, publicación/
reemplazo de resumen, transición de orden, recepción. Consultas no
requieren auditoría.

## 8. Documento PDF de la orden

**Nota de alcance:** la generación del PDF es una acción bajo demanda, no
automática. `crear_orden_borrador` (MCP) únicamente crea el registro en
base de datos; el PDF se genera solo cuando alguien llama explícitamente
a `POST /ordenes/{id}/pdf` (ver `03-diseno.md`, sección 6, para el
razonamiento completo basado en el glosario del PDF de requerimientos).

| # | Regla |
|---|---|
| R28 | `POST /ordenes/{id}/pdf` genera y guarda el PDF; si ya existe, lo reemplaza. |
| R29 | Contenido mínimo: número de orden, fecha de creación, proveedor, producto, cantidad, precio unitario, total, bodega destino, estado. |
| R30 | Si el estado es `BORRADOR`, el PDF muestra marca de agua diagonal, semitransparente y legible con el texto "BORRADOR". |
| R31 | `GET /ordenes/{id}/pdf` responde `application/pdf`; si no se ha generado, `404`. |

(Ver R20 para la invalidación al cambiar de estado.)

## 9. Servidor MCP — herramientas exactas

1. `consultar_stock_producto(productoId)` → `GET /productos/{id}/stock`
2. `consultar_bodegas_criticas()` → `GET /bodegas/criticas`
3. `consultar_productos_en_riesgo()` → `GET /productos/riesgo`
4. `consultar_kpis()` → `GET /kpis`
5. `crear_orden_borrador(productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario)` → `POST /ordenes`
6. `publicar_resumen(resumen)` → `POST /panel/resumen`

**R32:** no existe herramienta para aprobar órdenes (restricción de diseño
obligatoria). El MCP server usa un usuario con rol `AGENTE`, sin acceso
directo a la base de datos y sin lógica de negocio propia.

## 10. Skill y flujo n8n

`skills/operacion-logitrack/SKILL.md` debe indicar como mínimo:
- consultar primero riesgos y KPIs;
- crear máximo una orden en borrador por ejecución;
- no aprobar, cancelar ni recibir órdenes;
- publicar solo un JSON que cumpla el contrato del resumen (sección 6);
- informar el error si una herramienta falla.

Flujo único `Resumen diario de inventario`:
1. Schedule Trigger, 6:00 a.m. `America/Bogota`.
2. Nodo AI Agent usando las 6 herramientas MCP + skill.
3. Consulta KPIs y productos en riesgo.
4. Si hay productos en riesgo, crea máximo una orden para el primer producto
   listado. Cantidad = `ceil(max(1, puntoReorden × 2 - stockTotal))`.
5. Publica el resumen del panel.
6. Registra salida de éxito o error en la ejecución de n8n.

## 11. Dashboard

Debe mostrar: los 4 indicadores, movimientos de ayer, ocupación por bodega,
narrativa/alertas/acciones del último resumen, productos en riesgo, órdenes
en BORRADOR. Debe permitir generar y visualizar el PDF de una orden en
BORRADOR (con marca de agua visible). Reutiliza login JWT del proyecto
anterior, pero guarda el JWT solo en `sessionStorage` (no `localStorage`,
a diferencia del proyecto base). Botón "Aprobar" visible solo para ADMIN
autenticado. La tabla se actualiza tras aprobar una orden.

## 12. Pruebas obligatorias (mínimo)

| # | Caso | Resultado esperado |
|---|---|---|
| T1 | Consumo diario = 0 | cobertura `null`, estado `SIN_CONSUMO` |
| T2 | Stock igual al punto de reorden | producto no está en riesgo |
| T3 | Cantidad de orden 0 o negativa | `400` |
| T4 | Orden `CANCELADA` intenta aprobarse | `400` |
| T5 | Orden `APROBADA` pasa a `RECIBIDA` | genera movimiento `ENTRADA` |
| T6 | `AGENTE` intenta aprobar orden | `403` |
| T7 | Resumen con severidad inválida o ID inexistente | `400`, resumen anterior se conserva |
| T8 | PDF de orden en `BORRADOR` | se guarda con marca de agua; al cambiar estado, no disponible hasta regenerar |

Más al menos una prueba de integración para `PATCH /ordenes/{id}/estado`
o `POST /panel/resumen`.

## 13. Endpoints nuevos (resumen)

| Método y ruta | Comportamiento mínimo |
|---|---|
| `GET /kpis` | 4 indicadores + movimientos de ayer + `calculadoEn` |
| `GET /productos/{id}/stock` | stock total + desglose por bodega |
| `GET /productos/riesgo` | lista de productos en riesgo con datos completos |
| `GET /bodegas/criticas` | bodegas con ocupación ≥ 90% |
| `GET /proveedores` | proveedores precargados |
| `GET /ordenes` | lista de órdenes, filtro opcional `estado` |
| `POST /ordenes` | crea orden en `BORRADOR`, calcula total en servidor |
| `GET /ordenes/{id}` | detalle de una orden |
| `POST /ordenes/{id}/pdf` | genera y guarda PDF |
| `GET /ordenes/{id}/pdf` | devuelve PDF guardado o `404` |
| `PATCH /ordenes/{id}/estado` | cambia estado según tabla de transiciones |
| `POST /panel/resumen` | valida y publica resumen |
| `GET /panel/resumen` | último resumen válido o `404` |