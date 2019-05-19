package cn.lastwhisper.server.beans;

import java.io.IOException;
import java.util.Properties;

/**
 * @author lastwhisper
 * @desc
 */
public class HttpServerConfig {
    private static int port;
    private static String index;
    private static String error;

    static {
        Properties pros = new Properties();
        try {
            pros.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("mytomcat.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        port = Integer.valueOf(pros.getProperty("port"));
        index = pros.getProperty("index");
        error = pros.getProperty("error");
    }

    public static int getPort() {
        return port;
    }

    public static String getIndex() {
        return index;
    }

    public static String getError() {
        return error;
    }
}
