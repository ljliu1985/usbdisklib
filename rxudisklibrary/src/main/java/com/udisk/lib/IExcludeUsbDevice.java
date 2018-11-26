package com.udisk.lib;

import android.hardware.usb.UsbDevice;

/**
 * @author ljliu1985
 * @date 2018/11/26
 */
public interface IExcludeUsbDevice {

    boolean excludeUsbDevice(UsbDevice usbDevice);
}
