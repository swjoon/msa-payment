package app.backend.orderservice.order.orchestrator;

import app.backend.orderservice.order.dto.req.ConfirmOrderPaymentReqDto;
import app.backend.orderservice.order.dto.req.CreateOrderReqDto;
import app.backend.orderservice.order.dto.res.CreateOrderResDto;
import app.backend.orderservice.order.dto.res.GetOrderTestResultDto;
import app.backend.orderservice.order.dto.res.GetOrderWithPaymentDto;

public interface OrderOrchestrator {

	CreateOrderResDto createOrder(CreateOrderReqDto createOrderReqDto);

	GetOrderWithPaymentDto confirmOrderPayment(Long orderId, ConfirmOrderPaymentReqDto requestDto);

	GetOrderTestResultDto getOrderTestResult(Long itemId);
}
