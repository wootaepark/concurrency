package com.example.concurrencycontrol.redis.lettuce;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.concurrencycontrol.dto.PurchaseReqDto;
import com.example.concurrencycontrol.entity.Product;
import com.example.concurrencycontrol.repository.ProductRepository;
import com.example.concurrencycontrol.service.ProductService;

class LettuceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private LockService lockService;

	private ProductService productService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		productService = new ProductService(productRepository, lockService);
	}

	@Test
	void 두명의_유저가_남은자리_하나를_차지하려는_경우() throws InterruptedException {
		// given
		Product product = new Product(1L, "Test Product", 1L);
		PurchaseReqDto purchaseReqDto = new PurchaseReqDto(1L);

		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		doAnswer(invocation -> {
			Product prod = invocation.getArgument(0);
			prod.decreaseRemain(); // decrease the quantity
			return null;
		}).when(lockService).decrease(any(), any());

		Thread thread1 = new Thread(() -> {
			productService.purchase(purchaseReqDto);
		});

		Thread thread2 = new Thread(() -> {
			productService.purchase(purchaseReqDto);
		});

		thread1.start();
		thread2.start();

		thread1.join();
		thread2.join();

		// After both threads execute, the product quantity should have decreased twice
		assertEquals(0L, product.getQuantity());
	}
}
