package app.backend.orderservice.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.backend.orderservice.global.response.ApiResponse;
import app.backend.orderservice.infrastructure.client.item.adapter.ItemAdapter;
import app.backend.orderservice.infrastructure.client.item.dto.req.GetItemDto;
import app.backend.orderservice.order.dto.req.ConfirmOrderPaymentReqDto;
import app.backend.orderservice.order.dto.req.CreateOrderReqDto;
import app.backend.orderservice.order.dto.res.CreateOrderResDto;
import app.backend.orderservice.order.dto.res.GetOrderResDto;
import app.backend.orderservice.order.dto.res.GetOrderTestResultDto;
import app.backend.orderservice.order.dto.res.GetOrderWithPaymentDto;
import app.backend.orderservice.order.orchestrator.OrderOrchestrator;
import app.backend.orderservice.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final ItemAdapter itemAdapter;
	private final OrderService orderService;
	private final OrderOrchestrator orderGateway;

	/**
	 * 주문서 생성 API
	 *
	 * @param requestDto - 주문서 생성 관련 정보
	 * @return CreateOrderResDto - 생성된 주문 정보
	 */
	@PostMapping
	public ApiResponse<CreateOrderResDto> createOrder(
		@RequestBody final CreateOrderReqDto requestDto
	) {

		CreateOrderResDto res = orderGateway.createOrder(requestDto);

		return ApiResponse.of(true, HttpStatus.CREATED, "Create Order Success", res);
	}

	@GetMapping("/{orderId}")
	public ApiResponse<GetOrderResDto> readOrder(@PathVariable final Long orderId) {

		GetOrderResDto res = orderService.readOrder(orderId);

		return ApiResponse.of(true, HttpStatus.OK, "Read Order Date Success", res);
	}

	/**
	 * 결제 진행 API
	 *
	 * @param orderId - 주문 데이터 ID
	 * @param requestDto - Payment Info Data
	 * @return GetOrderWithPaymentDto - 업데이트 된 주문 정보
	 */
	@PostMapping("/{orderId}/payment/confirmation")
	public ApiResponse<GetOrderWithPaymentDto> confirmOrderPayment(
		@PathVariable final Long orderId,
		@RequestBody @Valid final ConfirmOrderPaymentReqDto requestDto
	) {

		GetOrderWithPaymentDto res = orderGateway.confirmOrderPayment(orderId, requestDto);

		return ApiResponse.of(true, HttpStatus.CREATED, "Confirm Order With Payment Success", res);
	}

	@GetMapping("/test/{itemId}")
	public ApiResponse<GetItemDto> getItem(@PathVariable final Long itemId) {

		return ApiResponse.of(true, HttpStatus.OK, "Get Item Success", itemAdapter.getItem(itemId));
	}

	// Todo : 결제 취소

	// test
	@GetMapping("/test/{itemId}/result")
	public ApiResponse<GetOrderTestResultDto> getOrderResult(@PathVariable final Long itemId) {

		GetOrderTestResultDto res = orderGateway.getOrderTestResult(itemId);

		return ApiResponse.of(true, HttpStatus.OK, "Get Order Result Success", res);
	}
}
