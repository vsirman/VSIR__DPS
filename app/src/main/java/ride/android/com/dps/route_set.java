package ride.android.com.dps;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;

import es.dmoral.toasty.Toasty;
import ride.android.com.dps.base.ThemeActivity;
import ride.android.com.dps.util.AMapUtil;
import ride.android.com.dps.util.ColorPaletteUtils;

import ride.android.com.dps.util.ToastUtil;


public class route_set extends ThemeActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener,AMapLocationListener {



    private TextView tv_start,tv_end;
    private PoiResult poiResult;
    private int currentPage = 0;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private ProgressDialog progDialog = null;
    private String mKeyWords = "";
    private Button bt_colse,bt_change,bt_show_line,bt_go_naiv;
    private Intent intent2;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;


    private double startPointJ,startPointL,startPointJ2,startPointL2;
    private double endPointJ,endPointL;
    private LatLonPoint startpoint,endpoint;
    private Toolbar toolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_set);
        intView();
        toolbar();



    }

    @Override
    protected void onStart() {
        super.onStart();
        startlocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.stopLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNowTheme();
        mLocationClient.startLocation();
    }

    private void intView(){
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_end = (TextView) findViewById(R.id.tv_end);
        bt_colse= (Button) findViewById(R.id.close);
        bt_change = (Button) findViewById(R.id.bt_change);
        bt_show_line = (Button) findViewById(R.id.bt_show_Line);
        bt_go_naiv = (Button) findViewById(R.id.bt_go_naiv);


        tv_end.setOnClickListener(this);
        bt_colse.setOnClickListener(this);
        bt_change.setOnClickListener(this);
        bt_show_line.setOnClickListener(this);
        bt_go_naiv.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_end:
                    Intent intent = new Intent(this, InputTipsActivity.class);
                    startActivityForResult(intent, 1);
                break;
            case R.id.close:
                tv_end.setText("");
                bt_colse.setVisibility(View.INVISIBLE);
                break;
            case R.id.bt_change:
                if (bt_change.getText().toString().equals("默认")){
                    tv_start.setText("当前位置");
                    bt_change.setText("更改");
                }else if (bt_change.getText().toString().equals("更改")){
                    Intent intent1 = new Intent(this, InputTipsActivity.class);
                    startActivityForResult(intent1, 2);

                }
                break;
            case R.id.bt_show_Line:
                if (tv_end.getText().toString().equals("")){
                    Toasty.warning(this,"当前未设置终点",Toast.LENGTH_LONG,true).show();
                }else if(tv_start.getText().toString().equals("当前位置")){
                    getDataIntent();
                }else if (!tv_start.getText().toString().equals("当前位置")){
                    NogetDataIntent2();
                }
                break;
            case R.id.bt_go_naiv:
                if (tv_end.getText().toString().equals("")){
                    Toasty.warning(this,"当前未设置终点",Toast.LENGTH_LONG,true).show();
                }else if(tv_start.getText().toString().equals("当前位置")){
                    startAMapNavi();
                }else if (!tv_start.getText().toString().equals("当前位置")){
                    startAMapNavi2();
                }
                break;
        }
    }

    private void startAMapNavi() {
        Intent intent = new Intent(this, RouteNaviActivity.class);
        LatLonPoint latLng1X = new LatLonPoint(startPointJ, startPointL);
        LatLonPoint latLng2X = new LatLonPoint(endPointJ, endPointL);
        LatLng latLng1 = AMapUtil.convertToLatLng(latLng1X);
        LatLng latLng2 = AMapUtil.convertToLatLng(latLng2X);
        float distance = AMapUtils.calculateLineDistance(latLng1, latLng2);
        if (distance > 80000) //这是80KM,如果大于80km,需要重新确定起点及终点
        {
            Toasty.warning(this, "当前距离大于80km，不建议您骑行", Toast.LENGTH_LONG,true).show();
        }else {
            intent.putExtra("start", new NaviLatLng(startPointJ, startPointL));
            intent.putExtra("end", new NaviLatLng(endPointJ, endPointL));
            startActivity(intent);
        }
    }
    private void startAMapNavi2() {
        Intent intent = new Intent(this, RouteNaviActivity.class);
        LatLonPoint latLng1X = new LatLonPoint(startPointJ2, startPointL2);
        LatLonPoint latLng2X = new LatLonPoint(endPointJ, endPointL);
        LatLng latLng1 = AMapUtil.convertToLatLng(latLng1X);
        LatLng latLng2 = AMapUtil.convertToLatLng(latLng2X);
        float distance = AMapUtils.calculateLineDistance(latLng1, latLng2);
        if (distance > 80000) //这是80KM,如果大于80km,需要重新确定起点及终点
        {
            Toasty.warning(this, "当前距离大于80km，不建议您骑行", Toast.LENGTH_LONG,true).show();
        }else {
            intent.putExtra("gps", false);
            intent.putExtra("start", new NaviLatLng(startPointJ2, startPointL2));
            intent.putExtra("end", new NaviLatLng(endPointJ, endPointL));
            startActivity(intent);
        }
    }

    private void getDataIntent() {
        Intent intent3 = new Intent(this, RideRouteActivity.class);
        Bundle bundle = new Bundle();
        LatLonPoint latLng1X = new LatLonPoint(startPointJ, startPointL);
        LatLonPoint latLng2X = new LatLonPoint(endPointJ, endPointL);
        LatLng latLng1 = AMapUtil.convertToLatLng(latLng1X);
        LatLng latLng2 = AMapUtil.convertToLatLng(latLng2X);
        float distance = AMapUtils.calculateLineDistance(latLng1, latLng2);
        if (distance > 80000) //这是80KM,如果大于80km,需要重新确定起点及终点
        {
            Toasty.warning(this, "当前距离大于80km，不建议您骑行", Toast.LENGTH_LONG,true).show();
        } else {
            bundle.putDouble("endLatitude", endPointJ);
            bundle.putDouble("endLongitude", endPointL);
            bundle.putDouble("startJ", startPointJ);
            bundle.putDouble("startL", startPointL);
            intent3.putExtras(bundle);

            startActivity(intent3);
        }
    }
    private void NogetDataIntent2(){

        Intent intent3  = new Intent(this,RideRouteActivity.class);
        Bundle bundle = new Bundle();
       LatLonPoint latLng1X =new LatLonPoint(startPointJ2,startPointL2);
        LatLonPoint latLng2X =new LatLonPoint(endPointJ,endPointL);
        LatLng latLng1 = AMapUtil.convertToLatLng(latLng1X);
        LatLng latLng2 = AMapUtil.convertToLatLng(latLng2X);
        float distance = AMapUtils.calculateLineDistance(latLng1,latLng2);
        if (distance > 80000){
            Toasty.warning(this, "当前距离大于80km，不建议您骑行", Toast.LENGTH_LONG,true).show();
        }else {
            bundle.putDouble("endLatitude", endPointJ);
            bundle.putDouble("endLongitude", endPointL);
            bundle.putDouble("startJ", startPointJ2);
            bundle.putDouble("startL", startPointL2);
            intent3.putExtras(bundle);
            startActivity(intent3);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data!=null) {
            if (requestCode == 1) {
                Tip tip = data.getParcelableExtra("ExtraTip");
                if (tip.getPoiID() == null || tip.getPoiID().equals("")) {
                    doSearchQuery(tip.getName());
                }else {
                    tv_end.setText(tip.getName());

                    endPointJ = tip.getPoint().getLatitude();
                    endPointL = tip.getPoint().getLongitude();
                    if (!tip.getName().equals("")) {
                        bt_colse.setVisibility(View.VISIBLE);
                        tv_end.setClickable(false);
                    }
                }
            }else if (requestCode == 2) {
                Tip tip1 = data.getParcelableExtra("ExtraTip");

                if (tip1.getPoiID() == null || tip1.getPoiID().equals("")) {
                            doSearchQuery(tip1.getName());
                        }else {
                    tv_start.setText(tip1.getName());
                    bt_change.setText("默认");
                            startPointJ2 = tip1.getPoint().getLatitude();
                            startPointL2 = tip1.getPoint().getLongitude();
                        }
            }


        }
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
                     if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(route_set.this,
                                "该关键词范围太大，没有搜索到准确数据！");
                    }
                }
            } else {
                ToastUtil.show(route_set.this,
                        "该关键词范围太大，没有搜索到准确数据！");
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
       Toasty.info(this,infomation,Toast.LENGTH_LONG,true).show();

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {

                amapLocation.getLocationType();
                startPointJ =  amapLocation.getLatitude();
                startPointL = amapLocation.getLongitude();
                amapLocation.getAccuracy();

            } else {

                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }
    private void startlocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();

           mLocationClient.setLocationListener(this);

            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

            mLocationOption.setOnceLocation(false);
            mLocationOption.setInterval(3000);

            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } else {
            mLocationClient.startLocation();
        }
    }
    public void toolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        assert toolbar != null;
        toolbar.setTitle("起、终点设置");
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void updateNowTheme() {
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());

        bt_change.setTextColor(getPrimaryColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isTranslucentStatusBar()) {
                getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
            } else {
                getWindow().setStatusBarColor(getPrimaryColor());
            }
           }
    }

}
