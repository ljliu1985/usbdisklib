package com.udisk.lib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import lk.test.myudisklibrary.R;


/**
 * Created by ljliu on 2018/6/13.
 */

public abstract class AbstractFileItemRecyclerViewAdapter extends RecyclerView.Adapter<AbstractFileItemRecyclerViewAdapter.UsbFileViewHolder> {


    public void setOnItemClickLitener(OnUsbFileItemListener onUsbFileItemListener) {
        this.mOnUsbFileItemListener = onUsbFileItemListener;
    }


    protected OnUsbFileItemListener mOnUsbFileItemListener;
    protected Context mContext;
    protected List<UsbFileItem> list;
    protected SparseArray<Drawable> sparseArray;
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    protected final SelectMode mSelectMode;

    protected int[] icon_ids = {R.drawable.file_icon, R.drawable.excel_icon, R.drawable.txt_icon, R.drawable.unknow_icon, R.drawable.exe_icon, R.drawable.picture_icon};

    public AbstractFileItemRecyclerViewAdapter(Context context, SelectMode selectMode) {
        sparseArray = new SparseArray();
        Drawable drawable;
        this.mContext = context;
        this.mSelectMode = selectMode;
        for (int id : icon_ids) {
            drawable = context.getResources().getDrawable(id);
            drawable.setBounds(0, 0, 80, 80);
            sparseArray.put(id, drawable);
        }
    }


    /**
     * Reads the contents of the directory and notifies that the View shall be
     * updated.
     *
     * @throws IOException If reading contents of a directory fails.
     */
    public void refresh(List<UsbFileItem> files) {
        this.list = files;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    protected static final int getFileTypeResId(String fileName) {
        if (fileName.toLowerCase().matches(UsbHelper.REGEX_EXCEl_FILE)) {
            return R.drawable.excel_icon;
        }
        if (fileName.toLowerCase().matches(UsbHelper.REGEX_IMAGE_FILE)) {
            return R.drawable.picture_icon;
        }
        if (fileName.toLowerCase().matches(UsbHelper.REGEX_EXE_FILE)) {
            return R.drawable.exe_icon;
        }
        if (fileName.toLowerCase().matches(UsbHelper.REGEX_TXT_FILE)) {
            return R.drawable.txt_icon;
        }
        return R.drawable.unknow_icon;
    }


    public class UsbFileViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivFile;
        public final TextView tvFileName;
        public final TextView tvFileTime;
        public final TextView tvFileSize;
        public final CheckBox cbMultiSelect;
        public final RadioButton rbSingleSelect;
        public final LinearLayout ll_file_item;

        public UsbFileViewHolder(View itemView) {
            super(itemView);
            ivFile = (ImageView) itemView.findViewById(R.id.iv_file);
            tvFileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            tvFileTime = (TextView) itemView.findViewById(R.id.tv_file_time);
            tvFileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
            cbMultiSelect = (CheckBox) itemView.findViewById(R.id.cb_multi_select);
            rbSingleSelect = (RadioButton) itemView.findViewById(R.id.rb_single_select);
            ll_file_item = (LinearLayout) itemView.findViewById(R.id.ll_file_item);
        }

    }


}
