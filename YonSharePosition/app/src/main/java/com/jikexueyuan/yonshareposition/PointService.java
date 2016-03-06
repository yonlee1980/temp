package com.jikexueyuan.yonshareposition;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.jikexueyuan.model.BitmapAndLatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PointService extends Service {
    private final IBinder mBinder = new PointBinder();
    private BitmapAndLatLng receivedBitmapAndLatLng = null;
    private SharedPreferences shared;
    private BitmapAndLatLng localBitmapAndLatLng = null;

    /**
     * 服务构造方法
     */
    public PointService() {
    }

    /**
     * 自定义绑定器
     */
    public class PointBinder extends Binder {
        PointService getService() {
            return PointService.this;
        }

        public void setData(BitmapAndLatLng localData) {
            localBitmapAndLatLng = localData;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //链接socket服务器
        connect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                send(localBitmapAndLatLng);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private Callback callback = null;

    public static interface Callback {
        void receivePoint(BitmapAndLatLng bitmapAndLatLng);
    }

    //---------------------以下Socket相关-------------------
    private Socket socket = null;
    private String ip = null;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    /**
     * 链接socket
     */
    public void connect() {
        //得到sharedPrerences
        shared = getSharedPreferences("share", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
        ip = shared.getString("ip", "0.0.0.0");
        if (ip != "0.0.0.0") {
            AsyncTask<Void, BitmapAndLatLng, Void> read = new AsyncTask<Void, BitmapAndLatLng, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        socket = new Socket(ip, 10001);
                        writer = new ObjectOutputStream(socket.getOutputStream());
                        reader = new ObjectInputStream(socket.getInputStream());
                        Object bitmapAndLatLng = null;
                        if (reader != null) {
                            while ((bitmapAndLatLng = reader.readObject()) != null) {
                                receivedBitmapAndLatLng = (BitmapAndLatLng) bitmapAndLatLng;
                                publishProgress(receivedBitmapAndLatLng);
                            }
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(BitmapAndLatLng... values) {
                    super.onProgressUpdate(values);
                    if (callback != null) {
                        callback.receivePoint(values[0]);
                    }
                }
            };
            read.execute();
        } else {
            Toast.makeText(getApplicationContext(), "无法得到有效网址，请检查服务器。。。。", Toast.LENGTH_LONG).show();
        }
    }

    public void send(BitmapAndLatLng bitmapAndLatLng) {
        if (socket != null) {
            try {
                if (writer != null) {
                    writer.writeObject(bitmapAndLatLng);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
