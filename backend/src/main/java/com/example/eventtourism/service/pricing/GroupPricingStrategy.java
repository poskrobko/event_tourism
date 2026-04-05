package com.example.eventtourism.service.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("groupPricing")
public class GroupPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculate(BigDecimal basePrice, int amount) {
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(amount));
        if (amount >= 5) {
            return total.multiply(BigDecimal.valueOf(0.9));
        }
        return total;
    }
}
