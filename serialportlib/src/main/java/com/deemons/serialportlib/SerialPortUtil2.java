package com.deemons.serialportlib;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2018/5/31.
 */
public class SerialPortUtil2 {

    private static String TAG = "SerialPortUtil2";
    /**
     * 标记当前串口状态(true:打开,false:关闭)
     **/
    public static boolean isFlagSerial2 = false;
    public static SerialPort serialPort = null;
    public static InputStream inputStream = null;
    public static OutputStream outputStream = null;
    public static Thread receiveThread = null;
    public static SerialCallBack serialCallBack;
    private static int code;

    public static void setSerialCallBack(SerialCallBack callBack) {
        serialCallBack = callBack;
    }

    /**
     * 打开串口
     * @param device        串口节点
     * @param baudrate      波特率
     * @return
     */
    public static boolean open(String device, int baudrate) {
        try {
            serialPort = new SerialPort(new File(device), baudrate, 1, 8, 1, 0);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            receiveCopy();
            isFlagSerial2 = true;
        } catch (IOException e) {
            e.printStackTrace();
            isFlagSerial2 = false;
        }
        return isFlagSerial2;
    }

    /**
     * 关闭串口
     */
    public static boolean close() {
        boolean isClose = false;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            isClose = true;
            isFlagSerial2 = false;//关闭串口时，连接状态标记为false
        } catch (IOException e) {
            e.printStackTrace();
            isClose = false;
        }
        return isClose;
    }

    /**
     * 发送16进制串口指令
     */
    public static void sendString(int codes, String data) {
        code = codes;
        if (!isFlagSerial2) {
            return;
        }
        try {
            outputStream.write(ByteUtils.hex2byte(data));
            outputStream.flush();
            Log.e(TAG, "App--->串口:" + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收串口数据的方法
     */
    public static void receiveCopy() {
        if (receiveThread != null && !isFlagSerial2) {
            return;
        }
        receiveThread = new Thread() {
            @Override
            public void run() {
                while (isFlagSerial2) {
                    try {
                        byte[] readData = new byte[32];
                        if (inputStream == null) {
                            return;
                        }
                        int size = inputStream.read(readData);
                        if (size > 0 && isFlagSerial2) {
                            String strData = ByteUtils.byteToStr(readData, size);
                            serialCallBack.onSerialData(code, strData);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        receiveThread.start();
    }

    /**
     * 串口数据回调
     */
    public interface SerialCallBack {
        void onSerialData(int code, String serialPortData);
    }
}