package cn.lastwhisper.server.core;

import cn.lastwhisper.server.beans.HttpServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @author lastwhisper
 * @desc 多线程处理加入分发器
 */
public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService pool;

    // 启动服务
    public void start() {
        try {
            serverSocket = new ServerSocket(HttpServerConfig.getPort());
            pool = new ThreadPoolExecutor(HttpServerConfig.getCorePoolSize(),
                    HttpServerConfig.getMaximumPoolSize(), HttpServerConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(HttpServerConfig.getQueueSize()),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.AbortPolicy());
            isRunning = true;
            logger.info("MyTomcat server " + HttpServerConfig.getPort() + " startup");
            revice();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("MyTomcat server startup fail");
        }
    }

    // 接受连接请求，响应请求
    public void revice() {
        while (isRunning) {
            try {
                Socket client = serverSocket.accept();
                pool.execute(new Dispatcher(client));
                //new Thread(new Dispatcher(client)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 停止服务
    public void stop() {
        isRunning = false;
        try {
            this.serverSocket.close();
            pool.shutdown();
            logger.info("MyTomcat server stop");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
