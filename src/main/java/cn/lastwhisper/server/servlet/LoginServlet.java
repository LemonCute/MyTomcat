package cn.lastwhisper.server.servlet;

import cn.lastwhisper.server.core.Request;
import cn.lastwhisper.server.core.Response;
import cn.lastwhisper.server.core.Servlet;

public class LoginServlet extends Servlet {

	@Override
	public void doGet(Request request, Response response) {
		response.print("<html>");
		response.print("<head>");
		response.print("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">" );
		response.print("<title>");
		response.print("第一个servlet");
		response.print("</title>");
		response.print("</head>");
		response.print("<body>");
		response.print("<p>欢迎回来:" + request.getParameter("uname")+"</p>");
		response.print("</body>");
		response.print("</html>");
	}

	@Override
	public void doPost(Request request, Response response) {
		doGet(request, response);
	}

}
