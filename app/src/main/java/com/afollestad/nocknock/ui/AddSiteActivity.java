package com.afollestad.nocknock.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
    private TextView textUrlWarning;

    private boolean isClosing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsite);

        rootLayout = findViewById(R.id.rootView);
        inputName = (EditText) findViewById(R.id.inputName);
        inputUrl = (EditText) findViewById(R.id.inputUrl);
        textUrlWarning = (TextView) findViewById(R.id.textUrlWarning);
        inputInterval = (EditText) findViewById(R.id.checkIntervalInput);
        spinnerInterval = (Spinner) findViewById(R.id.checkIntervalSpinner);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> closeActivityWithReveal());

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

        inputUrl.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                final String inputStr = inputUrl.getText().toString().trim();
                if (inputStr.isEmpty()) return;
                final Uri uri = Uri.parse(inputStr);
                if (uri.getScheme() == null) {
                    inputUrl.setText("http://" + inputStr);
                    textUrlWarning.setVisibility(View.GONE);
                } else if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                    textUrlWarning.setVisibility(View.VISIBLE);
                    textUrlWarning.setText(R.string.warning_http_url);
                } else {
                    textUrlWarning.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.doneBtn).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        closeActivityWithReveal();
    }

    private void closeActivityWithReveal() {
        if (isClosing) return;
        isClosing = true;
        final int fabSize = getIntent().getIntExtra("fab_size", toolbar.getMeasuredHeight());
        final int cx = (int) getIntent().getFloatExtra("fab_x", rootLayout.getMeasuredWidth() / 2) + (fabSize / 2);
        final int cy = (int) getIntent().getFloatExtra("fab_y", rootLayout.getMeasuredHeight() / 2) + toolbar.getMeasuredHeight() + (fabSize / 2);
        float initialRadius = Math.max(cx, cy);

        final Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, initialRadius, 0);
        circularReveal.setDuration(300);
        circularReveal.setInterpolator(new AccelerateInterpolator());
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rootLayout.setVisibility(View.INVISIBLE);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        circularReveal.start();
    }

    private void circularRevealActivity() {
        final int cx = rootLayout.getMeasuredWidth() / 2;
        final int cy = rootLayout.getMeasuredHeight() / 2;
        final float finalRadius = Math.max(cx, cy);
        final Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);

        circularReveal.setDuration(300);
        circularReveal.setInterpolator(new DecelerateInterpolator());

        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    // Done button
    @Override
    public void onClick(View view) {
        isClosing = true;

        ServerModel model = new ServerModel();
        model.name = inputName.getText().toString().trim();
        model.url = inputUrl.getText().toString().trim();
        model.status = ServerStatus.WAITING;

        if (model.name.isEmpty()) {
            ((TextInputLayout) inputName.getParent()).setError(getString(R.string.please_enter_name));
            isClosing = false;
            return;
        } else {
            ((TextInputLayout) inputName.getParent()).setError(null);
        }

        if (model.url.isEmpty()) {
            ((TextInputLayout) inputUrl.getParent()).setError(getString(R.string.please_enter_url));
            isClosing = false;
            return;
        } else {
            final TextInputLayout urlTl = (TextInputLayout) inputUrl.getParent();
            urlTl.setError(null);
            if (!Patterns.WEB_URL.matcher(model.url).find()) {
                urlTl.setError(getString(R.string.please_enter_valid_url));
                isClosing = false;
                return;
            } else {
                final Uri uri = Uri.parse(model.url);
                if (uri.getScheme() == null)
                    model.url = "http://" + model.url;
            }
        }

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

        model.lastCheck = System.currentTimeMillis() - model.checkInterval;

        setResult(RESULT_OK, new Intent()
                .putExtra("model", model));
        finish();
        overridePendingTransition(R.anim.fade_out, R.anim.fade_out);
    }
}