package com.logging;

import org.apache.commons.logging.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Date: 13.07.2019
 * @author shell
 */
@Aspect
@Component
public class LoggerWorker {

    @Autowired
    private Log log;

    public LoggerWorker() {
    }

    @Pointcut("execution(* (@Logging *).*(..))")
    public void annotClass() {
    }

    @Pointcut("@annotation(Logging)")
    public void annotMethod() {
    }

    @Before(value = "annotClass() || annotMethod()")
    public Object loggingBeforeCalling(JoinPoint joinPoint) {
        Object object = joinPoint.getTarget();
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Объект класса: ", object.getClass().getName());
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methName = methodSignature.getMethod().getReturnType() + " " + methodSignature.getMethod().getName();
        printLog(loggingLvl, "Вызван метод: ", methName);
        Object[] arg = joinPoint.getArgs();
        Parameter[] parameters = methodSignature.getMethod().getParameters();
        printLog(loggingLvl, "С параметрами: ", objectsToStr(parameters));
        printLog(loggingLvl, "Аргументами: ", objectsToStr(arg));
        return object;
    }

    private String objectsToStr(Object[] args) {
        return Arrays.stream(args)
                .map(Object::toString).collect(Collectors.joining("; "));
    }

    private void printLog(LoggingLvl lvl, String msg, Object parametr) {
        if(parametr != null) {
            switch (lvl) {
                case INFO:
                    log.info(msg + parametr.toString());
                    break;
                case WARN:
                    log.warn(msg + parametr.toString());
                    break;
                case DEBUG:
                    log.debug(msg + parametr.toString());
                    break;
                case TRACE:
                    log.error(msg, (Throwable) parametr);
            }
        }
    }

    @After(value = "annotClass() || annotMethod()")
    public void loggingAfterProeced(JoinPoint joinPoint) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Метод был выполнен", null);
    }

    @AfterReturning(value = "annotClass() || annotMethod()", returning = "result")
    public void loggingAfterProccedAndReturnValue(JoinPoint joinPoint, Object result) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        if(result != null) {
            printLog(loggingLvl, "Из метода был возвращено значение: ", result.toString());
            printLog(loggingLvl, "Тип данных возвращенного значения: ", result.getClass().getName());
        }
    }

    @AfterThrowing(value = "annotClass() || annotMethod()", throwing = "error")
    public void loggingAfterThrowing(JoinPoint joinPoint, Throwable error) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Исключительная ситуация, сообщение: ", error.getMessage());
        printLog(LoggingLvl.TRACE,"Стек вызовов: ", error);
    }
}
