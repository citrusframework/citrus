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

package org.citrusframework.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AntRunActionTest extends UnitTestSupport {

	@Test
	public void testRunTarget() {
		AntRunAction ant = new AntRunAction.Builder()
		        .buildFilePath("classpath:org/citrusframework/actions/build.xml")
		        .target("sayHello")
                .listener(new AssertingBuildListener() {
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
                })
                .build();


		ant.execute(context);
	}

	@Test
    public void testRunTargets() {
        final List<String> executedTargets = new ArrayList<String>();
        final List<String> echoMessages = new ArrayList<String>();

        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .targets("sayHello,sayGoodbye")
                .listener(new AssertingBuildListener() {
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
                })
                .build();

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
        final Properties props = new Properties();
        props.put("welcomeText", "Welcome!");

        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .target("sayHello")
                .properties(props)
                .listener(new AssertingBuildListener() {
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
                })
                .build();

        ant.execute(context);
    }

	@Test
    public void testWithPropertyFile() {
        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .target("sayHello")
                .propertyFile("classpath:org/citrusframework/actions/build.properties")
                .listener(new AssertingBuildListener() {
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
                })
                .build();

        ant.execute(context);
    }

	@Test
    public void testWithPropertyOverwrite() {
        final Properties props = new Properties();
        props.put("welcomeText", "Welcome!");

        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .target("sayHello")
                .properties(props)
                .propertyFile("classpath:org/citrusframework/actions/build.properties")
                .listener(new AssertingBuildListener() {
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
                })
                .build();

        ant.execute(context);
    }

	@Test
    public void testWithNoPropertyDefault() {
        final Properties props = new Properties();
        props.put("checked", "true");

        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .target("checkMe")
                .properties(props)
                .listener(new AssertingBuildListener() {
                    @Override
                    public void taskStarted(BuildEvent event) {
                        Assert.assertEquals(event.getTarget().getName(), "checkMe");
                    }
                })
                .build();

        ant.execute(context);
    }

	@Test
    public void testWithMissingProperty() {
        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .target("checkMe")
                .build();

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
        AntRunAction ant = new AntRunAction.Builder()
                .buildFilePath("classpath:org/citrusframework/actions/build.xml")
                .target("unknownTarget")
                .build();

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
