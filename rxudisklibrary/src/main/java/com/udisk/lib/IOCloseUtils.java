package com.udisk.lib;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by junx on 2017/9/8.
 */

public final class IOCloseUtils {

    private IOCloseUtils() {
    }

    public static final void closeStream(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (null != closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    closeable = null;
                }
            }
        }
    }
}
