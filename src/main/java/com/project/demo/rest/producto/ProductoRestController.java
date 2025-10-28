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
    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Producto> productosPage = productoRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productosPage.getTotalPages());
        meta.setTotalElements(productosPage.getTotalElements());
        meta.setPageNumber(productosPage.getNumber() + 1);
        meta.setPageSize(productosPage.getSize());

        return new GlobalResponseHandler().handleResponse("Productos recuperados correctamente",
                productosPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('USER','SUPER_ADMIN')")
    public ResponseEntity<?> getProductosByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Producto> productosPage = productoRepository.findByCategoryId(categoryId, pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productosPage.getTotalPages());
        meta.setTotalElements(productosPage.getTotalElements());
        meta.setPageNumber(productosPage.getNumber() + 1);
        meta.setPageSize(productosPage.getSize());

        return new GlobalResponseHandler().handleResponse("Productos retrieved successfully",
                productosPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductoById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Producto> foundProducto = productoRepository.findById(id);
        if(foundProducto.isPresent()) {
            return new GlobalResponseHandler().handleResponse("Producto retrieved successfully",
                    foundProducto.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Producto " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
   // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addProducto(@RequestBody Producto producto, HttpServletRequest request) {
        if (producto.getCategory() != null && producto.getCategory().getId() != null) {
            Optional<Category> category = categoryRepository.findById(producto.getCategory().getId());
            if (category.isPresent()) {
                producto.setCategory(category.get());
                Producto savedProducto = productoRepository.save(producto);
                return new GlobalResponseHandler().handleResponse("Producto created successfully",
                        savedProducto, HttpStatus.CREATED, request);
            } else {
                return new GlobalResponseHandler().handleResponse("Category not found",
                        HttpStatus.BAD_REQUEST, request);
            }
        } else {
            return new GlobalResponseHandler().handleResponse("Category is required",
                    HttpStatus.BAD_REQUEST, request);
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
   // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProducto(@PathVariable Long id, @RequestBody Producto producto, HttpServletRequest request) {
        Optional<Producto> existingProducto = productoRepository.findById(id);
        if (existingProducto.isPresent()) {
            Producto productoToUpdate = existingProducto.get();
            productoToUpdate.setName(producto.getName());
            productoToUpdate.setDescription(producto.getDescription());
            productoToUpdate.setPrice(producto.getPrice());
            productoToUpdate.setStock(producto.getStock());

            if (producto.getCategory() != null && producto.getCategory().getId() != null) {
                Optional<Category> category = categoryRepository.findById(producto.getCategory().getId());
                if (category.isPresent()) {
                    productoToUpdate.setCategory(category.get());
                } else {
                    return new GlobalResponseHandler().handleResponse("Category not found",
                            HttpStatus.BAD_REQUEST, request);
                }
            }

            Producto savedProducto = productoRepository.save(productoToUpdate);
            return new GlobalResponseHandler().handleResponse("Producto updated successfully",
                    savedProducto, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Producto " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
 //   @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteProducto(@PathVariable Long id, HttpServletRequest request) {
        Optional<Producto> foundProducto = productoRepository.findById(id);
        if(foundProducto.isPresent()) {
            productoRepository.deleteById(foundProducto.get().getId());
            return new GlobalResponseHandler().handleResponse("Producto deleted successfully",
                    foundProducto.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Producto " + id + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}

