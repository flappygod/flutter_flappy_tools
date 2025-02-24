package com.flappy.flutter_flappy_tools;

import java.io.File;


public class CreateDirTool {


    ///delete file
    public static boolean deleteFile(File file) {
        boolean flag = true;
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return true;
            }
            for (File childFile : childFiles) {
                boolean men = deleteFile(childFile);
                if (!men) {
                    flag = false;
                }
            }
        } else {
            if (!file.delete()) {
                flag = false;
            }
        }
        return flag;
    }

}
