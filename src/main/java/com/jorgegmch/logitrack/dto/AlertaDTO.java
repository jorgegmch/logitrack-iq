package com.jorgegmch.logitrack.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlertaDTO {
    @NotBlank(message = "La severidad es obligatoria")
    private String severidad;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El detalle es obligatorio")
    private String detalle;

    private Long productoId;
    private Long ordenId;
    private Long bodegaId;
}