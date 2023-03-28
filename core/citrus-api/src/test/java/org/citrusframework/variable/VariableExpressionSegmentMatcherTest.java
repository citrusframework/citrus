package org.citrusframework.variable;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class VariableExpressionSegmentMatcherTest {

    @Test(dataProvider = "matcherDataprovider")
    public void testExpression(TestData testData) {

        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher(testData.expression);

        for (SegmentAttributes attributes : testData.segmentAttributes) {
            assertTrue(matcher.nextMatch());
            assertEquals(attributes.name, matcher.getSegmentExpression());
            assertEquals(attributes.index, matcher.getSegmentIndex());
        }
        assertFalse(matcher.nextMatch());
    }

    @DataProvider(name = "matcherDataprovider")
    public static TestData[] matcherExpressions() {
        return new TestData[]
                {
                    new TestData("var.prop1[1].prop2[2].prop3")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("prop1", 1)
                        .addSegmentAttributes("prop2", 2)
                        .addSegmentAttributes("prop3", -1),
                    new TestData("var[2].prop1.prop2[2].prop3")
                        .addSegmentAttributes("var", 2)
                        .addSegmentAttributes("prop1", -1)
                        .addSegmentAttributes("prop2", 2)
                        .addSegmentAttributes("prop3", -1),
                    new TestData("var.jsonPath($.name1.name2)")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("$.name1.name2", -1),
                    new TestData("var.jsonPath($['store']['book'][0]['author'])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("$['store']['book'][0]['author']", -1),
                    new TestData("var.jsonPath($.store.book[*].author)")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("$.store.book[*].author", -1),
                    new TestData("var.jsonPath($..author)")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("$..author", -1),
                    new TestData("var.jsonPath($..book[(@.length-1)])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("$..book[(@.length-1)]", -1),
                    new TestData("var.jsonPath($..book[?(@.price<10)])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("$..book[?(@.price<10)]", -1),
                    new TestData("var1.prop1[1].prop2[2].jsonPath($..book[?(@.price<10)])")
                        .addSegmentAttributes("var1", -1)
                        .addSegmentAttributes("prop1", 1)
                        .addSegmentAttributes("prop2", 2)
                        .addSegmentAttributes("$..book[?(@.price<10)]", -1),
                    new TestData("var.xpath(//title[@lang='en'])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("//title[@lang='en']", -1),
                    new TestData("var.xpath(/bookstore/book[price>35.00])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("/bookstore/book[price>35.00]", -1),
                    new TestData("var.xpath(/bookstore/book[position()<3])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("/bookstore/book[position()<3]", -1),
                    new TestData("var.xpath(/bookstore/book[last()-1])")
                        .addSegmentAttributes("var", -1)
                        .addSegmentAttributes("/bookstore/book[last()-1]", -1),
                    new TestData("var1.prop1[1].prop2[2].xpath(//title[@lang='en']])")
                        .addSegmentAttributes("var1", -1)
                        .addSegmentAttributes("prop1", 1)
                        .addSegmentAttributes("prop2", 2)
                        .addSegmentAttributes("//title[@lang='en']]", -1),
                };

    }

    private static class TestData {
        String expression;
        List<SegmentAttributes> segmentAttributes = new ArrayList<>();

        public TestData(String expression) {
            this.expression = expression;
        }

        TestData addSegmentAttributes(String name, int index) {
            segmentAttributes.add(new SegmentAttributes(name, index));
            return this;
        }

        @Override
        public String toString() {
            return expression;
        }
    }

    private static class SegmentAttributes {

        // The name of the segment
        String name;

        // The index. -1 if not appropriate.
        int index;

        public SegmentAttributes(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
}
