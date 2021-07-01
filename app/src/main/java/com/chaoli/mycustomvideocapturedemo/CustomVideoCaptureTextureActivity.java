package com.chaoli.mycustomvideocapturedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaoli.mycustomvideocapturedemo.customView.GlRenderView;
import com.chaoli.mycustomvideocapturedemo.customView.GlRenderWrapper;
import com.chaoli.mycustomvideocapturedemo.debug.GenerateTestUserSig;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


import static com.tencent.trtc.TRTCCloudDef.TRTCRoleAnchor;
import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_LIVE;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE;


public class CustomVideoCaptureTextureActivity extends AppCompatActivity implements GlRenderWrapper.OnDrawFrameListener {

    public GlRenderView mGLSurfaceView;

    private List<String> mRemoteUidList;             // 远端用户Id列表
    private List<TXCloudVideoView> mRemoteViewList;
    private List<Size> preViewList = new ArrayList<>();
    private TRTCCloud mTRTCCloud;
    private TRTCCloudDef.TRTCParams trtcParams;
    private String mUserId;
    private String mRoomId;
    private TRTCCloudImplListener mTRTCCloudListener;
    private TRTCCloudDef.TRTCVideoFrame frame;
    private TextView trtc_tv_room_number;
    private TextView tv_showInfo;
    private StringBuffer tvShowInfo = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_video_capture_texture);
        handleIntent();
        initView();
        initData();
    }

    private void initData() {
        mGLSurfaceView.getGlRender().setOnDrawFrameListener(this);

        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloudListener = new CustomVideoCaptureTextureActivity.TRTCCloudImplListener(this);
        mTRTCCloud.setListener(mTRTCCloudListener);


        trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        trtcParams.userId = mUserId;
        trtcParams.roomId = Integer.parseInt(mRoomId);

        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);
        trtcParams.role = TRTCRoleAnchor;

        // 打开自定义采集
        mTRTCCloud.enableCustomVideoCapture(true);

        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_480;
        encParam.videoFps = 20;
        encParam.videoBitrate = 1500;
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
//        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE;
        mTRTCCloud.setVideoEncoderParam(encParam);

        frame = new TRTCCloudDef.TRTCVideoFrame();
//        frame.pixelFormat = TRTC_VIDEO_PIXEL_FORMAT_I420;
//        frame.pixelFormat = TRTC_VIDEO_PIXEL_FORMAT_NV21;
//        frame.bufferType = TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY;


        mTRTCCloud.startLocalAudio();
        mTRTCCloud.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_DISABLE);
        mTRTCCloud.enterRoom(trtcParams, TRTC_APP_SCENE_LIVE);
    }

    private void initView() {
        mGLSurfaceView = findViewById(R.id.mGLSurfaceView);
        //4：3比例预览
        preViewList.add(new Size(960, 1280));
        preViewList.add(new Size(720, 960));
        preViewList.add(new Size(480, 640));

        mGLSurfaceView.setSize(preViewList.get(1));
        mGLSurfaceView.render();

        tv_showInfo = findViewById(R.id.tv_showInfo2);
        trtc_tv_room_number = findViewById(R.id.trtc_tv_room_number2);
        trtc_tv_room_number.setText(mRoomId);


        mRemoteUidList = new ArrayList<>();
        mRemoteViewList = new ArrayList<>();
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_12));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_22));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_32));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_42));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_52));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_62));
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            if (intent.getStringExtra("user_id") != null) {
                mUserId = intent.getStringExtra("user_id");
            }
            if (intent.getStringExtra("room_id") != null) {
                mRoomId = intent.getStringExtra("room_id");
            }
        }
    }

    private class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<CustomVideoCaptureTextureActivity> mContext;

        public TRTCCloudImplListener(CustomVideoCaptureTextureActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {

            int index = mRemoteUidList.indexOf(userId);
            if (available) {
                if (index != -1) { //如果mRemoteUidList有，就不重复添加
                    return;
                }
                mRemoteUidList.add(userId);
                refreshRemoteVideoViews();
            } else {
                if (index == -1) { //如果mRemoteUidList没有，说明已关闭画面
                    return;
                }
                /// 关闭用户userId的视频画面
                mTRTCCloud.stopRemoteView(userId);
                mRemoteUidList.remove(index);
                refreshRemoteVideoViews();
            }

        }

        private void refreshRemoteVideoViews() {
            for (int i = 0; i < mRemoteViewList.size(); i++) {
                if (i < mRemoteUidList.size()) {
                    String remoteUid = mRemoteUidList.get(i);
                    mRemoteViewList.get(i).setVisibility(View.VISIBLE);
                    // 开始显示用户userId的视频画面
                    mTRTCCloud.setRemoteViewFillMode(remoteUid, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);

                    mTRTCCloud.startRemoteView(remoteUid, mRemoteViewList.get(i));
                } else {
                    mRemoteViewList.get(i).setVisibility(View.GONE);
                }
            }
        }

        // 错误通知监听，错误通知意味着 SDK 不能继续运行
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
//            Log.d("chaoli", "sdk callback onError");
            CustomVideoCaptureTextureActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }
    }

    /**
     * 离开通话
     */
    private void exitRoom() {
        mTRTCCloud.stopLocalAudio();
//        mTRTCCloud.stopLocalPreview();
        mTRTCCloud.exitRoom();
        //销毁 trtc 实例
        if (mTRTCCloud != null) {
            mTRTCCloud.setListener(null);
        }
        mTRTCCloud = null;
        TRTCCloud.destroySharedInstance();
    }


    @Override
    public void onTexture(int textureId, android.opengl.EGLContext eglContext, int width, int height) {

        frame.texture = new TRTCCloudDef.TRTCTexture();
        frame.bufferType = TRTC_VIDEO_BUFFER_TYPE_TEXTURE;
        frame.timestamp = 0;
        frame.texture.textureId = textureId;
        frame.texture.eglContext14 = eglContext;
        frame.width = height;
        frame.height = width;
        frame.pixelFormat = TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D;
        mTRTCCloud.sendCustomVideoData(frame);

        if (tvShowInfo.length() == 0) {
            tvShowInfo.append("textureId  " + textureId + "\n"
                    + "width  " + height + "\n"
                    + "height  " + width + "\n"
                    + "Texture_2D  " + "\n"
                    + "EGLContext14  " + "\n");
        }
        showInfo(tvShowInfo);

    }

    private void showInfo(final StringBuffer tvShowInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_showInfo.setText(tvShowInfo);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitRoom();
    }
}