package app.backend.orderservice.order.orchestrator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.orderservice.global.constant.TraceConstants;
import app.backend.orderservice.global.util.IdempotencyUtil;
import app.backend.orderservice.infrastructure.client.item.adapter.ItemAdapter;
import app.backend.orderservice.infrastructure.client.item.constatnt.ItemConstants;
import app.backend.orderservice.infrastructure.client.item.dto.req.GetItemDto;
import app.backend.orderservice.infrastructure.client.item.dto.res.UpdateItemStockDto;
import app.backend.orderservice.infrastructure.client.item.entity.CalculationType;
import app.backend.orderservice.infrastructure.client.item.exception.ItemInternalException;
import app.backend.orderservice.infrastructure.client.payment.adapter.PaymentAdapter;
import app.backend.orderservice.infrastructure.client.payment.constants.PaymentStatus;
import app.backend.orderservice.infrastructure.client.payment.dto.res.ConfirmPaymentResDto;
import app.backend.orderservice.infrastructure.client.payment.exception.PaymentInternalException;
import app.backend.orderservice.order.constant.OrderConstants;
import app.backend.orderservice.order.dto.req.ConfirmOrderPaymentReqDto;
import app.backend.orderservice.order.dto.req.CreateOrderReqDto;
import app.backend.orderservice.order.dto.res.CreateOrderResDto;
import app.backend.orderservice.order.dto.res.GetOrderTestResultDto;
import app.backend.orderservice.order.dto.res.GetOrderWithPaymentDto;
import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.entity.OrderStatus;
import app.backend.orderservice.order.event.OrderEventService;
import app.backend.orderservice.order.exception.OrderErrorCode;
import app.backend.orderservice.order.exception.OrderException;
import app.backend.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOrchestratorImpl implements OrderOrchestrator {

	private final static List<OrderStatus> statusList = List.of(
		OrderStatus.CREATED,
		OrderStatus.CONFIRMED,
		OrderStatus.REJECTED,
		OrderStatus.PENDING);

	private final ItemAdapter itemAdapter;
	private final PaymentAdapter paymentAdapter;

	private final OrderService orderService;
	private final OrderEventService orderEventService;

	@Override
	public CreateOrderResDto createOrder(final CreateOrderReqDto createOrderReqDto) {

		GetItemDto item = null;

		try {
			item = itemAdapter.getItem(createOrderReqDto.itemId());

		} catch (ItemInternalException ex) {
			throw new OrderException(OrderErrorCode.ITEM_NOT_FOUND);
		}

		if (item.stock() < createOrderReqDto.stock()) {
			throw new OrderException(OrderErrorCode.ITEM_STOCK_SHORTAGE);
		}

		return orderService.createOrder(createOrderReqDto);
	}

	// Saga 패턴중 오케스트레이션 패턴으로 중앙 관리형으로 설계.
	@Override
	public GetOrderWithPaymentDto confirmOrderPayment(final Long orderId, final ConfirmOrderPaymentReqDto requestDto) {

		// 1. 주문정보 확인.
		Order order = orderService.getOrder(orderId);

		String decreaseIdempotencyKey = IdempotencyUtil.getIdempotencyKey(
			OrderConstants.DECREASE,
			order.getItemId(),
			order.getId()
		);

		try {
			// 2-1 : item 내부통신으로 item 정상 차감 확인
			boolean isSuccess = itemAdapter.updateItemStock(
				order.getItemId(),
				UpdateItemStockDto.from(
					decreaseIdempotencyKey,
					order.getStock(),
					CalculationType.MINUS)
			);

			// 2-2 : item 차감 실패
			if (!isSuccess) {

				throw new OrderException(OrderErrorCode.ITEM_STOCK_SHORTAGE);
			}

		} catch (ItemInternalException ex) {

			log.warn(
				"Item 서비스 호출 실패. orderId={}, itemId={}, code={}, status={}, traceId={}",
				orderId,
				order.getItemId(),
				ex.getCode(),
				ex.getStatus(),
				ex.getTraceId(),
				ex
			);

			if (ItemConstants.ITEM_ERROR_STOCK_SHORTAGE.equals(ex.getCode())) {

				log.info("RejectReason : 재고부족");

				orderService.rejectOrder(orderId);

				throw new OrderException(OrderErrorCode.ITEM_STOCK_SHORTAGE);
			}

			if (ItemConstants.ITEM_ERROR_NOT_FOUND.equals(ex.getCode())) {

				log.info("RejectReason : 아이템 x");

				orderService.rejectOrder(orderId);

				throw new OrderException(OrderErrorCode.ITEM_NOT_FOUND);
			}

			log.info("RejectReason : transaction : {}, 멱등성키 : {}", MDC.get(TraceConstants.TRACE_ID_MDC_KEY),
				decreaseIdempotencyKey);

			orderEventService
				.rejectOrderWithItemReleaseIfDeductedEvent(
					orderId,
					order.getItemId(),
					order.getStock(),
					decreaseIdempotencyKey,
					IdempotencyUtil.getIdempotencyKey(
						OrderConstants.RELEASE,
						order.getItemId(),
						order.getId()
					)
				);

			throw new OrderException(OrderErrorCode.ITEM_STOCK_DEDUCT_FAILED);
		}

		try {
			// 3. payment 내부통신으로 외부 api 로 결제 프로세스 진행.
			ConfirmPaymentResDto paymentInfo = paymentAdapter.confirmPayment(orderId, requestDto);

			Order updateOrder;

			if (paymentInfo.status() == PaymentStatus.ABORTED) {

				log.info("RejectReason : 승인 거절");

				String releaseIdempotencyKey = IdempotencyUtil.getIdempotencyKey(
					OrderConstants.RELEASE,
					order.getItemId(),
					order.getId()
				);

				updateOrder = orderEventService
					.rejectOrderWithItemReleaseEvent(
						orderId,
						order.getItemId(),
						order.getStock(),
						releaseIdempotencyKey
					);

			} else if (paymentInfo.status() == PaymentStatus.DONE) {

				updateOrder = orderService.confirmOrder(orderId);

			} else {

				updateOrder = orderEventService
					.pendingOrderWithPaymentCheckEvent(
						orderId,
						requestDto.orderId(),
						requestDto.paymentKey()
					);
			}

			// 4. 주문 정보 수정 및 반환

			return GetOrderWithPaymentDto.from(updateOrder, paymentInfo);

		} catch (
			PaymentInternalException ex) {
			log.warn(
				"Payment 서비스 호출 실패. orderId={}, code={}, status={}, traceId={}",
				orderId,
				ex.getCode(),
				ex.getStatus(),
				ex.getTraceId(),
				ex
			);

			orderEventService.pendingOrderWithPaymentCheckEvent(orderId, requestDto.orderId(), requestDto.paymentKey());

			throw new OrderException(OrderErrorCode.PAYMENT_TIMEOUT);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public GetOrderTestResultDto getOrderTestResult(final Long itemId) {

		List<Integer> info = getOrderResultInfo(itemId);

		GetItemDto itemInfo = itemAdapter.getItem(itemId);

		return GetOrderTestResultDto.from(
			itemId,
			orderService.getOrderCountByItemId(itemId),
			info.get(0),
			info.get(1),
			info.get(2),
			info.get(3),
			info.get(4),
			info.get(5),
			info.get(6),
			info.get(7),
			itemInfo
		);
	}

	private List<Integer> getOrderResultInfo(final Long itemId) {
		List<Integer> info = new ArrayList<>();

		for (int i = 0; i < 4; i++) {
			List<Order> orderList = orderService.getOrderTestResult(itemId, statusList.get(i));

			info.add(orderList.size());
			info.add(orderList.stream().mapToInt(Order::getStock).sum());
		}

		return info;
	}
}
