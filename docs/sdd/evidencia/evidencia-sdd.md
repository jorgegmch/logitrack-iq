# Evidencia SDD/TDD — LogiTrack IQ

> Documento en construccion. Se actualiza a medida que avanza el proceso
> de implementacion. Cada seccion nueva se agrega al final del ciclo
> rojo->verde correspondiente.

## 1. Hashes de commits obligatorios

| # | Tipo | Mensaje | Hash | Fecha |
|---|------|---------|------|-------|
| 1 | docs | `docs: define LogiTrack IQ scope` | `5d18f66` | 24 ago 2026 |
| 2 | test | `test: define reorder and order-state rules` | `PENDIENTE - completar con git log` | |
| 3 | feat | `feat: implement LogiTrack IQ rules` | `PENDIENTE - completar con git log` | |

> Recordatorio: correr `git log --oneline` y copiar aqui los hashes
> reales de los commits de test y feat ya hechos durante esta sesion
> (POST /ordenes, PATCH /ordenes/{id}/estado).

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
> rojo->verde se siguio de forma estricta.

## 5. Pendiente antes de la entrega final

- [ ] Completar hashes reales de commits 2 y 3
- [ ] Agregar evidencia de T7, T8
- [ ] Agregar evidencia de los controladores restantes
- [ ] Revisar y ajustar la reflexion final (tono propio, verificar
      limite de 150 palabras)