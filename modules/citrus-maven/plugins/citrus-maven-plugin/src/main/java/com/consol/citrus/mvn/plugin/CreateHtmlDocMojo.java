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
