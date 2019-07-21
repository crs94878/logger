package com.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация, которым необходимо пометить класс, что бы включить логгирование
 * Date: 13.07.2019
 * @author shell
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {

    /** Уровень логгирования */
    LoggingLvl level() default LoggingLvl.DEBUG;
}
