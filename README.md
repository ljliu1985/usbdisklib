Introduction
============
For Android 6.0 above system to use USB Disk.
基于libaums库修改，android 6.0系统以上使用U盘，或者系统磁盘.

project gradle:
```java
maven { url 'https://jitpack.io' }

maven { url 'https://dl.bintray.com/magnusja/maven' }
```

app gradle:
```java
 implementation 'com.github.ljliu1985:usbdisklib:1.4'
```

Screenshots
===========
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2018-08-20-091339.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2018-08-20-091408.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2018-08-20-092051.png">


Simple Usage
------------
1. In Application init

```java
UsbSdk.init(this);
```

2.) select ".xls|.xlsx" files:

```java
   new UsbDialogFrament.Builder(activity).setFilter(UsbHelper.REGEX_EXCEl_FILE).setSelectCallBack(new CommonSelectCallBack() {
                    @Override
                    public void onSelectSingleCallBack(Object objFile) {
                        if (objFile instanceof UsbFile) {
                            Log.i(TAG, "UsbFile type, path:" + ((UsbFile) objFile).getAbsolutePath());
                            //usb file
                           /*UsbHelper.saveUsbFileToSdcard(objFile, new File("/sdcard/xxx"), new UsbHelper.ProgressListener() {
                               @Override
                               public void onProgress(int progress) {

                               }

                               @Override
                               public void onFailed(String msg) {

                               }

                               @Override
                               public void onSuccess() {

                               }
                           });*/
                        } else if (objFile instanceof File) {
                            Log.i(TAG, "File type, path:" + ((File) objFile).getAbsolutePath());
                            //sdcard file
                        }
                    }
                }).setSelectMode(SelectMode.SelectSingleFile).show();
```                

3)select sigle Dir
...java
   new UsbDialogFrament.Builder(this).setFilter(UsbHelper.REGEX_IMAGE_FILE).setSelectCallBack(new CommonSelectCallBack() {
                    @Override
                    public void onSelectSingleCallBack(Object objFile) {
                        if (objFile instanceof UsbFile) {
                            //usb file
                            Log.i(TAG, "UsbFile type, path:" + ((UsbFile) objFile).getAbsolutePath());
                        } else if (objFile instanceof File) {
                            //sdcard file
                            Log.i(TAG, "File type, path:" + ((File) objFile).getAbsolutePath());
                        }

                    }
                }).setSelectMode(SelectMode.SelectSingleDir).show();
...

4) select multi files
```java
   new UsbDialogFrament.Builder(this).setSelectCallBack(new CommonSelectCallBack() {
                    @Override
                    public void onSelectMultiCallBack(List fileList) {
                        //objFile list
                        if (fileList.get(0) instanceof UsbFile) {
                            //usb file
                            Log.i(TAG, "UsbFile type, path:" + ((UsbFile) fileList.get(0)).getAbsolutePath());
                        } else if (fileList.get(0) instanceof File) {
                            //sdcard file
                            Log.i(TAG, "File type, path:" + ((File) fileList.get(0)).getAbsolutePath());
                        }
                    }
                }).setSelectMode(SelectMode.SelectMultiFile).show();
 ```
