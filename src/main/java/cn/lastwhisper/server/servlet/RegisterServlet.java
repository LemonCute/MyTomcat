package cn.lastwhisper.server.servlet;

import cn.lastwhisper.server.core.Request;
import cn.lastwhisper.server.core.Response;
import cn.lastwhisper.server.core.Servlet;

public class RegisterServlet extends Servlet {

    @Override
    public void doGet(Request request, Response response) {
        //关注业务逻辑
        String uname = request.getParameter("uname");
        String[] hobbys = request.getParameterValues("hobby");
        response.print("<html>");
        response.print("<head>");
        response.print("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">");
        response.print("<title>");
        response.print("注册成功");
        response.print("</title>");
        response.print("</head>");
        response.print("<body>");
        response.println("<p>你注册的信息为:" + uname + "</p>");
        response.println("<p>你的爱好为:" + "</p>");
        for (String hobby : hobbys) {
            response.print("<p><font size=\"5\" face=\"arial\" color=\"red\">"+hobby+"</font></p>");
        }
        response.print("</body>");
        response.print("</html>");


    }

    @Override
    public void doPost(Request request, Response response) {
        doGet(request, response);
    }

}
