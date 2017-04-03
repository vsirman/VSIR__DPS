package ride.android.com.dps;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import es.dmoral.toasty.Toasty;

import ride.android.com.dps.util.ColorPaletteUtils;
import ride.android.com.dps.base.ThemeActivity;
import ride.android.com.dps.util.DataCleanManager;
import ride.android.com.dps.util.SharedPreferencesUtils;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


public class perdonal_set extends ThemeActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    Toolbar toolbar;
    private SwitchCompat aSwitch,aSwitch2;
    private int change;
    private SeekBar seekBar;
    private LinearLayout layout;
    private TextView speedText,data;
    private RelativeLayout relativeLayout,relativeLayout2,relativeLayout3,relativeLayout4;
    private BluetoothAdapter mBt;
    private static final int REQUEST_ENABLE_BT = 2;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.personal_set);

        toolbar();
        intView();
        SwitchInit();

    }

    private void intView(){
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        layout = (LinearLayout) findViewById(R.id.speedVlay);
        speedText = (TextView) findViewById(R.id.speedText);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.relativeLayout2);
        data = (TextView) findViewById(R.id.data);
        relativeLayout3 = (RelativeLayout) findViewById(R.id.relativeLayout3);
        relativeLayout4 = (RelativeLayout) findViewById(R.id.relativeLayout4);



        try {
            data.setText(DataCleanManager.getTotalCacheSize(perdonal_set.this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        relativeLayout.setOnClickListener(this);
        relativeLayout2.setOnClickListener(this);
        relativeLayout3.setOnClickListener(this);
        relativeLayout4.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(this);
        int speed = (int) SharedPreferencesUtils.getParam(perdonal_set.this,"int1",60);
        seekBar.setProgress(speed);
    }
    private void SwitchInit(){
        mBt = BluetoothAdapter.getDefaultAdapter();
        aSwitch = (SwitchCompat) findViewById(R.id.switch1);
        aSwitch2 = (SwitchCompat) findViewById(R.id.switch2);
        change = (int) SharedPreferencesUtils.getParam(perdonal_set.this,"int",0);
        if (change == 1){
            aSwitch.setChecked(true);
            layout.setVisibility(View.VISIBLE);
        }else {
            aSwitch.setChecked(false);
            layout.setVisibility(View.GONE);
        }

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    SharedPreferencesUtils.setParam(perdonal_set.this,"boolean",false);
                    SharedPreferencesUtils.setParam(perdonal_set.this,"int",1);
                    layout.setVisibility(View.VISIBLE);
                }else {
                    SharedPreferencesUtils.setParam(perdonal_set.this,"boolean",true);
                    SharedPreferencesUtils.setParam(perdonal_set.this,"int",0);
                    layout.setVisibility(View.GONE);
                }
            }
        });

        if (!mBt.isEnabled()){
            aSwitch2.setChecked(false);

        }else {
            aSwitch2.setChecked(true);
        }
        aSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }else {
                    mBt.disable();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED){
                    aSwitch2.setChecked(false);
                }else if (resultCode == RESULT_OK){
                aSwitch2.setChecked(true);
            }
        }
    }

    public void toolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        assert toolbar != null;
        toolbar.setTitle("个人设置");
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        speedText.setText(i+"Km/h");
        SharedPreferencesUtils.setParam(perdonal_set.this,"int1",i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.relativeLayout:
                DialogShow();
                break;
            case R.id.relativeLayout2:
                DataCleanManager.clearAllCache(this);
                data.setText("0K");
                Toasty.success(perdonal_set.this,"清理完成",Toast.LENGTH_LONG).show();
                break;
            case R.id.relativeLayout3:
                startActivity(new Intent(perdonal_set.this,dateil_some_thing.class));
                break;
            case R.id.relativeLayout4:
                primaryColorPiker();
                break;

        }
    }
    private void DialogShow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("联系我们")
                .setMessage(R.string.textshow)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();
        builder.show();


    }
    private void primaryColorPiker() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, getDialogStyle());

        final View dialogLayout = getLayoutInflater().inflate(R.layout.color_piker_primary, null);
        final LineColorPicker colorPicker = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary);
        final LineColorPicker colorPicker2 = (LineColorPicker) dialogLayout.findViewById(R.id.color_picker_primary_2);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.cp_primary_title);
        CardView dialogCardView = (CardView) dialogLayout.findViewById(R.id.cp_primary_card);
        dialogCardView.setCardBackgroundColor(getCardBackgroundColor());

        colorPicker.setColors(ColorPaletteUtils.getBaseColors(getApplicationContext()));
        for (int i : colorPicker.getColors())
            for (int i2 : ColorPaletteUtils.getColors(getBaseContext(), i))
                if (i2 == getPrimaryColor()) {
                    colorPicker.setSelectedColor(i);
                    colorPicker2.setColors(ColorPaletteUtils.getColors(getBaseContext(), i));
                    colorPicker2.setSelectedColor(i2);
                    break;
                }

        dialogTitle.setBackgroundColor(getPrimaryColor());

        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(c);
                }

                toolbar.setBackgroundColor(c);
                dialogTitle.setBackgroundColor(c);
                colorPicker2.setColors(ColorPaletteUtils.getColors(getApplicationContext(), colorPicker.getColor()));
                colorPicker2.setSelectedColor(colorPicker.getColor());
            }
        });
        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int c) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(c));
                    } else getWindow().setStatusBarColor(c);
                    if (isNavigationBarColored())
                        getWindow().setNavigationBarColor(c);
                    else
                        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000));
                }
                toolbar.setBackgroundColor(c);
                dialogTitle.setBackgroundColor(c);
            }
        });
        dialogBuilder.setView(dialogLayout);

        dialogBuilder.setNeutralButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                }
                toolbar.setBackgroundColor(getPrimaryColor());
                dialog.cancel();
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesUtils.setParam(perdonal_set.this,getString(R.string.primary_darkcolor), colorPicker.getColor());
                SharedPreferencesUtils.setParam(perdonal_set.this,getString(R.string.preference_primary_color),colorPicker2.getColor());
                updateTheme();
                setNavBarColor();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
                    } else {
                        getWindow().setStatusBarColor(getPrimaryColor());
                    }
                }
            }
        });

        dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isTranslucentStatusBar()) {
                        getWindow().setStatusBarColor(ColorPaletteUtils.getObscuredColor(getPrimaryColor()));
                    } else getWindow().setStatusBarColor(getPrimaryColor());
                    }
                toolbar.setBackgroundColor(getPrimaryColor());

            }
        });
        dialogBuilder.show();
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
