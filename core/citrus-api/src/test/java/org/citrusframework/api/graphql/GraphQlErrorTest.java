/*
 * Copyright the original author or authors.
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
package org.citrusframework.api.graphql;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphQlErrorTest {

    @Test
    public void shouldBuildError() {
        GraphQlError error = GraphQlError.error()
                .message("Book not found")
                .path("books", 1, "title")
                .code("NOT_FOUND");

        assertEquals("Book not found", error.getMessage());
        assertEquals(List.of("books", 1, "title"), error.getPath());
        assertEquals("NOT_FOUND", error.getCode());
        assertEquals("[NOT_FOUND] Book not found at books[1].title", error.toString());
    }

    @Test
    public void shouldLeaveUnsetFieldsEmpty() {
        GraphQlError error = GraphQlError.error();

        assertNull(error.getMessage());
        assertNull(error.getCode());
        assertTrue(error.getPath().isEmpty());
        assertEquals("<any error>", error.toString());
    }

    @Test
    public void shouldDescribePartialError() {
        assertEquals("[INTERNAL]", GraphQlError.error().code("INTERNAL").toString());
        assertEquals("boom", GraphQlError.error().message("boom").toString());
        assertEquals("at book", GraphQlError.error().path("book").toString());
    }

    @Test
    public void shouldParseDottedPath() {
        assertEquals(List.of("books", 1, "title"), GraphQlError.parsePath("books[1].title"));
        assertEquals(List.of("book", "author"), GraphQlError.parsePath("book.author"));
        assertEquals(List.of("matrix", 0, 2), GraphQlError.parsePath("matrix[0][2]"));
        assertTrue(GraphQlError.parsePath("").isEmpty());
        assertTrue(GraphQlError.parsePath(null).isEmpty());
    }

    @Test
    public void shouldFormatPathLikeParsedPath() {
        assertEquals("books[1].title", GraphQlError.formatPath(List.of("books", 1, "title")));
        assertEquals("", GraphQlError.formatPath(List.of()));
    }
}
