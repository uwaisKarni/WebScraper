package com.webscraper.atlys;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webscraper.atlys.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTitle(String title);
}
