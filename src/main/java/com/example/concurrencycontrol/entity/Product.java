package com.example.concurrencycontrol.entity;

import com.example.concurrencycontrol.dto.RegisterReqDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Long quantity;

	public void decreaseRemain() {
		quantity--;
	}

	public static Product createOf(RegisterReqDto reqDto) {
		return Product.builder()
			.name(reqDto.getName())
			.quantity(reqDto.getQuantity())
			.build();
	}

}
