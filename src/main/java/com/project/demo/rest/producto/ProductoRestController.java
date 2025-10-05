package com.project.demo.rest.producto;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.producto.Producto;
import com.project.demo.logic.entity.producto.ProductoRepository;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductoRestController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Producto> pageData = productoRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(pageData.getTotalPages());
        meta.setTotalElements(pageData.getTotalElements());
        meta.setPageNumber(pageData.getNumber() + 1);
        meta.setPageSize(pageData.getSize());

        return new GlobalResponseHandler()
                .handleResponse("Productos recuperados correctamente", pageData.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> getOne(@PathVariable Long id, HttpServletRequest request) {
        Optional<Producto> found = productoRepository.findById(id);
        if (found.isEmpty()) {
            return new GlobalResponseHandler()
                    .handleResponse("Producto id " + id + " no encontrado", HttpStatus.NOT_FOUND, request);
        }
        return new GlobalResponseHandler().handleResponse("OK", found.get(), HttpStatus.OK, request);
    }

    @PostMapping("/producto")
    @PreAuthorize("hasRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> create(@RequestBody Producto p, HttpServletRequest request) {
        // Validaciones mínimas
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            return new GlobalResponseHandler().handleResponse("El nombre es obligatorio", null, HttpStatus.BAD_REQUEST, request);
        }
        if (p.getPrice() == null || p.getPrice() <= 0) {
            return new GlobalResponseHandler().handleResponse("El precio debe ser > 0", null, HttpStatus.BAD_REQUEST, request);
        }
        if (p.getStock() < 0) {
            return new GlobalResponseHandler().handleResponse("La cantidad en stock debe ser ≥ 0", null, HttpStatus.BAD_REQUEST, request);
        }
        if (p.getCategory() == null || p.getCategory().getId() == null) {
            return new GlobalResponseHandler().handleResponse("La categoría es obligatoria", null, HttpStatus.BAD_REQUEST, request);
        }

        Category cat = categoryRepository.findById(p.getCategory().getId()).orElse(null);
        if (cat == null) {
            return new GlobalResponseHandler().handleResponse("La categoría indicada no existe", null, HttpStatus.BAD_REQUEST, request);
        }
        p.setCategory(cat);

        Producto saved = productoRepository.save(p);
        return new GlobalResponseHandler().handleResponse("Producto creado correctamente", saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Producto p, HttpServletRequest request) {
        Optional<Producto> existing = productoRepository.findById(id);
        if (existing.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Producto id " + id + " no encontrado", null, HttpStatus.NOT_FOUND, request);
        }
        Producto e = existing.get();

        if (p.getName() != null && !p.getName().trim().isEmpty()) e.setName(p.getName());
        if (p.getDescription() != null) e.setDescription(p.getDescription());
        if (p.getPrice() != null) {
            if (p.getPrice() <= 0) {
                return new GlobalResponseHandler().handleResponse("El precio debe ser > 0", null, HttpStatus.BAD_REQUEST, request);
            }
            e.setPrice(p.getPrice());
        }
        if (p.getStock() >= 0) e.setStock(p.getStock());

        if (p.getCategory() != null && p.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(p.getCategory().getId()).orElse(null);
            if (cat == null) {
                return new GlobalResponseHandler().handleResponse("La categoría indicada no existe", null, HttpStatus.BAD_REQUEST, request);
            }
            e.setCategory(cat);
        }

        Producto saved = productoRepository.save(e);
        return new GlobalResponseHandler().handleResponse("Producto actualizado correctamente", saved, HttpStatus.OK, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!productoRepository.existsById(id)) {
            return new GlobalResponseHandler().handleResponse("Producto id " + id + " no encontrado", null, HttpStatus.NOT_FOUND, request);
        }
        productoRepository.deleteById(id);
        return new GlobalResponseHandler().handleResponse("Producto eliminado correctamente", null, HttpStatus.NO_CONTENT, request);
    }
}
