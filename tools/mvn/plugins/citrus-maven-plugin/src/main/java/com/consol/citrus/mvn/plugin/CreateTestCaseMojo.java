package com.consol.citrus.mvn.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.consol.citrus.util.TestCaseCreator;

/**
 * Goal which creates a new test case using test case creator.
 *
 * @goal create-test
 */
public class CreateTestCaseMojo extends AbstractMojo
{
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
    
    /** @parameter default-value="" */
    private String targetPackage;
    
    public void execute() throws MojoExecutionException
    {
        try {    
            TestCaseCreator creator = TestCaseCreator.build()
                .withName(name)
                .withAuthor(author)
                .withDescription(description)
                .usePackage(targetPackage);
            
            creator.createTestCase();
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong usage exception!");
            getLog().info("Use parameters in the following way: [test.name] [test.author] [test.description] [test.subfolder]");
        }
    }
}
