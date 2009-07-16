package com.consol.citrus.context;

import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.consol.citrus.TestConstants;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.exceptions.VariableNameValueException;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;

@Configurable(preConstruction=true)
public class TestContext {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestContext.class);
    
    /** Local variables */
    protected Map<String, String> variables;
    
    /** Global variables */
    @Autowired
    private transient GlobalVariables globalVariables;
    
    @Autowired 
    private transient FunctionRegistry functionRegistry;
    
    /**
     * Default constructor
     */
    public TestContext() {
        variables = new LinkedHashMap<String, String>();
        variables.putAll(globalVariables.getVariables());
    }
    
    /**
     * Try to get the value for the variable expression.
     * Expression can be a constant, function or simple variable name.
     * @param variableExpression expression to be parsed
     * @return value of variable as String
     * @throws TestSuiteException
     */
    public String getVariable(final String variableExpression) throws TestSuiteException {
        if (VariableUtils.cutOffVariablesPrefix(variableExpression).equals(TestConstants.REPLY_TO_QUEUE) &&
                variables.containsKey(TestConstants.REPLY_TO_QUEUE) == false) {
            throw new TestSuiteException("Trying to read constant " + TestConstants.REPLY_TO_QUEUE + ", but constant was not set before");
        }

        String value = null;

        if (variables.containsKey(VariableUtils.cutOffVariablesPrefix(variableExpression))) {
            value = (String)variables.get(VariableUtils.cutOffVariablesPrefix(variableExpression));
        }

        if (value == null) {
            throw new TestSuiteException("Unknown variable " + variableExpression);
        }

        return value;
    }
    
    /**
     * Creates a new variable with the respective value
     * @param variableName, name of new variable
     * @param value, value of new variable
     * @return
     * @throws TestSuiteException
     */
    public void setVariable(final String variableName, String value) throws TestSuiteException {
        if (variableName == null || variableName.length() == 0 || VariableUtils.cutOffVariablesPrefix(variableName).length() == 0) {
            throw new TestSuiteException("No variable name defined");
        }

        if (value == null) {
            throw new VariableNameValueException("Trying to set variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + ", but value is null");
        }

        if(log.isDebugEnabled()) {
            log.debug("Setting variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + " to value: " + value);
        }

        variables.put(VariableUtils.cutOffVariablesPrefix(variableName), value);
    }
    
    /**
     * All variables in map will be added to global varibables.
     * Existing variables will be overwritten.
     * @param context
     */
    public void addVariables(Map<String, String> variablesToSet) {
        for (Entry entry : variablesToSet.entrySet()) {
            if (entry.getValue() != null) {
                setVariable(entry.getKey().toString(), entry.getValue().toString());
            } else {
                setVariable(entry.getKey().toString(), "");
            }
        }
    }
    
    /**
     * Read all entries in messageElements map and try to create new variables.
     * The valueSet will be the variable names, while the keys will be xpath expressions
     * to the respective value in the W3C XML document.
     * @param messageElements map holding variable names and xpath expressions.
     * @param doc W3C XML document, holding the variable values.
     * @throws UnknownElementException
     * @throws VariableNameValueException
     * @throws TestSuiteException
     */
    public void createVariablesFromMessageValues(final Map messageElements, Message message) throws UnknownElementException, VariableNameValueException, TestSuiteException {
        if (messageElements == null || messageElements.isEmpty()) return;

        if(log.isDebugEnabled()) {
            log.debug("Reading XML elements from document");
        }

        Iterator it = messageElements.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            String pathExpression = entry.getKey().toString();
            String variableName = (String)entry.getValue();

            if(log.isDebugEnabled()) {
                log.debug("Reading element: " + pathExpression);
            }
            
            Document doc = XMLUtils.parseMessagePayload(message);
            
            if (XMLUtils.isXPathExpression(pathExpression)) {
                String value = XMLUtils.evaluateXPathExpression(doc, pathExpression);

                setVariable(variableName, value);
            } else {
                Node node = XMLUtils.findNodeByName(doc, pathExpression);

                if (node == null) {
                    throw new UnknownElementException("Element could not be found in DOM tree - using path expression" + pathExpression);
                }

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getFirstChild() != null) {
                        setVariable((String)messageElements.get(pathExpression), node.getFirstChild().getNodeValue());
                    } else {
                        setVariable((String)messageElements.get(pathExpression), "");
                    }
                } else {
                    setVariable((String)messageElements.get(pathExpression), node.getNodeValue());
                }
            }
        }
    }
    
    /**
     * If the values of the map are containing variable names,
     * the values are replaced by the variables current value.
     *
     * @param map
     * @throws VariableNameValueException
     */
    public void replaceVariablesInMap(final Map map) throws VariableNameValueException, TestSuiteException {
        for (Iterator iterMap = map.entrySet().iterator(); iterMap.hasNext();) {
            Entry entry = (Entry) iterMap.next();
            String key = entry.getKey().toString();
            String value = (String)entry.getValue();

            // If value is a variable
            if (VariableUtils.isVariableName(value)) {
                // then replace variable name by variable value
                map.put(key, getVariable(value));
            } else if(functionRegistry.isFunction(value)) {
                map.put(key, FunctionUtils.resolveFunction(value, this));
            } 
        }
    }
    
    /**
     * Replace variables in list with respective values
     * @param list
     * @throws VariableNameValueException
     * @throws TestSuiteException
     */
    public List replaceVariablesInList(List list) throws VariableNameValueException, TestSuiteException {
        List variableFreeList = new ArrayList();

        for (int i = 0; i < list.size(); i++) {
            String variable = (String)list.get(i);
            if (VariableUtils.isVariableName(variable)) {
                // then replace variable by variable value
                variableFreeList.add(getVariable(variable));
            } else if(functionRegistry.isFunction(variable)) {
                variableFreeList.add(FunctionUtils.resolveFunction(variable, this));
            } else {
                variableFreeList.add(variable);
            }
        }

        return variableFreeList;
    }
    
    /**
     * This method will search for header elements to be extracted as variables.
     *
     * @param extractHeaderValues map containing elements to be extracted from message header
     * @param receivedHeaderValues header elements from received message
     * @throws VariableNameValueException
     */
    public void createVariablesFromHeaderValues(final Map extractHeaderValues, final Map receivedHeaderValues) throws UnknownElementException, VariableNameValueException, TestSuiteException {
        if (extractHeaderValues== null || extractHeaderValues.isEmpty()) return;

        for (Iterator iter = extractHeaderValues.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            String headerElementName = entry.getKey().toString();
            String targetVariableName = (String)entry.getValue();

            if (receivedHeaderValues.get(headerElementName) == null) {
                throw new UnknownElementException("Could not find header element " + headerElementName + " in received header");
            }

            setVariable(targetVariableName, (String)receivedHeaderValues.get(headerElementName));
        }
    }
    
    /**
     * Sets all XML elements values of a XML message for each
     * entry in the messageElements map.
     * Each key of the map has to be an XML element path expression.
     * Each value of the map can either be a variable name or a static value.
     * The element value in the XML document is replaced by the respective value expression.
     *
     * @param messageElements map holding the elements to be overwritten
     * @param doc XML document
     * @throws UnknownElementException
     * @throws VariableNameValueException
     * @throws TestSuiteException
     */
    public void replaceMessageValues(final Map messageElements, final Message message) throws TestSuiteException {
        Iterator it = messageElements.entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            String pathExpression = entry.getKey().toString();
            String valueExpression = (String)entry.getValue();

            if (VariableUtils.isVariableName(valueExpression)) {
                valueExpression = getVariable(valueExpression);
            } else if(functionRegistry.isFunction(valueExpression)) {
                valueExpression = FunctionUtils.resolveFunction(valueExpression, this);
            } 

            if (valueExpression == null) {
                throw new TestSuiteException("Can not set null values in XML document - path expression is " + pathExpression);
            }
            
            Document doc = XMLUtils.parseMessagePayload(message);

            if (doc == null) {
                throw new TestSuiteException("Not able to set message elements, because no XML ressource defined");
            }

            Node node;

            if (XMLUtils.isXPathExpression(pathExpression)) {
                node = XMLUtils.findNodeByXPath(doc, pathExpression);
            } else {
                node = XMLUtils.findNodeByName(doc, pathExpression);
            }

            if (node == null) {
                throw new UnknownElementException("Element could not be found in DOM tree - using path expression" + pathExpression);
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getFirstChild() == null) {
                    node.appendChild(doc.createTextNode(valueExpression));
                } else {
                    node.getFirstChild().setNodeValue(valueExpression);
                }
            } else {
                node.setNodeValue(valueExpression);
            }
            
            //write manipulated doc back to message payload
            message.setMessagePayload(XMLUtils.serialize(doc));

            if(log.isDebugEnabled()) {
                log.debug("Element " +  pathExpression + " was set to value: " + valueExpression);
            }
        }
    }
    
    public void clear() {
        variables.clear();
        variables.putAll(globalVariables.getVariables());
    }
    
    /**
     * Checks if variables are defined yet
     * @return boolean flag to mark existence
     */
    public boolean hasVariables() {
        return (this.variables != null && !this.variables.isEmpty());
    }
    
    /**
     * Spring property setter.
     * @param variables
     */
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * Getter for global variables
     * @return global variables
     */
    public Map getVariables() {
        return variables;
    }

	public void setGlobalVariables(GlobalVariables globalVariables) {
		this.globalVariables = globalVariables;
	}

    /**
     * @return the globalVariables
     */
    public Map<String, String> getGlobalVariables() {
        return globalVariables.getVariables();
    }
    
    /**
     * Method to combine the replacement of old variable declaration (%) and new one (${...})
     * @param str
     * @return
     * @throws TestSuiteException
     * @throws ParseException
     */
    public String replaceDynamicContentInString(String str) throws TestSuiteException, ParseException {
        str = VariableUtils.replaceVariablesInString(str, this);
        str = FunctionUtils.replaceFunctionsInString(str, this);
        return str;
    }

    /**
     * Method to combine the replacement of old variable declaration (%) and new one (${...})
     * @param str
     * @param enableQuoting
     * @return
     * @throws TestSuiteException
     * @throws ParseException
     */
    public String replaceDynamicContentInString(String str, boolean enableQuoting) throws TestSuiteException, ParseException {
        str = VariableUtils.replaceVariablesInString(str, this, enableQuoting);
        str = FunctionUtils.replaceFunctionsInString(str, this, enableQuoting);
        return str;
    }

    /**
     * @return the functionRegistry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
}
