package com.example.concurrencycontrol.redis.lettuce;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.concurrencycontrol.entity.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockService {

	private final RedisLockRepository redisLockRepository;

	@Transactional
	public void decrease(Product product, Long key) throws InterruptedException {
		// 이쪽 코드는 ProductService 로 이동하면 좋을 듯

		if (product.getQuantity() <= 0) {
			throw new IllegalArgumentException("수량 부족!!");

		}

		while (Boolean.FALSE.equals(redisLockRepository.lock(key))) {
			System.out.println("제대로 저장되지 않는다.");
			Thread.sleep(100); // spin lock 의 방식 (장점이자 단점 -> 대체 수단 redisson)
		}
		System.out.println("제대로 저장이 됨");

		product.decreaseRemain();

		// 아래 코드를 주석처리하면 레디스에서 값 확인 가능 (설정된 시간동안)
		redisLockRepository.unlock(key);
	}

}
