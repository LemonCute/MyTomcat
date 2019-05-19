package cn.lastwhisper.server.core;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cn.lastwhisper.server.beans.Entity;
import cn.lastwhisper.server.beans.Mapping;

public class WebXmlHandler extends DefaultHandler {
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