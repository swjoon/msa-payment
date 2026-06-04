package app.backend.orderservice.infrastructure.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.backend.orderservice.global.constant.TraceConstants;
import app.backend.orderservice.infrastructure.client.constants.InternalServiceType;
import app.backend.orderservice.infrastructure.client.dto.FeignErrorResponse;
import app.backend.orderservice.infrastructure.client.error.InternalServiceExceptionFactory;
import app.backend.orderservice.infrastructure.client.error.UnknownInternalServiceException;
import app.backend.orderservice.infrastructure.client.item.exception.ItemInternalException;
import app.backend.orderservice.infrastructure.client.payment.exception.PaymentInternalException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FeignErrorDecoderConfig {

	private static final String UNKNOWN_CODE = "UNKNOWN_INTERNAL_SERVICE_ERROR";
	private static final String UNKNOWN_MESSAGE = "내부 서비스 호출 중 오류가 발생했습니다.";

	@Bean
	public ErrorDecoder feignErrorDecoder(
		ObjectMapper objectMapper,
		InternalServiceExceptionFactory exceptionFactory

	) {
		return (methodKey, response) -> {
			String body = readBody(response);

			FeignErrorResponse errorResponse = parseErrorResponse(objectMapper, body);

			String code = extractCode(errorResponse);
			String message = extractMessage(errorResponse);
			String traceId = extractTraceId(response, errorResponse);
			int status = response.status();

			InternalServiceType serviceType = resolveServiceType(methodKey);

			log.warn(
				"내부 통신에 실패했습니다. targetService={}, methodKey={}, status={}, code={}, message={}, traceId={}, body={}",
				serviceType.getServiceName(),
				methodKey,
				status,
				code,
				message,
				traceId,
				body
			);

			return exceptionFactory.create(
				serviceType,
				code,
				message,
				status,
				traceId
			);
		};
	}

	private String readBody(Response response) {
		if (response.body() == null) {
			return "";
		}

		try (InputStream inputStream = response.body().asInputStream()) {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.warn("응답 본문의 에러 메시지를 읽을 수 없습니다.", e);
			return "";
		}
	}

	private FeignErrorResponse parseErrorResponse(ObjectMapper objectMapper, String body) {
		if (body == null || body.isBlank()) {
			return null;
		}

		try {
			return objectMapper.readValue(body, FeignErrorResponse.class);
		} catch (Exception e) {
			log.warn("Feign 에러 응답 변환에 실패했습니다. body={}", body, e);
			return null;
		}
	}

	private String extractCode(FeignErrorResponse errorResponse) {
		if (errorResponse == null || errorResponse.code() == null || errorResponse.code().isBlank()) {
			return UNKNOWN_CODE;
		}

		return errorResponse.code();
	}

	private String extractMessage(FeignErrorResponse errorResponse) {
		if (errorResponse == null || errorResponse.message() == null || errorResponse.message().isBlank()) {
			return UNKNOWN_MESSAGE;
		}

		return errorResponse.message();
	}

	private String extractTraceId(Response response, FeignErrorResponse errorResponse) {
		if (errorResponse != null && errorResponse.traceId() != null && !errorResponse.traceId().isBlank()) {
			return errorResponse.traceId();
		}

		Collection<String> values = response.headers().get(TraceConstants.TRACE_ID_HEADER);

		if (values != null && !values.isEmpty()) {
			return values.iterator().next();
		}

		String traceId = MDC.get(TraceConstants.TRACE_ID_MDC_KEY);

		return traceId != null ? traceId : TraceConstants.NO_TRACE;
	}

	private InternalServiceType resolveServiceType(String methodKey) {
		if (methodKey.contains("ItemServiceClient")) {
			return InternalServiceType.ITEM;
		}

		if (methodKey.contains("PaymentServiceClient")) {
			return InternalServiceType.PAYMENT;
		}

		return InternalServiceType.UNKNOWN;
	}
}