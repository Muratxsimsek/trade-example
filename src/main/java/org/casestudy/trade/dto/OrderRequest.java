package org.casestudy.trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.casestudy.trade.enums.OrderSide;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class OrderRequest {
    @NotNull
    private String assetName;
    @NotNull
    private OrderSide side;
    @NotNull
    private int size;
    @NotNull
    private BigDecimal price;
}