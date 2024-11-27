package com.example.concurrencycontrol.redis.redisson;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.concurrencycontrol.dto.PurchaseReqDto;
import com.example.concurrencycontrol.entity.Product;
import com.example.concurrencycontrol.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedissonLockStockFacade {

	private final RedissonClient redissonClient;
	private final ProductRepository productRepository;

	public void decrease(PurchaseReqDto dto) {
		// key 를 통한 lock 객체 가져오기
		RLock lock = redissonClient.getLock(dto.getProductId().toString());

		int retryCount = 3;  // 최대 재시도 횟수
		int retryDelay = 1000;  // 재시도 간격 (밀리초)

		while (retryCount > 0) {
			try {
				// 락을 얻기 위한 최대 대기 시간 5초,
				// 락이 획득된 후 최대 1초 동안 유지
				boolean available = lock.tryLock(5, 10, TimeUnit.SECONDS);

				if (available) {
					Product product = productRepository.findById(dto.getProductId())
						.orElseThrow(() -> new IllegalArgumentException("해당 상품이 없습니다."));
					System.out.println("product id: " + product.getId());
					product.decreaseRemain();
					System.out.println("티켓 구매 성공");

					// 트랜잭션 커밋 후 락 해제
					if (TransactionSynchronizationManager.isSynchronizationActive()) {
						TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
							@Override
							public void afterCommit() {
								lock.unlock();
								System.out.println("트랜잭션 커밋 후 락 해제");
							}
						});
					}
					return; // 락을 성공적으로 얻었다면 종료
				} else {
					System.out.println("락 획득 실패, 재시도 중...");
					retryCount--;
					if (retryCount > 0) {
						// 일정 시간 후 재시도
						Thread.sleep(retryDelay);
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println("락을 여러 번 재시도했지만 여전히 획득하지 못했습니다.");
	}

}



