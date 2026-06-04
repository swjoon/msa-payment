package app.backend.orderservice.global.util;

import java.time.LocalDateTime;
import java.util.UUID;

public class UuidUtil {

	public static String getUUID(final int length) {

		return UUID.randomUUID().toString().substring(0, length);
	}

	public static String getUUIDWithLocalDateTimeFormat(final int length) {

		return String.format("%s-%s-%s", LocalDateTime.now(), getUUID(length), getUUID(length));
	}

}
