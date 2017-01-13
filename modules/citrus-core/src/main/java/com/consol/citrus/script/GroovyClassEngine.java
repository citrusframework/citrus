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
package com.consol.citrus.script;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

/**
 * Class that manages one Groovy class loader, but custom default cache enabled setting.
 * @author VASCO Data Security
 * @since 2.7
 */
public class GroovyClassEngine {

    private static Logger log = LoggerFactory.getLogger(GroovyClassEngine.class);
    
    /** Cache generated Groovy classes */
    private boolean defaultCacheSource = true;

    /** Groovy class loader */
    private static GroovyClassLoader groovyClassloader = null;
    
    /**
     * Constructor.
     * <br>
     * Reads property 'cache-groovy-source' for default groovy cache value.
     */
    public GroovyClassEngine() {
        String cacheGroovySource = System.getProperty("cache-groovy-source");
        if (StringUtils.hasText(cacheGroovySource)) {
            defaultCacheSource = Boolean.valueOf(cacheGroovySource);
            log.debug("Using 'cache-groovy-source' property value: '" + cacheGroovySource + "'");
        }
        if (groovyClassloader == null) {
            groovyClassloader = createGroovyClassLoader();
        }
    }
    
    /**
     * Remove the class variables.
     */
    public void destroy() {
        if (groovyClassloader != null) {
            groovyClassloader.clearCache();
        
            try {
                groovyClassloader.close();
            } catch (IOException e) {
                log.warn("Could not close Groovy class loader", e);
            }
        }
    }
    
    /**
     * Enable or disable default Groovy source caching.
     * @param enable TRUE if source cache must be enabled.
     */
    public void setDefaultCacheSource(boolean enable)
    {
        defaultCacheSource = enable;
    }
    
    /**
     * Gets the default cacheSource value.
     * @return True of Groovy source cache is enabled.
     */
    public boolean isDefaultCacheSource()
    {
        return defaultCacheSource;
    }
    
    /**
     * Returns a GroovyObject that uses a supplied cache setting.
     * @param code Groovy code.
     * @param cacheSource Boolean that overrides the default cache setting. 
     * @return GroovyObject
     */
    public GroovyObject getGroovyObject(String code, boolean cacheSource) {
        try{
            // Generate a hash out of groovy code
            String hash = DigestUtils.md5Hex(code.replaceAll("\\s",""));
        
            GroovyCodeSource gcs = createGroovyCodeSource(code, hash);
        
            // Get groovy class and instantiate an object 
            Class<?> groovyClass = groovyClassloader.parseClass(gcs, cacheSource);
            
            return (GroovyObject)groovyClass.newInstance();
            
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Returns a GroovyObject that uses the default cache setting.
     * @param code Groovy code.
     * @return GroovyObject
     */
    public GroovyObject getGroovyObject(String code) {
        return getGroovyObject(code, defaultCacheSource);
    }
    
    /**
     * Create a GroovyClassLoader.
     * @return GroovyClassLoader
     */
    protected GroovyClassLoader createGroovyClassLoader()
    {
        return new GroovyClassLoader();
    }
    
    /**
     * Create a GroovyCodeSource object. 
     * @param code Groovy code.
     * @param hash Hash of the Groovy code.
     * @return GroovyCodeSource
     */
    protected GroovyCodeSource createGroovyCodeSource(final String code, final String hash)
    {
        return AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            /**
             * @see java.security.PrivilegedAction#run()
             */
            public GroovyCodeSource run() {
                return new GroovyCodeSource(code, "script" + hash + ".groovy", "/groovy/script");
                }
            });
    }
}
