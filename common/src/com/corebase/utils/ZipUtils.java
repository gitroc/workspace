package com.corebase.utils;

import java.io.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    
    /**
     * 解压缩zip文件
     * @param zipFilePath
     * @param destPath
     * @throws Exception
     */
    public static void unZipFolder(String zipFilePath,String destPath) throws Exception{
        
        //创建输出目录
        File destFile = new File(destPath);
        if(!destFile.exists()){
            destFile.mkdirs();
        }
        
        //开始循环加压缩文件到目的文件夹中
        ZipInputStream inZip = null;
        FileOutputStream out = null;
        try {
            inZip = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = null;
            String zipEntryName = "";
            while((zipEntry = inZip.getNextEntry())!=null){
                zipEntryName = zipEntry.getName();
                if(zipEntry.isDirectory()){
                    File folder = new File(destPath+File.separator+zipEntryName);
                    folder.mkdirs();
                }else{
                    File file = new File(destPath+File.separator+zipEntryName);
                    if(file!=null&&!file.getParentFile().exists()){
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                    out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while((len = inZip.read(buffer))>0){
                        out.write(buffer,0,len);
                        out.flush();
                    }
                }
            }
        } finally {
            if(inZip != null) {
                inZip.close();
            }
            if(out != null) {
                out.close();
            }
        }
    }

    public static byte[] compressToBytes(String str) {
        if(str == null) {
            return null;
        }

        byte[] zipBytes = null;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ZipOutputStream zipOut = null;

        try {
            zipOut = new ZipOutputStream(byteOut);
            zipOut.putNextEntry(new ZipEntry("zipEntry"));
            zipOut.write(str.getBytes());
            zipOut.closeEntry();
            zipBytes = byteOut.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return zipBytes;
    }

    public static void compressToFile(File dest, String str) throws IOException {
        if(str == null) {
            return;
        }

        GZIPOutputStream zipOut = null;

        try {
            zipOut = new GZIPOutputStream(new FileOutputStream(dest));
            zipOut.write(str.getBytes());
        } finally {
            if(zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
