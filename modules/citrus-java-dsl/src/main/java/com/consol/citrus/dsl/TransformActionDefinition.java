package com.consol.citrus.dsl;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.TransformAction;

public class TransformActionDefinition extends AbstractActionDefinition<TransformAction> {
	
	public TransformActionDefinition(TransformAction action){
		super(action);
	}

	public TransformActionDefinition variable(String targetVariable){
		action.setTargetVariable(targetVariable);
		return this;
	}
	
	public TransformActionDefinition source(String xmlData){
		
		action.setXmlData(xmlData);
		return this;
	}
	
	public TransformActionDefinition source(Resource xmlResource){
		action.setXmlResource(xmlResource);
		return this;
	}
	
	public TransformActionDefinition withXSLT(String xsltData){
		action.setXsltData(xsltData);
		return this;
	}
	
	public TransformActionDefinition withXSLT(Resource xsltResource){
		action.setXsltResource(xsltResource);
		return this;
	}
}
