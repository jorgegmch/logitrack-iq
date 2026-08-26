package com.jorgegmch.logitrack.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrdenCompraRequest {
    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    @NotNull(message = "La bodega destino es obligatoria")
    private Long bodegaDestinoId;

    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    private BigDecimal precioUnitario;
}