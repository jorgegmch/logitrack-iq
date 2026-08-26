package com.jorgegmch.logitrack.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResumenPanelRequest {
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @Size(min = 20, max = 500, message = "La narrativa debe tener entre 20 y 500 caracteres")
    private String narrativa;

    @NotNull(message = "El campo alertas es obligatorio, use un arreglo vacío si no aplica")
    @Valid
    private List<AlertaDTO> alertas;

    @NotNull(message = "El campo accionesSugeridas es obligatorio, use un arreglo vacío si no aplica")
    @Valid
    private List<AccionSugeridaDTO> accionesSugeridas;
}