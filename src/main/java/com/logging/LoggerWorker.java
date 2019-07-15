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
 * Класс, АОП для вывода логов дейсвтвий c классами, которые помеченны аннотацией {@link Logging}
 * Date: 13.07.2019
 * @author shell
 */
@Aspect
@Component
public class LoggerWorker {

    /** Логгер */
    @Autowired
    private Log log;

    /** Конструктор по умолчанию */
    public LoggerWorker() {
    }

    /**  */
    @Pointcut("execution(* (@Logging *).*(..))")
    public void annotClass() {
    }

    /** */
    @Pointcut("@annotation(Logging)")
    public void annotMethod() {
    }

    /**
     *
     * @param joinPoint
     * @return
     */
    @Before(value = "annotClass() || annotMethod()")
    public Object loggingBeforeCalling(JoinPoint joinPoint) {
        Object object = joinPoint.getTarget();
        LoggingLvl loggingLvl = object.getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Объект класса: ", object.getClass().getName());
        methWithParamLog(joinPoint, loggingLvl);
        return object;
    }

    /**
     *
     * @param joinPoint
     * @param loggingLvl
     */
    private void methWithParamLog(JoinPoint joinPoint, LoggingLvl loggingLvl) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methName = methodSignature.getMethod().getReturnType() + " " + methodSignature.getMethod().getName();
        printLog(loggingLvl, "Вызван метод: ", methName);
        Object[] arg = joinPoint.getArgs();
        Parameter[] parameters = methodSignature.getMethod().getParameters();
        printLog(loggingLvl, "С параметрами: ", objectsToStr(parameters));
        printLog(loggingLvl, "Аргументами: ", objectsToStr(arg));
    }

    /**
     *
     * @param joinPoint
     */
    @After(value = "annotClass() || annotMethod()")
    public void loggingAfterProeced(JoinPoint joinPoint) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Метод был выполнен", null);
    }

    /**
     *
     * @param joinPoint
     * @param result
     */
    @AfterReturning(value = "annotClass() || annotMethod()", returning = "result")
    public void loggingAfterProccedAndReturnValue(JoinPoint joinPoint, Object result) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        if(result != null) {
            printLog(loggingLvl, "Из метода был возвращено значение: ", result.toString());
            printLog(loggingLvl, "Тип данных возвращенного значения: ", result.getClass().getName());
        }
    }

    /**
     *
     * @param joinPoint
     * @param error
     */
    @AfterThrowing(value = "annotClass() || annotMethod()", throwing = "error")
    public void loggingAfterThrowing(JoinPoint joinPoint, Throwable error) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Исключительная ситуация, сообщение: ", error.getMessage());
        printLog(LoggingLvl.TRACE,"Стек вызовов: ", error);
    }

    /**
     * Конвертирование массива объектов в строку
     * @param objects объекты
     * @return строка
     */
    private String objectsToStr(Object[] objects) {
        return Arrays.stream(objects)
                .map(Object::toString).collect(Collectors.joining("; "));
    }

    /**
     * Вывод логов программы
     * @param lvl Уровень логгирования
     * @param msg сообщение
     * @param parametr параметры сообщения
     */
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
}
