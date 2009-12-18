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

package com.consol.citrus.variable;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

public class LoadPropertiesAsGlobalVariablesTest extends AbstractBaseTest {
    @Test
    public void testPropertyLoadingFromClasspath() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:com/consol/citrus/variable/loadtest.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();
        
        Assert.assertTrue(globalVariables.getVariables().size() == 1);
        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
    }
    
//    @Test
//    public void testPropertyLoadingFromFilesystem() {
//        PropertyLoader propertyLoader = new PropertyLoader();
//        propertyLoader.setPropertyFiles(Collections.singletonList("file:src/test/resources/com/consol/citrus/variable/loadtest.properties"));
//        GlobalVariables globalVariables = new GlobalVariables();        
//        propertyLoader.setGlobalVariables(globalVariables);
//        
//        propertyLoader.loadPropertiesAsVariables();
//        
//        Assert.assertTrue(globalVariables.getVariables().size() == 1);
//        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
//    }
//    
//    @Test
//    public void testPropertyLoading() {
//        PropertyLoader propertyLoader = new PropertyLoader();
//        propertyLoader.setPropertyFiles(Collections.singletonList("src/test/resources/com/consol/citrus/variable/loadtest.properties"));
//        GlobalVariables globalVariables = new GlobalVariables();        
//        propertyLoader.setGlobalVariables(globalVariables);
//        
//        propertyLoader.loadPropertiesAsVariables();
//        
//        Assert.assertTrue(globalVariables.getVariables().size() == 1);
//        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
//    }
    
    @Test
    public void testOverrideExistingVariables() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:com/consol/citrus/variable/loadtest.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();
        globalVariables.getVariables().put("property.load.test", "InitialValue");
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();
        
        Assert.assertTrue(globalVariables.getVariables().size() == 1);
        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
        Assert.assertFalse(globalVariables.getVariables().get("property.load.test").equals("InitialValue"));
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testPropertyFileDoesNotExist() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:file_not_exists.properties"));
        
        GlobalVariables globalVariables = new GlobalVariables();        
        propertyLoader.setGlobalVariables(globalVariables);
        
        propertyLoader.loadPropertiesAsVariables();
    }
}
