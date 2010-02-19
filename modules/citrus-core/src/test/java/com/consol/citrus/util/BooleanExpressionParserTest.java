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

package com.consol.citrus.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class BooleanExpressionParserTest {
    
    @Test
    public void testExpressionParser() {
        Assert.assertTrue(BooleanExpressionParser.evaluate("1 = 1"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("1 = 2"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("1 lt 2"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("2 lt 1"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("2 gt 1"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("1 gt 2"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("2 lt= 2"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("2 gt= 2"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("2 lt= 1"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("2 gt= 3"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 = 1)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 = 1) and (2 = 2)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 lt= 1) and (2 gt= 2)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 gt 2) or (2 = 2)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("((1 = 5) and (2 = 6)) or (2 gt 1)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 = 2)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 = 1) and (2 = 3)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 lt 1) and (2 gt 2)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 gt 2) or (2 = 3)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("((1 = 5) and (2 = 6)) or (2 lt 1)"));
    }
    
    @Test
    public void testExpressionParserWithUnknownOperator() {
        try {
            BooleanExpressionParser.evaluate("wahr");
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Unknown operator 'wahr'");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of unknown operator");
    }
    
    @Test
    public void testExpressionParserWithBrokenExpression() {
        try {
            BooleanExpressionParser.evaluate("1 = ");
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Unable to parse boolean expression '1 = '. Maybe expression is incomplete!");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of broken expression");
    }
}
