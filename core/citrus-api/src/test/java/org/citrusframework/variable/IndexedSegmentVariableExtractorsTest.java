package org.citrusframework.variable;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.SegmentVariableExtractorRegistry.MapVariableExtractor;
import org.citrusframework.variable.SegmentVariableExtractorRegistry.ObjectFieldValueExtractor;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class IndexedSegmentVariableExtractorsTest {

    private final TestContext context = new TestContext();

    private static VariableExpressionSegmentMatcher matcher(String segmentExpr) {
        VariableExpressionSegmentMatcher m = new VariableExpressionSegmentMatcher(segmentExpr);
        assertThat(m.nextMatch()).as("first segment should match").isTrue();
        return m;
    }

    @Test
    public void mapExtractor_listIndex_success() {
        Map<String, Object> ctx = Map.of("names", List.of("A", "B", "C"));
        var extractor = MapVariableExtractor.INSTANCE;

        var m = matcher("names[1]");

        assertThat(extractor.canExtract(context, ctx, m)).isTrue();
        assertThat(extractor.extractValue(context, ctx, m)).isEqualTo("B");
    }

    @Test
    public void mapExtractor_arrayIndex_success() {
        Map<String, Object> ctx = Map.of("nums", new int[] {10, 20, 30});
        var extractor = MapVariableExtractor.INSTANCE;

        var m = matcher("nums[2]");

        assertThat(extractor.canExtract(context, ctx, m)).isTrue();
        assertThat(extractor.extractValue(context, ctx, m)).isEqualTo(30);
    }

    @Test
    public void mapExtractor_arrayIndex_nullElement_success() {
        Map<String, Object> ctx = Map.of("nums", new Integer[] {10, 20, null});
        var extractor = MapVariableExtractor.INSTANCE;

        var m = matcher("nums[2]");

        assertThat(extractor.canExtract(context, ctx, m)).isTrue();
        assertThat(extractor.extractValue(context, ctx, m)).isNull();
    }

    @Test
    public void mapExtractor_unknownKey_failsWithHelpfulMessage() {
        Map<String, Object> ctx = Map.of("names", List.of("A", "B"));
        var extractor = MapVariableExtractor.INSTANCE;

        var m = matcher("missing");

        assertThatThrownBy(() -> extractor.extractValue(context, ctx, m))
            .isInstanceOf(CitrusRuntimeException.class)
            .extracting("message", STRING)
            .isEqualToIgnoringWhitespace("""
                  Unable to extract value using expression 'missing'!
                  Reason: Unknown key 'missing' in Map.
                  From object (java.util.ImmutableCollections$Map1):
                  {names=[A, B]}""");
    }

    @Test
    public void mapExtractor_indexOutOfBounds_failsWithSizeInfo() {
        Map<String, Object> ctx = Map.of("names", List.of("A", "B", "C"));
        var extractor = MapVariableExtractor.INSTANCE;

        var m = matcher("names[3]"); // OOB

        assertThatThrownBy(() -> extractor.extractValue(context, ctx, m))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Unable to extract value using expression 'names[3]'")
            .hasMessageContaining("Index 3 out of bounds (list size 3) for segment 'names'");
    }

    @Test
    public void mapExtractor_wrongTypeForIndexing_failsWithTypeInfo() {
        Map<String, Object> ctx = Map.of("names", 42); // not list/array
        var extractor = MapVariableExtractor.INSTANCE;

        var m = matcher("names[0]");

        assertThatThrownBy(() -> extractor.extractValue(context, ctx, m))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Unable to extract value using expression 'names[0]'")
            .hasMessageContaining("Expected array or List for indexed access, but was java.lang.Integer (segment 'names')");
    }

    public static class Person {
        int[] numbers = {10, 20, 30};
        List<String> tags = List.of("alpha", "beta");
        String name = "Peter";
        String nullField = null;
    }

    @Test
    public void objectFieldExtractor_arrayIndex_success() {
        var person = new Person();
        var extractor = ObjectFieldValueExtractor.INSTANCE;

        var m = matcher("numbers[2]");

        assertThat(extractor.canExtract(context, person, m)).isTrue();
        assertThat(extractor.extractValue(context, person, m)).isEqualTo(30);
    }

    @Test
    public void objectFieldExtractor_listIndex_success() {
        var person = new Person();
        var extractor = ObjectFieldValueExtractor.INSTANCE;

        var m = matcher("tags[0]");

        assertThat(extractor.canExtract(context, person, m)).isTrue();
        assertThat(extractor.extractValue(context, person, m)).isEqualTo("alpha");
    }

    @Test
    public void objectFieldExtractor_nullValueFromField_success() {
        var person = new Person();
        var extractor = ObjectFieldValueExtractor.INSTANCE;

        var m = matcher("nullField");

        assertThat(extractor.canExtract(context, person, m)).isTrue();
        assertThat(extractor.extractValue(context, person, m)).isNull();
    }

    @Test
    public void objectFieldExtractor_unknownField_failsWithHelpfulMessage() {
        var person = new Person();
        var extractor = ObjectFieldValueExtractor.INSTANCE;

        var m = matcher("missing");

        assertThatThrownBy(() -> extractor.extractValue(context, person, m))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Unable to extract value using expression 'missing'")
            .hasMessageContaining("Reason: Unknown field 'missing' on type org.citrusframework.variable.IndexedSegmentVariableExtractorsTest$Person");
    }

    @Test
    public void objectFieldExtractor_wrongTypeForIndexing_failsWithTypeInfo() {
        var person = new Person(); // field 'name' is String
        var extractor = ObjectFieldValueExtractor.INSTANCE;

        var m = matcher("name[0]");

        assertThatThrownBy(() -> extractor.extractValue(context, person, m))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Unable to extract value using expression 'name[0]'")
            .hasMessageContaining("Expected array or List for indexed access, but was java.lang.String (segment 'name')");
    }

    @Test
    public void objectFieldExtractor_indexOutOfBounds_failsWithLengthInfo() {
        var person = new Person();
        var extractor = ObjectFieldValueExtractor.INSTANCE;

        var m = matcher("numbers[9]");

        assertThatThrownBy(() -> extractor.extractValue(context, person, m))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Unable to extract value using expression 'numbers[9]'")
            .hasMessageContaining("Index 9 out of bounds (array length 3) for segment 'numbers'");
    }
}
