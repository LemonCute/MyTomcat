package cn.lastwhisper.server.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.lastwhisper.server.beans.Entity;
import cn.lastwhisper.server.beans.Mapping;

/**
 * @author lastwhisper
 * @desc webxml的上下文信息
 */
public class WebXmlContext {
	// key-->servlet-name  value -->servlet-class
	private Map<String,String> entityMap = new HashMap<String,String>();
	// key -->url-pattern value -->servlet-name
	private Map<String,String> mappingMap = new HashMap<String,String>();
	public WebXmlContext(List<Entity> entitys, List<Mapping> mappings) {
		
		// 将entity 的List转成了对应map<servlet-name,servlet-class>
		for (Entity entity : entitys) {
			entityMap.put(entity.getName(), entity.getClazz());
		}
		// 将mapping 的List转成了对应map<url-pattern,servlet-name>
		for (Mapping mapping : mappings) {
			Set<String> patterns = mapping.getPatterns();
			for (String pattern : patterns) {
				mappingMap.put(pattern, mapping.getName());
			}
		}
	}
	
	/**
	 * 
	 * @author lastwhisper
	 * @desc 通过URL的路径找到了对应class
	 * @param pattern
	 * @return
	 */
	public String getClazz(String pattern) {
		String name = mappingMap.get(pattern);
		return entityMap.get(name);
	}
}
