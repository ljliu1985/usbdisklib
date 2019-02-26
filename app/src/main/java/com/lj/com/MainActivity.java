package com.lj.com;

import android.Manifest;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mjdev.libaums.fs.UsbFile;
import com.udisk.lib.CommonSelectCallBack;
import com.udisk.lib.IExcludeUsbDevice;
import com.udisk.lib.RxPermissionsUtil;
import com.udisk.lib.SelectMode;
import com.udisk.lib.StyleColor;
import com.udisk.lib.UsbDialogFrament;
import com.udisk.lib.UsbHelper;
import com.udisk.lib.UsbSdk;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private Button btn_select_excel;
    private Button btn_select_folder;
    private Button btn_select_multi_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_select_excel = findViewById(R.id.btn_select_excel);
        btn_select_folder = findViewById(R.id.btn_select_folder);
        btn_select_multi_file = findViewById(R.id.btn_select_multi_file);
        btn_select_excel.setOnClickListener(this);
        btn_select_folder.setOnClickListener(this);
        btn_select_multi_file.setOnClickListener(this);
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
        RxPermissionsUtil.requestPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_select_excel:
                new UsbDialogFrament.Builder(this).setFilter(UsbHelper.REGEX_EXCEl_FILE).setSelectCallBack(new CommonSelectCallBack() {
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
                }).setSelectMode(SelectMode.SelectSingleFile).setStyleColor(StyleColor.Red).show();
                break;
            case R.id.btn_select_folder:
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
                break;
            case R.id.btn_select_multi_file:
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
                break;
        }


    }
}
