# 03. Diseño — LogiTrack IQ

## 1. Entidades nuevas

Todas siguen el patrón Lombok ya usado en el proyecto base (`@Data`,
`@EqualsAndHashCode(of = "idXxx")`, `@NoArgsConstructor`,
`@AllArgsConstructor`) y la convención de nombrar los campos de relación
`ManyToOne` con sufijo `Id` (ej. `productoId` es un objeto `Producto`,
igual que `Movimiento.usuarioId` es un objeto `Usuario` en el código
existente). Esto difiere de los nombres puramente descriptivos usados en
el PDF de requerimientos (que habla de "producto", "proveedor",
"creadoPor"), pero mantiene consistencia con el resto del código.

### 1.1 `Proveedor`

```java
package com.jorgegmch.logitrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proveedor")
@Data
@EqualsAndHashCode(of = "idProveedor")
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProveedor;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String contacto;

    @Column(name = "dias_entrega", nullable = false)
    private Integer diasEntrega;
}
```

**Decisión:** `diasEntrega` se valida en dos capas: `@Min(1)` / `@Max(90)`
con Bean Validation en el DTO de entrada (`ProveedorRequest`, si se crea
un endpoint de escritura), y la restricción `CHECK` ya definida en
`database/schema.sql` como segunda barrera — mismo patrón de
`chk_bodega_capacidad` en el proyecto base.

No se anota con `@EntityListeners(AuditoriaListener.class)`: los
proveedores se cargan una sola vez vía `database/data.sql` y no forman
parte de las acciones auditables exigidas por R27.

### 1.2 `Producto` (extensión — agregar campo a la clase existente)

```java
@ManyToOne
@JoinColumn(name = "proveedor_principal_id")
private Proveedor proveedorPrincipalId;
```

**Decisión:** relación opcional (`nullable` por defecto). Un producto sin
`proveedorPrincipalId` existe válidamente (sigue teniendo stock,
movimientos, etc.) pero queda excluido de los cálculos de riesgo (R9),
validado en el **servicio**, no con una restricción de base de datos — es
una regla de negocio, no de integridad referencial.

### 1.3 `OrdenCompra`

```java
package com.jorgegmch.logitrack.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jorgegmch.logitrack.entity.enums.EstadoOrden;
import com.jorgegmch.logitrack.listener.AuditoriaListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "orden_compra")
@Data
@EqualsAndHashCode(of = "idOrdenCompra")
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrdenCompra;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto productoId;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedorId;

    @ManyToOne
    @JoinColumn(name = "bodega_destino_id", nullable = false)
    private Bodega bodegaDestinoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @ManyToOne
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPorId;

    @Lob
    @Column(name = "pdf_generado")
    private byte[] pdfGenerado;

    @Column(name = "fecha_generacion_pdf")
    private LocalDateTime fechaGeneracionPdf;
}
```

**`EstadoOrden`** (nuevo enum, mismo paquete que `TipoMovimiento`/`Rol`):
```java
package com.jorgegmch.logitrack.entity.enums;

public enum EstadoOrden {
    BORRADOR,
    APROBADA,
    RECIBIDA,
    CANCELADA
}
```

**Decisiones:**
- `@EntityListeners(AuditoriaListener.class)` habilita auditoría
  automática de creación, actualización (transición de estado) y
  eliminación, sin código adicional (ver sección 8).
- `total` se calcula en el servicio (`cantidad × precioUnitario`) y se
  persiste — no se recalcula en cada lectura, para que el histórico de
  una orden no cambie si el precio del producto cambia después.
- `pdfGenerado` como `byte[]` con `@Lob` (columna `bytea` en PostgreSQL),
  sin almacenamiento externo de archivos.
- No existe un setter público de `estado` sin validación en el flujo de
  negocio: la transición se hace a través de
  `OrdenCompraService.cambiarEstado()`, que aplica la tabla de
  transiciones (R17) antes de persistir. Lombok genera `setEstado()` por
  `@Data`, pero el servicio nunca lo llama directamente sin antes validar.

