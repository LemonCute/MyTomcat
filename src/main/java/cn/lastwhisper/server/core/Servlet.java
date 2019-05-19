package cn.lastwhisper.server.core;

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
