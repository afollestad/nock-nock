package com.afollestad.nocknock.api;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** @author Aidan Follestad (afollestad) */
public final class ServerStatus {

  public static final int OK = 1;
  public static final int WAITING = 2;
  public static final int CHECKING = 3;
  public static final int ERROR = 4;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({OK, WAITING, CHECKING, ERROR})
  public @interface Enum {}
}
