package cn.lastwhisper.server.core;


import cn.lastwhisper.server.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.*;

/**
 * @author lastwhisper
 * @desc 封装请求协议: 获取 method uri以及请求参数
 */
public class Request {
    private static Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    // 回车
    private final String CRLF = "\r\n";
    // http请求信息
    private String requestInfo;
    // 请求方式
    private String method;
    // 请求url
    private String url;
    // 请求参数字符串username=zhangsan&pwd=123456
    private String queryParamStr;
    // 存储请求参数
    private Map<String, List<String>> parameterMap;

    public Request(Socket client) throws IOException {
        this(client.getInputStream());
    }

    public Request(InputStream is) {
        parameterMap = new HashMap<String, List<String>>();
        byte[] datas = new byte[1024 * 1024 * 10];
        int len;
        try {
            len = is.read(datas);
            requestInfo = new String(datas, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            logger.error("datas value："+datas);
            return;
        }
        // 分解字符串
        parseRequestInfo();
        // 转成Map uname=张三&hobby=篮球&hobby=读书&other=
        parseParams();
    }

    /**
     *
     * @author lastwhisper
     * @desc 解析http请求信息
     */
    private void parseRequestInfo() {
        // 1. 获取请求方式: 开头到第一个/
        method = requestInfo.substring(0, requestInfo.indexOf("/")).toLowerCase().trim();
        // 2. 获取请求url: 第一个/ 到 HTTP/;获取get参数
        // 2.1 获取/的位置
        int startIdx = requestInfo.indexOf("/") + 1;
        // 2.2 获取 HTTP/的位置
        int endIdx = requestInfo.indexOf("HTTP/");
        // 2.3 分割字符串
        url = requestInfo.substring(startIdx, endIdx).trim();
        // 2.4 获取?的位置
        int queryIdx = url.indexOf("?");
        // 表示存在get请求参数
        if (queryIdx >= 0) {
            String[] urlParam = url.split("\\?");
            url = urlParam[0];
            queryParamStr = urlParam[1].trim();
        }

        // 3. 获取post请求参数
        if ("post".equals(method)) {
            String requestBody = requestInfo.substring(requestInfo.lastIndexOf(CRLF)).trim();
            if (null == queryParamStr) {
                queryParamStr = requestBody;
            } else {
                queryParamStr += "&" + requestBody;
            }
        }
        queryParamStr = null == queryParamStr ? "" : queryParamStr;
    }

    /**
     * @author lastwhisper
     * @desc 将请求参数转为Map uname=张三&hobby=篮球&hobby=读书&other=
     */
    private void parseParams() {
        // 1. 分割字符串 &
        String[] keyValues = this.queryParamStr.split("&");
        for (String queryStr : keyValues) {
            // 2. 再次分割字符串 =
            String[] kv = queryStr.split("=");
            // 防止other= ,value没有值导致kv[1]数组越界,
            kv = Arrays.copyOf(kv, 2);
            // 3. 获取key和value
            String key = kv[0];
            String value = kv[1] == null ? null : decode(kv[1], "utf-8");
            // 4. 存储到Map中
            if (!parameterMap.containsKey(key)) { // 第一次出现key（name）
                parameterMap.put(key, new ArrayList<String>());
            }
            parameterMap.get(key).add(value);
        }
    }

    /**
     *
     * @author lastwhisper
     * @desc 处理中文
     * @param value 待处理字符串
     * @param enc   解码后的编码
     * @return
     */
    private String decode(String value, String enc) {
        try {
            return java.net.URLDecoder.decode(value, enc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @author lastwhisper
     * @desc 通过name获取对应的多个值
     * @param key 参数名
     * @return
     */
    public String[] getParameterValues(String key) {
        List<String> values = this.parameterMap.get(key);
        if (null == values || values.size() < 1) {
            return null;
        }
        return values.toArray(new String[0]);
    }

    /**
     *
     * @author lastwhisper
     * @desc 通过name获取对应的一个值
     * @param key 参数名
     * @return
     */
    public String getParameter(String key) {
        String[] values = getParameterValues(key);
        return values == null ? null : values[0];
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getQueryParamStr() {
        return queryParamStr;
    }
}
