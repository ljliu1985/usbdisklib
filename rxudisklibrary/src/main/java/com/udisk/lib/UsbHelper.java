package com.udisk.lib;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.github.magnusja.libaums.javafs.JavaFsFileSystemCreator;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.FileSystemFactory;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.github.mjdev.libaums.partition.Partition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public final class UsbHelper {

    public static final String REGEX_ALL_FILE = ".*";
    public static final String REGEX_EXCEl_FILE = ".*\\.(xls|xlsx)$";
    public static final String REGEX_2003EXCEl_FILE = ".*\\.xls$";
    public static final String REGEX_IMAGE_FILE = ".*\\.(jpg|png|bmp)$";
    public static final String REGEX_EXE_FILE = ".*\\.exe$";
    public static final String REGEX_TXT_FILE = ".*\\.txt$";


    static {
        FileSystemFactory.registerFileSystem(new JavaFsFileSystemCreator());
    }

    //上下文对象
    private Context mContext;
    //USB 设备列表
    private UsbMassStorageDevice[] storageDevices;
    //USB 广播
    private USBBroadCastReceiver mUsbReceiver;
    //当前路径
    private UsbFile currentFolder = null;


    private UsbListener usbListener;
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    /**
     * USB 操作监听
     */
    public interface UsbListener {
        //USB 插入
        void insertUsb(UsbDevice usbDevice);

        //USB 移除
        void removeUsb(UsbDevice usbDevice);

        //获取读取USB权限
        void getReadUsbPermission(UsbDevice usbDevice);

        //读取USB信息失败
        void failedReadUsb(UsbDevice usbDevice);
    }


    private class USBBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION:
                    //接受到自定义广播
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //允许权限申请
                        if (usbDevice != null) {
                            //回调
                            if (usbListener != null) {
                                usbListener.getReadUsbPermission(usbDevice);
                            }
                        }
                    } else {
                        if (usbListener != null) {
                            usbListener.failedReadUsb(usbDevice);
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到存储设备插入广播
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        if (usbListener != null) {
                            usbListener.insertUsb(device_add);
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    //接收到存储设备拔出广播
                    UsbDevice device_remove = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_remove != null) {
                        if (usbListener != null) {
                            usbListener.removeUsb(device_remove);
                        }
                    }
                    break;
            }
        }
    }


    public UsbHelper(Context context, UsbListener usbListener) {
        this.mContext = context;
        this.usbListener = usbListener;
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        //注册广播
        registerReceiver();
    }

    /**
     * 注册 USB 监听广播
     */
    private void registerReceiver() {
        mUsbReceiver = new USBBroadCastReceiver();
        mContext.registerReceiver(mUsbReceiver, getIntentFilter());
    }

    /**
     * 监听USB 广播
     */
    public static IntentFilter getIntentFilter() {
        //监听otg插入 拔出
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        //注册监听自定义广播
        intentFilter.addAction(ACTION_USB_PERMISSION);

        return intentFilter;
    }

    /**
     * 读取 USB设备列表
     *
     * @return USB设备列表
     */
    public UsbMassStorageDevice[] getDeviceList() {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        //获取存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //可能有几个 一般只有一个 因为大部分手机只有1个otg插口
        for (UsbMassStorageDevice device : storageDevices) {
//            Log.e(TAG, device.getUsbDevice().getDeviceName());
            //有就直接读取设备是否有权限
            if (!usbManager.hasPermission(device.getUsbDevice())) {
                //没有权限请求权限
                usbManager.requestPermission(device.getUsbDevice(), pendingIntent);
            }
        }
        return storageDevices;
    }


    /**
     * 获取device 根目录文件
     *
     * @param device USB 存储设备
     * @return 设备根目录下文件列表
     */
    public ArrayList<UsbFile> readDevice(UsbMassStorageDevice device) {
        ArrayList<UsbFile> usbFiles = new ArrayList<>();
        try {
            //初始化
            device.init();
            //获取partition
            Partition partition = device.getPartitions().get(0);
            FileSystem currentFs = partition.getFileSystem();
            //获取根目录
            UsbFile root = currentFs.getRootDirectory();
            currentFolder = root;
            //将文件列表添加到ArrayList中
            Collections.addAll(usbFiles, root.listFiles());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usbFiles;
    }


    /**
     * 复制文件到 USB
     *
     * @param targetFile       需要复制的文件
     * @param saveFolder       复制的目标文件夹
     * @param progressListener 进度回调
     * @return 复制结果
     */
    public static void saveSDFileToUsb(final File targetFile, final UsbFile saveFolder, final ProgressListener progressListener) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> p) throws Exception {
                FileInputStream fis = null;
                UsbFileOutputStream uos = null;
                try {
                    //USB文件是否存在
                    UsbFile saveFile = null;
                    for (UsbFile usbFile : saveFolder.listFiles()) {
                        if (usbFile.getName().equals(targetFile.getName())) {
                            usbFile.delete();
                            break;
                        }
                    }
                    //创建新文件
                    saveFile = saveFolder.createFile(targetFile.getName());
                    //开始写入
                    fis = new FileInputStream(targetFile);//读取选择的文件的
                    uos = new UsbFileOutputStream(saveFile);
                    int bytesRead;
                    byte[] buffer = new byte[1024];
                    int writeCount = 0;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        uos.write(buffer, 0, bytesRead);
                        writeCount += bytesRead;
                        p.onNext(writeCount);
                    }
                    uos.flush();
                    p.onComplete();
                } catch (Exception e) {
                    p.onError(e);
                } finally {
                    IOCloseUtils.closeStream(fis, uos);
                }

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer value) {
                        if (null != progressListener) {
                            progressListener.onProgress(value);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        /**
                         * 重复加载Lib可能会报这个错
                         */
                        //Could not write to device, result == -1 errno 1337 errno-lib could not be loaded!
                        if (null != progressListener) {
                            progressListener.onFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (null != progressListener) {
                            progressListener.onSuccess();
                        }
                    }
                });

    }

    /**
     * @param progressListener 进度回调
     * @return 复制结果
     */
    public static void saveUsbFileToSdcard(final Object objFile, final File targetFile, final ProgressListener progressListener) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> p) throws Exception {
                InputStream is = null;
                FileOutputStream uos = null;
                try {
                    //创建新文件
                    //开始写入
                    if (objFile instanceof UsbFile) {
                        is = new UsbFileInputStream((UsbFile) objFile);//读取选择的文件的
                    } else {
                        is = new FileInputStream((File) objFile);//读取选择的文件的
                    }
                    uos = new FileOutputStream(targetFile);
                    int bytesRead;
                    byte[] buffer = new byte[1024];
                    int writeCount = 0;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        uos.write(buffer, 0, bytesRead);
                        writeCount += bytesRead;
                        p.onNext(writeCount);
                    }
                    uos.flush();
                    p.onComplete();
                } catch (Exception e) {
                    p.onError(e);
                } finally {
                    IOCloseUtils.closeStream(is, uos);
                }

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer value) {
                        if (null != progressListener) {
                            progressListener.onProgress(value);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != progressListener) {
                            progressListener.onFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (null != progressListener) {
                            progressListener.onSuccess();
                        }
                    }
                });

    }

    /**
     * 复制 USB文件到本地
     *
     * @param targetFile       需要复制的文件
     * @param savePath         复制的目标文件路径
     * @param progressListener 下载进度回调
     * @return 复制结果
     */
    public boolean saveUSbFileToLocal(UsbFile targetFile, String savePath,
                                      DownloadProgressListener progressListener) {
        boolean result;
        try {
            //开始写入
            UsbFileInputStream uis = new UsbFileInputStream(targetFile);//读取选择的文件的
            FileOutputStream fos = new FileOutputStream(savePath);
            //这里uis.available一直为0
//            int avi = uis.available();
            long avi = targetFile.getLength();
            int writeCount = 0;
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = uis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                writeCount += bytesRead;
//                Log.e(TAG, "Progress : write : " + writeCount + " All : " + avi);
                if (progressListener != null) {
                    //回调下载进度
                    progressListener.downloadProgress((int) (writeCount * 100 / avi));
                }
            }
            fos.flush();
            uis.close();
            fos.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 获取上层目录文件夹
     *
     * @return usbFile : 父目录文件 / null ：无父目录
     */
    public UsbFile getParentFolder() {
        if (currentFolder != null && !currentFolder.isRoot()) {
            return currentFolder.getParent();
        } else {
            return null;
        }
    }


    /**
     * Class to compare {@link UsbFile}s. If the {@link UsbFile} is an directory
     * it is rated lower than an file, ie. directories come first when sorting.
     */
    private static Comparator<UsbFileItem> comparator = new Comparator<UsbFileItem>() {

        @Override
        public int compare(UsbFileItem lhs, UsbFileItem rhs) {

            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            }

            if (rhs.isDirectory() && !lhs.isDirectory()) {
                return 1;
            }

            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    public static List<UsbFileItem> getUsbFileList(Object dir, String regex) throws IOException {
        ArrayList<UsbFileItem> itemList = new ArrayList<>();
        if (dir instanceof UsbFile) {
            for (UsbFile file : ((UsbFile) dir).listFiles()) {
                if (file.isDirectory() || file.getName().matches(regex)) {
                    itemList.add(new UsbFileItem(file));
                }
            }
        } else if (dir instanceof File) {
            if (null == ((File) dir).listFiles()) {
                return itemList;
            }
            for (File file : ((File) dir).listFiles()) {
                if (file.isDirectory() || file.getName().matches(regex)) {
                    itemList.add(new UsbFileItem(file));
                }
            }
        }


        Collections.sort(itemList, comparator);
        return itemList;
    }


    /**
     * 获取当前 USBFolder
     */
    public UsbFile getCurrentFolder() {
        return currentFolder;
    }

    /**
     * 退出 UsbHelper
     */
    public void unregister() {
        if (mUsbReceiver != null) {
            mContext.unregisterReceiver(mUsbReceiver);
        }
    }

    /**
     * 下载进度回调
     */
    public interface DownloadProgressListener {
        void downloadProgress(int progress);
    }

    public interface ProgressListener {
        void onProgress(int progress);

        void onFailed(String msg);

        void onSuccess();
    }


}