### 1.4 `ResumenPanel`

```java
package com.jorgegmch.logitrack.entity;

import java.time.LocalDate;

import com.jorgegmch.logitrack.listener.AuditoriaListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditoriaListener.class)
@Table(name = "resumen_panel")
@Data
@EqualsAndHashCode(of = "idResumenPanel")
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPanel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResumenPanel;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "contenido_json", nullable = false, columnDefinition = "TEXT")
    private String contenidoJson;

    @ManyToOne
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autorId;
}
```

**Decisión clave (R11):** en vez de un `UPDATE` in-place cuando se
republica un resumen para la misma fecha, el servicio hace:
1. Busca el resumen existente de esa fecha (`findByFecha`).
2. Si existe, lo elimina físicamente (`delete`) — no soft delete, porque
   `ResumenPanel` no es una entidad de auditoría inmutable como
   `Auditoria`, sino un estado de "última versión válida".
3. Guarda el nuevo resumen.
4. `AuditoriaListener` captura el `DELETE` + `INSERT` automáticamente vía
   `@EntityListeners`, dejando rastro de que hubo un reemplazo —
   satisfaciendo "queda registrada en auditoría" sin código adicional.

**`contenidoJson` como `String` (TEXT), no como columnas separadas:** se
guarda el JSON crudo tal como llegó (ya validado), porque el contrato
(sección 6 de la especificación) es flexible en su estructura interna
(arreglos de longitud variable) y el dashboard solo necesita recuperarlo
íntegro para renderizarlo.

## 2. Diagrama de entidades (texto)

```
Bodega ──< InventarioBodega >── Producto ──> Proveedor
                                    │              ▲
                                    │              │
                                    └──> OrdenCompra ──> Bodega (destino)
                                              │
                                              └──> Usuario (creadoPorId)

ResumenPanel ──> Usuario (autorId)

Movimiento ──< DetalleMovimiento >── Producto
   │
   └──> Bodega (origen / destino, según tipo)
```

## 3. Diagrama de flujo de negocio (texto)

```
[n8n Schedule 6am]
        │
        ▼
[AI Agent] ──consulta──> [MCP: consultar_kpis, consultar_productos_en_riesgo]
        │
        ▼ (si hay producto en riesgo)
[MCP: crear_orden_borrador] ──> [API: POST /ordenes] ──> OrdenCompra(BORRADOR)
        │
        ▼
[MCP: publicar_resumen] ──> [API: POST /panel/resumen] ──> ResumenPanel
        │
        ▼
[Dashboard] <── GET /kpis, /ordenes, /panel/resumen ── [ADMIN revisa]
        │
        ▼ (opcional: ADMIN genera PDF de la orden para revisión formal)
[POST /ordenes/{id}/pdf] ──> PDF con marca de agua BORRADOR
        │
        ▼ (ADMIN aprueba)
[PATCH /ordenes/{id}/estado → APROBADA]
        │
        ▼ (ADMIN recibe)
[PATCH /ordenes/{id}/estado → RECIBIDA]
        │
        ▼ (transacción única, R18)
[Movimiento ENTRADA creado] ──> [InventarioBodega actualizado]
        │
        ▼
[Dashboard refleja inventario actualizado]
```

**Nota:** el paso de generación de PDF es opcional y no bloquea la
aprobación — ver sección 6 para el detalle de esta decisión.

## 4. Decisiones de arquitectura adicionales

