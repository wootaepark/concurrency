package com.example.concurrencycontrol.redis.lettuce;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLockRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisLockRepository(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public Boolean lock(Long key) {
		Boolean lockResult = redisTemplate
			.opsForValue()
			.setIfAbsent(generated(key), "lock", Duration.ofMillis(30000)); // 30초 대기 (unlock 되기 전 최대 유효시간)

		System.out.println("결과 " + lockResult);

		return lockResult;
	}

	public Boolean unlock(Long key) {
		return redisTemplate.delete(generated(key));
	}

	private String generated(Long key) {
		return key.toString();
	}
}
