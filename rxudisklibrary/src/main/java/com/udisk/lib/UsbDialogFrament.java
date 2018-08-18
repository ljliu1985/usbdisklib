package com.udisk.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lk.test.myudisklibrary.R;

/**
 * Created by ljliu on 2018/6/14.
 */

@SuppressLint("ValidFragment")
public class UsbDialogFrament extends DialogFragment implements UsbObserver, OnUsbFileItemListener, View.OnClickListener {

    private RecyclerView recyclerView;
    private Button btnGoback;
    private Button btnUdisk;
    private Button btnSdcard;
    private Button btnExit;
    private Button btn_confirm;
    private TextView tv_path;

    private static final String TAG = "xxx";
    private FileSystem currentFs;
    private List<UsbFileItem> list;
    private Object root;
    private AbstractFileItemRecyclerViewAdapter mFileItemRecyclerViewAdapter;
    private Context mContext;
    //过滤文件后缀
    private String regex;
    private String currPath;
    private SelectCallBack callCack;
    private SelectMode mSelectMode;
    private FragmentActivity mActivity;


    private UsbDialogFrament(UsbDiialogParams p) {
        this.mActivity = p.activity;
        this.callCack = p.callBack;
        this.mSelectMode = p.selectMode;
        this.regex = p.regex;
        if (mSelectMode == null) {
            mSelectMode = SelectMode.SelectSingleDir;
        }
        if (TextUtils.isEmpty(this.regex)) {
            regex = UsbHelper.REGEX_ALL_FILE;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_udisk, container, false);
        mContext = getActivity();
        init(view);
        return view;
    }

    public void init(View view) {
        btnGoback = (Button) view.findViewById(R.id.btn_goback);
        btnUdisk = (Button) view.findViewById(R.id.btn_udisk);
        btnSdcard = (Button) view.findViewById(R.id.btn_sdcard);
        btnExit = (Button) view.findViewById(R.id.btn_exit);
        btn_confirm = (Button) view.findViewById(R.id.btn_confirm);
        tv_path = (TextView) view.findViewById(R.id.tv_path);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        btnGoback.setOnClickListener(this);
        btnSdcard.setOnClickListener(this);
        btnUdisk.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);

        if (mSelectMode == SelectMode.SelectMultiFile) {
            mFileItemRecyclerViewAdapter = new SelectMulstFileItemRecyclerViewAdapter(mContext, mSelectMode);
        } else {
            mFileItemRecyclerViewAdapter = new SelectSingleFileItemRecyclerViewAdapter(mContext, mSelectMode);
        }

        mFileItemRecyclerViewAdapter.setOnItemClickLitener(this);
        UsbSdk.addRegister(this);

