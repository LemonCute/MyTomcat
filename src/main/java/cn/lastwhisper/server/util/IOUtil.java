package cn.lastwhisper.server.util;

import cn.lastwhisper.server.core.Response;

import java.io.*;
import java.net.Socket;

/**
 * @author lastwhisper
 * @desc
 */
public class IOUtil {
    /**
     *
     * @author lastwhisper
     * @desc 将InputStream中的内容转为String
     * @param inputStream
     * @return
     */
    public static String InputStreamToString(InputStream inputStream) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @author lastwhisper
     * @desc 将html响应给客户端
     * @param response
     * @param status 响应状态码
     * @param htmlName
     * @return void
     */
    public static void responseHtml(Response response, int status, String htmlName) {
        InputStream is = null;
        try {
            is = new FileInputStream(PathUtil.getRootPath("webapps/" + htmlName));
            response.print(IOUtil.InputStreamToString(is));
            response.pushToBrowser(status);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @author lastwhisper
     * @desc 释放socket资源
     */
    public static void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
