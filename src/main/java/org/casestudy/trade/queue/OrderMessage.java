package org.casestudy.trade.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.casestudy.trade.dto.OrderRequest;

@Data
@AllArgsConstructor
public class OrderMessage {
    private String customerId;
    private OrderRequest request;
}