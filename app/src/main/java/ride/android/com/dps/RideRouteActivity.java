package ride.android.com.dps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.RideRouteQuery;
import com.amap.api.services.route.WalkRouteResult;

import es.dmoral.toasty.Toasty;
import ride.android.com.dps.base.ThemeActivity;
import ride.android.com.dps.overlay.RideRouteOverlay;
import ride.android.com.dps.util.AMapUtil;
import ride.android.com.dps.util.ColorPaletteUtils;
import ride.android.com.dps.util.ToastUtil;



public class RideRouteActivity extends ThemeActivity implements OnMapClickListener,
        OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter, OnRouteSearchListener, OnClickListener {
    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private RideRouteResult mRideRouteResult;
    private LatLonPoint mStartPoint;
    private LatLonPoint mEndPoint;
    private final int ROUTE_TYPE_RIDE = 4;

    private RelativeLayout mBottomLayout;
    private TextView mRotueTimeDes,textView;
    private Button mStartNaiv;
    private ProgressDialog progDialog = null;
    private double endLatitude,endLongitude,startLatitude,startLongitude;
    private ImageView map_traffic;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_calculate_route);

        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(bundle);
        getIntentData();
        init();
        setfromandtoMarker();
        searchRouteResult(ROUTE_TYPE_RIDE, RouteSearch.RidingDefault);

    }


    private void getIntentData(){
        Bundle bundle = this.getIntent().getExtras();
        endLatitude =bundle.getDouble("endLatitude");
        endLongitude =bundle.getDouble("endLongitude");
        startLatitude =bundle.getDouble("startJ");
        startLongitude =bundle.getDouble("startL");
        mEndPoint = new LatLonPoint(endLatitude,endLongitude);
        mStartPoint = new LatLonPoint(startLatitude,startLongitude);
    }

    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }


    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mStartNaiv = (Button) findViewById(R.id.calculate_route_start_navi);
        mStartNaiv.setOnClickListener(this);
        aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM);
        map_traffic = (ImageView) findViewById(R.id.map_traffic);
        map_traffic.setOnClickListener(this);
        textView = (TextView) findViewById(R.id.tv_datel);
    }



    private void registerListener() {
        aMap.setOnMapClickListener(RideRouteActivity.this);
        aMap.setOnMarkerClickListener(RideRouteActivity.this);
        aMap.setOnInfoWindowClickListener(RideRouteActivity.this);
        aMap.setInfoWindowAdapter(RideRouteActivity.this);

    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }


    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            Toasty.info(mContext, "定位中，稍后再试...", Toast.LENGTH_LONG,true).show();
            return;
        }
        if (mEndPoint == null) {
            Toasty.warning(mContext, "未设置终点", Toast.LENGTH_LONG,true).show();

        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_RIDE) {// 骑行路径规划
            RideRouteQuery query = new RideRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateRideRouteAsyn(query);// 异步路径规划骑行模式查询
        }
    }


    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }


    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        updateNowTheme();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mRideRouteResult = result;
                    final RidePath ridePath = mRideRouteResult.getPaths()
                            .get(0);
                    RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                            this, aMap, ridePath,
                            mRideRouteResult.getStartPos(),
                            mRideRouteResult.getTargetPos());
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap();
                    rideRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) ridePath.getDistance();
                    int dur = (int) ridePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    mRotueTimeDes.setText(des);
                    mBottomLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    RideRouteDetailActivity.class);
                            intent.putExtra("ride_path", ridePath);
                            intent.putExtra("ride_result",
                                    mRideRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result.getPaths() == null) {
                    ToastUtil.show(mContext, "未找到相关数据");
                }
            } else {
                ToastUtil.show(mContext, "未找到相关数据");
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.calculate_route_start_navi:
                startAMapNavi();
                finish();
                break;
            case R.id.map_traffic:
                setTraffic();
                break;
        }
    }


    private void startAMapNavi() {

        Intent intent = new Intent(this, RouteNaviActivity.class);
        intent.putExtra("start", new NaviLatLng(startLatitude,startLongitude));
        intent.putExtra("end", new NaviLatLng(endLatitude, endLongitude));
        startActivity(intent);
    }

    private void setTraffic() {
        if (aMap.isTrafficEnabled()) {
            map_traffic.setImageResource(R.mipmap.map_traffic_white);
            aMap.setTrafficEnabled(false);
        } else {
            map_traffic.setImageResource(R.mipmap.map_traffic_hl_white);
            aMap.setTrafficEnabled(true);
        }
    }
    private void updateNowTheme() {


        mStartNaiv.setBackgroundColor(getPrimaryColor());
        textView.setTextColor(getPrimaryColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTranslucentStatusBar()) {
                getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
            } else {
                getWindow().setStatusBarColor(getPrimaryColor());
            }
        }
    }


}


