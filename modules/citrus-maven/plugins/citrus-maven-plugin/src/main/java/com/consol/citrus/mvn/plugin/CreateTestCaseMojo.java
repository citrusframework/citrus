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

package com.consol.citrus.mvn.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.TestCaseCreator;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;

/**
 * Creates new Citrus test cases with empty XML test file and executable Java class.
 * 
 * Mojo offers an interactive mode, where the plugin prompts for parameters during execution. In
 * non-interactive mode the parameters are given as command line arguments.
 *
 * @author Christoph Deppisch
 * @goal create-test
 */
public class CreateTestCaseMojo extends AbstractMojo {
    /**
     * The name of the test case (must start with upper case letter). 
     * @parameter 
     *          expression="${name}" 
     *          default-value="" */
    private String name;
    
    /**
     * The test author
     * @parameter
     *          expression="${author}" 
     *          default-value="Unknown" */
    private String author;

    /**
     * Describes the test case and its actions
     * @parameter
     *          expression="${description}" 
     *          default-value="TODO: Description" */
    private String description;
    
    /** 
     * Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"
     * @parameter 
     *          expression="${targetPackage}"
     *          default-value="com.consol.citrus" */
    private String targetPackage;
    
    /** 
     * Whether to run this command in interactive mode. Defaults to "true".
     * @parameter 
     *          expression="${interactiveMode}"
     *          default-value="true" */
    private boolean interactiveMode;

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit3, junit4)
     * @parameter 
     *          expression="${framework}"
     *          default-value="testng" */
    private String framework;
    
    /** @component
     *  @required */
    private Prompter prompter;

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {
        try {
        	while(interactiveMode && !StringUtils.hasText(name)) {
        		name = prompter.prompt("Enter test name");
        	}
        	
        	if(!StringUtils.hasText(name)) {
        		throw new CitrusRuntimeException("Test must have a name!");
        	}
        	
        	if(interactiveMode) {
        		author = prompter.prompt("Enter test author:", author);
        		description = prompter.prompt("Enter test description:", description);
        		targetPackage = prompter.prompt("Enter test package:", targetPackage);
        		framework = prompter.prompt("Choose unit test framework", framework);
        		
        		String confirm = prompter.prompt("Confirm test creation:\n" +
        		        "framework: " + framework + "\n" +
    			        "name: " + name + "\n" +
    					"author: " + author + "\n" +
    					"description: " + description + "\n" +
    					"package: " + targetPackage + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");
    	
		    	if(confirm.equalsIgnoreCase("n")) {
		    		return;
		    	}
        	}
        	
            TestCaseCreator creator = TestCaseCreator.build()
                .withFramework(UnitFramework.fromString(framework))
                .withName(name)
                .withAuthor(author)
                .withDescription(description)
                .usePackage(targetPackage);
            
            creator.createTestCase();
            
            getLog().info("Successfully created new test case \n" +
                        "framework: " + framework + "\n" +
            		    "name: " + name + "\n" +
    					"author: " + author + "\n" +
    					"description: " + description + "\n" +
    					"package: " + targetPackage);
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        } catch (PrompterException e) {
			getLog().info(e);
			getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
		}
    }
}
