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
