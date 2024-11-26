package com.example.concurrencycontrol.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.concurrencycontrol.dto.PurchaseReqDto;
import com.example.concurrencycontrol.dto.RegisterReqDto;
import com.example.concurrencycontrol.entity.Product;
import com.example.concurrencycontrol.redis.lettuce.RedisLockRepository;
import com.example.concurrencycontrol.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final RedisLockRepository redisLockRepository;
	//private final LockService lockService;

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

			//TransactionSynchronization
			//해당 트랜잭션이 커밋된 후에 실행 될 코드를 지정 할 수 있다.
			//문제 발생 예상 원인 : lock, unlock 변환 시간 사이 트랜잭션이 실행되는 로직 예외가 발생 할 수 있기 때문이다.

			// 11-25일 현재 상황 초당 2000개 요청까지는 정상처리
			// 정확히 2001 개 부터 그보다 적은 값이 이루어진다.

			// 이 경우 스핀락을 사용한경우
			// 만약 lettuce 에서 분산락을 사용하고 싶은 경우 직접 구현이 필요하다
			// 그리고 이러한 분산락의 방식을 좀 더 쉽게 해주는 것이 pub/sub 방식의 redisson

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "구매 성공";
	}

}
