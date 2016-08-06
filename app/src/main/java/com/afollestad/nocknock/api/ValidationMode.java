package com.afollestad.nocknock.api;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class ValidationMode {

    public final static int STATUS_CODE = 1;
    public final static int TERM_SEARCH = 2;
    public final static int JAVASCRIPT = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_CODE, TERM_SEARCH, JAVASCRIPT})
    public @interface Enum {
    }
}
