package com.consol.citrus.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.*;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.consol.citrus.TestCase;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 06.03.2009
 */
public class GroovyTestCaseParser implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;

    /** Builds a test case using the application context and test context */
    public interface TestCaseBuilder {
        public TestCase build(ApplicationContext applicationContext);
    }
    
    public TestCase parse(Resource groovyScript) {
        BufferedReader templateReader = null;
        BufferedReader bodyReader = null;
        
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            
            StringBuilder script = new StringBuilder();
            bodyReader = new BufferedReader(new FileReader(groovyScript.getFile()));
            templateReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("test_template.groovy")));
            String line;
            while ((line = templateReader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase("\"+++++ BODY +++++\"") == false) {
                    script.append(line + "\n");
                } else {
                    String bodyLine;                
                    while ((bodyLine = bodyReader.readLine()) != null) {
                        script.append(bodyLine + "\n");
                    }
                }
            }
            
            Class groovyClass = loader.parseClass(script.toString());
    
            GroovyObject groovyObject;
            groovyObject = (GroovyObject) groovyClass.newInstance();
            
            if(groovyObject instanceof TestCaseBuilder) {
                TestCase test = ((TestCaseBuilder)groovyObject).build(applicationContext);
                return test;
            } else {
                throw new CitrusRuntimeException("Unable to parse groovy script. Script must implement TestCaseBuilder.");
            }
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            try {
                if(templateReader != null) {
                    templateReader.close();
                }
                
                if(bodyReader != null) {
                    bodyReader.close();
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
}
