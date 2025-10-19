package org.service.hotel.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.service.hotel.util.CorrelationContext;

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