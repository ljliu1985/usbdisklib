package com.udisk.lib;

import android.view.View;

/**
 * Created by ljliu on 2018/6/15.
 */

public interface OnUsbFileItemListener {

    void onItemClick(View view, int position);

    void onCheckedSingle(View view, int position, boolean isChecked);

    void onCheckedMulti(View view, int position, boolean isChecked);
}