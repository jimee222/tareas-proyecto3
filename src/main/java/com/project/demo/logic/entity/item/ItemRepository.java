package com.project.demo.logic.entity.item;

import com.project.demo.logic.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item,Long> {

}
