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

import com.afollestad.inquiry.Inquiry;
import com.afollestad.nocknock.R;
import com.afollestad.nocknock.adapter.ServerAdapter;
import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.dialogs.AboutDialog;
import com.afollestad.nocknock.util.MathUtil;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private final static int ADD_SITE_RQ = 6969;
    private final static String SITES_TABLE_NAME = "sites";

    private FloatingActionButton mFab;
    private RecyclerView mList;
    private ServerAdapter mAdapter;
    private TextView mEmptyText;
    private SwipeRefreshLayout mRefreshLayout;

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

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.md_green),
                ContextCompat.getColor(this, R.color.md_yellow),
                ContextCompat.getColor(this, R.color.md_red));

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        Inquiry.init(this, "nocknock", 1);
        refreshModels();
    }

    private void refreshModels() {
        mEmptyText.setVisibility(View.GONE);
        Inquiry.get()
                .selectFrom(SITES_TABLE_NAME, ServerModel.class)
                .all(this::setModels);
    }

    private void setModels(ServerModel[] models) {
        mAdapter.set(models);
        mEmptyText.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            Inquiry.deinit();
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
        mRefreshLayout.setRefreshing(false);
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
                startActivityForResult(new Intent(MainActivity.this, AddSiteActivity.class)
                        .putExtra("fab_x", mOrigFabX)
                        .putExtra("fab_y", mOrigFabY)
                        .putExtra("fab_size", mFab.getMeasuredWidth())
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), ADD_SITE_RQ);
                mFab.postDelayed(() -> {
                    mFab.setX(mOrigFabX);
                    mFab.setY(mOrigFabY);
                }, 600);
            }
        });
        mFabAnimator.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ServerModel model = (ServerModel) data.getSerializableExtra("model");
            mAdapter.add(model);
            mEmptyText.setVisibility(View.GONE);

            Inquiry.get().insertInto(SITES_TABLE_NAME, ServerModel.class)
                    .values(model)
                    .run(changed -> {
                        //TODO?
                    });
        }
    }
}