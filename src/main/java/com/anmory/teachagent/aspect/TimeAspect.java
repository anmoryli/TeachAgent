package com.anmory.teachagent.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-28 上午10:32
 */

@Component
@Aspect
@Slf4j
public class TimeAspect {
    @Around("execution(* com.anmory.teachagent.controller.*.*(..))")// 切点表达式
     public Object time(ProceedingJoinPoint pjp) throws Throwable {
         long start = System.currentTimeMillis();
         Object result = pjp.proceed();
         long end = System.currentTimeMillis();
         log.info("方法执行耗时：{}ms", end - start);
         return result;
     }
}
