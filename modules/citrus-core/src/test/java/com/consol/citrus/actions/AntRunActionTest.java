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

package com.consol.citrus.actions;

import java.util.*;

import org.apache.tools.ant.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class AntRunActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testRunTarget() {
		AntRunAction ant = new AntRunAction();
		ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
		ant.setTarget("sayHello");
		
		ant.setBuildListener(new AssertingBuildListener() {
    		@Override
    		public void taskStarted(BuildEvent event) {
    		    Assert.assertEquals(event.getTarget().getName(), "sayHello");
    		} 
    		
    		@Override
    		public void messageLogged(BuildEvent event) {
    		    if (event.getTask() != null && event.getTask().getTaskName().equals("echo")) {
    		        Assert.assertEquals(event.getMessage(), "Welcome to Citrus!");
    		    }
    		}
		});
		
		ant.execute(context);
	}
	
	@Test
    public void testRunTargets() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTargets("sayHello,sayGoodbye");
        
        final List<String> executedTargets = new ArrayList<String>();
        final List<String> echoMessages = new ArrayList<String>();
        
        ant.setBuildListener(new AssertingBuildListener() {
            @Override
            public void taskStarted(BuildEvent event) {
                executedTargets.add(event.getTarget().getName());
            } 
            
            @Override
            public void messageLogged(BuildEvent event) {
                if (event.getTask() != null && event.getTask().getTaskName().equals("echo")) {
                    echoMessages.add(event.getMessage());
                }
            }
        });
        
        ant.execute(context);

        Assert.assertEquals(executedTargets.size(), 2L);
        Assert.assertEquals(executedTargets.get(0), "sayHello");
        Assert.assertEquals(executedTargets.get(1), "sayGoodbye");
        
        Assert.assertEquals(echoMessages.size(), 2L);
        Assert.assertEquals(echoMessages.get(0), "Welcome to Citrus!");
        Assert.assertEquals(echoMessages.get(1), "Goodbye!");
    }
	
	@Test
    public void testWithProperties() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTarget("sayHello");
        
        Properties props = new Properties();
        props.put("welcomeText", "Welcome!");
        ant.setProperties(props);
        
        ant.setBuildListener(new AssertingBuildListener() {
            @Override
            public void taskStarted(BuildEvent event) {
                Assert.assertEquals(event.getTarget().getName(), "sayHello");
            } 
            
            @Override
            public void messageLogged(BuildEvent event) {
                if (event.getTask() != null && event.getTask().getTaskName().equals("echo")) {
                    Assert.assertEquals(event.getMessage(), "Welcome!");
                }
            }
        });
        
        ant.execute(context);
    }
	
	@Test
    public void testWithPropertyFile() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTarget("sayHello");

        ant.setPropertyFilePath("classpath:com/consol/citrus/actions/build.properties");
        
        ant.setBuildListener(new AssertingBuildListener() {
            @Override
            public void taskStarted(BuildEvent event) {
                Assert.assertEquals(event.getTarget().getName(), "sayHello");
            } 
            
            @Override
            public void messageLogged(BuildEvent event) {
                if (event.getTask() != null && event.getTask().getTaskName().equals("echo")) {
                    Assert.assertEquals(event.getMessage(), "Welcome with property file!");
                }
            }
        });
        
        ant.execute(context);
    }
	
	@Test
    public void testWithPropertyOverwrite() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTarget("sayHello");
        
        Properties props = new Properties();
        props.put("welcomeText", "Welcome!");
        ant.setProperties(props);
        
        ant.setPropertyFilePath("classpath:com/consol/citrus/actions/build.properties");
        
        ant.setBuildListener(new AssertingBuildListener() {
            @Override
            public void taskStarted(BuildEvent event) {
                Assert.assertEquals(event.getTarget().getName(), "sayHello");
            } 
            
            @Override
            public void messageLogged(BuildEvent event) {
                if (event.getTask() != null && event.getTask().getTaskName().equals("echo")) {
                    Assert.assertEquals(event.getMessage(), "Welcome with property file!");
                }
            }
        });
        
        ant.execute(context);
    }
	
	@Test
    public void testWithNoPropertyDefault() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTarget("checkMe");
        
        Properties props = new Properties();
        props.put("checked", "true");
        ant.setProperties(props);
        
        ant.setBuildListener(new AssertingBuildListener() {
            @Override
            public void taskStarted(BuildEvent event) {
                Assert.assertEquals(event.getTarget().getName(), "checkMe");
            } 
        });
        
        ant.execute(context);
    }
	
	@Test
    public void testWithMissingProperty() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTarget("checkMe");
        
        try {
            ant.execute(context);
            Assert.fail("Missing build exception due to missing property");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), BuildException.class);
            Assert.assertEquals(e.getMessage(), "Failed to run ANT build file");
            Assert.assertTrue(e.getCause().getMessage().contains("Failed with missing property"));
        }
    }
	
	@Test
    public void testUnknownTarget() {
        AntRunAction ant = new AntRunAction();
        ant.setBuildFilePath("classpath:com/consol/citrus/actions/build.xml");
        ant.setTarget("unknownTarget");
        
        try {
            ant.execute(context);
            Assert.fail("Missing build exception due to unknown target");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getCause().getClass(), BuildException.class);
            Assert.assertEquals(e.getMessage(), "Failed to run ANT build file");
            Assert.assertTrue(e.getCause().getMessage().contains("\"unknownTarget\" does not exist in the project"));
        }
    }
	
	/**
	 * Build lsitener implements all interface methods, subclass may overwrite special
	 * methods for testing purpose doing assertions on build event.
	 */
	private static class AssertingBuildListener implements BuildListener {
        public void buildStarted(BuildEvent event) {
        }

        public void buildFinished(BuildEvent event) {
        }

        public void targetStarted(BuildEvent event) {
        }

        public void targetFinished(BuildEvent event) {
        }

        public void taskStarted(BuildEvent event) {
        }

        public void taskFinished(BuildEvent event) {
        }

        public void messageLogged(BuildEvent event) {
        }
	}
	
}
