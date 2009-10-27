/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.mvn.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.util.CollectionUtils;

import com.consol.citrus.doc.ExcelTestDocGenerator;

/**
 * Goal which creates a test documentation in excel.
 *
 * @goal create-excel-doc
 */
public class CreateExcelDocMojo extends AbstractMojo {
    /** @parameter default-value="Unknown" */
    private String company;
    
    /** @parameter default-value="Citrus Testframework" */
    private String author;
    
    /** @parameter expression="${outputFile}" 
     *             default-value="CitrusTests" */
    private String outputFile;
    
    /** @parameter default-value="Citrus Test Documentation" */
    private String pageTitle;
    
    /** @parameter default-value="src/citrus/tests" */
    private String testDirectory;
    
    /** @parameter default-value="" */
    private String customHeaders;
    
    /** @parameter 
     *          expression="${interactiveMode}"
     *          default-value="true" */
    private boolean interactiveMode;
    
    /** @component
     *  @required */
    private Prompter prompter;
    
    public void execute() throws MojoExecutionException {
    	try {
			if(interactiveMode) {
				company = prompter.prompt("Enter company:", company);
				author = prompter.prompt("Enter author:", author);
				pageTitle = prompter.prompt("Enter page title:", pageTitle);
				outputFile = prompter.prompt("Enter output file name:", outputFile);
				customHeaders = prompter.prompt("Enter custom headers:", customHeaders);
				
				String confirm = prompter.prompt("Confirm Excel documentation: outputFile='target/" + outputFile + ".xls'\n", 
				        CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");
    	
		    	if(confirm.equalsIgnoreCase("n")) {
		    		return;
		    	}
			}
			
			ExcelTestDocGenerator creator = ExcelTestDocGenerator.build()
			                .withOutputFile(outputFile)
			                .withPageTitle(pageTitle)
			                .withAuthor(author)
			                .withCompany(company)
			                .useTestDirectory(testDirectory)
			                .withCustomHeaders(customHeaders);
			
			creator.generateDoc();
			
			getLog().info("Successfully created Excel documentation: outputFile='target/" + outputFile + ".xls'");
		} catch (PrompterException e) {
			getLog().info(e);
		}
    }
}
