package com.engineerbiz.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time conversions between UTC persistence representation and local Cambodia (Asia/Phnom_Penh) display.
 */
public final class DateTimeUtils {

    public static final ZoneId PHNOM_PENH_ZONE = ZoneId.of("Asia/Phnom_Penh");
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private DateTimeUtils() {
    }

    public static ZonedDateTime toPhnomPenhTime(Instant utcInstant) {
        if (utcInstant == null) {
            return null;
        }
        return utcInstant.atZone(PHNOM_PENH_ZONE);
    }

    public static String formatPhnomPenhTime(Instant utcInstant) {
        if (utcInstant == null) {
            return null;
        }
        return toPhnomPenhTime(utcInstant).format(DISPLAY_FORMATTER);
    }
}
