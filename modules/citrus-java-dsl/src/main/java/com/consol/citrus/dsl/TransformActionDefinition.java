package com.consol.citrus.dsl;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.TransformAction;

/**
 * Action transforms a XML document(specified inline or from external file resource)
 * with a XSLT document(specified inline or from external file resource)
 * and puts the result in the specified variable.
 */
public class TransformActionDefinition extends AbstractActionDefinition<TransformAction> {
	
	public TransformActionDefinition(TransformAction action){
		super(action);
	}

	/**
	 * Set the target variable for the result
	 * @param targetVariable the targetVariable to set
	 */
	public TransformActionDefinition variable(String targetVariable){
		action.setTargetVariable(targetVariable);
		return this;
	}
	
	/**
	 * Set the XML document
	 * @param xmlData the xmlData to set
	 */
	public TransformActionDefinition source(String xmlData){
		
		action.setXmlData(xmlData);
		return this;
	}
	
	/**
	 * Set the XML document as resource
	 * @param xmlResource the xmlResource to set
	 */
	public TransformActionDefinition source(Resource xmlResource){
		action.setXmlResource(xmlResource);
		return this;
	}
	
	/**
	 * Set the XSLT document
	 * @param xsltData the xsltData to set
	 */
	public TransformActionDefinition withXSLT(String xsltData){
		action.setXsltData(xsltData);
		return this;
	}
	
	/**
	 * Set the XSLT document as resource
	 * @param xsltResource the xsltResource to set
	 */
	public TransformActionDefinition withXSLT(Resource xsltResource){
		action.setXsltResource(xsltResource);
		return this;
	}
}
