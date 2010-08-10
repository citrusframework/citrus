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

import com.consol.citrus.doc.HtmlTestDocGenerator;

/**
 * Creates test overview documentation in HTML. The web page contains a list of
 * all available tests with meta information.
 *
 * @author Christoph Deppisch 
 * @goal create-html-doc
 */
public class CreateHtmlDocMojo extends AbstractMojo {
    /**
     * The overview title displayed at the top of the test overview
     * @parameter 
     *      default-value="Overview" */
    private String overviewTitle;
    
    /** 
     * Number of columns in test overview table
     * @parameter 
     *      default-value="1" */
    private String columns;
    
    /** 
     * Name of output file (.html file extension is added automatically and can be left out). Defaults to "CitrusTests"
     * @parameter 
     *      expression="${outputFile}" 
     *      default-value="CitrusTests" */
    private String outputFile;
    
    /**
     * Page title displayed at the top of the page 
     * @parameter 
     *      default-value="Citrus Test Documentation" */
    private String pageTitle;
    
    /** 
     * All test files in this directory are included into the report. Defaults to "src/citrus/tests"
     * @parameter 
     *      default-value="src/citrus/tests" */
    private String testDirectory;
    
    /**
     * Company or project logo displayed on top of page. Defaults to "logo.png" 
     * @parameter 
     *      default-value="logo.png" */
    private String logo;
    
    /** 
     * Whether to use interactive mode where user is prompted for parameter input during execution.
     * @parameter 
     *      expression="${interactiveMode}"
     *      default-value="true" */
    private boolean interactiveMode;
    
    /** @component
     *  @required */
    private Prompter prompter;
    
    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {
    	try {
			if(interactiveMode) {
				overviewTitle = prompter.prompt("Enter overview title:", overviewTitle);
				columns = prompter.prompt("Enter number of columns in overview:", columns);
				pageTitle = prompter.prompt("Enter page title:", pageTitle);
				outputFile = prompter.prompt("Enter output file name:", outputFile);
				logo = prompter.prompt("Enter file path to logo:", logo);
				
				String confirm = prompter.prompt("Confirm HTML documentation: outputFile='target/" + outputFile + ".html'\n", 
				        CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");
    	
		    	if(confirm.equalsIgnoreCase("n")) {
		    		return;
		    	}
			}
			
			HtmlTestDocGenerator creator = HtmlTestDocGenerator.build()
			                .withOutputFile(outputFile)
			                .withPageTitle(pageTitle)
			                .withOverviewTitle(overviewTitle)
			                .withColumns(columns)
			                .useTestDirectory(testDirectory)
			                .withLogo(logo);
			
			creator.generateDoc();
			
			getLog().info("Successfully created HTML documentation: outputFile='target/" + outputFile + ".html'");
		} catch (PrompterException e) {
			getLog().info(e);
		}
    }
}
