package cn.lastwhisper.server.beans;

/**
 * @desc servlet对应实体类 
 * <servlet>
	<servlet-name>login</servlet-name>
	<servlet-class>com.sxt.server.basic.servlet.LoginServlet</servlet-class>
   </servlet>
 *
 * @author lastwhisper
 */
public class Entity {
	// servlet-name
	private String name;
	// servlet-class
	private String clazz;
	
	public Entity() {
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
}
