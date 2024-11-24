package com.example.concurrencycontrol.dto;

import lombok.Getter;

@Getter
public class PurchaseReqDto {
	private Long productId;

	public PurchaseReqDto(long l) {
		this.productId = l;
	}
}
