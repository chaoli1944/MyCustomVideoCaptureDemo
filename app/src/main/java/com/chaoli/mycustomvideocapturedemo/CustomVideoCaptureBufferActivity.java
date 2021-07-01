package com.chaoli.mycustomvideocapturedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chaoli.mycustomvideocapturedemo.YUV.YUVUtils;
import com.chaoli.mycustomvideocapturedemo.customView.Camera2Helper;
import com.chaoli.mycustomvideocapturedemo.debug.GenerateTestUserSig;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.trtc.TRTCCloudDef.TRTCRoleAnchor;
import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_LIVE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_I420;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_NV21;

public class CustomVideoCaptureBufferActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, Camera2Helper.OnPreviewListener {

    private String mUserId;
    private String mRoomId;
    private TRTCCloud mTRTCCloud;
    private TRTCCloudImplListener mTRTCCloudListener;
    private TRTCCloudDef.TRTCParams trtcParams;
    private TRTCCloudDef.TRTCVideoFrame frame;
    private List<String> mRemoteUidList;             // 远端用户Id列表
    private List<TXCloudVideoView> mRemoteViewList;            // 远端画面列表
    private TextView mShowYUVInfo;
    private TextureView textureView;
    private Camera2Helper mCamera2Helper;
    private List<Size> preViewList = new ArrayList<>();
    private boolean isPushFlag = true;
    private StringBuffer imageReaderSB;
    private TextView trtc_tv_room_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_video_capture_buffer);
        handleIntent();
        initView();
        initData();
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

    private void initView() {

        mShowYUVInfo = findViewById(R.id.tv_showYUVInfo);

        textureView = findViewById(R.id.textureView);

        textureView.setSurfaceTextureListener(this);

        trtc_tv_room_number = findViewById(R.id.trtc_tv_room_number);
        trtc_tv_room_number.setText(mRoomId);

        mRemoteUidList = new ArrayList<>();
        mRemoteViewList = new ArrayList<>();
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_1));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_2));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_3));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_4));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_5));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_6));
    }

    private void initData() {
        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloudListener = new CustomVideoCaptureBufferActivity.TRTCCloudImplListener(this);
        mTRTCCloud.setListener(mTRTCCloudListener);

        imageReaderSB = new StringBuffer();
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
        frame.bufferType = TRTC_VIDEO_BUFFER_TYPE_BYTE_ARRAY;


        mTRTCCloud.startLocalAudio();
        mTRTCCloud.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_DISABLE);
        mTRTCCloud.setVideoEncoderRotation(TRTCCloudDef.TRTC_VIDEO_ROTATION_270);
        mTRTCCloud.enterRoom(trtcParams, TRTC_APP_SCENE_LIVE);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        preViewList.add(new Size(720, 960));
        preViewList.add(new Size(480, 640));
        preViewList.add(new Size(360, 480));
        preViewList.add(new Size(240, 320));
        Size previewSize = preViewList.get(1);
        mCamera2Helper = new Camera2Helper(CustomVideoCaptureBufferActivity.this, previewSize);
        SurfaceTexture texture = textureView.getSurfaceTexture();
        mCamera2Helper.openCamera(previewSize.getWidth(), previewSize.getWidth(), texture);
        mCamera2Helper.setOnPreviewListener(this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    @Override
    public void onPreviewFrame(Image image) {

        try {
            String imageDataType = YUVUtils.isYUV420PorYUV420SP(image);
            boolean imageFormatSupported = YUVUtils.isImageFormatSupported(image);
            if (imageDataType.equals("I420")) {
                frame.pixelFormat = TRTC_VIDEO_PIXEL_FORMAT_I420;
                frame.data = YUVUtils.getI420DataFromImage(image);
            } else if (imageDataType.equals("NV21")) {
                frame.pixelFormat = TRTC_VIDEO_PIXEL_FORMAT_NV21;
                frame.data = YUVUtils.getNV21DataFromImage(image);
            }


            frame.width = image.getWidth();
            frame.height = image.getHeight();

            mTRTCCloud.sendCustomVideoData(frame);


            if (isPushFlag) {
                int format = image.getFormat();
                Image.Plane[] planes = image.getPlanes();

                imageReaderSB.append(imageDataType+ "\n" +":imageFormat " + format + "\n" + "\n");

                for (int i = 0; i < planes.length; i++) {
                    ByteBuffer buffer = planes[i].getBuffer();

                    imageReaderSB.append("pixelStride  " + planes[i].getPixelStride() + "\n"
                            + "rowStride   " + planes[i].getRowStride() + "\n"
                            + "width  " + image.getWidth() + "\n"
                            + "height  " + image.getHeight() + "\n"
                            + "buffer size  " + buffer.remaining() + "\n"
                            + "\n");

                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mShowYUVInfo.setText(imageReaderSB);
                    }
                });

                isPushFlag = false;
            }




        } catch (Exception e) {
            Log.e("chaoli", "onImageAvailable: ", e);
        }

    }

    private class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<CustomVideoCaptureBufferActivity> mContext;

        public TRTCCloudImplListener(CustomVideoCaptureBufferActivity activity) {
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
            CustomVideoCaptureBufferActivity activity = mContext.get();
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
    protected void onDestroy() {
        super.onDestroy();
        mTRTCCloud.exitRoom();

    }


}