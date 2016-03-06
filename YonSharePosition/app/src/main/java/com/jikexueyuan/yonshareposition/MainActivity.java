package com.jikexueyuan.yonshareposition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.jikexueyuan.model.BitmapAndLatLng;
import com.jikexueyuan.model.ChooseHeadDialog;
import com.jikexueyuan.model.HeadAdapter;
import com.jikexueyuan.model.IpGet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ChooseHeadDialog.ChooseHeadDialogListener {
    //百度地图相关
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    //百度定位相关
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //我的位置
    private LatLng point = null;
    //接收到的位置
    private LatLng receivedPoint = null;
    //当前选定人的位置，当解决RecyclerView点击事件的时候，可以点击图像 切换主角位置，暂留
    private LatLng selectedPoint = null;
    //自己要发送的数据
    private BitmapAndLatLng mBitmapAndLatLng;
    //接收到的数据
    private BitmapAndLatLng receivedBitmapAndLatlng;
    //自己的bitmap
    private Bitmap bitmap;
    //bitmap的临时路径
    private String bitmapPath = null;
    //自己的IP
    private String ip = null;
    //视图控件
    private Button btnShare, btnHide;
    private TextView shareDescription;
    private RecyclerView recyclerView;
    //判断是否开启共享
    private boolean isShared = false;
    //存储头像Boolean，服务器Ip值，
    private SharedPreferences sharedPreferences;
    //百度云推送相关标签
    private List<String> taglist;
    //自定义对话框
    private DialogFragment dialog;
    //以下RecyclerView相关
    private List<BitmapAndLatLng> list;
    private HeadAdapter adapter;

    //以下Service 相关
    private PointService mBoundService;
    private PointService.PointBinder mBinder = null;
    private boolean mIsBound;

    //是否第一次定位
    private boolean isFirstLoc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //开始接受推送，并设置接受推送的标签为yong
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, "Z0s4LTZsd01rt2ePWAbzhrP2");
        taglist = new ArrayList<>();
        taglist.add("yong");
        PushManager.setTags(getApplicationContext(), taglist);

        //控件初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        btnHide = (Button) findViewById(R.id.btnHide);
        btnShare = (Button) findViewById(R.id.btnShare);
        shareDescription = (TextView) findViewById(R.id.tvHint);
        recyclerView = (RecyclerView) findViewById(R.id.viewShowHead);

        //初始化各种设置
        init();
    }

    /**
     * 相关初始设置
     */
    private void init() {
        //设置点击事件
        btnShare.setOnClickListener(this);
        btnHide.setOnClickListener(this);

        //RecyclerView设置为横向，传入adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        list = new ArrayList<>();
        adapter = new HeadAdapter(list);
        recyclerView.setAdapter(adapter);

        //初始化对话框dialog
        dialog = new ChooseHeadDialog();

        //预设一个bitmap值
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_openmap_mark);

        //地图相关方面的设置
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mLocationClient = new LocationClient(getApplicationContext());
        //初始化位置监听方式
        initLocation();

        //得到sharedPreferences
        sharedPreferences = getSharedPreferences("share", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
        //得到bitmapPath
        File tmpDirs = new File(Environment.getExternalStorageDirectory() + "/com.jikexueyuan.yonshareposition.avator");
        if (!tmpDirs.exists()) {
            tmpDirs.mkdirs();
        }
        bitmapPath = tmpDirs.getAbsolutePath() + "avatar.png";
    }

    /**
     * 定位参数设置
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(2000);
        option.setCoorType("bd0911");
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 按钮点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShare:
                //位置共享事件
                sharePosition();
                break;
            case R.id.btnHide:
                //头像隐藏事件
                hideHead();
                break;
        }
    }

    /**
     * 根据当前状态，确定是开启共享位置，还是关闭
     */
    private void sharePosition() {
        if (isShared) {
            //解除共享服务
            doUnbindService();
            //当前是共享状态时，关闭共享
            recyclerView.setVisibility(View.GONE);
            shareDescription.setVisibility(View.VISIBLE);
            shareDescription.setText(R.string.share_open);
            btnHide.setVisibility(View.GONE);
            btnShare.setBackgroundResource(R.drawable.exit_default);
            //改变boolean值，下次点击能进入共享位置事件
            isShared = false;
            //重置bitmap,mBitmapAndLatLng
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_openmap_mark);
            mBitmapAndLatLng = new BitmapAndLatLng(ip, bitmap, point);
            //清空List，重新加入一个自己的位置对象
            list.clear();
            list.add(mBitmapAndLatLng);
            //重新刷新视图
            addOverlay(point);
        } else if (sharedPreferences.getBoolean("isHeadChoosed", false)) {

            //如果当前未共享，并且头像已经设置，那就开启共享
            recyclerView.setVisibility(View.VISIBLE);
            shareDescription.setVisibility(View.GONE);
            btnHide.setVisibility(View.VISIBLE);
            btnHide.setBackgroundResource(R.drawable.close);
            btnShare.setBackgroundResource(R.drawable.exit_pressed);
            //得到当前头像
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(new File(Environment.getExternalStorageDirectory(), "head.png")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //改变Boolean值，下次点击能进入解除共享事件
            isShared = true;
            //更新头像
            notifyHeadChanged(ip, bitmap, point);
            //选定当前点
            selectedPoint = point;
            //开启共享服务
            doBindService();
        } else {
            //如果头像未设置，那就进入这里设置头像先
            dialog.show(getSupportFragmentManager(), "headChoose");
        }
    }

    /**
     * 根据得到参数，改变list,改变adapter
     *
     * @param ip
     * @param bitmap
     * @param latLng
     */
    private void notifyHeadChanged(String ip, Bitmap bitmap, LatLng latLng) {
        //先实例化一个boolean值，赋值假
        boolean isChanged = false;
        //循环检查list中的值，看是否有跟传入的IP相同的
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIp().equals(ip)) {
                //如果IP相同，就用当前的BitmapAndLatLng替换掉原来的
                list.set(i, new BitmapAndLatLng(ip, bitmap, latLng));
                isChanged = true;
            }
        }
        if (!isChanged) {
            //如果IP没有相同的，那就新增加一个BitmapAndLatLng
            list.add(new BitmapAndLatLng(ip, bitmap, latLng));
        }
        //adapter通知改变，
        adapter.notifyDataSetChanged();
        addOverlay(selectedPoint);
    }

    /**
     * 隐藏与显示RecyclerView方法
     */
    private void hideHead() {
        if (shareDescription.getVisibility() == View.VISIBLE) {
            //当前为隐藏状态时，显示RecyclerView
            recyclerView.setVisibility(View.VISIBLE);
            shareDescription.setVisibility(View.GONE);
            btnHide.setBackgroundResource(R.drawable.close);
        } else {
            //当前为显示状态时，隐藏RecyclerView
            recyclerView.setVisibility(View.GONE);
            shareDescription.setVisibility(View.VISIBLE);
            shareDescription.setText(R.string.share_close);
            btnHide.setBackgroundResource(R.drawable.open);
        }
    }

    /**
     * 对话框点击确认时的相关事件
     * 得到头像
     * 设置当前状态为头像已经设置
     *
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        File tmpDirs = new File(Environment.getExternalStorageDirectory() + "/com.example.administrator.avator");
        if (!tmpDirs.exists()) {
            tmpDirs.mkdirs();
        } else {
            //得到当前头像
            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "avatar.png");
        }
        //改变当前状态为，头像已经设置
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isHeadChoosed", true);
        editor.commit();
    }

    /**
     * 对话框点击否定时的相关事件
     * 应用系统默认头像
     * 设置当前状态为头像已经设置
     *
     * @param dialog
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //启用系统默认头像
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.touxiang_ops);
        //改变当前状态为，头像已经设置
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isHeadChoosed", true);
        editor.commit();
    }

    /**
     * 定位监听器
     */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                point = new LatLng(location.getLatitude(), location.getLongitude());
                ip = IpGet.getLocalIpAdress();
                if (mBitmapAndLatLng == null) {
                    //第一次就新建一个BitmapAndLatLng
                    mBitmapAndLatLng = new BitmapAndLatLng(ip, bitmap, point);
                } else {
                    //更新数据
                    mBitmapAndLatLng.setLatLng(point);
                    mBitmapAndLatLng.setBitmap(bitmap);
                }
                if (mBinder != null) {
                    //发送数据
                    mBoundService.send(mBitmapAndLatLng);
                }
                if (isFirstLoc) {
                    list.add(new BitmapAndLatLng(ip, bitmap, point));
                    addOverlay(point);
                    isFirstLoc = false;
                }
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                Toast.makeText(getApplicationContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                Toast.makeText(getApplicationContext(), R.string.newwork_exception, Toast.LENGTH_SHORT).show();
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                Toast.makeText(getApplicationContext(), R.string.criteria_exception, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //服务连接的初始以及实例
    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * 服务开启后
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (PointService.PointBinder) service;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mBinder.setData(mBitmapAndLatLng);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            mBoundService = mBinder.getService();
            mBoundService.setCallback(new PointService.Callback() {
                @Override
                public void receivePoint(BitmapAndLatLng bitmapAndLatLng) {
                    Message msg = new Message();
                    msg.obj = bitmapAndLatLng;
                    handler.sendMessage(msg);
                }
            });
        }

        /**
         * 服务意外断开是，mBoundService赋值空
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    //接收信息处理器
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            receivedBitmapAndLatlng = (BitmapAndLatLng) msg.obj;
            //接收到数据以后跟新list
            notifyHeadChanged(receivedBitmapAndLatlng.getIp(), receivedBitmapAndLatlng.getBitmap(), receivedBitmapAndLatlng.getLatLng());
        }
    };

    /**
     * 绑定服务
     * 两秒刷新一次视图
     */
    private void doBindService() {
        bindService(new Intent(MainActivity.this, PointService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * 解绑服务
     */
    private void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * 以传入点刷新视图
     *
     * @param latLng
     */
    private void addOverlay(LatLng latLng) {
        mBaiduMap.clear();
        for (int i = 0; i < list.size(); i++) {
            OverlayOptions option = new MarkerOptions()
                    .position(list.get(i).getLatLng())
//                    .icon(BitmapDescriptorFactory.fromBitmap(list.get(i).getBitmap()));
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_geo));
            mBaiduMap.addOverlay(option);
            if (isShared) {
                //添加头像显示
                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(list.get(i).getBitmap());
                imageView.setMaxHeight(96);
                imageView.setMaxWidth(96);
                Point p = mBaiduMap.getProjection().toScreenLocation(list.get(i).getLatLng());
                p.y -= 48;
                LatLng pInfo = mBaiduMap.getProjection().fromScreenLocation(p);
                InfoWindow infoWindow = new InfoWindow(imageView, pInfo, 1);
                mBaiduMap.showInfoWindow(infoWindow);
            }
        }
        //mMapView.addView(new View(getApplicationContext(),));
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(update);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
        PushManager.stopWork(getApplicationContext());
        doUnbindService();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            dialog.show(getSupportFragmentManager(), "headChoose");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
