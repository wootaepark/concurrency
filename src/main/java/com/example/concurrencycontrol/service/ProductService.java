package com.example.concurrencycontrol.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.concurrencycontrol.dto.PurchaseReqDto;
import com.example.concurrencycontrol.dto.RegisterReqDto;
import com.example.concurrencycontrol.entity.Product;
import com.example.concurrencycontrol.redis.lettuce.RedisLockRepository;
import com.example.concurrencycontrol.redis.redisson.RedissonLockStockFacade;
import com.example.concurrencycontrol.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final RedisLockRepository redisLockRepository;
	private final RedissonLockStockFacade redissonLockStockFacade;

	public void register(RegisterReqDto registerReqDto) {
		Product product = Product.createOf(registerReqDto);
		productRepository.save(product);
	}

	@Transactional
	public String purchase(PurchaseReqDto purchaseReqDto) {
		// 락의 범위 시작
		try {

			while (Boolean.FALSE.equals(redisLockRepository.lock(purchaseReqDto.getProductId()))) {
				Thread.sleep(100); // spin lock 의 방식 (장점이자 단점 -> 대체 수단 redisson)
			}

			Product product = productRepository.findById(purchaseReqDto.getProductId())
				.orElseThrow(() -> new IllegalArgumentException("없는 아이디 값"));

			if (product.getQuantity() <= 0) {
				throw new IllegalArgumentException("수량 부족!!");
			}

			product.decreaseRemain();

			System.out.println("수량 감소 처리 완료");

			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						redisLockRepository.unlock(purchaseReqDto.getProductId());
						System.out.println("트랜잭션 커밋 후 락 해제");
					}
				});
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "구매 성공";
	}

	// redisson 전용 구매 서비스
	@Transactional
	public String redissonPurchase(PurchaseReqDto purchaseReqDto) {

		redissonLockStockFacade.decrease(purchaseReqDto);
		return "구매성공";

	}

}
