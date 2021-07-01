package com.chaoli.mycustomvideocapturedemo.YUV;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;

import androidx.annotation.RequiresApi;

public class YUVUtils {


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String isYUV420PorYUV420SP(Image image) {
        Image.Plane[] planes = image.getPlanes();
        int pixelStride = planes[1].getPixelStride();
        String imageDataType = pixelStride == 1 ? "I420" : "NV21";

        return imageDataType;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static byte[] getNV21DataFromImage(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * 3 / 2];

        // Y-buffer
        ByteBuffer yBuffer = planes[0].getBuffer();
        int ySize = yBuffer.remaining();


        // V-buffer
        ByteBuffer vBuffer = planes[2].getBuffer();
        int vSize = vBuffer.remaining();


        yBuffer.get(data, 0, ySize);

        vBuffer.get(data, ySize, vSize);


        return data;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static byte[] getI420DataFromImage(Image image) {

        Image.Plane[] planes = image.getPlanes();
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] yBytes = new byte[width * height];
        byte[] uBytes = new byte[width * height / 4];
        byte[] vBytes = new byte[width * height / 4];
        byte[] i420 = new byte[width * height * 3 / 2];


        for (int i = 0; i < planes.length; i++) {
            int dstIndex = 0;
            int uIndex = 0;
            int vIndex = 0;
            int pixelStride = planes[i].getPixelStride();
            int rowStride = planes[i].getRowStride();

            ByteBuffer buffer = planes[i].getBuffer();

            byte[] bytes = new byte[buffer.capacity()];

            buffer.get(bytes);
            int srcIndex = 0;
            if (i == 0) {
                for (int j = 0; j < height; j++) {
                    System.arraycopy(bytes, srcIndex, yBytes, dstIndex, width);
                    srcIndex += rowStride;
                    dstIndex += width;
                }
            } else if (i == 1) {
                for (int j = 0; j < height / 2; j++) {
                    for (int k = 0; k < width / 2; k++) {
                        uBytes[dstIndex++] = bytes[srcIndex];
                        srcIndex += pixelStride;
                    }

                    if (pixelStride == 2) {
                        srcIndex += rowStride - width;
                    } else if (pixelStride == 1) {
                        srcIndex += rowStride - width / 2;
                    }
                }
            } else if (i == 2) {
                for (int j = 0; j < height / 2; j++) {
                    for (int k = 0; k < width / 2; k++) {
                        vBytes[dstIndex++] = bytes[srcIndex];
                        srcIndex += pixelStride;
                    }

                    if (pixelStride == 2) {
                        srcIndex += rowStride - width;
                    } else if (pixelStride == 1) {
                        srcIndex += rowStride - width / 2;
                    }
                }
            }
            System.arraycopy(yBytes, 0, i420, 0, yBytes.length);
            System.arraycopy(uBytes, 0, i420, yBytes.length, uBytes.length);
            System.arraycopy(vBytes, 0, i420, yBytes.length + uBytes.length, vBytes.length);

        }

        return i420;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }


}
