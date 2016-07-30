package com.afollestad.nocknock.util;

/**
 * @author Aidan Follestad (afollestad)
 */
public class TimeUtil {

    private static long SECOND = 1000;
    private static long MINUTE = SECOND * 60;
    private static long HOUR = MINUTE * 60;
    private static long DAY = HOUR * 24;
    private static long WEEK = DAY * 7;
    private static long MONTH = WEEK * 4;

    public static String str(long duration) {
        if (duration >= MONTH) {
            return (duration / MONTH) + "mo";
        } else if (duration >= WEEK) {
            return (duration / WEEK) + "w";
        } else if (duration >= DAY) {
            return (duration / DAY) + "d";
        } else if (duration >= HOUR) {
            return (duration / HOUR) + "h";
        } else if (duration >= MINUTE) {
            return (duration / MINUTE) + "m";
        } else {
            return "<1m";
        }
    }
}
