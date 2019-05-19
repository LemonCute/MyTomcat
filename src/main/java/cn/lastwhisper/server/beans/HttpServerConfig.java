package cn.lastwhisper.server.beans;

import java.io.IOException;
import java.util.Properties;

/**
 * @author lastwhisper
 * @desc
 */
public class HttpServerConfig {
    private static Integer port;
    private static String index;
    private static String error;
    private static Integer corePoolSize;
    private static Integer maximumPoolSize;
    private static Long keepAliveTime;
    private static Integer queueSize;

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
        corePoolSize = Integer.valueOf(pros.getProperty("corePoolSize"));
        maximumPoolSize = Integer.valueOf(pros.getProperty("maximumPoolSize"));
        keepAliveTime = Long.valueOf(pros.getProperty("keepAliveTime"));
        queueSize = Integer.valueOf(pros.getProperty("queueSize"));
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

    public static Integer getCorePoolSize() {
        return corePoolSize;
    }

    public static Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public static Long getKeepAliveTime() {
        return keepAliveTime;
    }

    public static Integer getQueueSize() {
        return queueSize;
    }
}
