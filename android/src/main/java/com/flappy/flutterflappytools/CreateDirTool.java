package com.flappy.flutterflappytools;

import android.text.TextUtils;

import java.io.File;


public class CreateDirTool {

    public static void createDir(String DIRPATH) throws Exception {

        File file = new File(DIRPATH);
        synchronized (CreateDirTool.class) {
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Exception e = new Exception(
                            "createDir failed, no dir or dirpath wrong");
                    throw e;
                }
            }
        }
        File nomidia = new File(DIRPATH + ".nomedia");
        if (!nomidia.exists()) {
            nomidia.createNewFile();
        }
    }

    public static boolean deleteFile(File file) {
        boolean b = true;
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return true;
            }
            for (int i = 0; i < childFiles.length; i++) {
                boolean men = deleteFile(childFiles[i]);
                if (!men) {
                    b = false;
                }
            }
        } else {
            if (!file.delete()) {
                b = false;
            }
            file = null;
        }
        return b;
    }


    public static boolean isExitsSdcard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else return !TextUtils.isEmpty(android.os.Environment
                .getExternalStorageDirectory().getAbsolutePath());
    }
}
