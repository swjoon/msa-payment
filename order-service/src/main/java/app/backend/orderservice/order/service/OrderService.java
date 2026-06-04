package app.backend.orderservice.order.service;

import java.util.List;

import app.backend.orderservice.order.dto.req.CreateOrderReqDto;
import app.backend.orderservice.order.dto.res.CreateOrderResDto;
import app.backend.orderservice.order.dto.res.GetOrderResDto;
import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.entity.OrderStatus;

public interface OrderService {

	CreateOrderResDto createOrder(CreateOrderReqDto createOrderReqDto);

	GetOrderResDto readOrder(Long orderId);

	Order getOrder(Long orderId);

	Order confirmOrder(Long orderId);

	Order pendingOrder(Long orderId);

	void rejectOrder(Long orderId);

	Order cancelOrder(Long orderId);

	List<Order> getOrderTestResult(Long itemId, OrderStatus orderStatus);

	Long getOrderCountByItemId(Long itemId);

}
