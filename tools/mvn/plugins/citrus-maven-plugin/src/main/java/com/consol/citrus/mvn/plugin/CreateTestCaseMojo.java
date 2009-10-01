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

import com.consol.citrus.util.TestCaseCreator;

/**
 * Goal which creates a new test case using test case creator.
 *
 * @goal create-test
 */
public class CreateTestCaseMojo extends AbstractMojo {
    /** @parameter expression="${name}"
     *  @required */
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
    
    public void execute() throws MojoExecutionException {
        try {    
            TestCaseCreator creator = TestCaseCreator.build()
                .withName(name)
                .withAuthor(author)
                .withDescription(description)
                .usePackage(targetPackage);
            
            creator.createTestCase();
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong usage exception!");
            getLog().info("Use parameters in the following way: [test.name] [test.author] [test.description] [test.package]");
        }
    }
}
