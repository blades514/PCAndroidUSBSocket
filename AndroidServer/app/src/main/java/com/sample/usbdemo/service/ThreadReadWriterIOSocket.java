package com.sample.usbdemo.service;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.sample.usbdemo.event.DownloadEvent;
import com.sample.usbdemo.util.FileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThreadReadWriterIOSocket implements Runnable {
    private Socket client;
    private Context context;

    public ThreadReadWriterIOSocket(Context context, Socket client) {
        this.client = client;
        this.context = context;
    }

    @Override
    public void run() {
        Log.d(androidService.TAG, "a client has connected to server!");
        BufferedOutputStream out;
        BufferedInputStream in;
        try {
            // PC端发来的数据msg
            String currCMD = "";
            out = new BufferedOutputStream(client.getOutputStream());
            in = new BufferedInputStream(client.getInputStream());
            androidService.ioThreadFlag = true;
            while (androidService.ioThreadFlag) {
                try {
                    if (!client.isConnected()) {
                        break;
                    }
                    // 接收PC发来的数据
                    Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "will read......");
                    // 读操作命令
                    currCMD = readCMDFromSocket(in);
                    Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "**currCMD ==== " + currCMD);
                    // 根据命令分别处理数据
                    if (currCMD.equals("sendFile")) {
                        // 准备接收文件数据
                        try {
                            out.write("service receive OK".getBytes());
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 接收文件数据，4字节文件长度，4字节文件格式，其后是文件数据
                        byte[] filelength = new byte[4];
                        byte[] fileformat = new byte[4];
                        byte[] filebytes = null;

                        // 从socket流中读取完整文件数据
                        filebytes = receiveFileFromSocket(in, out, filelength, fileformat);

                        try {
                            // 生成文件
                            String filesDir = FileUtils.getFile(context);
                            File file = new File(filesDir, "update.zip");
                            if (file.exists()) {
                                file.delete();
                            }
                            file.createNewFile();
                            Log.v(androidService.TAG, "fileName:" + file.getName());
                            Log.v(androidService.TAG, "filePath:" + file.getPath());

                            writeFile(file, filebytes, 0, filebytes.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (client != null) {
                    Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "client.close()");
                    client.close();
                }
            } catch (IOException e) {
                Log.e(androidService.TAG, Thread.currentThread().getName() + "---->" + "read write error333333");
                e.printStackTrace();
            }
        }
    }


    public void writeFile(File file, byte[] data, int offset, int count) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(data, offset, count);
        Log.v(androidService.TAG, "文件保存完成");
        Log.v(androidService.TAG, "file.length:" + file.length());
        EventBus.getDefault().post(new DownloadEvent(file));
    }



    /**
     * 功能：从socket流中读取完整文件数据
     * <p>
     * InputStream in：socket输入流
     * <p>
     * byte[] filelength: 流的前4个字节存储要转送的文件的字节数
     * <p>
     * byte[] fileformat：流的前5-8字节存储要转送的文件的格式（如.apk）
     */
    public static byte[] receiveFileFromSocket(InputStream in, OutputStream out, byte[] filelength, byte[] fileformat) {
        byte[] filebytes = null;// 文件数据
        try {
            in.read(filelength);// 读文件长度
            int filelen = bytesToInt(filelength);// 文件长度从4字节byte[]转成Int
            String strtmp = "read file length ok:" + filelen;
            out.write(strtmp.getBytes("utf-8"));
            out.flush();

            filebytes = new byte[filelen];
            int pos = 0;
            int rcvLen = 0;
            while ((rcvLen = in.read(filebytes, pos, filelen - pos)) > 0) {
                pos += rcvLen;
            }
            Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "read file OK:file size="
                    + filebytes.length);
            out.write("read file ok".getBytes("utf-8"));
            out.flush();
        } catch (Exception e) {
            Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "receiveFileFromSocket error");
            e.printStackTrace();
        }
        return filebytes;
    }

    /**
     * 读取命令
     */
    public String readCMDFromSocket(InputStream in) {
        int MAX_BUFFER_BYTES = 2048;
        String msg = "";
        byte[] tempbuffer = new byte[MAX_BUFFER_BYTES];
        try {
            int numReaderBytes = in.read(tempbuffer, 0, tempbuffer.length);
            if (numReaderBytes != -1) {
                msg = new String(tempbuffer, 0, numReaderBytes, StandardCharsets.UTF_8);
            }
            tempbuffer = null;
        } catch (Exception e) {
            Log.v(androidService.TAG, Thread.currentThread().getName() + "---->" + "readFromSocket error");
            e.printStackTrace();
        }
        return msg;
    }

    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;
        addr |= ((bytes[1] << 8) & 0xFF00);
        addr |= ((bytes[2] << 16) & 0xFF0000);
        addr |= ((bytes[3] << 25) & 0xFF000000);
        return addr;

    }
}