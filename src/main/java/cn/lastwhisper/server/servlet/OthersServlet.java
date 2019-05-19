package cn.lastwhisper.server.servlet;

import cn.lastwhisper.server.core.Request;
import cn.lastwhisper.server.core.Response;
import cn.lastwhisper.server.core.Servlet;

public class OthersServlet extends Servlet {

	@Override
	public void doGet(Request request, Response response) {
		response.print("<p>其他测试页面</p>");
	}

	@Override
	public void doPost(Request request, Response response) {
		doGet(request, response);
	}

}
