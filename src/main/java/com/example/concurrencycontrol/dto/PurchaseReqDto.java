package com.example.concurrencycontrol.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PurchaseReqDto {
	private Long productId;

	public PurchaseReqDto(long l) {
		this.productId = l;
	}
}
