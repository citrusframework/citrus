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

package com.consol.citrus.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.text.ParseException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.Resource;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action executing groovy scripts either specified inline or from external file resource.
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class GroovyAction extends AbstractTestAction {

    /** Inline groovy script */
    private String script;

    /** External script file resource */
    private Resource fileResource;
    
    /** Executes a script using the TestContext */
    public interface ScriptExecutor {
        public void execute(TestContext context);
    }
    
    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            
            Class<?> groovyClass;
            
            if(script != null) {
                groovyClass = loader.parseClass(context.replaceDynamicContentInString(script));
            } else if(fileResource != null) {
                groovyClass = loader.parseClass(fileResource.getFile());
            } else {
                throw new CitrusRuntimeException("Neither inline script nor " +
                		"external file resource is defined for bean. " +
                		"Can not execute groovy script.");
            }
    
            if(groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");    
            }
            
            GroovyObject groovyObject;
            groovyObject = (GroovyObject) groovyClass.newInstance();
            
            if(groovyObject instanceof ScriptExecutor) {
                ((ScriptExecutor)groovyObject).execute(context);
            } else {
                Object[] args = {};
                groovyObject.invokeMethod("run", args);
            }
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }
    
    /**
     * @return the fileResource
     */
    public Resource getFileResource() {
        return fileResource;
    }

    /**
     * @param fileResource the fileResource to set
     */
    public void setFileResource(Resource fileResource) {
        this.fileResource = fileResource;
    }
}
