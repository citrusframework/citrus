package com.consol.citrus.util;

import java.util.Properties;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class PropertyUtilsTest extends AbstractBaseTest {
    @Test
    public void testPropertyReplacementSingleProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        
        String content = "This test has the name @test.name@!";
        
        String result = PropertyUtils.replacePropertiesInString(content, props);
        
        Assert.assertEquals("This test has the name MyTest!", result);
    }
    
    @Test
    public void testPropertyReplacementStartingWithProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        
        String content = "@test.name@ is the test's name!";
        
        String result = PropertyUtils.replacePropertiesInString(content, props);
        
        Assert.assertEquals("MyTest is the test's name!", result);
    }
    
    @Test
    public void testPropertyReplacementMultipleProperties() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");
        
        String content = "This test has the name @test.name@ and its author is @test.author@";
        
        String result = PropertyUtils.replacePropertiesInString(content, props);
        
        Assert.assertEquals("This test has the name MyTest and its author is Mickey Mouse", result);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testPropertyReplacementUnknownProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        
        String content = "This test has the name @test.name@ and its author is @test.author@";
        
        PropertyUtils.replacePropertiesInString(content, props);
    }
    
    @Test
    public void testPropertyReplacementNestedProperties() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse and @test.coauthor@");
        props.put("test.coauthor", "Donald Duck");
        
        String content = "This test has the name @test.name@ and its author is @test.author@";
        
        String result = PropertyUtils.replacePropertiesInString(content, props);
        
        Assert.assertEquals("This test has the name MyTest and its author is Mickey Mouse and Donald Duck", result);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testPropertyReplacementUnknownPropertyInNestedProperty() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse and @test.coauthor@");
        
        String content = "This test has the name @test.name@ and its author is @test.author@";
        
        PropertyUtils.replacePropertiesInString(content, props);
    }
    
    @Test
    public void testPropertyReplacementEscapeCharacter() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");
        
        String content = "This \\@test\\@ has the name @test.name@ and its author is @test.author@";
        
        String result = PropertyUtils.replacePropertiesInString(content, props);
        
        Assert.assertEquals("This @test@ has the name MyTest and its author is Mickey Mouse", result);
    }
    
    @Test
    public void testPropertyReplacementEscapeCharacterInDirectNeighbourhood() {
        Properties props = new Properties();
        props.put("test.name", "MyTest");
        props.put("test.author", "Mickey Mouse");
        
        String content = "This \\@test\\@ has the name \\@@test.name@\\@ and its author is @test.author@";
        
        String result = PropertyUtils.replacePropertiesInString(content, props);
        
        Assert.assertEquals("This @test@ has the name @MyTest@ and its author is Mickey Mouse", result);
    }
}
