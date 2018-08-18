package com.udisk.lib;

import com.github.mjdev.libaums.fs.UsbFile;

import java.util.List;

/**
 * Created by ljliu on 2018/6/15.
 */

public interface SelectCallBack {

    void onSelectSingleCallBack(Object usbFile);

    void onSelectMultiCallBack(List usbFileList);
}
