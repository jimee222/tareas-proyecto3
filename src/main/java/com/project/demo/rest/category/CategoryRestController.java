package com.project.demo.rest.category;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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
@RequestMapping("/categorias")
public class CategoryRestController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Category> pageData = categoryRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(pageData.getTotalPages());
        meta.setTotalElements(pageData.getTotalElements());
        meta.setPageNumber(pageData.getNumber() + 1);
        meta.setPageSize(pageData.getSize());

        return new GlobalResponseHandler()
                .handleResponse("Categorías recuperadas correctamente", pageData.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> getOne(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> found = categoryRepository.findById(id);
        if (found.isEmpty()) {
            return new GlobalResponseHandler()
                    .handleResponse("Categoría id " + id + " no encontrada", HttpStatus.NOT_FOUND, request);
        }
        return new GlobalResponseHandler().handleResponse("OK", found.get(), HttpStatus.OK, request);
    }

    @PostMapping("/categoria")
    @PreAuthorize("hasRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> create(@RequestBody Category c, HttpServletRequest request) {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            return new GlobalResponseHandler()
                    .handleResponse("El nombre es obligatorio", null, HttpStatus.BAD_REQUEST, request);
        }
        Category saved = categoryRepository.save(c);
        return new GlobalResponseHandler()
                .handleResponse("Categoría creada correctamente", saved, HttpStatus.CREATED, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category c, HttpServletRequest request) {
        Optional<Category> existing = categoryRepository.findById(id);
        if (existing.isEmpty()) {
            return new GlobalResponseHandler()
                    .handleResponse("Categoría id " + id + " no encontrada", null, HttpStatus.NOT_FOUND, request);
        }
        Category e = existing.get();
        if (c.getName() != null && !c.getName().trim().isEmpty()) e.setName(c.getName());
        if (c.getDescription() != null) e.setDescription(c.getDescription());
        Category saved = categoryRepository.save(e);
        return new GlobalResponseHandler()
                .handleResponse("Categoría actualizada correctamente", saved, HttpStatus.OK, request);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!categoryRepository.existsById(id)) {
            return new GlobalResponseHandler().handleResponse("Categoría id " + id + " no encontrada", null, HttpStatus.NOT_FOUND, request);
        }
        categoryRepository.deleteById(id);
        return new GlobalResponseHandler().handleResponse("Categoría eliminada correctamente", null, HttpStatus.NO_CONTENT, request);
    }
}
