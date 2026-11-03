package com.example.crud.repository;

import com.example.crud.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for Product entity.
 * Extends JpaRepository to get out-of-the-box CRUD operations backed by PostgreSQL.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @PostConstruct
    default void loadSampleData() {
        // Sample data will be loaded via data.sql if needed
    }
}
