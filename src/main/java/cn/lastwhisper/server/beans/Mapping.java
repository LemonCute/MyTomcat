package cn.lastwhisper.server.beans;

import java.util.HashSet;
import java.util.Set;

/**
 * @desc servlet-mapping实体类
 * 	<servlet-mapping>
		<servlet-name>login</servlet-name>
		<url-pattern>/login</url-pattern>
		<url-pattern>/g</url-pattern>
	</servlet-mapping>
 * @author lastwhisper
 */
public class Mapping {
	// servlet-name
	private String name;
	// url-pattern
	private Set<String> patterns;
	
	public Mapping() {
		patterns = new HashSet<String>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<String> getPatterns() {
		return patterns;
	}
	public void setPatterns(Set<String> patterns) {
		this.patterns = patterns;
	}
	public void addPatterns(String pattern) {
		this.patterns.add(pattern);
	}
	
}
