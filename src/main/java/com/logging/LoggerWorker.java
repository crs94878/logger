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
 * Класс, АОП для вывода логов действий c классами, которые помеченны аннотацией {@link Logging}
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

    /** Срез обозначающий помеченные аннотацией классы */
    @Pointcut("execution(* (@Logging *).*(..))")
    public void annotClass() {
    }

    /** Срез обозначающий, помеченные аннотацией методы */
    @Pointcut("@annotation(Logging)")
    public void annotMethod() {
    }

    /**
     * Метод запускается перед выполеннием точки выполнения
     * @param joinPoint точка выполения
     * @return объект на котором проихожит вызов метода выполенния
     */
    @Before(value = "annotClass() || annotMethod()")
    public Object loggingBeforeCalling(JoinPoint joinPoint) {
        Object object = joinPoint.getTarget();
        LoggingLvl loggingLvl = object.getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Объект класса: ", object.getClass().getName());
        printMethWithParam(joinPoint, loggingLvl);
        return object;
    }

    /**
     * Метод логгирует информаицию о методе в точке выпонения и об аргументах метода
     * @param joinPoint точка выполения
     * @param loggingLvl уровень логгирования
     */
    private void printMethWithParam(JoinPoint joinPoint, LoggingLvl loggingLvl) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methName = methodSignature.getMethod().getReturnType() + " " + methodSignature.getMethod().getName();
        printLog(loggingLvl, "Вызван метод: ", methName);
        Object[] arg = joinPoint.getArgs();
        Parameter[] parameters = methodSignature.getMethod().getParameters();
        printLog(loggingLvl, "С параметрами: ", objectsToStr(parameters));
        printLog(loggingLvl, "Аргументами: ", objectsToStr(arg));
    }

    /**
     * Метод запускается после выполения точки выполенния
     * @param joinPoint точка выполения
     */
    @After(value = "annotClass() || annotMethod()")
    public void loggingAfterProeced(JoinPoint joinPoint) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Метод был выполнен", null);
    }

    /**
     * Метод запускается после выполнения и возврата значения из точки выполнения
     * @param joinPoint точка выполнения
     * @param result возвращенное занчение
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
     * Метод логгирования запускается если во время выполнения было выброшено исключение
     * @param joinPoint Точка выполнения
     * @param error исключение
     */
    @AfterThrowing(value = "annotClass() || annotMethod()", throwing = "error")
    public void loggingAfterThrowing(JoinPoint joinPoint, Throwable error) {
        LoggingLvl loggingLvl = joinPoint.getTarget().getClass().getAnnotation(Logging.class).level();
        printLog(loggingLvl, "Исключительная ситуация, сообщение: ", error.getMessage());
        printLog(LoggingLvl.ERROR,"Стек вызовов: ", error);
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
            if  (log.isTraceEnabled() || lvl.equals(LoggingLvl.ERROR)) {
                log.error(msg, (Throwable) parametr);
            } else if (log.isDebugEnabled() || lvl.equals(LoggingLvl.DEBUG)) {
                log.debug(msg + parametr.toString());
            } else if (log.isWarnEnabled() || lvl.equals(LoggingLvl.WARN)) {
                log.warn(msg + parametr.toString());
            } else if (log.isInfoEnabled() || lvl.equals(LoggingLvl.INFO)) {
                log.info(msg + parametr.toString());
            }
        }
    }
}

