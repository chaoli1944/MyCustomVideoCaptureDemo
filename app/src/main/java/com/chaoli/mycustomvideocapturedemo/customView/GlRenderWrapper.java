package com.chaoli.mycustomvideocapturedemo.customView;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.Size;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlRenderWrapper implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera2Helper.OnPreviewSizeListener, Camera2Helper.OnPreviewListener{

    private final String TAG = "GlRenderWrapper";
    private final GlRenderView glRenderView;
    private  Size size;
    private Camera2Helper camera2Helper;
    private int[] mTextures;
    private SurfaceTexture mSurfaceTexture;
    private CameraFilter cameraFilter;
    private ScreenFilter screenFilter;
    private int mPreviewWdith;
    private int mPreviewHeight;
    private int screenSurfaceWid;
    private int screenSurfaceHeight;
    private int screenX;
    private int screenY;
    private EGLContext eglContext;


    public GlRenderWrapper(GlRenderView glRenderView) {
        this.glRenderView = glRenderView;

    }

    public GlRenderWrapper(GlRenderView glRenderView,Size size) {
        this.glRenderView = glRenderView;
        this.size = size;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        camera2Helper = new Camera2Helper((Activity) glRenderView.getContext(),size);

        mTextures = new int[1];
        //创建一个纹理
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        //将纹理和离屏buffer绑定
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        //监听有新图像到来
        mSurfaceTexture.setOnFrameAvailableListener(this);

        //使用fbo 将samplerExternalOES 输入到sampler2D中
        cameraFilter = new CameraFilter(glRenderView.getContext());
        //负责将图像绘制到屏幕上
        screenFilter = new ScreenFilter(glRenderView.getContext());

        eglContext = EGL14.eglGetCurrentContext();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //1080 1899

        camera2Helper.setPreviewSizeListener(this);
        camera2Helper.setOnPreviewListener(this);
        //打开相机
        camera2Helper.openCamera(width, height, mSurfaceTexture);



        float scaleX = (float) mPreviewHeight / (float) width;
        float scaleY = (float) mPreviewWdith / (float) height;

        float max = Math.max(scaleX, scaleY);

        screenSurfaceWid = (int) (mPreviewHeight / max);
        screenSurfaceHeight = (int) (mPreviewWdith / max);
        screenX = width - (int) (mPreviewHeight / max);
        screenY = height - (int) (mPreviewWdith / max);

       //prepare 传如 绘制到屏幕上的宽 高 起始点的X坐标 起使点的Y坐标
        cameraFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);
        screenFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);

    }
    private float[] mtx = new float[16];
    @Override
    public void onDrawFrame(GL10 gl) {
        int textureId;
        // 配置屏幕
        //清理屏幕 :告诉opengl 需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //更新获取一张图
        mSurfaceTexture.updateTexImage();

        mSurfaceTexture.getTransformMatrix(mtx);
        //cameraFiler需要一个矩阵，是Surface和我们手机屏幕的一个坐标之间的关系
        cameraFilter.setMatrix(mtx);

        textureId = cameraFilter.onDrawFrame(mTextures[0]);

        int id = screenFilter.onDrawFrame(textureId);

        onPreviewListener.onTexture(id, eglContext,mPreviewWdith,mPreviewHeight);
    }

    OnDrawFrameListener onPreviewListener;

    public void setOnDrawFrameListener(OnDrawFrameListener onPreviewListener) {
        this.onPreviewListener = onPreviewListener;
    }

    public interface OnDrawFrameListener {
        void onTexture(int textureId, EGLContext eglContext,int mPreviewWdith,int mPreviewHeight);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glRenderView.requestRender();
    }

    @Override
    public void onSize(int width, int height) {
        mPreviewWdith = width;
        mPreviewHeight = height;
    }

    @Override
    public void onPreviewFrame(Image image) {
        //todo
    }
}
