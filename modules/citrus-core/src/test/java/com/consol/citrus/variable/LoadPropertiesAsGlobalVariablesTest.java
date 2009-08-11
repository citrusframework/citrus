package com.consol.citrus.variable;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;

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
