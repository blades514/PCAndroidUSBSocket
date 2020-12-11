package com.sample.main;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 测试usb与pc通信 通过adb端口转发方式
 *
 * @author chl
 */
public class PCClient {

    public static void main(String[] args) throws InterruptedIOException {
        try {
            // adb 指令
            Runtime.getRuntime().exec("adb shell am broadcast -a NotifyServiceStop");
            Thread.sleep(3000);
            Runtime.getRuntime().exec("adb forward tcp:12580 tcp:10088"); // 端口转换
            Thread.sleep(3000);
            Runtime.getRuntime().exec("adb shell am broadcast -a NotifyServiceStart");
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();

        }
        Socket socket = null;
        try {
            InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
            System.out.println("Connecting...");
            socket = new Socket(inetAddress, 12580);
            System.out.println("socket receive");
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("请输入1将发送文件到手机端：");
                String strWord = br.readLine();
                if ("1".equals(strWord)) {
                    // 通知手机端 准备发送文件
                    out.write("sendFile".getBytes());
                    out.flush();
                    System.out.println("通知手机端 准备发送文件");
                    // 手机返回
                    System.out.println("手机端回执：" + readFromSocket(in));
                    byte[] fileBytes = getFileBytes();
                    System.out.println("要发送文件大小：" + fileBytes.length);
                    // 将整数转成4字节byte数组
                    byte[] fileLength = intToByte(fileBytes.length);
                    // 字节流中前4字节为文件长度，4字节文件格式，以后是文件流
                    // 注意如果write里的byte[]超过socket的缓存，系统自动分包写过去，所以对方要循环写完
                    out.write(fileLength);
                    out.flush();
                    System.out.println("通知手机端文件大小");
                    // 手机返回
                    System.out.println("手机端回执：" + readFromSocket(in));
                    // 发送文件
                    out.write(fileBytes);
                    out.flush();
                    System.out.println("发送文件到手机端");

                    // 手机返回
                    String strRead = readFromSocket(in);
                    System.out.println("手机端回执：" + strRead);
                    System.out.println("=============================================");
                }
            }

        } catch (Exception e) {
            System.out.println("TCP ERROR:" + e.toString());
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                    System.out.println("socket关闭");
                }
            } catch (IOException e) {
                System.out.println("TCP ERROR:" + e.toString());
            }
        }
    }

    /**
     * 从InputStream流中读数据
     */
    public static String readFromSocket(InputStream in) {
        int MAX_BUFFER_BYTES = 4000;
        String msg = "";
        byte[] tempBuffer = new byte[MAX_BUFFER_BYTES];
        try {
            int numReaderBytes = in.read(tempBuffer, 0, tempBuffer.length);
            msg = new String(tempBuffer, 0, numReaderBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * Int转byte[]
     */
    public static byte[] intToByte(int i) {
        byte[] abyte0 = new byte[4];
        abyte0[0] = (byte) (0xff & i);
        abyte0[1] = (byte) ((0xff00 & i) >> 8);
        abyte0[2] = (byte) ((0xff0000 & i) >> 16);
        abyte0[3] = (byte) ((0xff000000 & i) >> 24);
        return abyte0;
    }

    /**
     * 获取文件字节流
     * */
    public static byte[] getFileBytes() throws IOException {
        File file = new File("C:\\Users\\Admin\\Desktop\\ConnectToAndroid", "update.zip");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int l = bis.available();
        byte[] b = new byte[l];
        bis.read(b, 0, l);
        bis.close();
        return b;
    }
}
