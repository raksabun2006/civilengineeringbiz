package com.engineerbiz.common.util;

/**
 * System-wide business and operational constants.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class
    }

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    public static final String DEFAULT_CURRENCY = "USD";
    public static final String KHR_CURRENCY = "KHR";

    public static final String API_V1_PREFIX = "/api/v1";
}
