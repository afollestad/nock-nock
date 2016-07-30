package com.afollestad.nocknock.util;

import android.graphics.Path;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class MathUtil {

    public static Path bezierCurve(FloatingActionButton fab, View rootView) {
        final int fabCenterX = (int) (fab.getX() + fab.getMeasuredWidth() / 2);
        final int fabCenterY = (int) (fab.getY() + fab.getMeasuredHeight() / 2);

        final int endCenterX = (rootView.getMeasuredWidth() / 2) - (fab.getMeasuredWidth() / 2);
        final int endCenterY = (rootView.getMeasuredHeight() / 2) - (fab.getMeasuredHeight() / 2);

        final int halfX = (fabCenterX - endCenterX) / 2;
        final int halfY = (fabCenterY - endCenterY) / 2;
        int mControlX = endCenterX + halfX;
        int mControlY = endCenterY + halfY;
        mControlY -= halfY;
        mControlX += halfX;

        Path path = new Path();
        path.moveTo(fab.getX(), fab.getY());
        path.quadTo(mControlX, mControlY, endCenterX, endCenterY);

        return path;
    }
}