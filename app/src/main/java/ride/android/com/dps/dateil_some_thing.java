package ride.android.com.dps;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ride.android.com.dps.base.ThemeActivity;
import ride.android.com.dps.util.ColorPaletteUtils;

public class dateil_some_thing extends ThemeActivity {

    private CollapsingToolbarLayout cTl;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private AppBarLayout appBarLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dateil_some_thing);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        cTl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        cTl.setTitle("详细功能介绍");
        cTl.setExpandedTitleColor(Color.GRAY);
        cTl.setCollapsedTitleTextColor(Color.WHITE);


        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
               if(state == State.COLLAPSED){

                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
                    } else {
                        getWindow().setStatusBarColor(getPrimaryColor());
                    }
                    //折叠状态
                }
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "我们已收到您的支持，Thank you~~", Snackbar.LENGTH_LONG)
                        .setAction("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        }).show();
            }
        });
    }
    private void updateNowTheme() {
        cTl.setContentScrimColor(getPrimaryColor());
    }
    @Override
    public void onResume() {
        super.onResume();
        updateNowTheme();
    }

    static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public void onStateChanged(AppBarLayout appBarLayout, State state) {

        }
    }
}
