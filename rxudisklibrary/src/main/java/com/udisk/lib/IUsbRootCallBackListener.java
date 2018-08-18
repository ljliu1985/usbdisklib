package com.udisk.lib;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

public interface IUsbRootCallBackListener {

    void onUsbMonitorCallBack(UsbFile root,FileSystem fs);

}
