package com.afollestad.nocknock.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.nocknock.R;
import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.api.ServerStatus;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AddSiteActivity extends AppCompatActivity implements View.OnClickListener {

    private View rootLayout;
    private Toolbar toolbar;

    private EditText inputName;
    private EditText inputUrl;
    private EditText inputInterval;
    private Spinner spinnerInterval;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsite);

        rootLayout = findViewById(R.id.rootView);
        inputName = (EditText) findViewById(R.id.inputName);
        inputUrl = (EditText) findViewById(R.id.inputUrl);
        inputInterval = (EditText) findViewById(R.id.checkIntervalInput);
        spinnerInterval = (Spinner) findViewById(R.id.checkIntervalSpinner);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            closeFromNavWithReveal();
        });

        if (savedInstanceState == null) {
            rootLayout.setVisibility(View.INVISIBLE);
            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }

        ArrayAdapter<String> intervalOptionsAdapter = new ArrayAdapter<>(this, R.layout.list_item_spinner,
                getResources().getStringArray(R.array.interval_options));
        spinnerInterval.setAdapter(intervalOptionsAdapter);

        findViewById(R.id.doneBtn).setOnClickListener(this);
    }

    private void closeFromNavWithReveal() {
        final int offset = (int) getResources().getDimension(R.dimen.content_inset);
        final int cx = rootLayout.getMeasuredWidth();
        final int cy = rootLayout.getMeasuredHeight();
        float initialRadius = Math.max(cx, cy);

        final Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, offset, offset, initialRadius, 0);
        circularReveal.setDuration(300);
        circularReveal.setInterpolator(new AccelerateInterpolator());
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        circularReveal.start();
    }

    private void circularRevealActivity() {
        final int cx = rootLayout.getMeasuredWidth() / 2;
        final int cy = rootLayout.getMeasuredHeight() / 2;
        final int animDuration = 300;
        final float finalRadius = Math.max(cx, cy);
        final Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);

        circularReveal.setDuration(animDuration);
        circularReveal.setInterpolator(new DecelerateInterpolator());

        toolbar.setAlpha(0f);
        rootLayout.setVisibility(View.VISIBLE);

        toolbar.animate().alpha(1f).setDuration(animDuration).start();
        circularReveal.start();
    }

    // Done button
    @Override
    public void onClick(View view) {
        ServerModel model = new ServerModel();
        model.name = inputName.getText().toString().trim();
        model.url = inputUrl.getText().toString().trim();
        model.lastCheck = -1;
        model.status = ServerStatus.WAITING;

        String intervalStr = inputInterval.getText().toString().trim();
        if (intervalStr.isEmpty()) intervalStr = "0";
        model.checkInterval = Integer.parseInt(intervalStr);

        switch (spinnerInterval.getSelectedItemPosition()) {
            case 0: // minutes
                model.checkInterval *= (60 * 1000);
                break;
            case 1: // hours
                model.checkInterval *= (60 * 60 * 1000);
                break;
            case 2: // days
                model.checkInterval *= (60 * 60 * 24 * 1000);
                break;
            default: // weeks
                model.checkInterval *= (60 * 60 * 24 * 7 * 1000);
                break;
        }

        setResult(RESULT_OK, new Intent()
                .putExtra("model", model));
        finish();
        overridePendingTransition(R.anim.fade_out, R.anim.fade_out);
    }
}