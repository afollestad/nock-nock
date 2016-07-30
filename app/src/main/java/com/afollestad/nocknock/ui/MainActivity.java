package com.afollestad.nocknock.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

import com.afollestad.nocknock.R;
import com.afollestad.nocknock.adapter.ServerAdapter;
import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.api.ServerStatus;
import com.afollestad.nocknock.dialogs.AboutDialog;
import com.afollestad.nocknock.util.MathUtil;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private FloatingActionButton mFab;
    private RecyclerView mList;
    private ServerAdapter mAdapter;
    private TextView mEmptyText;

    private ObjectAnimator mFabAnimator;
    private float mOrigFabX;
    private float mOrigFabY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new ServerAdapter();
        mEmptyText = (TextView) findViewById(R.id.emptyText);

        mList = (RecyclerView) findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(mAdapter);

        SwipeRefreshLayout sr = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        sr.setOnRefreshListener(this);
        sr.setColorSchemeColors(ContextCompat.getColor(this, R.color.md_green),
                ContextCompat.getColor(this, R.color.md_yellow),
                ContextCompat.getColor(this, R.color.md_red));

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        ServerModel model = new ServerModel();
        model.name = "Personal Site";
        model.url = "https://aidanfollestad.com";
        model.id = 1;
        model.status = ServerStatus.OK;
        model.checkInterval = 1000 * 60;
        model.lastCheck = System.currentTimeMillis();
        mAdapter.add(model);

        model = new ServerModel();
        model.name = "Polar Request Manager";
        model.url = "https://polar.aidanfollestad.com";
        model.id = 2;
        model.status = ServerStatus.CHECKING;
        model.checkInterval = 1000 * 60 * 2;
        model.lastCheck = System.currentTimeMillis();
        mAdapter.add(model);

        mEmptyText.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            AboutDialog.show(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        // TODO check all servers in order
    }

    // FAB clicked
    @Override
    public void onClick(View view) {
        mOrigFabX = mFab.getX();
        mOrigFabY = mFab.getY();
        final Path curve = MathUtil.bezierCurve(mFab, mList);
        if (mFabAnimator != null)
            mFabAnimator.cancel();
        mFabAnimator = ObjectAnimator.ofFloat(view, View.X, View.Y, curve);
        mFabAnimator.setInterpolator(new PathInterpolator(0.4f, 0.4f, 1, 1));
        mFabAnimator.setDuration(300);
        mFabAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(new Intent(MainActivity.this, AddSiteActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                mFab.postDelayed(() -> {
                    mFab.setX(mOrigFabX);
                    mFab.setY(mOrigFabY);
                }, 600);
            }
        });
        mFabAnimator.start();
    }
}