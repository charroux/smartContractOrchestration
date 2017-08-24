package orcha.lang.compiler.referenceimpl.xmlgenerator.impl

import org.jdom2.Element
import org.jdom2.Namespace
import orcha.lang.compiler.qualityOfService.QueueOption

trait Poller{

	public Element poller(QueueOption queueOption) {
		
		Namespace namespace = Namespace.getNamespace("int", "http://www.springframework.org/schema/integration")
		
		Element element = new Element("poller", namespace)
		
		if(queueOption.fixedDelay != -1){			
			element.setAttribute("fixed-delay", queueOption.fixedDelay.toString())
		} else if(queueOption.fixedRate != -1){
			element.setAttribute("fixed-rate", queueOption.fixedRate.toString())
		} else if(queueOption.cron != ""){
			element.setAttribute("cron", queueOption.cron)
		}
		
		return element
	}

}
