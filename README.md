Introduction
============
For Android 6.0 above system to use USB Disk.
基于libaums库修改，android 6.0系统以上使用U盘，或者系统磁盘.

project gradle:
```groovy
maven { url 'https://jitpack.io' }

maven { url 'https://dl.bintray.com/magnusja/maven' }
```

app gradle:
```groovy
 implementation 'com.github.ljliu1985:usbdisklib:1.8'
```

Screenshots
===========
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2018-08-20-091339.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2018-08-20-091408.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2018-08-20-092051.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2019-02-26-163133.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2019-02-26-163155.png">
<img src="https://github.com/ljliu1985/DemoUSBDisk/blob/master/device-2019-02-26-163216.png">


Simple Usage
------------
1. In Application init

```java
UsbSdk.init(this);
```

 init for exclude other usb device.
 
```java
UsbSdk.init(getApplication()).excludeUsbDevice(new IExcludeUsbDevice() {
            @Override
            public boolean excludeUsbDevice(UsbDevice usbDevice) {
                //eg
                if (usbDevice.getVendorId() == 8201) {
                    return true;
                }
                return false;
            }
        });
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
                }).setStyleColor(StyleColor.Red).setSelectMode(SelectMode.SelectSingleFile).show();
```                

3.)select sigle Dir

```java
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
                }).setSelectMode(SelectMode.SelectSingleDir).setStyleColor(StyleColor.Blue).show();
```      

4.) select multi files

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
                }).setSelectMode(SelectMode.SelectMultiFile).setStyleColor(StyleColor.Green).show();
 ```
 
 5.备注:从U盘复制文件到SD卡，若此目录下面有数千个文件，关闭IO流时会耗费大量时间，复制会变慢，可以考虑不关闭U盘文件的IO流，SD卡的IO流可关闭。