        initSdcard();
    }


    public void initSdcard() {
        String localPath = getEnvironmentPath();
        StatFs statFs = new StatFs(localPath);

        long blockSize;
        long totalBlocks;
        long availableBlocks;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            totalBlocks = statFs.getBlockCountLong();
            availableBlocks = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            totalBlocks = statFs.getBlockCount();
            availableBlocks = statFs.getAvailableBlocks();
        }

        String capacity = Formatter.formatFileSize(mActivity, availableBlocks * blockSize);
        String free = Formatter.formatFileSize(mActivity, totalBlocks * blockSize);
        btnSdcard.setText(String.format(" %s\r\n%s/%s", localPath, capacity, free));
        if (UsbSdk.hasDisk()) {
            return;
        }
        recyclerView.setAdapter(mFileItemRecyclerViewAdapter);
        root = getEnvironmentRoot();
        currPath = localPath;
        updateShowFiles();

    }

    public static String getEnvironmentPath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        return Environment.getDataDirectory().getPath();
    }

    public static File getEnvironmentRoot() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory();
        }
        return Environment.getDataDirectory();
    }


    @Override
    public void onItemClick(View view, final int position) {
        if (position >= list.size()) {
            return;
        }
        currPath = list.get(position).getAbsolutePath();
        if (list.get(position).isDirectory()) {
            root = list.get(position).usbFile;
            updateShowFiles();
        }

         /*
          if(currentFs!=null){
          switch (currentFs.getType()) {
                case PartitionTypes.FAT12:
                case PartitionTypes.FAT16:
                case PartitionTypes.FAT32:
                case PartitionTypes.LINUX_EXT:
                    currPath = list.get(position).getAbsolutePath();
                    if (list.get(position).isDirectory()) {
                        root = list.get(position).usbFile;
                        updateShowFiles();
                    }
                    break;
                default:
                    return;
            }
            }*/


    }

    @Override
    public void onCheckedSingle(View view, int position, boolean isChecked) {
        if (position >= list.size()) {
            return;
        }
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout()) {

            callCack.onSelectSingleCallBack(list.get(position).usbFile);
            dismiss();
        }
    }

    @Override
    public void onCheckedMulti(View view, int position, boolean isChecked) {
        if (position >= list.size()) {
            return;
        }
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.isComputingLayout()) {
            synchronized (list) {
                list.get(position).isChecked = isChecked;
            }
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_goback) {
            try {
                if (root != null) {
                    if (root instanceof UsbFile) {
                        if (((UsbFile) root).getParent() != null) {
                            root = ((UsbFile) root).getParent();
                        }
                    } else if (root instanceof File) {
                        if (((File) root).getParent() != null) {
                            root = ((File) root).getParentFile();
                        }
                    }
                    updateShowFiles();
                }
            } catch (Exception e) {
                dismiss();
                showToast("Not support File System!");
                return;
            }
        } else if (i == R.id.btn_udisk) {
            if (root != null) {
                root = currentFs.getRootDirectory();
                currPath = "/";
                updateShowFiles();
            }

        } else if (i == R.id.btn_sdcard) {
            root = getEnvironmentRoot();
            currPath = getEnvironmentPath();
            updateShowFiles();
        } else if (i == R.id.btn_exit) {
            this.dismiss();

        } else if (i == R.id.btn_confirm) {
            if (null != callCack && null != list) {
                synchronized (list) {
                    if (mSelectMode == SelectMode.SelectMultiFile) {
                        List callBackList = new ArrayList<>();
                        for (UsbFileItem item : list) {
                            if (item.isChecked) {
                                callBackList.add(item.usbFile);
                            }
                        }
                        callCack.onSelectMultiCallBack(callBackList);
                    } else {
                        for (UsbFileItem item : list) {
                            if (item.isChecked) {
                                callCack.onSelectSingleCallBack(item.usbFile);
                                break;
                            }
                        }

                    }
                }
            }
            this.dismiss();

        }

    }

    public void updateShowFiles() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> b) {
                try {
                    if (root instanceof File) {
                        currPath = ((File) root).getAbsolutePath();
                    } else {
                        currPath = ((UsbFile) root).getAbsolutePath();
                    }
                    list = UsbHelper.getUsbFileList(root, regex);
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
                    }

                    @Override
                    public void onComplete() {
                        mFileItemRecyclerViewAdapter.refresh(list);
                        tv_path.setText(currPath);
                    }
                });
    }


    @Override
    public void onChanged() {

        try {
            currentFs = UsbSdk.getFileSystem();
            if (null == currentFs) {
                btnUdisk.setVisibility(View.INVISIBLE);
                tv_path.setText("未检测到U盘");
                mFileItemRecyclerViewAdapter.refresh(new ArrayList<UsbFileItem>());
                return;
            }
            root = currentFs.getRootDirectory();
            if (root == null) {
                mFileItemRecyclerViewAdapter.refresh(new ArrayList<UsbFileItem>());
                tv_path.setText("未检测到U盘");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            mFileItemRecyclerViewAdapter.refresh(new ArrayList<UsbFileItem>());
            return;
        }
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> b) {
                try {
                    list = UsbHelper.getUsbFileList(root, regex);
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
                        Log.e(TAG, "error setting up device", e);
                        e.printStackTrace();
                        btnUdisk.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        String capacity = Formatter.formatFileSize(mContext, currentFs.getCapacity());
                        String free = Formatter.formatFileSize(mContext, currentFs.getFreeSpace());
                        btnUdisk.setVisibility(View.VISIBLE);
                        btnUdisk.setText(String.format(" %s\r\n%s/%s", currentFs.getVolumeLabel(), capacity, free));
                        recyclerView.setAdapter(mFileItemRecyclerViewAdapter);
                        mFileItemRecyclerViewAdapter.refresh(list);
                        tv_path.setText("/");
                    }
                });


    }


    public static class Builder {

        private UsbDiialogParams p;
        private UsbDialogFrament usbDialogFrament;

        public Builder(FragmentActivity activity) {
            p = new UsbDiialogParams();
            p.activity = activity;
        }

        public Builder setFilter(String regex) {
            p.regex = regex;
            return this;
        }

        public Builder setSelectCallBack(SelectCallBack callBack) {
            p.callBack = callBack;
            return this;
        }

        public Builder setSelectMode(SelectMode selectMode) {
            p.selectMode = selectMode;
            return this;
        }

        public UsbDialogFrament build() {
            usbDialogFrament = new UsbDialogFrament(p);
            return usbDialogFrament;
        }


        public void show() {
            if (null == usbDialogFrament) {
                build();
            }
            FragmentTransaction ft = usbDialogFrament.mActivity.getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            usbDialogFrament.show(ft, this.getClass().getSimpleName());
        }
    }

    public void showToast(String s) {
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        UsbSdk.removeRegister(this);
        super.onDestroy();
    }
}
