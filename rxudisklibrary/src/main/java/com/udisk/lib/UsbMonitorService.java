package com.udisk.lib;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UsbMonitorService extends Service implements UsbHelper.UsbListener, Application.ActivityLifecycleCallbacks {


    private String TAG = UsbMonitorService.class.getSimpleName();
    private UsbHelper usbHelper;
    private UsbMassStorageDevice[] massStorageDevices;
    private UsbFile root;
    private FileSystem currentFs;
    private Application application;
    private UsbMonitorServiceComm mUsbMonitorServiceComm = new UsbMonitorServiceComm();
    private IUsbRootCallBackListener mCallBackListener;
    private boolean requestFlag = true;
    private IExcludeUsbDevice mExcludeUsbDeviceImpl;


    @Override
    public void insertUsb(UsbDevice usbDevice) {
        requestFlag = true;
        discoverDevice();
    }

    @Override
    public void removeUsb(UsbDevice usbDevice) {
        discoverDevice();
    }

    @Override
    public void getReadUsbPermission(UsbDevice usbDevice) {
        setupDevice();
    }

    @Override
    public void failedReadUsb(UsbDevice usbDevice) {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (currentFs == null) {
            discoverDevice();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public class UsbMonitorServiceComm extends Binder {


        public void setOnUsbRootCallBackListener(IUsbRootCallBackListener listener) {
            mCallBackListener = listener;
        }

        public void setOnExcludeUsbDevice(IExcludeUsbDevice excludeUsbDeviceImpl) {
            mExcludeUsbDeviceImpl = excludeUsbDeviceImpl;
        }

        public void registerApp(Application app) {
            application = app;
            application.registerActivityLifecycleCallbacks(UsbMonitorService.this);
            setupDevice();
        }


        public void unregister() {
            if (null != application) {
                application.unregisterActivityLifecycleCallbacks(UsbMonitorService.this);
            }
            usbHelper.unregister();
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        usbHelper = new UsbHelper(this, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mUsbMonitorServiceComm;
    }


    /**
     * Searches for connected mass storage devices, and initializes them if it
     * could find some.
     */
    private void discoverDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        massStorageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        UsbDevice usbDevice = null;
        for (UsbMassStorageDevice usbMassStorageDevice : massStorageDevices) {
            UsbDevice device = usbMassStorageDevice.getUsbDevice();
            if (!excludeUsbDevice(device)) {
                usbDevice = device;
                break;
            }
        }
        if (null == usbDevice) {
            Log.i(TAG, "no udisk");
            if (null == root && null == currentFs) {
                return;
            }
            root = null;
            currentFs = null;
            mCallBackListener.onUsbMonitorCallBack(null, null);
            return;
        }

        if (usbManager.hasPermission(usbDevice)) {
            Log.d(TAG, "received usb device via intent");
            // requesting permission is not needed in this case
            setupDevice();
        } else {
            // first request permission from user to communicate with the
            // underlying
            // UsbDevice
            if (requestFlag) {
                requestFlag = false;
                PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(UsbHelper.ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(usbDevice, permissionIntent);
            }
        }
    }


    /**
     * Sets the device up and shows the contents of the root directory.
     */
    private void setupDevice() {
        if (null != currentFs || null == massStorageDevices) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> b) {
                try {
                    UsbDevice usbDevice = null;
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    for (UsbMassStorageDevice usbMassStorageDevice : massStorageDevices) {
                        usbDevice = usbMassStorageDevice.getUsbDevice();
                        if (!excludeUsbDevice(usbDevice)) {
                            if (usbManager.hasPermission(usbDevice)) {
                                usbMassStorageDevice.init();
                                // we always use the first partition of the device
                                currentFs = usbMassStorageDevice.getPartitions().get(0).getFileSystem();
                                root = currentFs.getRootDirectory();
                                break;
                            }
                        }
                    }
                    if (null == usbDevice) {
                        return;
                    }

                    b.onComplete();
                } catch (Exception e) {
                    b.onError(e);
                }

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean value) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (null != mCallBackListener) {
                            mCallBackListener.onUsbMonitorCallBack(null, null);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (null != mCallBackListener) {
                            mCallBackListener.onUsbMonitorCallBack(root, currentFs);
                        }
                    }
                });

    }

    /**
     * exclude usb device
     *
     * @param usbDevice
     * @return
     */
    public boolean excludeUsbDevice(UsbDevice usbDevice) {
        if (null == usbDevice) {
            return true;
        }
        if (null != mExcludeUsbDeviceImpl) {
            return mExcludeUsbDeviceImpl.excludeUsbDevice(usbDevice);
        }
        return false;
    }

}
