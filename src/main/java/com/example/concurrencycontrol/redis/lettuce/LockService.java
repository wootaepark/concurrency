package com.example.concurrencycontrol.redis.lettuce;

import org.springframework.stereotype.Service;

import com.example.concurrencycontrol.entity.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockService {

	private final RedisLockRepository redisLockRepository;

	public void decrease(Product product, Long key) throws InterruptedException {
		if (product.getQuantity() <= 0) {
			throw new IllegalArgumentException("수량 부족!!");
		}

		while (Boolean.FALSE.equals(redisLockRepository.lock(key))) {
			System.out.println("제대로 저장되지 않는다.");
			Thread.sleep(100);
		}
		System.out.println("제대로 저장이 됨");

		product.decreaseRemain();

		redisLockRepository.unlock(key);
	}

}
