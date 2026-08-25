package com.jorgegmch.logitrack.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jorgegmch.logitrack.dto.ProductoRequest;
import com.jorgegmch.logitrack.entity.Producto;
import com.jorgegmch.logitrack.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "Gestión del catálogo de productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public List<Producto> listar() {
        return productoService.listarProductos();
    }

    @Operation(summary = "Buscar un producto por su id")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @GetMapping("/{id}")
    public Producto buscarPorId(@PathVariable("id") Long id) {
        return productoService.buscarProductoPorId(id);
    }

    @Operation(summary = "Crear un nuevo producto")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Producto crear(@Valid @RequestBody ProductoRequest request) {
        return productoService.crearProducto(request.getNombre(), request.getCategoria(), request.getPrecio());
    }

    @Operation(summary = "Actualizar un producto existente")
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable("id") Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizarProducto(id, request.getNombre(), request.getCategoria(), request.getPrecio());
    }

    @Operation(summary = "Eliminar un producto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id) {
        productoService.eliminarProducto(id);
    }
}
