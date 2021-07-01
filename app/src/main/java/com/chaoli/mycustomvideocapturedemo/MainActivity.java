package com.chaoli.mycustomvideocapturedemo;


import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.rtmp.TXLiveBase;

import java.util.ArrayList;
import java.util.List;






public class MainActivity extends Activity {

    private EditText mInputUserId;
    private EditText mInputRoomId;
    private TextView tv_sdkVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initData();

        checkPermission();

    }

    private void initView() {
        mInputUserId = findViewById(R.id.et_input_username);
        mInputRoomId = findViewById(R.id.et_input_room_id);
        tv_sdkVersion = findViewById(R.id.tv_sdkVersion);
        findViewById(R.id.bt_enter_room_buffer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startEnterRoom(1);
            }
        });
        findViewById(R.id.bt_enter_room_texture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startEnterRoom(2);
            }
        });
        findViewById(R.id.rtc_entrance_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInput(); // 点击非EditText输入区域，隐藏键盘
            }
        });

    }

    private void initData() {
        mInputRoomId.setText("1256732");
        String time = String.valueOf(System.currentTimeMillis());
        String userId = time.substring(time.length() - 8);
        mInputUserId.setText(userId);
        String sdkVersionStr = TXLiveBase.getSDKVersionStr();

        tv_sdkVersion.setText("版本号：" + sdkVersionStr);

    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)) {
                permissions.add(Manifest.permission.WRITE_SETTINGS);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        permissions.toArray(new String[0]),
                        0x1000);
                return false;
            }
        }
        return true;
    }

    private void startEnterRoom(int w) {
        if (TextUtils.isEmpty(mInputUserId.getText().toString().trim())
                || TextUtils.isEmpty(mInputRoomId.getText().toString().trim())) {
            Toast.makeText(MainActivity.this, "房间号和用户名不能为空", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = null;
        if(w==1){
            intent = new Intent(MainActivity.this, CustomVideoCaptureBufferActivity.class);
        }else {
            intent = new Intent(MainActivity.this, CustomVideoCaptureTextureActivity.class);
        }
//        Intent intent = new Intent(MainActivity.this, CustomVideoCaptureTextureActivity.class);
        intent.putExtra("room_id", mInputRoomId.getText().toString().trim());
        intent.putExtra("user_id", mInputUserId.getText().toString().trim());

        startActivity(intent);
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

}