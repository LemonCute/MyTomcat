package cn.lastwhisper.server.core;

import cn.lastwhisper.server.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 	web
 * @author lastwhisper
 *
 */
public class WebApp {

    private static Logger logger = LoggerFactory.getLogger(WebApp.class);

    private static WebXmlContext webContext;

    static {
        try {
            // SAX解析
            // 1、获取解析工厂
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // 2、从解析工厂获取解析器
            SAXParser parse = factory.newSAXParser();
            // 3、编写处理器
            // 4、加载文档 Document 注册处理器
            WebXmlHandler handler = new WebXmlHandler();
            // 5、解析
            String webxmlpath = PathUtil.getRootPath("webapps" + "/WEB-INF/web.xml");
            parse.parse(webxmlpath, handler);
            // 获取数据
            webContext = new WebXmlContext(handler.getEntitys(), handler.getMappings());
        } catch (Exception e) {
            logger.error("parse web.xml fail");
        }
    }

    /**
     *
     * @author lastwhisper
     * @desc 通过url获取配置文件对应的servlet
     * @param url
     * @return
     */
    public static Servlet getServletFromUrl(String url) {
        // /login
        String className = webContext.getClazz("/" + url);
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
            Servlet servlet = (Servlet) clazz.getConstructor().newInstance();
            return servlet;
        } catch (Exception e) {
        }
        return null;

    }

}
