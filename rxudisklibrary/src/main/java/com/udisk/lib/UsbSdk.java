package com.udisk.lib;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

import java.io.File;

public class UsbSdk extends MyObservable<UsbObserver> implements IUsbRootCallBackListener {

    private static final String TAG = UsbSdk.class.getSimpleName();
    private ServiceConnection serviceConnection;
    private boolean connectionFlag = false;
    private UsbMonitorService.UsbMonitorServiceComm mUsbMonitorServiceComm;
    private IExcludeUsbDevice excludeUsbDeviceImpl;

    private final String USB_SDK_SERVICE_ACTION = "com.udisk.lib.UsbMonitorService";
    private UsbFile mRoot;
    private FileSystem currentFs;
    private static UsbSdk usbSdk;

    public static UsbSdk init(Application app) {
        if (null == usbSdk) {
            usbSdk = new UsbSdk(app);
        }
        return usbSdk;
    }

    private UsbSdk(final Application app) {
        Intent intent = new Intent(USB_SDK_SERVICE_ACTION);
        intent.setClass(app, UsbMonitorService.class);
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "UsbMonitorService onServiceDisconnected");
                connectionFlag = false;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "UsbMonitorService onServiceConnected");
                connectionFlag = true;
                mUsbMonitorServiceComm = (UsbMonitorService.UsbMonitorServiceComm) service;
                mUsbMonitorServiceComm.setOnUsbRootCallBackListener(UsbSdk.this);
                mUsbMonitorServiceComm.registerApp(app);
                mUsbMonitorServiceComm.setOnExcludeUsbDevice(excludeUsbDeviceImpl);
            }
        };
        app.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void excludeUsbDevice(IExcludeUsbDevice excludeUsbDeviceImpl) {
        this.excludeUsbDeviceImpl = excludeUsbDeviceImpl;
        if (null != mUsbMonitorServiceComm) {
            mUsbMonitorServiceComm.setOnExcludeUsbDevice(excludeUsbDeviceImpl);
        }
    }

    public static void unregister() {
        if (null == usbSdk) {
            return;
        }
        if (usbSdk.connectionFlag && usbSdk.mUsbMonitorServiceComm != null) {
            usbSdk.mUsbMonitorServiceComm.unregister();
        }
        usbSdk.unregisterAll();
    }

    public static void addRegister(UsbObserver usbObserver) {
        if (null == usbSdk) {
            throw new NullPointerException("do you init the usb sdk");
        }
        if (usbObserver == null) {
            return;
        }
        usbSdk.registerObserver(usbObserver);
        usbObserver.onChanged();
    }

    public static void removeRegister(UsbObserver usbObserver) {
        if (null == usbSdk || usbObserver == null) {
            return;
        }
        usbSdk.unregisterObserver(usbObserver);
    }

    /**
     * 通知USB信息变化
     */
    public void notifyChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }


    @Override
    public void onUsbMonitorCallBack(UsbFile root, FileSystem fs) {
        Log.i(TAG, "onCallBackUsbRootDir:" + root);
        this.mRoot = root;
        this.currentFs = fs;
        notifyChanged();
    }

    public static void copyLocalFileToUsbRoot(String sdcardFilePath, UsbHelper.ProgressListener progressListener) {
        if (null == usbSdk) {
            throw new NullPointerException("do you init the usb sdk");
        }
        if (null == usbSdk.currentFs) {
            progressListener.onFailed("no usb disk");
            return;
        }
        UsbHelper.saveSDFileToUsb(new File(sdcardFilePath), usbSdk.currentFs.getRootDirectory(), progressListener);
    }

    public static void copyUsbFileToSDCard(UsbFile usbFile, File targetFile, UsbHelper.ProgressListener progressListener) {
        if (null == usbSdk) {
            throw new NullPointerException("do you init the usb sdk");
        }
        UsbHelper.saveUsbFileToSdcard(usbFile, targetFile, progressListener);
    }

    public static UsbFile getRoot() {
        if (null == usbSdk) {
            throw new NullPointerException("do you init the usb sdk");
        }
        return usbSdk.mRoot;
    }

    public static FileSystem getFileSystem() {
        if (null == usbSdk) {
            throw new NullPointerException("do you init the usb sdk");
        }
        return usbSdk.currentFs;
    }

    public static boolean hasDisk() {
        if (null == usbSdk) {
            throw new NullPointerException("do you init the usb sdk");
        }
        if (null == usbSdk.currentFs || null == usbSdk.mRoot) {
            return false;
        }
        return true;
    }


}
