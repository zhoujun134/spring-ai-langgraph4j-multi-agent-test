package com.zj.ai.study.service;

import com.zj.ai.study.domain.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class TestDataInitializer {

    public final static List<Order> orders = Arrays.asList(
            new Order(1L, "电子产品", LocalDate.of(2025, 7, 10), new BigDecimal("3999.00"), 100, "华东"),
            new Order(2L, "电子产品", LocalDate.of(2025, 8, 15), new BigDecimal("3999.00"), 150, "华北"),
            new Order(3L, "电子产品", LocalDate.of(2025, 9, 20), new BigDecimal("3999.00"), 180, "华南"),
            new Order(4L, "服装", LocalDate.of(2025, 7, 5), new BigDecimal("599.00"), 200, "华东"),
            new Order(5L, "服装", LocalDate.of(2025, 8, 25), new BigDecimal("599.00"), 220, "华北"),
            new Order(6L, "服装", LocalDate.of(2025, 9, 10), new BigDecimal("599.00"), 250, "华南"),
            new Order(7L, "食品", LocalDate.of(2025, 7, 12), new BigDecimal("99.00"), 500, "华东"),
            new Order(8L, "食品", LocalDate.of(2025, 8, 18), new BigDecimal("99.00"), 600, "华北"),
            new Order(9L, "食品", LocalDate.of(2025, 9, 25), new BigDecimal("99.00"), 700, "华南"),
            new Order(10L, "家居", LocalDate.of(2025, 7, 20), new BigDecimal("1999.00"), 80, "华东")
    );
}
