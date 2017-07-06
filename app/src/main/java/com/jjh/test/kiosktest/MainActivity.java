package com.jjh.test.kiosktest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Timer autoBrightTimer;
    private TimerTask autoBrightTimerTask;

    LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        findViewById(R.id.btnMaxBright).setOnClickListener(this);
        findViewById(R.id.btnMinBright).setOnClickListener(this);
        findViewById(R.id.btnAutoBrightOn).setOnClickListener(this);
        findViewById(R.id.btnAutoBrightOff).setOnClickListener(this);
        findViewById(R.id.btnWifiOn).setOnClickListener(this);
        findViewById(R.id.btnWifiOff).setOnClickListener(this);
        findViewById(R.id.btnProgress).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!checkSystemWritePermission()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Permission")
                        .setMessage("This application need to have System Write Settings to change brightness for saving device battery.\n Do you want to set permission?")
                        .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if(autoBrightTimer != null) {
            setSystemBrightness(255);
            resetAutoBrightTimerTask();
        }
    }

    @Override
    public void onClick(View v) {
        String msg = "";
        switch(v.getId()){
            case R.id.btnMaxBright:
                msg = "Set system brightness to max!";
                setSystemBrightness(255);
                break;
            case R.id.btnMinBright:
                msg = "Set system brightness to min!";
                setSystemBrightness(0);
                break;
            case R.id.btnAutoBrightOn:
                msg = "Set auto system brightness!";
                autoBrightTimer = new Timer();
                resetAutoBrightTimerTask();
                break;
            case R.id.btnAutoBrightOff:
                msg = "Unset auto system brightness!";
                removeAutoBrightTimerTask();
                autoBrightTimer = null;
                break;
            case R.id.btnWifiOn:
                msg = "Set Wifi ON!";
                setWifiEnabled(true);
                break;
            case R.id.btnWifiOff:
                msg = "Set Wifi OFF";
                setWifiEnabled(false);
                break;
            case R.id.btnProgress:
                if(mDialog == null) mDialog = new LoadingDialog();
                mDialog.show(getSupportFragmentManager(), "loadingView");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                            }
                        });
                    }
                }).start();
                msg = "Show Progress";
                break;
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void resetAutoBrightTimerTask(){
        removeAutoBrightTimerTask();
        autoBrightTimerTask = new TimerTask() {
            @Override
            public void run() {
                setSystemBrightness(0);
            }
        };
        autoBrightTimer.schedule(autoBrightTimerTask, 5 * 1000);
    }

    private void removeAutoBrightTimerTask(){
        if(autoBrightTimerTask != null) {
            autoBrightTimerTask.cancel();
        }
        if(autoBrightTimer != null) {
            autoBrightTimer.purge();
        }
    }

    private void setSystemBrightness(int brightness){
        if(checkSystemWritePermission()) {
            ContentResolver cResolver = getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }

    private boolean checkSystemWritePermission() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasPermission = Settings.System.canWrite(this);
        }else{
            Toast.makeText(this,"Need permission to set setting!",Toast.LENGTH_SHORT).show();
        }
        return hasPermission;
    }

    private void setWifiEnabled(boolean isEnabled){
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(isEnabled);

        if(isEnabled && ((CheckBox)findViewById(R.id.cbAutoConnect)).isChecked()){
            String networkSSID = "5_hicare_5G";
            String networkPwd = "hicare12345";

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            /* for WEP
            conf.wepKeys[0] = "\"" + networkPwd + "\"";
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);*/
            //for WAP
            conf.preSharedKey = "\"" + networkPwd + "\"";
            //for Open
            //conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiManager.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for(WifiConfiguration wc : list){
                if(wc.SSID != null && wc.SSID.equals("\"" + networkSSID + "\"")){
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(wc.networkId, true);
                    wifiManager.reconnect();
                    break;
                }
            }
        }
    }
}
