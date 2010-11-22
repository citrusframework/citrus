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

import com.consol.citrus.doc.ExcelTestDocGenerator;

/**
 * Creates test documentation in MS Excel listing all available tests with 
 * meta information (name, author, description, date, ...) . 
 * 
 * @author Christoph Deppisch
 * @goal create-excel-doc
 */
public class CreateExcelDocMojo extends AbstractMojo {
    /** 
     * Name of company that goes into Excel meta information.
     * @parameter 
     *      default-value="Unknown" */
    private String company;
    
    /** 
     * Author name that goes into Excel meta information.
     * @parameter 
     *      default-value="Citrus Testframework" */
    private String author;
    
    /**
     * Name of output file (.xsl file extension is added automatically and can be left out). Defaults to "CitrusTests".
     * @parameter 
     *      expression="${outputFile}" 
     *      default-value="CitrusTests" */
    private String outputFile;
    
    /**
     * Page title displayed on top of the sheet. 
     * @parameter 
     *      default-value="Citrus Test Documentation" */
    private String pageTitle;
    
    /** 
     * Mojo looks in this directory for test files that are included in this report. Defaults to "src/citrus/tests"
     * @parameter 
     *      default-value="src/citrus/tests" */
    private String testDirectory;
    
    /** 
     * Customized column headers as comma separated value string (e.g. "Nr;Name;Author;Status;TestCase;Date").
     * @parameter 
     *      default-value="" */
    private String customHeaders;
    
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
