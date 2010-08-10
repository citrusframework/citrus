/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.util;

import java.util.Stack;

import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.container.TestActionContainer;

/**
 * Utility class for test cases providing several utility 
 * methods regarding Citrus test cases.
 * 
 * @author Christoph Deppisch
 */
public abstract class TestUtils {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);
    
    /**
     * 
     * @param test
     * @return
     */
    public static Stack<String> getFailureStack(final TestCase test) {
        final Stack<String> failureStack = new Stack<String>();
        
        try {
            final String testFilePath = test.getPackageName().replace('.', '/') + "/" + test.getName();
            
            Resource testFileResource = new ClassPathResource(testFilePath + ".xml");
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();
            
            reader.setContentHandler(new DefaultHandler() {
                //Locator providing actual line number information
                private Locator locator;
                //Failure stack finder
                private FailureStackFinder stackFinder;
                
                @Override
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {
                    
                    //start when actions element is reached
                    if(qName.equals("actions")) {
                        stackFinder = new FailureStackFinder(test);
                        return;
                    }
                    
                    if(stackFinder != null) {
                        if(stackFinder.isFailureStackElement(qName)) {
                            failureStack.push("at " + testFilePath + "(" + qName + ":" + locator.getLineNumber() + ")");
                            
                            if(stackFinder.getNestedActionContainer() != null && 
                                    stackFinder.getNestedActionContainer().getLastExecutedAction() != null) {
                                //continue with nested action container, in order to find out which action caused the failure
                                stackFinder = new FailureStackFinder(stackFinder.getNestedActionContainer());
                            } else {
                                //stop failure stack evaluation as failure-causing action was found
                                stackFinder = null;
                            }
                        }
                    }
                    
                    super.startElement(uri, localName, qName, attributes);
                }
                
                @Override
                public void setDocumentLocator(Locator locator) {
                    this.locator = locator;
                }
            });
            
            reader.parse(new InputSource(testFileResource.getInputStream()));
        } catch (Exception e) {
            log.warn("Unable to locate line numbers in test case for failure cause stack trace", e);
        }
        
        return failureStack;
    }
    
    /**
     * Failure stack finder listens for actions in a testcase 
     */
    private static class FailureStackFinder {
        /** Action list */
        private Stack<TestAction> actionStack = new Stack<TestAction>();
        
        /** Test action we are currently working on */
        TestAction action = null;
        
        /**
         * Default constructor using fields.
         * @param container
         */
        public FailureStackFinder(TestActionContainer container) {
            int lastActionIndex = container.getActionIndex(container.getLastExecutedAction());
            
            for (int i = lastActionIndex; i >= 0; i--) {
                actionStack.add(container.getActions().get(i));
            }
        }

        /**
         * Checks whether the target action is reached within the action container.
         * Method counts the actions inside the action container and waits for the target index
         * to be reached.
         * 
         * @param eventElement actual action name, can also be a nested element in the XML DOM tree so check name before evaluation
         * @return boolean flag to mark that target action is reached or not
         */
        public boolean isFailureStackElement(String eventElement) {
            if(action == null) {
                action = actionStack.pop();
            }
        
            /* filter method calls that actually are based on other elements within the DOM
             * tree. SAX content handler can not differ between action elements and other nested elements
             * in startElement event. 
             */
            if(eventElement.equals(action.getName())) {
                if(action instanceof TestActionContainer && !actionStack.isEmpty()) {
                    TestActionContainer container = (TestActionContainer)action;
                    for (int i = container.getActions().size()-1; i >= 0; i--) {
                        actionStack.add(container.getActions().get(i));
                    }
                }
                
                if(!actionStack.isEmpty()) {
                    action = null;
                }
            } else {
                return false;
            }
        
            return actionStack.isEmpty();
        }
        
        /**
         * Is target action a container itself? If yes the stack evaluation should
         * continue with nested container, in order to get nested action that caused the failure.
         * 
         * @return the nested container or null
         */
        public TestActionContainer getNestedActionContainer() {
            if(action instanceof TestActionContainer) {
                return (TestActionContainer)action;
            } else {
                return null;
            }
        }
    }
}
