package com.chaoli.mycustomvideocapturedemo.YUV;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class AutoFitTextureView extends TextureView {

    private int ratioW = 0;
    private int ratioH = 0;

    public AutoFitTextureView(Context context) {
        super(context);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置宽高比
     * @param width
     * @param height
     */
    public void setAspectRation(int width, int height){
        if (width < 0 || height < 0){
            throw new IllegalArgumentException("width or height can not be negative.");
        }
        //相机输出尺寸宽高默认是横向的，屏幕是竖向时需要反转
        // （后续适配屏幕旋转时会有更好的方案，这里先这样）
        ratioW = height;
        ratioH = width;
        //请求重新布局
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (0 == ratioW || 0 == ratioH){
            //未设定宽高比，使用预览窗口默认宽高
            setMeasuredDimension(width, height);
        }else {
            //设定宽高比，调整预览窗口大小（调整后窗口大小不超过默认值）
            if (width < height * ratioW / ratioH){
                setMeasuredDimension(width, width * ratioH / ratioW);
            }else {
                setMeasuredDimension(height * ratioW / ratioH, height);
            }
        }

    }
}
