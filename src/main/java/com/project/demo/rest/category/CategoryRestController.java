package com.project.demo.rest.category;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.producto.Producto;
import com.project.demo.logic.entity.producto.ProductoRepository;
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
@RequestMapping("/categories")
public class CategoryRestController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Category> ordersPage = categoryRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(ordersPage.getTotalPages());
        meta.setTotalElements(ordersPage.getTotalElements());
        meta.setPageNumber(ordersPage.getNumber() + 1);
        meta.setPageSize(ordersPage.getSize());

        return new GlobalResponseHandler()
                .handleResponse("Categorías recuperadas correctamente",
                        ordersPage.getContent(), HttpStatus.OK, meta);
    }



    @GetMapping("/{categoryId}/productos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductosFromCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        if(foundCategory.isPresent()) {
            Pageable pageable = PageRequest.of(page-1, size);
            Page<Producto> productosPage = productoRepository.findByCategoryId(categoryId, pageable);

            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(productosPage.getTotalPages());
            meta.setTotalElements(productosPage.getTotalElements());
            meta.setPageNumber(productosPage.getNumber() + 1);
            meta.setPageSize(productosPage.getSize());

            return new GlobalResponseHandler().handleResponse("Productos from category retrieved successfully",
                    productosPage.getContent(), HttpStatus.OK, meta);
        } else {
            return new GlobalResponseHandler().handleResponse("Category " + categoryId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addCategory(@RequestBody Category category, HttpServletRequest request) {
        Category savedOrder = categoryRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created successfully",
                savedOrder, HttpStatus.CREATED, request);
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> editCategory(@RequestBody Category category, HttpServletRequest request) {
        Category savedOrder = categoryRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created successfully",
                savedOrder, HttpStatus.CREATED, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        Optional<Category> foundItem = categoryRepository.findById(id);
        if(foundItem.isPresent()) {
            categoryRepository.deleteById(foundItem.get().getId());
            return new GlobalResponseHandler().handleResponse("Category deleted successfully",
                    foundItem.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category " + id + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/{categoryId}/productos")
    //@PreAuthorize("isAuthenticated()")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addProductoToCategory(@PathVariable Long categoryId, @RequestBody Producto producto, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        if(foundCategory.isPresent()) {
            Category category = foundCategory.get();
            producto.setCategory(category);
            Producto savedProducto = productoRepository.save(producto);
            return new GlobalResponseHandler().handleResponse("Producto added to Category successfully",
                    savedProducto, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category " + categoryId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{categoryId}/productos/{productoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> removeProductoFromCategory(@PathVariable Long categoryId, @PathVariable Long productoId, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        Optional<Producto> foundProducto = productoRepository.findById(productoId);

        if(foundCategory.isPresent() && foundProducto.isPresent()) {
            Producto producto = foundProducto.get();
            if(producto.getCategory().getId().equals(categoryId)) {
                productoRepository.deleteById(productoId);
                return new GlobalResponseHandler().handleResponse("Producto removed from Category successfully",
                        producto, HttpStatus.OK, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Producto " + productoId + " does not belong to Category " + categoryId,
                        HttpStatus.BAD_REQUEST, request);
            }
        } else {
            return new GlobalResponseHandler().handleResponse("Category or producto not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }



}
