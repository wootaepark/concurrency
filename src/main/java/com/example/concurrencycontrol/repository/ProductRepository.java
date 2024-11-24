package com.example.concurrencycontrol.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.concurrencycontrol.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
