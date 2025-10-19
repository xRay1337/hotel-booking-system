package org.service.booking.util;

import org.slf4j.MDC;
import java.util.UUID;

public class CorrelationContext {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    public static void setCorrelationId(String id) {
        MDC.put(CORRELATION_ID_HEADER, id);
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public static void removeCorrelationId() {
        MDC.remove(CORRELATION_ID_HEADER);
    }

    public static void initCorrelationIdIfAbsent() {
        if (getCorrelationId() == null) {
            setCorrelationId(UUID.randomUUID().toString());
        }
    }
}