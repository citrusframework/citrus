/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

package com.consol.citrus.util;

import java.util.Properties;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

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
