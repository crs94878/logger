package com.logging;

/**
 * Date: 15.07.2019
 * @author shell
 */
public enum LoggingLvl {

    INFO(0),

    WARN(1),

    DEBUG(2),

    TRACE(3);

    private LoggingLvl(int lvl) {
        this.lvl = lvl;
    }

    private final int lvl;
}