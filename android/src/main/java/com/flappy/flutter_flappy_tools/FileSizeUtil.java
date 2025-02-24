package com.flappy.flutter_flappy_tools;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;


/****************************
 * version  1.0.0
 */
public class FileSizeUtil {
    public static final int SIZE_TYPE_B = 1;
    public static final int SIZE_TYPE_KB = 2;
    public static final int SIZE_TYPE_MB = 3;
    public static final int SIZE_TYPE_GB = 4;

    //get size
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
            return formatFileSize(blockSize, sizeType);
        } catch (Exception e) {
            return 0;
        }
    }


    //get file size long
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        } else {
            file.createNewFile();
        }
        return size;
    }

    //get dic size
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File[] fList = f.listFiles();
        assert fList != null;
        for (File file : fList) {
            if (file.isDirectory()) {
                size = size + getFileSizes(file);
            } else {
                size = size + getFileSize(file);
            }
        }
        return size;
    }


    //format file size by setting
    private static double formatFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZE_TYPE_B:
                fileSizeLong = Double.parseDouble(df.format((double) fileS));
                break;
            case SIZE_TYPE_KB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1024.0));
                break;
            case SIZE_TYPE_MB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1048576.0));
                break;
            case SIZE_TYPE_GB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1073741824.0));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }


}