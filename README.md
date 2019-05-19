>该项目源码地址：https://github.com/ggb2312/MyTomcat

# 1. 项目简介
一个极简的tomcat。使用java实现最基本的tomcat的功能，能够接收Http请求、处理Http请求（提供Servlet接口）、响应Http请求。

项目结构：

![项目结构](https://upload-images.jianshu.io/upload_images/5336514-85401dbfefcd31a6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


项目演示：

![项目运行](https://upload-images.jianshu.io/upload_images/5336514-3b98a9965b0d0969.gif?imageMogr2/auto-orient/strip)


# 2. 涉及的技术
- I/O
- 网络编程
- XML解析 https://www.jianshu.com/p/8df626ea70ed
- 反射 
- HTTP协议 https://www.runoob.com/http/http-messages.html

# 3. 各部分设计



## 3.1 servlet接口设计

```java
/**
 * @author lastwhisper
 * @desc 服务器小脚本接口
 */
public abstract class Servlet {
	/**
	 * 
	 * @author lastwhisper
	 * @desc 处理GET请求
	 * @param request
	 * @param response
	 */
	public abstract void doGet(Request request, Response response);

	/**
	 * 
	 * @author lastwhisper
	 * @desc 处理POST请求
	 * @param request
	 * @param response
	 */
	public abstract void doPost(Request request, Response response);

	/**
	 * 
	 * @author lastwhisper
	 * @desc 处理请求
	 * @param request
	 * @param response
	 */
	public void service(Request request, Response response) {
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			doGet(request, response);
		} else if ("POST".equalsIgnoreCase(request.getMethod())) {
			doPost(request, response);
		}
	}
}
```



## 3.2  解析web.xml、反射创建对象

使用过servlet的朋友都知道，tomcat通过web.xml的配置找到url与对应的servlet全名，然后通过反射创建servlet。

比如有如下web.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app>
	<servlet>
		<servlet-name>login</servlet-name>
		<servlet-class>cn.lastwhisper.server.basic.servlet.LoginServlet</servlet-class>
	</servlet>
	<servlet>
		<servlet-name>reg</servlet-name>
		<servlet-class>cn.lastwhisper.server.basic.servlet.RegisterServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>login</servlet-name>
		<url-pattern>/login</url-pattern>
		<url-pattern>/g</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>reg</servlet-name>
		<url-pattern>/reg</url-pattern>
	</servlet-mapping>
</web-app>
```

使用java解析xml，并通过反射创建对象。
```java
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author lastwhisper
 *
 */
public class XmlConvertion {

	public static void main(String[] args) throws Exception{
		// SAX解析
		// 1、获取解析工厂
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// 2、从解析工厂获取解析器
		SAXParser parse = factory.newSAXParser();
		// 3、编写处理器
		// 4、加载文档 Document 注册处理器
		WebHandler handler = new WebHandler();
		// 5、解析
		parse.parse(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("cn/lastwhisper/server/basic/servlet/web.xml"), handler);

		// 获取数据
		WebContext context = new WebContext(handler.getEntitys(), handler.getMappings());
		// 假设你输入了 /login
		String className = context.getClazz("/g");
		Class clz = Class.forName(className);
		Servlet servlet = (Servlet) clz.getConstructor().newInstance();
		servlet.service();
	}

}

class WebHandler extends DefaultHandler {
	private List<Entity> entitys;
	private List<Mapping> mappings;
	private Entity entity;
	private Mapping mapping;
	private String tag; // 存储操作标签
	private boolean isMapping = false;

	@Override
	public void startDocument() throws SAXException {
		entitys = new ArrayList<Entity>();
		mappings = new ArrayList<Mapping>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		tag = qName;
		if ("servlet".equals(tag)) {
			entity = new Entity();
			isMapping = false;
		} else if ("servlet-mapping".equals(tag)) {
			mapping = new Mapping();
			isMapping = true;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String contents = new String(ch, start, length).trim();
		if (!isMapping) { // 操作servlet
			if ("servlet-name".equals(tag)) {
				entity.setName(contents);
			} else if ("servlet-class".equals(tag)) {
				entity.setClazz(contents);
			}
		} else { // 操作servlet-mapping
			if ("servlet-name".equals(tag)) {
				mapping.setName(contents);
			} else if ("url-pattern".equals(tag)) {
				mapping.addPatterns(contents);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("servlet".equals(qName)) {
			entitys.add(entity);
		} else if ("servlet-mapping".equals(qName)) {
			mappings.add(mapping);
		}
		tag = null; // tag丢弃了
	}

	@Override
	public void endDocument() throws SAXException {
	}

	public List<Entity> getEntitys() {
		return entitys;
	}

	public List<Mapping> getMappings() {
		return mappings;
	}

}
```

## 3.3 接收Http请求

接收Http请求的步骤：

1. 创建serversocket
2. 建立连接获取socket
3. 通过输入流获取请求协议

```java
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lastwhisper
 * @desc 使用ServerSocket建立与浏览器的连接，获取请求协议
 */
public class Server01 {
	private ServerSocket serverSocket;

	public static void main(String[] args) {
		Server01 server = new Server01();
		server.start();
	}

	// 启动服务
	public void start() {
		try {
			serverSocket = new ServerSocket(8888);
			revice();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("服务器启动失败....");
		}
	}

	// 接受连接处理
	public void revice() {
		try {
			// 接受连接
			Socket socket = serverSocket.accept();
			// 获取请求协议
			InputStream inputStream = socket.getInputStream();
			byte[] datas = new byte[1024 * 1024];
			int len = inputStream.read(datas);
			String request = new String(datas, 0, len);
			System.out.println(request);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("客户端错误");
		}
	}

	// 停止服务
	public void stop() {

	}
}

```
运行main方法，使用浏览器访问 http://localhost:8888/ 可以看到控制台输出了浏览器发起的Http请求

```
GET / HTTP/1.1
Host: localhost:8888
Connection: keep-alive
Purpose: prefetch
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
Accept-Encoding: gzip, deflate, br
Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7
Cookie: Hm_lvt_8875c662941dbf07e39c556c8d97615f=1554520109,1555680733
````


## 3.4 响应Http请求

响应Http请求的步骤：

1. 准备响应内容
2. 获取字节数的长度
3. 拼接响应协议（注意空格与换行）
4. 使用输入输出流

```java
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * @author lastwhisper
 * @desc 返回响应协议
 */
public class Server02 {
	private ServerSocket serverSocket;

	public static void main(String[] args) {
		Server02 server = new Server02();
		server.start();
	}

	// 启动服务
	public void start() {
		try {
			serverSocket = new ServerSocket(8888);
			revice();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("服务器启动失败....");
		}
	}

	// 接受连接请求，响应请求
	public void revice() {
		try {
			// 1. 接受连接请求
			Socket client = serverSocket.accept();
			// 2. 获取http请求内容
			InputStream inputStream = client.getInputStream();
			byte[] datas = new byte[1024 * 1024];
			int len = inputStream.read(datas);
			String request = new String(datas, 0, len);
			System.out.println(request);

			// 3. 构造响应请求
			StringBuilder content = new StringBuilder();
			content.append("<html>");
			content.append("<head>");
			content.append("<title>");
			content.append("服务器响应成功");
			content.append("</title>");
			content.append("</head>");
			content.append("<body>");
			content.append("tomcat server终于回来了。。。。");
			content.append("</body>");
			content.append("</html>");
			int size = content.toString().getBytes().length; // 必须获取字节长度

			StringBuilder response = new StringBuilder();
			String blank = " ";
			String CRLF = "\r\n";
			// 3.1 响应行: HTTP/1.1 200 OK
			response.append("HTTP/1.1").append(blank);
			response.append(200).append(blank);
			response.append("OK").append(CRLF);
			// 3.2 响应头(最后一行存在空行):
			/*
			 * Date:Mon,31Dec209904:25:57GMT 
			 * Server:tomcat Server/0.0.1;charset=GBK
			 * Content-type:text/html 
			 * Content-length:39725426
			 */
			response.append("Date:").append(new Date()).append(CRLF);
			response.append("Server:").append("tomcat Server/0.0.1;charset=GBK").append(CRLF);
			response.append("Content-type:text/html").append(CRLF);
			response.append("Content-length:").append(size).append(CRLF);
			response.append(CRLF);
			// 3.3 正文
			response.append(content.toString());
			// 4. 响应到到客户端
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			bw.write(response.toString());
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("客户端错误");
		}
	}

	// 停止服务
	public void stop() {

	}
}

```
运行main方法，在浏览器访问 http://localhost:8888/ ，即可看到自己构造的响应。


## 3.5 封装响应Response

由于每次构造的响应只有响应内容和响应状态码是不固定的，所以可用封装响应。

1. 动态添加内容
2. 根据状态码拼接响应头协议
3. 根据状态码响应请求

```java
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;

/**
 * @author lastwhisper
 * @desc 封装响应实体
 */
public class Response {
	private BufferedWriter bw;

	// 响应正文
	private StringBuilder content;
	// 协议头（响应行、响应头、回车）信息
	private StringBuilder headInfo;
	// 正文的字节数
	private int len;

	private final String BLANK = " ";
	private final String CRLF = "\r\n";

	private Response() {
		content = new StringBuilder();
		headInfo = new StringBuilder();
		len = 0;
	}

	public Response(Socket client) throws IOException {
		this(client.getOutputStream());
	}

	public Response(OutputStream os) {
		this();
		bw = new BufferedWriter(new OutputStreamWriter(os));
	}

	// 动态添加内容（无换行）
	public Response print(String info) {
		content.append(info);
		len += info.getBytes().length;
		return this;
	}

	// 动态添加内容（有换行）
	public Response println(String info) {
		content.append(info).append(CRLF);
		len += (info + CRLF).getBytes().length;
		return this;
	}

	// 构建响应行、响应头信息
	private void createHeadInfo(int code) {
		// 1.响应行 HTTP/1.1 200 OK
		headInfo.append("HTTP/1.1").append(BLANK);
		headInfo.append(code).append(BLANK);
		switch (code) {
		case 200:
			headInfo.append("OK").append(CRLF);
			break;
		case 404:
			headInfo.append("NOT FOUND").append(CRLF);
			break;
		case 505:
			headInfo.append("SERVER ERROR").append(CRLF);
			break;
		}

		// 2.响应头(最后一行存在空行)
		/*
		 * Date:Mon,31Dec209904:25:57GMT 
		 * Server:tomcat Server/0.0.1;charset=GBK
		 * Content-type:text/html 
		 * Content-length:39725426
		 */
		headInfo.append("Date:").append(new Date()).append(CRLF);
		headInfo.append("Server:").append("tomcat Server/0.0.1;charset=GBK").append(CRLF);
		headInfo.append("Content-type:text/html").append(CRLF);
		headInfo.append("Content-length:").append(len).append(CRLF);
		headInfo.append(CRLF);
	}

	// 推送响应信息
	public void pushToBrowser(int code) throws IOException {
		if (null == headInfo) {
			code = 505;
		}
		createHeadInfo(code);
		bw.append(headInfo);
		bw.append(content);
		bw.flush();
	}
}

```


## 3.6 封装请求request

http的请求消息如下：

```
GET /hello HTTP/1.1
User-Agent: curl/7.16.3 libcurl/7.16.3 OpenSSL/0.9.7l zlib/1.2.3
Host: www.example.com
Accept-Language: en, mi
```
我们需要将请求类型、url、以及请求参数提取出来，用来映射到servlet上。

1. 通过分解字符串获取method、url、get参数、post请求体
2. 通过Map封装请求参数

```java
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lastwhisper
 * @desc 封装请求协议: 获取 method uri以及请求参数
 */
public class Request2 {
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
	// 回车
	private final String CRLF = "\r\n";

	public Request2(Socket client) throws IOException {
		this(client.getInputStream());
	}

	public Request2(InputStream is) {
		parameterMap = new HashMap<String, List<String>>();
		byte[] datas = new byte[1024 * 1024];
		int len;
		try {
			len = is.read(datas);
			requestInfo = new String(datas, 0, len);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// 分解字符串
		parseRequestInfo();
		// 转成Map uname=张三&hobby=篮球&hobby=读书&other=
		convertMap();
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
		url = requestInfo.substring(startIdx, endIdx);
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
//		System.out.println(method + "-->" + url + "-->" + queryParamStr);
	}

	/**
	 * @author lastwhisper
	 * @desc 将请求参数转为Map uname=张三&hobby=篮球&hobby=读书&other=
	 */
	private void convertMap() {
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

```


## 3.7 增加内容分发器

使用内容分发器，可以同时处理多个请求，并区分静态页面请求和动态请求，使用的是短连接。

```java
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * @author lastwhisper
 * @desc 分发器 加入状态内容处理 404 505 以及首页
 */
public class Dispatcher implements Runnable {
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
			IOUtils.closeSocket(client);
		}

	}

	@Override
	public void run() {
		try {
			if (request.getUrl() == null || request.getUrl().equals("")) {
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("index.html");
				response.print(IOUtils.InputStreamToString(is));
				response.pushToBrowser(200);
				is.close();
			}
			if (request.getUrl().endsWith("html")) {
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(request.getUrl());
				response.print(IOUtils.InputStreamToString(is));
				response.pushToBrowser(200);
				is.close();
			}
			Servlet servlet = WebApp.getServletFromUrl(request.getUrl());
			if (servlet != null) {
				servlet.service(request, response);
				response.pushToBrowser(200);
			} else {
				// 错误....
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("error.html");
				response.print(IOUtils.InputStreamToString(is));
				response.pushToBrowser(404);
				is.close();
			}
		} catch (IOException e) {
			try {
				response.println("服务器端错误");
				response.pushToBrowser(505);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		IOUtils.closeSocket(client);
	}

}

````
