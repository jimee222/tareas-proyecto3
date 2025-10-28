package com.project.demo.rest.item;


import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.item.Item;
import com.project.demo.logic.entity.item.ItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemRestController {
    @Autowired
    private ItemRepository itemRepository;


    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Item> ordersPage = itemRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(ordersPage.getTotalPages());
        meta.setTotalElements(ordersPage.getTotalElements());
        meta.setPageNumber(ordersPage.getNumber() + 1);
        meta.setPageSize(ordersPage.getSize());

        return new GlobalResponseHandler().handleResponse("Order retrieved successfully",
                ordersPage.getContent(), HttpStatus.OK, meta);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemRepository.findById(id)
                .map(existingItem -> {
                    existingItem.setName(item.getName());
                    existingItem.setDescription(item.getDescription());
                    existingItem.setStatus(String.valueOf(item.getPrice()));
                    existingItem.setStatus(item.getStatus());
                    return itemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    item.setId(id);
                    return itemRepository.save(item);
                });
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> patchItem(@PathVariable Long itemId, @RequestBody Item item, HttpServletRequest request) {
        Optional<Item> foundItem = itemRepository.findById(itemId);
        if(foundItem.isPresent()) {
            if(item.getName() != null) foundItem.get().setName(item.getName());
            if(item.getDescription() != null) foundItem.get().setDescription(item.getDescription());
            if(item.getPrice() != null) foundItem.get().setStatus(String.valueOf(item.getPrice()));
            if(item.getStatus() != null) foundItem.get().setStatus(item.getStatus());
            itemRepository.save(foundItem.get());
            return new GlobalResponseHandler().handleResponse("Order updated successfully",
                    foundItem.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Order id " + itemId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }


    @PostMapping("/item")
    public ResponseEntity<?> addItem(@RequestBody Item item, HttpServletRequest request) {
        Item savedItem = itemRepository.save(item);
        return new GlobalResponseHandler().handleResponse("Item created successfully",
                savedItem, HttpStatus.CREATED, request);
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId, HttpServletRequest request) {
        Optional<Item> foundItem = itemRepository.findById(itemId);
        if(foundItem.isPresent()) {
            itemRepository.deleteById(foundItem.get().getId());
            return new GlobalResponseHandler().handleResponse("Item deleted successfully",
                    foundItem.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Item id " + itemId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }
}