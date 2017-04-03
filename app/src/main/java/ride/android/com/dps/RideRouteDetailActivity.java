package ride.android.com.dps;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.route.RidePath;

import ride.android.com.dps.adapter.RideSegmentListAdapter;
import ride.android.com.dps.base.ThemeActivity;
import ride.android.com.dps.util.AMapUtil;
import ride.android.com.dps.util.ColorPaletteUtils;


public class RideRouteDetailActivity extends ThemeActivity {
	private RidePath mRidePath;
	private TextView mTitle,mTitleWalkRoute;
	private ListView mRideSegmentList;
	private RideSegmentListAdapter mRideSegmentListAdapter;
	Toolbar toolbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);
		getIntentData();
		toolbar();
		mTitleWalkRoute = (TextView) findViewById(R.id.firstline);
		String dur = AMapUtil.getFriendlyTime((int) mRidePath.getDuration());
		String dis = AMapUtil
				.getFriendlyLength((int) mRidePath.getDistance());
		mTitleWalkRoute.setText(dur + "(" + dis + ")");
		mRideSegmentList = (ListView) findViewById(R.id.bus_segment_list);
		mRideSegmentListAdapter = new RideSegmentListAdapter(
				this.getApplicationContext(), mRidePath.getSteps());
		mRideSegmentList.setAdapter(mRideSegmentListAdapter);

	}

	private void getIntentData() {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		mRidePath = intent.getParcelableExtra("ride_path");
	}



	public void toolbar(){
		toolbar = (Toolbar) findViewById(R.id.toolbar);

		assert toolbar != null;
		toolbar.setTitle("骑行详细路线");
		toolbar.setTitleTextColor(getResources().getColor(R.color.white));
		setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	private void updateNowTheme() {
		toolbar.setPopupTheme(getPopupToolbarStyle());
		toolbar.setBackgroundColor(getPrimaryColor());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (isTranslucentStatusBar()) {
				getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
			} else {
				getWindow().setStatusBarColor(getPrimaryColor());
			}
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		updateNowTheme();
	}
}
