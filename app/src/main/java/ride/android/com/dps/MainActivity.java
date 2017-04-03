package ride.android.com.dps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;


import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.linroid.filtermenu.library.FilterMenu;
import com.linroid.filtermenu.library.FilterMenuLayout;



import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import ride.android.com.dps.adapter.SensorEventHelper;
import ride.android.com.dps.base.ThemeActivity;
import ride.android.com.dps.overlay.PoiOverlay;
import ride.android.com.dps.util.ColorPaletteUtils;

import ride.android.com.dps.util.ToastUtil;


public class MainActivity extends ThemeActivity implements OnClickListener,
        OnMarkerClickListener, InfoWindowAdapter,
        OnPoiSearchListener, AMap.OnMapLoadedListener,
        AMap.OnMapClickListener, LocationSource,
         AMapLocationListener {



    private AMap mAMap;
    private String mKeyWords = "";
    private ProgressDialog progDialog = null;

    private MapView mapView;
    private PoiResult poiResult;
    private int currentPage = 0;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private TextView mKeywordsTextView;
    private Marker mPoiMarker;
    private ImageView mCleanKeyWords;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private OnLocationChangedListener mListener;

    private Marker locMarker;
    private Circle ac;
    private Circle c;
    private long start;
    private final Interpolator interpolator1 = new LinearInterpolator();
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();

    private SensorEventHelper mSensorHelper;
    Toolbar toolbar;
    private FilterMenuLayout layout3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCleanKeyWords = (ImageView)findViewById(R.id.clean_keywords);
        mCleanKeyWords.setOnClickListener(this);
        layout3 = (FilterMenuLayout) findViewById(R.id.filter_menu3);
        attachMenu3(layout3);


        mapView = (MapView)findViewById(R.id.map);
        assert mapView != null;
        mapView.onCreate(savedInstanceState);


        init();
        mKeyWords = "";
        toolbar();

    }





    


    private void init() {
        if (mAMap == null) {
            mAMap = mapView.getMap();
            setUpMap();
        }
        mSensorHelper = new SensorEventHelper(this);
        mSensorHelper.registerSensorListener();
        mKeywordsTextView = (TextView) findViewById(R.id.main_keywords);
        assert mKeywordsTextView != null;
        mKeywordsTextView.setOnClickListener(this);

    }

    private FilterMenu attachMenu3(FilterMenuLayout layout) {
        return new FilterMenu.Builder(this)
                .addItem(R.mipmap.personal)
                .addItem(R.mipmap.line)
                .attach(layout)
                .withListener(listener)
                .build();
    }
    private void setUpMap() {
        mAMap.setOnMarkerClickListener(this);
        mAMap.setInfoWindowAdapter(this);
        mAMap.getUiSettings().setRotateGesturesEnabled(false);
        mAMap.setOnMapLoadedListener(this);
        mAMap.setOnMapClickListener(this);
        mAMap.setLocationSource(this);
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);

        mAMap.setMyLocationEnabled(true);
        mAMap.getUiSettings().setScaleControlsEnabled(true);
        mAMap.getUiSettings().setZoomControlsEnabled(false);
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + mKeyWords);
        progDialog.show();
    }

    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    protected void doSearchQuery(String keywords) {
        showProgressDialog();
        currentPage = 0;

        query = new PoiSearch.Query(keywords, "", "");

        query.setPageSize(10);

        query.setPageNum(currentPage);

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        return view;
    }


    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(MainActivity.this, infomation);

    }



    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {
                if (result.getQuery().equals(query)) {
                    poiResult = result;

                    List<PoiItem> poiItems = poiResult.getPois();
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();

                    if (poiItems != null && poiItems.size() > 0) {
                        mAMap.clear();
                        locMarker = null;
                        PoiOverlay poiOverlay = new PoiOverlay(mAMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(MainActivity.this,
                                "对不起，没有搜索到相关数据！");
                    }
                }
            } else {
                ToastUtil.show(MainActivity.this,
                        "对不起，没有搜索到相关数据！");
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem item, int rCode) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6 && data
                != null) {
            mAMap.clear();
           locMarker= null;
            Tip tip = data.getParcelableExtra("ExtraTip");
            if (tip.getPoiID() == null || tip.getPoiID().equals("")) {
                doSearchQuery(tip.getName());
            } else {
                addTipMarker(tip);
            }
            mKeywordsTextView.setText(tip.getName());
            if(!tip.getName().equals("")){
                mCleanKeyWords.setVisibility(View.VISIBLE);
            }
        } else if (resultCode == 7 && data != null) {
            mAMap.clear();
            locMarker = null;
            String keywords = data.getStringExtra("KeyWord");
            if(keywords != null && !keywords.equals("")){
                doSearchQuery(keywords);
            }
            mKeywordsTextView.setText(keywords);
            if(!keywords.equals("")){
                mCleanKeyWords.setVisibility(View.VISIBLE);
            }
        }
    }


    private void addTipMarker(Tip tip) {
        if (tip == null) {
            return;
        }
        mPoiMarker = mAMap.addMarker(new MarkerOptions());
        LatLonPoint point = tip.getPoint();
        if (point != null) {
            LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
            mPoiMarker.setPosition(markerPosition);
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 17));
        }
        mPoiMarker.setTitle(tip.getName());
        mPoiMarker.setSnippet(tip.getAddress());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_keywords:
                Intent intent = new Intent(this, InputTipsActivity.class);
                startActivityForResult(intent, 6);
                break;
            case R.id.clean_keywords:
                mKeywordsTextView.setText("");
                mAMap.clear();
                locMarker = null;
                mCleanKeyWords.setVisibility(View.GONE);
            default:
                break;
        }
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        startlocation();
    }

    @Override
    public void deactivate() {

    }
    private Marker addMarker(LatLng point) {
        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.navi_map_gps_locked);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);
        Marker marker = mAMap.addMarker(new MarkerOptions().position(point).icon(des)
                .anchor(0.5f, 0.5f));
        return marker;
    }
    @Override
    public void onResume() {
        super.onResume();
        updateNowTheme();
        updateTheme();
        mapView.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();

        }

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
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        try {
            mTimer.cancel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (locMarker != null) {
            locMarker.destroy();
        }
        deactivate();
    }

    private void startlocation() {

        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();

            mLocationClient.setLocationListener(this);

            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

            mLocationOption.setOnceLocation(true);

            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } else {
            mLocationClient.startLocation();
        }
    }

    private void addLocationMarker(AMapLocation aMapLocation) {

        LatLng mylocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        float accuracy = aMapLocation.getAccuracy();
        if (locMarker == null) {
            locMarker = addMarker(mylocation);
            ac = mAMap.addCircle(new CircleOptions().center(mylocation)
                    .fillColor(Color.argb(100, 255, 218, 185)).radius(accuracy)
                    .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(5));
            c = mAMap.addCircle(new CircleOptions().center(mylocation)
                    .fillColor(Color.argb(70, 255, 218, 185))
                    .radius(accuracy).strokeColor(Color.argb(255, 255, 228, 185))
                    .strokeWidth(0));
        } else {
            locMarker.setPosition(mylocation);
            ac.setCenter(mylocation);
            ac.setRadius(accuracy);
            c.setCenter(mylocation);
            c.setRadius(accuracy);
        }
        Scalecircle(c);
    }


    public void Scalecircle(final Circle circle) {
        start = SystemClock.uptimeMillis();
        mTimerTask = new circleTask(circle, 1000);
        mTimer.schedule(mTimerTask, 0, 30);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (mListener != null && aMapLocation != null) {
            if (mTimerTask != null) {
                mTimerTask.cancel();
                mTimerTask = null;
            }
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                LatLng mylocation = new LatLng(aMapLocation.getLatitude(),
                        aMapLocation.getLongitude());
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 15));
                addLocationMarker(aMapLocation);
                mSensorHelper.setCurrentMarker(locMarker);
            } else {
                String errText = "";
                if (aMapLocation.getErrorCode() == 2){
                     errText = "定位失败,"+"仅扫描到单个wifi，且没有基站信息,"+"请重新尝试。";
                }else if (aMapLocation.getErrorCode() == 4){
                    errText = "定位失败,"+ "请检查设备网络是否通畅";
                }else if (aMapLocation.getErrorCode() == 5){
                    errText = "定位结果解析失败,"+ "您可以稍后再试，或检查网络连接是否存在异常。";
                }else if (aMapLocation.getErrorCode() == 9){
                    errText = "定位初始化时出现异常,"+ "请重新启动定位";
                }else if (aMapLocation.getErrorCode() == 12){
                    errText = "定位失败,"+ "缺少定位权限"+"请在设备的设置中开启app的定位权限。";
                }else if (aMapLocation.getErrorCode() == 14){
                    errText = "GPS 定位失败，由于设备当前 GPS 状态差。";
                }
                Toasty.error(this,errText,Toast.LENGTH_SHORT, true).show();
                Log.e("AmapErr", errText);
            }
        }
    }







    private class circleTask extends TimerTask {
        private double r;
        private Circle circle;
        private long duration = 1000;

        public circleTask(Circle circle, long rate) {
            this.circle = circle;
            this.r = circle.getRadius();
            if (rate > 0) {
                this.duration = rate;
            }
        }

        @Override
        public void run() {
            try {
                long elapsed = SystemClock.uptimeMillis() - start;
                float input = (float) elapsed / duration;
//                外圈循环缩放
//                float t = interpolator.getInterpolation((float)(input-0.25));//return (float)(Math.sin(2 * mCycles * Math.PI * input))
//                double r1 = (t + 2) * r;
//                外圈放大后消失
                float t = interpolator1.getInterpolation(input);
                double r1 = (t + 1) * r;
                circle.setRadius(r1);
                if (input > 2) {
                    start = SystemClock.uptimeMillis();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    public void toolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert toolbar != null;
        toolbar.setTitle("骑行助手");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

    }



    FilterMenu.OnMenuChangeListener listener = new FilterMenu.OnMenuChangeListener() {
        @Override
        public void onMenuItemClick(View view, int position) {
            switch (position){
                case 0:
                    startActivity(new Intent(MainActivity.this, perdonal_set.class));
                    break;
                case 1:
                    startActivity(new Intent(MainActivity.this,route_set.class));
                    break;
            }
        }

        @Override
        public void onMenuCollapse() {

        }


        @Override
        public void onMenuExpand() {

        }
    };
    private void updateNowTheme(){
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());

        layout3.setPrimaryColor(getPrimaryDarkColor());
        layout3.setPrimaryDarkColor(getPrimaryColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTranslucentStatusBar()) {
                getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
            } else {
                getWindow().setStatusBarColor(getPrimaryColor());
            }
        }


    }

}
