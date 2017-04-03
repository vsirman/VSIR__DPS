package ride.android.com.dps;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import ride.android.com.dps.Dialog.DeviceListActivity;
import ride.android.com.dps.Receiver.BluetoothChatService;
import ride.android.com.dps.util.SharedPreferencesUtils;



public class RouteNaviActivity extends Activity implements AMapNaviListener, AMapNaviViewListener {


    private static final int TIME=1000;
    private static boolean clbz=true;
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private int frsitDistain,nextDistain;
    private int FactionName = 1;
    private int SactionName;

    AMapNaviView mAMapNaviView;
    AMapNavi mAMapNavi;
    boolean mIsGps;
    private TextView tv_show;
    private String mConnectedDeviceName = null;

    private String mConnectedDeviceAddress = null;

    private ArrayAdapter<String> mConversationArrayAdapter;

    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothChatService mChatService = null;

    final Handler handler = new Handler();
    final String[] istain = {null};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.naiv_activity);


        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);


        getNaviParam();
        getText();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



    }

    private void getText(){
        tv_show = (TextView) findViewById(R.id.textShow);
        tv_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clbz=false;
                clearAutoConnect();
                launch();
            }
        });
    }

    private void getNaviParam() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        int speed = (int) SharedPreferencesUtils.getParam(RouteNaviActivity.this,"int1",60);
        mAMapNavi.setEmulatorNaviSpeed(speed);
        mIsGps = (boolean) SharedPreferencesUtils.getParam(RouteNaviActivity.this,"boolean",true);
        NaviLatLng start = intent.getParcelableExtra("start");
        NaviLatLng end = intent.getParcelableExtra("end");
        calculateDriveRoute(start, end);
    }


    private void calculateDriveRoute(NaviLatLng start, NaviLatLng end) {
        int strategyFlag = 0;
        List<NaviLatLng> startList = new ArrayList<NaviLatLng>();

        List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();

        List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
        try {
            strategyFlag = mAMapNavi.strategyConvert(true, false, false, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startList.add(start);
        endList.add(end);
        mAMapNavi.calculateRideRoute(start, end);
    }

    private void setupChat() {
        mChatService = new BluetoothChatService(this, mHandler);
        mOutStringBuffer = new StringBuffer("");
        launch();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }else if (mChatService == null) {
            setupChat();
        }

    }
    @Override
    protected synchronized void onResume() {
        super.onResume();
        mAMapNaviView.onResume();
        if (mChatService != null) {
             if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            DialogShow();
        }
        return true;
    }

    private void DialogShow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("确定退出导航？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RouteNaviActivity.this.finish();
                        if (mChatService != null)
                            mChatService.stop();

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
        builder.show();


    }


    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();

    }

    @Override
    public void finish() {
        super.finish();
        handler2.removeCallbacks(runnable2);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        mAMapNavi.stopNavi();
        clbz=false;
        if (mChatService != null)
        {mChatService.stop();}
        handler.removeCallbacks(runnable);
    }

    private void clearAutoConnect()
    {
        SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
        sharedata.clear();
        sharedata.commit();

    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            clbz=true;
                            SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
                            sharedata.putString(String.valueOf(0),mConnectedDeviceName);
                            sharedata.putString(String.valueOf(1),mConnectedDeviceAddress);
                            sharedata.commit();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;

                case MESSAGE_DEVICE_NAME:

                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    mConnectedDeviceAddress= msg.getData().getString(DEVICE_ADDRESS);
                    Toasty.success(getApplicationContext(), "连接到 "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    tv_show.setText("已连接"+"\n"+mConnectedDeviceName);

                    break;
                case MESSAGE_TOAST:
                    Toasty.warning(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    tv_show.setText(R.string.text_show);
                    handler2.postDelayed(runnable2,TIME);
                    break;
            }
        }
    };
    Handler handler2 = new Handler();
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            if(clbz) {
                launch();
            }

        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    setupChat();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toasty.info(this, "拒绝打开蓝牙设备。", Toast.LENGTH_SHORT).show();
                    if (mChatService != null){
                        mChatService.stop();
                        handler2.removeCallbacks(runnable2);
                    }
                }
        }
    }

    private void launch() {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    @Override
    public void onInitNaviFailure() {
        Toasty.error(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {
    }

    @Override
    public void onStartNavi(int type) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }

    @Override
    public void onGetNavigationText(int type, String text) {

    }

    @Override
    public void onEndEmulatorNavi() {
    }

    @Override
    public void onArriveDestination() {
    }

    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {

    }

    @Override
    public void onArriveDestination(AMapNaviStaticInfo aMapNaviStaticInfo) {

    }

    @Override
    public void onCalculateRouteSuccess() {
        if (mIsGps) {
            mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
        } else {
            mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
        }
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int wayID) {

    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
    }

    @Override
    public void onNaviSetting() {
    }

    @Override
    public void onNaviMapMode(int isLock) {

    }

    @Override
    public void onNaviCancel() {
        finish();
    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {
    }

    @Deprecated
    @Override
    public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {

        frsitDistain = naviinfo.getCurStepRetainDistance();
        SactionName = naviinfo.getIconType();


        boolean change = (boolean) SharedPreferencesUtils.getParam(RouteNaviActivity.this,"change",true);
        if (change) {
            if (SactionName != FactionName) {
                sendMessage(String.valueOf(SactionName));
                FactionName = SactionName;
            }
            handler.postDelayed(runnable, 1000);
        }
        if (SactionName == 15 && frsitDistain == 5) {
            handler.removeCallbacksAndMessages(runnable);
        }
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (frsitDistain<1000&&frsitDistain>=100){
                istain[0] ="v"+"0"+String.valueOf(frsitDistain);
            }else if (frsitDistain<100&&frsitDistain>=10) {
                istain[0] = "v" + "00" + String.valueOf(frsitDistain);
            }else if (frsitDistain<10&&frsitDistain>0){
                istain[0] = "v" + "000" + String.valueOf(frsitDistain);
            }else if (frsitDistain>=1000&&frsitDistain<=10000){
                istain[0] = "v" + String.valueOf(frsitDistain);
            }else if (frsitDistain>10000){
                istain[0] = "v" + String.valueOf(9999);
            }
            sendMessage(istain[0]);

        }
    };


    private void sendMessage(String message) {

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);


        }
    }
    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
    }

    @Override
    public void hideCross() {
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onLockMap(boolean isLock) {
    }

    @Override
    public void onNaviViewLoaded() {
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }




}
