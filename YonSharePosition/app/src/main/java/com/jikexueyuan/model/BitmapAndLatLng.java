package com.jikexueyuan.model;

import android.graphics.Bitmap;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/2/27.
 */
public class BitmapAndLatLng implements Serializable {
    private static final long serialVersionUID = 0L;
    private String ip;
    private Bitmap bitmap;
    private LatLng latLng;

    public BitmapAndLatLng(String ip,Bitmap bitmap,LatLng latLng){
        this.ip = ip;
        this.bitmap = bitmap;
        this.latLng = latLng;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
