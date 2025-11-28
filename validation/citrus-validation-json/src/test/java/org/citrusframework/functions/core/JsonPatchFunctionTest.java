package org.citrusframework.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonPatchFunctionTest {

    private JsonPatchFunction function;

    @Mock
    private TestContext testContext;

    @BeforeEach
    void setUp() {
        function = new JsonPatchFunction();
    }

    private String execute(List<String> params) {
        JsonPatchFunction.Parameters parameters = function.getParameters();
        parameters.configure(params, testContext);
        return function.execute(parameters, testContext);
    }

    private void mockReplaceDynamicContent(String input, String output) {
        when(testContext.replaceDynamicContentInString(input)).thenReturn(output);
    }

    private void mockPassThrough(String... values) {
        for (String value : values) {
            when(testContext.replaceDynamicContentInString(value)).thenReturn(value);
        }
    }

    @Test
    void shouldReplaceSimpleValue() {
        // Given
        String jsonInput = """
                {
                  "name": "Citrus_Api_Test_Example",
                  "status": "In Design"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.status", "Ready");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.status", "Ready"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"status\":\"Ready\"")
            .contains("\"name\":\"Citrus_Api_Test_Example\"");
    }

    @Test
    void shouldReplaceMultipleValues() {
        // Given
        String jsonInput = """
                {
                  "components": [
                    {
                      "testStepValues": [
                        { "name": "dslOperation1", "value": "", "type": "String" },
                        { "name": "dslOperation2", "value": "", "type": "String" }
                      ]
                    }
                  ]
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.components[0].testStepValues[0].value", "!sleep:seconds(5)",
            "$.components[0].testStepValues[1].value", "!report:info('Test')");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.components[0].testStepValues[0].value", "!sleep:seconds(5)",
            "replace", "$.components[0].testStepValues[1].value", "!report:info('Test')"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"value\":\"!sleep:seconds(5)\"")
            .contains("\"value\":\"!report:info('Test')\"");
    }

    @Test
    void shouldAddElementToArray() {
        // Given
        String jsonInput = """
                {
                  "components": [
                    {
                      "testStepValues": [
                        { "name": "dslOperation1", "value": "test1" }
                      ]
                    }
                  ]
                }
                """;

        String newElement = "{\"name\":\"dslOperation2\",\"value\":\"test2\"}";

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("add", "$.components[0].testStepValues/-", newElement);

        List<String> params = asList(
            "${jsonSource}",
            "add", "$.components[0].testStepValues/-", newElement
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"name\":\"dslOperation1\"")
            .contains("\"name\":\"dslOperation2\"")
            .contains("\"value\":\"test2\"");
    }

    @Test
    void shouldAddElementAtSpecificIndex() {
        // Given
        String jsonInput = """
                {
                  "items": ["first", "third"]
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("add", "$.items[1]", "second");

        List<String> params = asList(
            "${jsonSource}",
            "add", "$.items[1]", "second"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result).contains("\"first\"", "\"second\"", "\"third\"");
    }

    @Test
    void shouldRemoveElement() {
        // Given
        String jsonInput = """
                {
                  "components": [
                    {
                      "testStepValues": [
                        { "name": "dslOperation1", "value": "test1" },
                        { "name": "dslOperation2", "value": "test2" },
                        { "name": "dslOperation3", "value": "test3" }
                      ]
                    }
                  ]
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("remove", "$.components[0].testStepValues[1]", "");

        List<String> params = asList(
            "${jsonSource}",
            "remove", "$.components[0].testStepValues[1]", ""
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"name\":\"dslOperation1\"")
            .contains("\"name\":\"dslOperation3\"")
            .doesNotContain("\"name\":\"dslOperation2\"");
    }

    @Test
    void shouldRemoveProperty() {
        // Given
        String jsonInput = """
                {
                  "name": "Test",
                  "status": "Ready",
                  "description": "To be removed"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("remove", "$.description", "");

        List<String> params = asList(
            "${jsonSource}",
            "remove", "$.description", ""
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"name\":\"Test\"")
            .contains("\"status\":\"Ready\"")
            .doesNotContain("description");
    }

    @Test
    void shouldCopyValue() {
        // Given
        String jsonInput = """
                {
                  "original": "value",
                  "target": "old"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("copy", "$.target", "$.original");

        List<String> params = asList(
            "${jsonSource}",
            "copy", "$.target", "$.original"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"original\":\"value\"")
            .contains("\"target\":\"value\"");
    }

    @Test
    void shouldMoveValue() {
        // Given
        String jsonInput = """
                {
                  "source": "moveMe",
                  "target": "old"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("move", "$.target", "$.source");

        List<String> params = asList(
            "${jsonSource}",
            "move", "$.target", "$.source"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"target\":\"moveMe\"")
            .doesNotContain("\"source\"");
    }

    @Test
    void shouldHandleComplexJsonValue() {
        // Given
        String jsonInput = """
                {
                  "components": []
                }
                """;

        String complexValue = "{\"name\":\"ApiDsl\",\"testStepValues\":[{\"name\":\"dslOperation1\",\"value\":\"test\"}]}";

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("add", "$.components/-", complexValue);

        List<String> params = asList(
            "${jsonSource}",
            "add", "$.components/-", complexValue
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"name\":\"ApiDsl\"")
            .contains("\"name\":\"dslOperation1\"");
    }

    @Test
    void shouldHandleNumericValue() {
        // Given
        String jsonInput = """
                {
                  "count": 10
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.count", "42");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.count", "42"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result).contains("\"count\":42");
    }

    @Test
    void shouldHandleBooleanValue() {
        // Given
        String jsonInput = """
                {
                  "disabled": false
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.disabled", "true");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.disabled", "true"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result).contains("\"disabled\":true");
    }

    @Test
    void shouldHandleSpecialCharactersInStringValue() {
        // Given
        String jsonInput = """
                {
                  "text": "original"
                }
                """;

        String specialValue = "Text with \"quotes\" and \nnewlines";

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.text", specialValue);

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.text", specialValue
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result).contains("Text with \\\"quotes\\\" and \\nnewlines");
    }

    @Test
    void shouldApplyMultipleOperations() {
        // Given
        String jsonInput = """
                {
                  "name": "Test",
                  "status": "In Design",
                  "items": ["a", "b"]
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.status", "Ready", "add", "$.items/-", "c", "$.name", "Updated Test");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.status", "Ready",
            "add", "$.items/-", "c",
            "replace", "$.name", "Updated Test"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"status\":\"Ready\"")
            .contains("\"name\":\"Updated Test\"")
            .contains("\"c\"");
    }

    @Test
    void shouldHandleInlineJsonSource() {
        // Given
        String inlineJson = "{\"name\":\"Test\",\"status\":\"Draft\"}";

        mockPassThrough(inlineJson, "replace", "$.status", "Published");

        List<String> params = asList(
            inlineJson,
            "replace", "$.status", "Published"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result)
            .contains("\"name\":\"Test\"")
            .contains("\"status\":\"Published\"");
    }

    @Test
    void shouldThrowExceptionWhenNoParameters() {
        // Given
        List<String> params = emptyList();

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessageContaining("must not be empty");

        verify(testContext, never()).replaceDynamicContentInString(anyString());
    }

    @Test
    void shouldThrowExceptionWhenInvalidParameterCount() {
        // Given
        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.path" // Missing value
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessageContaining("Missing parameters");
    }

    @Test
    void shouldThrowExceptionWhenTooFewParameters() {
        // Given
        List<String> params = asList("${jsonSource}");

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessageContaining("Missing parameters");
    }

    @Test
    void shouldThrowExceptionWhenSourceIsEmpty() {
        // Given
        mockReplaceDynamicContent("${jsonSource}", null);
        mockPassThrough("replace", "$.path", "value");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.path", "value"
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("does not contain valid JSON");
    }

    @Test
    void shouldThrowExceptionWhenJsonIsInvalid() {
        // Given
        mockReplaceDynamicContent("${jsonSource}", "not a valid json");
        mockPassThrough("replace", "$.path", "value");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.path", "value"
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("does not contain valid JSON");
    }

    @Test
    void shouldThrowExceptionWhenInvalidOperation() {
        // Given
        mockPassThrough("invalid", "$.name", "value");

        List<String> params = asList(
            "${jsonSource}",
            "invalid", "$.name", "value"
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessageContaining("Invalid patch operation");
    }

    @Test
    void shouldThrowExceptionWhenPathNotFound() {
        // Given
        String jsonInput = """
                {
                  "name": "Test"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("replace", "$.nonexistent", "value");

        List<String> params = asList(
            "${jsonSource}",
            "replace", "$.nonexistent", "value"
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Failed to apply JSON Patch");
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonexistentPath() {
        // Given
        String jsonInput = """
                {
                  "name": "Test"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("remove", "$.nonexistent", "");

        List<String> params = asList(
            "${jsonSource}",
            "remove", "$.nonexistent", ""
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Failed to apply JSON Patch");
    }

    @Test
    void shouldThrowExceptionWhenAddingToInvalidPath() {
        // Given
        String jsonInput = """
                {
                  "name": "Test"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockPassThrough("add", "$.nonexistent.nested", "value");

        List<String> params = asList(
            "${jsonSource}",
            "add", "$.nonexistent.nested", "value"
        );

        // When / Then
        assertThatThrownBy(() -> execute(params))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessageContaining("Failed to apply JSON Patch");
    }

    @Test
    void shouldResolveDynamicContentInOperationParameters() {
        // Given
        String jsonInput = """
                {
                  "status": "Draft"
                }
                """;

        mockReplaceDynamicContent("${jsonSource}", jsonInput);
        mockReplaceDynamicContent("${operation}", "replace");
        mockReplaceDynamicContent("${path}", "$.status");
        mockReplaceDynamicContent("${newValue}", "Published");

        List<String> params = asList(
            "${jsonSource}",
            "${operation}", "${path}", "${newValue}"
        );

        // When
        String result = execute(params);

        // Then
        assertThat(result).contains("\"status\":\"Published\"");
    }
}