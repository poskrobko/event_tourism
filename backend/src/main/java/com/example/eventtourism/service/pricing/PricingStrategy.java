package com.example.eventtourism.service.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculate(BigDecimal basePrice, int amount);
}
