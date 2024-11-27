package com.example.concurrencycontrol.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.concurrencycontrol.dto.PurchaseReqDto;
import com.example.concurrencycontrol.dto.RegisterReqDto;
import com.example.concurrencycontrol.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

	private final ProductService productService;

	@PostMapping("/register")
	public void register(@RequestBody RegisterReqDto registerReqDto) {
		productService.register(registerReqDto);
	}

	@PostMapping("/purchase")
	public String purchase(@RequestBody PurchaseReqDto purchaseReqDto) {
		return productService.purchase(purchaseReqDto);
	}

	@PostMapping("/redisson/purchase")
	public String redissonPurchase(@RequestBody PurchaseReqDto purchaseReqDto) {
		return productService.redissonPurchase(purchaseReqDto);
	}
}
