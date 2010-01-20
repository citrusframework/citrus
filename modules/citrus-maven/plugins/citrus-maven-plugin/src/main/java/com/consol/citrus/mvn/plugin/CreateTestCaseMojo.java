/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * Goal which creates a new test case using test case creator.
 *
 * @goal create-test
 */
public class CreateTestCaseMojo extends AbstractMojo {
    /** @parameter 
     *          expression="${name}" 
     *          default-value="" */
    private String name;
    
    /** @parameter
     *          expression="${author}" 
     *          default-value="Unknown" */
    private String author;

    /** @parameter
     *          expression="${description}" 
     *          default-value="TODO: Description" */
    private String description;
    
    /** @parameter 
     *          expression="${targetPackage}"
     *          default-value="com.consol.citrus" */
    private String targetPackage;
    
    /** @parameter 
     *          expression="${interactiveMode}"
     *          default-value="true" */
    private boolean interactiveMode;
    
    /** @component
     *  @required */
    private Prompter prompter;
    
    public void execute() throws MojoExecutionException {
        try {
        	while(interactiveMode && StringUtils.hasText(name) == false) {
        		name = prompter.prompt("Enter test name");
        	}
        	
        	if(StringUtils.hasText(name) == false) {
        		throw new CitrusRuntimeException("Test must have a name!");
        	}
        	
        	if(interactiveMode) {
        		author = prompter.prompt("Enter test author:", author);
        		description = prompter.prompt("Enter test description:", description);
        		targetPackage = prompter.prompt("Enter test package:", targetPackage);
        		
        		String confirm = prompter.prompt("Confirm test creation:\n" +
    			        "name: " + name + "\n" +
    					"author: " + author + "\n" +
    					"description: " + description + "\n" +
    					"package: " + targetPackage + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");
    	
		    	if(confirm.equalsIgnoreCase("n")) {
		    		return;
		    	}
        	}
        	
            TestCaseCreator creator = TestCaseCreator.build()
                .withName(name)
                .withAuthor(author)
                .withDescription(description)
                .usePackage(targetPackage);
            
            creator.createTestCase();
            
            getLog().info("Successfully created new test case \n" +
            		    "name: " + name + "\n" +
    					"author: " + author + "\n" +
    					"description: " + description + "\n" +
    					"package: " + targetPackage);
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong usage exception!");
            getLog().info("Use parameters in the following way: [test.name] [test.author] [test.description] [test.package]");
        } catch (PrompterException e) {
			getLog().info(e);
		}
    }
}
