package com.ejada.ecommerce.shop.mapper;

import com.ejada.ecommerce.shop.dto.response.OrderItemResponse;
import com.ejada.ecommerce.shop.dto.response.OrderResponse;
import com.ejada.ecommerce.shop.entity.Order;
import com.ejada.ecommerce.shop.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper
public interface ShopMapper {

    OrderResponse toResponse(Order order);

    OrderItemResponse toResponse(OrderItem item);
}