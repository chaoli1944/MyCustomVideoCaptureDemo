package com.chaoli.mycustomvideocapturedemo.customView;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Size;

public class GlRenderView extends GLSurfaceView {

    GlRenderWrapper glRender;
    Size size;

    public GlRenderView(Context context) {
        this(context, null);
    }

    public GlRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public GlRenderWrapper getGlRender() {
        return glRender;
    }

    public void setSize(Size size) {
        this.size = size;

    }

    public void render(){

        setEGLContextClientVersion(2);
        glRender = new GlRenderWrapper(this, size);

        setRenderer(glRender);

        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
