package com.afollestad.nocknock.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

import com.afollestad.bridge.Bridge;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.nocknock.R;
import com.afollestad.nocknock.adapter.ServerAdapter;
import com.afollestad.nocknock.api.ServerModel;
import com.afollestad.nocknock.dialogs.AboutDialog;
import com.afollestad.nocknock.services.CheckService;
import com.afollestad.nocknock.util.AlarmUtil;
import com.afollestad.nocknock.util.MathUtil;
import com.afollestad.nocknock.views.DividerItemDecoration;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, ServerAdapter.ClickListener {

    private final static int ADD_SITE_RQ = 6969;
    private final static int VIEW_SITE_RQ = 6923;
    public final static String DB_NAME = "nock_nock";
    public final static String SITES_TABLE_NAME = "sites";

    private FloatingActionButton mFab;
    private RecyclerView mList;
    private ServerAdapter mAdapter;
    private TextView mEmptyText;
    private SwipeRefreshLayout mRefreshLayout;

    private ObjectAnimator mFabAnimator;
    private float mOrigFabX;
    private float mOrigFabY;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("MainActivity", "Received " + intent.getAction());
            if (CheckService.ACTION_RUNNING.equals(intent.getAction())) {
                if (mRefreshLayout != null)
                    mRefreshLayout.setRefreshing(false);
            } else {
                final ServerModel model = (ServerModel) intent.getSerializableExtra("model");
                if (mAdapter != null && mList != null && model != null) {
                    mList.post(() -> mAdapter.update(model));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new ServerAdapter(this);
        mEmptyText = (TextView) findViewById(R.id.emptyText);

        mList = (RecyclerView) findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(mAdapter);
        mList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.md_green),
                ContextCompat.getColor(this, R.color.md_yellow),
                ContextCompat.getColor(this, R.color.md_red));

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        Inquiry.init(this, DB_NAME, 1);
        Bridge.config()
                .defaultHeader("User-Agent", getString(R.string.app_name) + " (Android)");
    }

    private void showRefreshTutorial() {
        if (mAdapter.getItemCount() == 0) return;
        final SharedPreferences pr = PreferenceManager.getDefaultSharedPreferences(this);
        if (pr.getBoolean("shown_swipe_refresh_tutorial", false)) return;

        mFab.hide();
        final View tutorialView = findViewById(R.id.swipeRefreshTutorial);
        tutorialView.setVisibility(View.VISIBLE);
        tutorialView.setAlpha(0f);
        tutorialView.animate().cancel();
        tutorialView.animate().setDuration(300).alpha(1f).start();

        findViewById(R.id.understoodBtn).setOnClickListener(view -> {
            view.setOnClickListener(null);
            findViewById(R.id.swipeRefreshTutorial).setVisibility(View.GONE);
            pr.edit().putBoolean("shown_swipe_refresh_tutorial", true).commit();
            mFab.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckService.isAppOpen(this, true);

        try {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(CheckService.ACTION_CHECK_UPDATE);
            filter.addAction(CheckService.ACTION_RUNNING);
            registerReceiver(mReceiver, filter);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        refreshModels();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CheckService.isAppOpen(this, false);

        NotificationManagerCompat.from(this).cancel(CheckService.NOTI_ID);
        try {
            unregisterReceiver(mReceiver);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
        AlarmUtil.setSiteChecks(this, models);
        showRefreshTutorial();
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
        if (CheckService.isRunning(this)) {
            mRefreshLayout.setRefreshing(false);
            return;
        }
        startService(new Intent(this, CheckService.class));
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
        mFabAnimator.setInterpolator(new PathInterpolator(.5f, .5f));
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
            final ServerModel model = (ServerModel) data.getSerializableExtra("model");
            if (requestCode == ADD_SITE_RQ) {
                mAdapter.add(model);
                mEmptyText.setVisibility(View.GONE);
                Inquiry.get().insertInto(SITES_TABLE_NAME, ServerModel.class)
                        .values(model)
                        .run(inserted -> {
                            AlarmUtil.setSiteChecks(MainActivity.this, model);
                            checkSite(MainActivity.this, model);
                        });
            } else if (requestCode == VIEW_SITE_RQ) {
                Inquiry.get()
                        .update(MainActivity.SITES_TABLE_NAME, ServerModel.class)
                        .where("_id = ?", model.id)
                        .values(model)
                        .run(changed -> {
                            mAdapter.update(model);
                            AlarmUtil.setSiteChecks(MainActivity.this, model);
                            checkSite(MainActivity.this, model);
                        });
            }
        }
    }

    public static void removeSite(final Context context, final ServerModel model, final Runnable onRemoved) {
        Inquiry.init(context, DB_NAME, 1);
        new MaterialDialog.Builder(context)
                .title(R.string.remove_site)
                .content(Html.fromHtml(context.getString(R.string.remove_site_prompt, model.name)))
                .positiveText(R.string.remove)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    AlarmUtil.cancelSiteChecks(context, model);
                    final NotificationManagerCompat nm = NotificationManagerCompat.from(context);
                    nm.cancel(model.url, CheckService.NOTI_ID);
                    Inquiry.get()
                            .deleteFrom(SITES_TABLE_NAME, ServerModel.class)
                            .where("_id = ?", model.id)
                            .run();
                    if (onRemoved != null)
                        onRemoved.run();
                }).show();
    }

    public static void checkSite(Context context, ServerModel model) {
        context.startService(new Intent(context, CheckService.class)
                .putExtra(CheckService.MODEL_ID, model.id));
    }

    @Override
    public void onSiteSelected(final int index, final ServerModel model, boolean longClick) {
        if (longClick) {
            new MaterialDialog.Builder(this)
                    .title(R.string.options)
                    .items(R.array.site_long_options)
                    .negativeText(android.R.string.cancel)
                    .itemsCallback((dialog, itemView, which, text) -> {
                        if (which == 0) {
                            checkSite(MainActivity.this, model);
                        } else {
                            removeSite(MainActivity.this, model, () -> mAdapter.remove(index));
                        }
                    }).show();
        } else {
            startActivityForResult(new Intent(this, ViewSiteActivity.class)
                            .putExtra("model", model), VIEW_SITE_RQ,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }
    }
}