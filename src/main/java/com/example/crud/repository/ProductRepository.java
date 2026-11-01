package com.example.crud.repository;

import com.example.crud.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory "repository" backed by a ConcurrentHashMap.
 * Data is lost when the application stops.
 */
@Repository
public class ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    void loadSampleData() {
        save(new Product(null, "Laptop", "14-inch lightweight laptop", 899.99, 25));
        save(new Product(null, "Wireless Mouse", "Ergonomic Bluetooth mouse", 24.50, 150));
        save(new Product(null, "Mechanical Keyboard", "RGB backlit, blue switches", 79.90, 60));
        save(new Product(null, "Monitor", "27-inch 4K IPS display", 329.00, 40));
        save(new Product(null, "USB-C Hub", "7-in-1 multiport adapter", 39.99, 200));
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>(store.values());
        products.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        return products;
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.incrementAndGet());
        }
        store.put(product.getId(), product);
        return product;
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
