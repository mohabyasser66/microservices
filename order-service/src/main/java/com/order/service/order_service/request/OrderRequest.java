package com.order.service.order_service.request;

import com.order.service.order_service.dto.OrderItemsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private List<OrderItemsDto> orderItemsDtoList;

}