| Decisión | Justificación |
|---|---|
| Rol `AGENTE` como nuevo valor del enum `Rol` existente, no una entidad separada | Reutiliza `JwtAuthenticationFilter`, `UsuarioService.loadUserByUsername()` y la autorización basada en `requestMatchers` de `SecurityConfig` ya construidos; solo se agregan reglas de matcher nuevas (ver sección 7). |
| Servicio de cálculo de KPIs separado (`KpiService`) de `OrdenCompraService` | Single Responsibility — KPIs es lectura pura y agregación; órdenes es lógica transaccional con efectos secundarios. |
| Generación de PDF con librería a confirmar (OpenPDF por defecto) | Pendiente de decidir en el momento de implementar la sección 8 de la especificación (ver sección 9). |
| MCP server como proyecto Node/TypeScript independiente en `mcp-server/`, fuera del `pom.xml` de Spring Boot | Aislamiento de responsabilidades: el MCP no debe compartir classpath ni dependencias con el backend, solo consume su API REST vía HTTP. |

## 5. Decisión: fuente de verdad del stock

**Hallazgo (25/08/2026):** al verificar el estado real del schema
(`db_logitrack_iq`), se encontró que `inventario_bodega` tenía valores que
los movimientos existentes no explicaban por completo.

**Causa raíz confirmada (dos factores):**
1. **El `data.sql` original de `logitrack-api`** siembra 7 filas en
   `inventario_bodega`, pero solo incluye un movimiento `ENTRADA` que
   respalda 2 de ellas; las otras 5 nunca tuvieron movimiento, ni en el
   seed pristino provisto por el profesor.
2. **Ediciones manuales posteriores** (evidenciadas por un registro
   `PRODUCTO TEST` que no proviene de `POST /productos` ni del `data.sql`
   original) agregaban una segunda fuente de desfase.

Se revisó `InventarioBodegaController` y se confirmó que expone
únicamente endpoints de lectura — no existe ningún endpoint de escritura
sobre `InventarioBodega` en todo el backend. La única vía de escritura por
API es `POST /movimientos`, que sí actualiza el inventario de forma
correcta y transaccional.

**Decisión — archivos SQL unificados, no separados:** dado que
LogiTrack IQ es un proyecto nuevo que usa `logitrack-api` como base (no un
parche sobre un repositorio ya publicado), se descartó mantener
`schema.sql`/`data.sql` originales por separado de unos
`schema-logitrack-iq.sql`/`data-logitrack-iq.sql` adicionales. En su
lugar, `database/schema.sql` y `database/data.sql` son **archivos únicos
y completos**: incluyen desde el inicio las tablas heredadas
(`usuario`, `bodega`, `producto`, etc.), las tablas nuevas (`proveedor`,
`orden_compra`, `resumen_panel`), el rol `AGENTE` ya integrado en el
`CHECK` de `usuario` (sin `ALTER TABLE` posterior), y los movimientos de
inventario inicial ya completos — de modo que el stock es 100% trazable
por movimientos desde el primer arranque, sin necesidad de un paso de
"reconciliación" aparte. Cualquier persona que clone el repositorio y
ejecute `schema.sql` + `data.sql` obtiene un estado limpio, correcto y
determinista en un solo paso.

**Regla de implementación obligatoria (R33):** todos los endpoints nuevos
de LogiTrack IQ que reporten stock (`GET /productos/{id}/stock`,
`GET /productos/riesgo`, `GET /kpis`) deben calcular el stock agregando
`detalle_movimiento` (sumando `ENTRADA`, restando `SALIDA` en origen,
aplicando `TRANSFERENCIA` en ambos lados) — **nunca** leyendo
`inventario_bodega.stock` directamente.

## 6. Decisión: el PDF de la orden es un artefacto opcional, no un paso del flujo

**Aclaración importante de alcance:** el PDF de la orden (con marca de
agua BORRADOR) es un documento **derivado**, generado bajo demanda desde
una orden ya guardada — no es un paso obligatorio ni automático de la
creación de la orden. Evidencia textual del PDF de requerimientos:

- Glosario: *"Orden de compra: [...] No es un PDF por sí misma."* /
  *"PDF de la orden: Documento generado desde una orden guardada."*
- El flujo de n8n (sección 8 de la especificación) no incluye generación
  de PDF en ninguno de sus 6 pasos.
