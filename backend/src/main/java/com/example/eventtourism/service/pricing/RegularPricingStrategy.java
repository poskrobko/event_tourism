package com.example.eventtourism.service.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("regularPricing")
public class RegularPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculate(BigDecimal basePrice, int amount) {
        return basePrice.multiply(BigDecimal.valueOf(amount));
    }
}
