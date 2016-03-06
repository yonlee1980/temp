package com.jikexueyuan.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Administrator on 2016/3/4.
 */
public class HeadAdapter extends RecyclerView.Adapter {
    private List<BitmapAndLatLng> list;
    public HeadAdapter(List<BitmapAndLatLng> list){
        this.list = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setPadding(8,8,8,8);
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.getIv().setImageBitmap(list.get(position).getBitmap());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv;
        public ViewHolder(ImageView itemView) {
            super(itemView);
            iv = itemView;
        }

        public ImageView getIv() {
            return iv;
        }
    }
}
