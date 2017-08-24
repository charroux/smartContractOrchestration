package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Element
import org.jdom2.Namespace

trait Bean {
	
	public Element bean(String fullClassName){
		
		def properties = [:]
		Element bean = this.beanWithValue(fullClassName, properties)
		return bean

	}
	
	public Element bean(String id, String fullClassName){
		
		def properties = [:]		
		Element bean = this.beanWithValue(id, fullClassName, properties)		
		return bean

	}
	
	public Element beanWithValue(String fullClassName, def properties){
		
		Namespace namespace = Namespace.getNamespace("", "http://www.springframework.org/schema/beans")
		
		Element bean = new Element("bean", namespace)
		bean.setAttribute("class", fullClassName)

		properties.each { key, value ->
			Element property = new Element("property", namespace)
			property.setAttribute("name", key)
			property.setAttribute("value", value)
			bean.addContent(property)
		}
		
		return bean

	}
	
	public Element beanWithValue(String id, String fullClassName, def properties){
		
		Namespace namespace = Namespace.getNamespace("", "http://www.springframework.org/schema/beans")
		
		Element bean = this.beanWithValue(fullClassName, properties)
		bean.setAttribute("id", id)
		
		return bean

	}

	public Element beanWithRef(String fullClassName, def properties){
		
		Namespace namespace = Namespace.getNamespace("", "http://www.springframework.org/schema/beans")
		
		Element bean = new Element("bean", namespace)
		bean.setAttribute("class", fullClassName)

		properties.each { key, value ->
			Element property = new Element("property", namespace)
			property.setAttribute("name", key)
			property.setAttribute("ref", value)
			bean.addContent(property)
		}
		
		return bean

	}

}
