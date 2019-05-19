package cn.lastwhisper.server.core;


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
	}

	public Response(Socket client) throws IOException {
		this(client.getOutputStream());
//		this();
//		try {
//			bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//			headInfo = null;
//		}
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
