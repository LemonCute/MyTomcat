package cn.lastwhisper.server.core;

import cn.lastwhisper.server.beans.HttpServerConfig;
import cn.lastwhisper.server.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * @author lastwhisper
 * @desc 分发器 加入状态内容处理 404 505 以及首页
 */
public class Dispatcher implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private Socket client;
    private Request request;
    private Response response;

    public Dispatcher(Socket client) {
        this.client = client;
        try {
            // 获取请求
            // 获取响应
            request = new Request(client);
            response = new Response(client);
        } catch (IOException e) {
            e.printStackTrace();
            IOUtil.closeSocket(client);
            logger.error("Dispatcher初始化错误");
        }

    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();

            if (request.getUrl() == null || request.getUrl().equals("")) {
                IOUtil.responseHtml(response, 200, HttpServerConfig.getIndex());
            }
            if (request.getUrl().endsWith("html")) {
                IOUtil.responseHtml(response, 200, request.getUrl());
            }
            Servlet servlet = WebApp.getServletFromUrl(request.getUrl());
            if (servlet != null) {
                servlet.service(request, response);
                response.pushToBrowser(200);
            } else {
                // 错误....
                IOUtil.responseHtml(response, 404, HttpServerConfig.getError());
            }
            long end = System.currentTimeMillis();
            logger.info("reuqest url /" + request.getUrl() + " spend time " + (end - start) + "ms");
        } catch (IOException e) {
            try {
                response.println("服务器端错误");
                response.pushToBrowser(505);
            } catch (IOException e1) {
                e1.printStackTrace();
                logger.error("dispatcher处理请求错误");
            }
        }
        IOUtil.closeSocket(client);
    }

}
