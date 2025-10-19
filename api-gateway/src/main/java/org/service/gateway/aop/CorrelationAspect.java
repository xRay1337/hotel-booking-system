package org.service.gateway.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.service.gateway.util.CorrelationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CorrelationAspect {

    /**
     * Инициализирует Correlation ID, если он ещё не установлен.
     * Вызывается до каждого метода контроллера.
     */
    @Before("execution(* org.service.hotel.controller.*.*(..))")
    public void initCorrelationId() {
        CorrelationContext.initCorrelationIdIfAbsent();
    }
}