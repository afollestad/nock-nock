package com.afollestad.nocknock.util;

/** @author Aidan Follestad (afollestad) */
public class TimeUtil {

  public static final long SECOND = 1000;
  public static final long MINUTE = SECOND * 60;
  public static final long HOUR = MINUTE * 60;
  public static final long DAY = HOUR * 24;
  public static final long WEEK = DAY * 7;
  public static final long MONTH = WEEK * 4;

  public static String str(long duration) {
    if (duration <= 0) {
      return "";
    } else if (duration >= MONTH) {
      return (int) Math.ceil(((float) duration / (float) MONTH)) + "mo";
    } else if (duration >= WEEK) {
      return (int) Math.ceil(((float) duration / (float) WEEK)) + "w";
    } else if (duration >= DAY) {
      return (int) Math.ceil(((float) duration / (float) DAY)) + "d";
    } else if (duration >= HOUR) {
      return (int) Math.ceil(((float) duration / (float) HOUR)) + "h";
    } else if (duration >= MINUTE) {
      return (int) Math.ceil(((float) duration / (float) MINUTE)) + "m";
    } else {
      return "<1m";
    }
  }
}
