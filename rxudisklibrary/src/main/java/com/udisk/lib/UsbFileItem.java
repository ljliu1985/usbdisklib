package com.udisk.lib;

import com.github.mjdev.libaums.fs.UsbFile;

import java.io.File;

/**
 * Created by ljliu on 2018/6/15.
 */

public class UsbFileItem {
    public Object usbFile;
    public boolean isChecked = false;
    protected boolean isUsbFile;


    public UsbFileItem(Object usbFile) {
        if (usbFile instanceof UsbFile) {
            isUsbFile = true;
        } else if (usbFile instanceof File) {
            isUsbFile = false;
        } else {
            throw new UnsupportedOperationException("not support file type!");
        }
        this.usbFile = usbFile;
    }

    public long lastModified() {
        if (isUsbFile) {
            return ((UsbFile) usbFile).lastModified();
        }
        return ((File) usbFile).lastModified();
    }

    public String getName() {
        if (isUsbFile) {
            return ((UsbFile) usbFile).getName();
        }
        return ((File) usbFile).getName();
    }

    public boolean isDirectory() {
        if (isUsbFile) {
            return ((UsbFile) usbFile).isDirectory();
        }
        return ((File) usbFile).isDirectory();
    }

    public long getLength() {
        if (isUsbFile) {
            return ((UsbFile) usbFile).getLength();
        }
        return ((File) usbFile).length();
    }

    public String getAbsolutePath() {
        if (isUsbFile) {
            return ((UsbFile) usbFile).getAbsolutePath();
        }
        return ((File) usbFile).getAbsolutePath();
    }

}
