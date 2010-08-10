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

package com.consol.citrus.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

public class CitrusAntTask extends Java {
    
    private String suitename;
    
    private String test;
    
    private String packages;
    
    private String testngXml;
    
    @Override
    public void execute() throws BuildException {
        if(suitename != null && suitename.length() > 0) {
            this.createArg().setValue("-suitename");
            String[] suites = suitename.split(" ");
            for (String suite : suites) {
                this.createArg().setValue(suite);
            }
        }
        
        if(test != null && test.length() > 0) {
            this.createArg().setValue("-test");
            String[] tests = test.split(" ");
            for (String testname : tests) {
                this.createArg().setValue(testname);
            }
        }
        
        if(packages != null && packages.length() > 0) {
            this.createArg().setValue("-package");
            String[] packageNames = packages.split(" ");
            for (String packageName : packageNames) {
                this.createArg().setValue(packageName);
            }
        }
        
        if(testngXml != null && testngXml.length() > 0) {
            String[] testNgSuites = testngXml.split(" ");
            for (String testNgSuite : testNgSuites) {
                this.createArg().setValue(testNgSuite);
            }
        }
        
        this.setFork(true);
        
        this.setFailonerror(true);
        
        this.setClassname("com.consol.citrus.Citrus");
        
        this.setClasspath(new Path(getProject(), "src/citrus/tests"));
        this.setClasspath(new Path(getProject(), "src/citrus/resources"));
        this.setClasspath(new Path(getProject(), "target/test-classes"));
        
        this.setClasspathRef(new Reference(getProject(), "citrus-classpath"));
        
        super.execute();
    }

    /**
     * @param suitename the suitename to set
     */
    public void setSuitename(String suitename) {
        this.suitename = suitename;
    }

    /**
     * @return the suitename
     */
    public String getSuitename() {
        return suitename;
    }

    /**
     * @return the test
     */
    public String getTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(String test) {
        this.test = test;
    }

    /**
     * @return the packages
     */
    public String getPackage() {
        return packages;
    }

    /**
     * @param packages the packages to set
     */
    public void setPackage(String packages) {
        this.packages = packages;
    }

    /**
     * @return the testngXml
     */
    public String getTestngXml() {
        return testngXml;
    }

    /**
     * @param testngXml the testngXml to set
     */
    public void setTestngXml(String testngXml) {
        this.testngXml = testngXml;
    }
}
