package com.example.concurrencycontrol.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.concurrencycontrol.dto.PurchaseReqDto;
import com.example.concurrencycontrol.dto.RegisterReqDto;
import com.example.concurrencycontrol.entity.Product;
import com.example.concurrencycontrol.redis.lettuce.LockService;
import com.example.concurrencycontrol.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final LockService lockService;

	public void register(RegisterReqDto registerReqDto) {

		Product product = Product.createOf(registerReqDto);
		productRepository.save(product);
	}

	@Transactional

	public String purchase(PurchaseReqDto purchaseReqDto) {
		Product product = productRepository.findById(purchaseReqDto.getProductId())
			.orElseThrow(() -> new IllegalArgumentException("없는 아이디 값"));
		
		try {
			lockService.decrease(product, product.getId());
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}

		return "구매 성공";
	}

}
