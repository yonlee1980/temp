package com.jikexueyuan.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jikexueyuan.yonshareposition.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/3/4.
 */
public class ChooseHeadDialog extends DialogFragment {
    private static final int CAMERA_REQUEST = 1;
    private static final int LOCAL_REQUEST = 2;
    private static final int CROP_REQUEST = 3;
    private String tag = "camera";
    private ImageView ivCamera;
    private ImageView ivLocal;
    private String imgPath =null;

    public String getImgPath() {
        return imgPath;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //得到layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_choose_head, null);
        TextView tvCamera = (TextView) view.findViewById(R.id.tvCamera);
        TextView tvLocal = (TextView) view.findViewById(R.id.tvLocal);
        ivCamera = (ImageView) view.findViewById(R.id.ivCamera);
        ivLocal = (ImageView) view.findViewById(R.id.ivLocal);
        //导入布局，父视图为null，因为这是dialog
        builder.setView(view).setPositiveButton("选定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogPositiveClick(ChooseHeadDialog.this);
            }
        }).setNegativeButton("不选了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogNegativeClick(ChooseHeadDialog.this);
            }
        });

        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(iCamera, CAMERA_REQUEST);
            }
        });

        tvLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iLocal = new Intent(Intent.ACTION_GET_CONTENT);
                iLocal.setType("image/*");
                startActivityForResult(iLocal, LOCAL_REQUEST);
            }
        });

        return builder.create();
    }

    /**
     * 调用该对话框就必须实现这个接口，以便回馈数据
     */
    public interface ChooseHeadDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);

        public void onDialogNegativeClick(DialogFragment dialog);
    }

    //用这个接口的实例 传递事件
    ChooseHeadDialogListener mListener = null;

    /**
     * 重写onAttach方法，初始化监听器
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //验证，调用者是否实现了接口
        try {
            //初始化监听器，以便给调用者传递事件
            mListener = (ChooseHeadDialogListener) activity;
        } catch (ClassCastException e) {
            //如果调用者没有实现接口，就抛出异常
            throw new ClassCastException(activity.toString() + "must implement ChooseHeadDialogListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (data == null) {
                return;
            } else {
                Bundle b = data.getExtras();
                if (b != null) {
                    Bitmap bm = b.getParcelable("data");
                    Uri uri = saveBitmap(bm);
                    tag = "camera";
                    startImageZoom(uri);
                }
            }
        } else if (requestCode == LOCAL_REQUEST) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            Uri fUri = convertUri(uri);
            tag = "local";
            startImageZoom(fUri);
        } else if (requestCode == CROP_REQUEST) {
            if (data == null) {
                return;
            }
            Bundle b = data.getExtras();
            Bitmap bm = b.getParcelable("data");
            Bitmap roundBitmap = getCircleBitmap(bm);
            if (tag == "camera") {
                ivCamera.setVisibility(View.VISIBLE);
                ivCamera.setImageBitmap(roundBitmap);
                ivLocal.setVisibility(View.INVISIBLE);
            } else {
                ivLocal.setVisibility(View.VISIBLE);
                ivLocal.setImageBitmap(roundBitmap);
                ivCamera.setVisibility(View.INVISIBLE);
            }
            saveBitmap(roundBitmap);
        }
    }


    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xffffffff;
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0xff, 0, 0xff);
        paint.setColor(color);
        int x = bitmap.getWidth();
        canvas.drawCircle(x / 2, x / 2, x / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    public Uri saveBitmap(Bitmap bitmap) {
        File newFile = new File(Environment.getExternalStorageDirectory(),"head.png");
        try {
            FileOutputStream fos = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(newFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Uri convertUri(Uri uri) {
        InputStream is = null;
        try {
            is = getActivity().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return saveBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startImageZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 96);
        intent.putExtra("outputY", 96);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST);
    }
}
