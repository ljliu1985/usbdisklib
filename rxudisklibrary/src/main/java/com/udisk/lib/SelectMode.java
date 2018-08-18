package com.udisk.lib;

/**
 * Created by ljliu on 2018/6/15.
 */

public enum SelectMode {
    /**
     * 选择单个文件，选择多个文件
     */
    SelectSingleDir("目录单选",0),
    SelectMultiFile("文件多选",1),
    SelectSingleFile("文件单选",2);

    String name;
    int index;
    SelectMode(String name, int index) {
        this.name = name;
        this.index = index;
    }


}
