package com.afollestad.nocknock.api;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class ServerStatus {

    public final static int OK = 1;
    public final static int WAITING = 2;
    public final static int CHECKING = 3;
    public final static int ERROR = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OK, WAITING, CHECKING, ERROR})
    public @interface Enum {}
}