- Ninguna de las 6 herramientas MCP genera PDF.
- La regla *"al cambiar el estado de una orden, el PDF guardado se
  elimina [...] debe generarse nuevamente"* confirma que es un artefacto
  desechable/regenerable bajo demanda, no algo ligado automáticamente al
  ciclo de vida de la orden.

**Consecuencia de diseño:** `crear_orden_borrador` (MCP) solo crea el
registro en `orden_compra`. El PDF se genera exclusivamente cuando el
ADMIN lo solicita desde el dashboard (`POST /ordenes/{id}/pdf`), como
apoyo opcional para su revisión antes de aprobar — nunca como
prerrequisito técnico de la aprobación.

## 7. Reglas de seguridad nuevas (SecurityConfig)

**Corrección necesaria sobre lo existente:** `POST /movimientos` cae hoy
bajo un matcher genérico `.authenticated()`, lo que permitiría a `AGENTE`
registrar movimientos manualmente — violando la tabla de permisos de la
sección 7 de la especificación. Se agrega una regla explícita que excluye
a `AGENTE`:

```java
// Corrección sobre endpoint existente — agregar ANTES de los matchers
// genéricos de /movimientos/**
.requestMatchers(HttpMethod.POST, "/movimientos/**").hasAnyRole("ADMIN", "EMPLEADO")

// Reglas nuevas de LogiTrack IQ
.requestMatchers(HttpMethod.POST, "/ordenes").hasAnyRole("ADMIN", "AGENTE")
.requestMatchers(HttpMethod.PATCH, "/ordenes/*/estado").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/ordenes/*/pdf").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/panel/resumen").hasAnyRole("ADMIN", "AGENTE")
```

**Decisión — generación de PDF restringida a ADMIN:** ver sección 6.
Ninguna herramienta MCP genera PDF, por lo que `AGENTE` nunca necesita
este endpoint en el flujo automatizado. Se restringe siguiendo el mismo
principio de mínimo privilegio que ya aplica a aprobar/recibir/cancelar.
`GET /ordenes/{id}/pdf` (visualizar/descargar) no se restringe
adicionalmente: cae en el catch-all `.anyRequest().authenticated()`.

Las rutas de solo lectura nuevas (`GET /kpis`, `GET /ordenes`,
`GET /productos/riesgo`, `GET /bodegas/criticas`, `GET /proveedores`,
`GET /panel/resumen`) no requieren matcher específico: caen en
`.anyRequest().authenticated()`, permitiendo a `ADMIN` y `AGENTE` (y
`EMPLEADO`) consultarlas, tal como indica la tabla de permisos.

**Sin endpoint nuevo para crear el usuario AGENTE:** `POST /auth/register`
ya existe, ya está restringido a `ADMIN`, y ya acepta cualquier valor del
enum `Rol`. Al agregar `AGENTE` al enum, este endpoint sirve sin cambios
para crear el usuario técnico del MCP server.

## 8. Auditoría automática vía `AuditoriaListener`

`AuditoriaListener` es genérico (usa reflexión sobre el campo `@Id` y
serializa vía Jackson) y ya está enlazado a `Producto`, `Bodega`,
`Usuario` y `Movimiento` mediante `@EntityListeners`. Auditar
`OrdenCompra` y `ResumenPanel` (R27) **no requiere lógica adicional** en
los servicios: basta con anotar ambas entidades con
`@EntityListeners(AuditoriaListener.class)` — los eventos
`@PostPersist`/`@PostUpdate`/`@PostRemove` se disparan automáticamente y
quedan registrados vía `afterCommit()` + `REQUIRES_NEW`, igual que el
resto del sistema. `Proveedor` no lleva este listener (ver sección 1.1).

## 9. Pendientes de diseño (a resolver en implementación)

- [ ] Confirmar librería de generación de PDF (OpenPDF por defecto).
- [ ] Confirmar librería/framework para el `mcp-server` (Node + SDK oficial
      de MCP es la opción por defecto).