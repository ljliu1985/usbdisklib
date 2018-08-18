package com.udisk.lib;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.Date;

import lk.test.myudisklibrary.R;

/**
 * Created by ljliu on 2018/6/13.
 */

public class SelectSingleFileItemRecyclerViewAdapter extends AbstractFileItemRecyclerViewAdapter {


    public SelectSingleFileItemRecyclerViewAdapter(Context context, SelectMode selectMode) {
        super(context, selectMode);
    }

    @Override
    public UsbFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_radio, parent, false);
        return new UsbFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UsbFileViewHolder holder, final int position) {
        UsbFileItem item = list.get(position);
        String time = sdf.format(new Date(item.lastModified()));
        holder.tvFileTime.setText(time);
        holder.tvFileName.setText(item.getName());
        holder.rbSingleSelect.setChecked(item.isChecked);
        holder.rbSingleSelect.setVisibility(View.GONE);

        if (item.isDirectory()) {
            if (mSelectMode == SelectMode.SelectSingleDir) {
                holder.rbSingleSelect.setVisibility(View.VISIBLE);
            }
            holder.ivFile.setImageResource(R.drawable.file_icon);
            holder.tvFileSize.setText("");
        } else {
            if (mSelectMode == SelectMode.SelectSingleFile) {
                holder.rbSingleSelect.setVisibility(View.VISIBLE);
            }

            int id = getFileTypeResId(item.getName());
            holder.ivFile.setImageResource(id);
            String size = Formatter.formatFileSize(mContext, item.getLength());
            holder.tvFileSize.setText(size);
        }


        if (null != mOnUsbFileItemListener) {
            holder.ll_file_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnUsbFileItemListener.onItemClick(holder.itemView, position);
                }
            });

            holder.rbSingleSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mOnUsbFileItemListener.onCheckedSingle(buttonView, position, isChecked);
                }
            });
        }

    }


}
