package com.project.demo.logic.entity.producto;

import com.project.demo.logic.entity.producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
