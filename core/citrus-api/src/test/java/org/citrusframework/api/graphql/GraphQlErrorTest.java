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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class GraphQlErrorTest {

    @Test
    public void shouldBuildError() {
        GraphQlError error = GraphQlError.error()
                .message("Book not found")
                .path("books", 1, "title")
                .code("NOT_FOUND");

        assertEquals(error.getMessage(), "Book not found");
        assertEquals(error.getPath(), List.of("books", 1, "title"));
        assertEquals(error.getCode(), "NOT_FOUND");
        assertEquals(error.toString(), "[NOT_FOUND] Book not found at books[1].title");
    }

    @Test
    public void shouldLeaveUnsetFieldsEmpty() {
        GraphQlError error = GraphQlError.error();

        assertNull(error.getMessage());
        assertNull(error.getCode());
        assertTrue(error.getPath().isEmpty());
        assertEquals(error.toString(), "<any error>");
    }

    @Test
    public void shouldDescribePartialError() {
        assertEquals(GraphQlError.error().code("INTERNAL").toString(), "[INTERNAL]");
        assertEquals(GraphQlError.error().message("boom").toString(), "boom");
        assertEquals(GraphQlError.error().path("book").toString(), "at book");
    }

    @Test
    public void shouldParseDottedPath() {
        assertEquals(GraphQlError.parsePath("books[1].title"), List.of("books", 1, "title"));
        assertEquals(GraphQlError.parsePath("book.author"), List.of("book", "author"));
        assertEquals(GraphQlError.parsePath("matrix[0][2]"), List.of("matrix", 0, 2));
        assertTrue(GraphQlError.parsePath("").isEmpty());
        assertTrue(GraphQlError.parsePath(null).isEmpty());
    }

    @Test
    public void shouldFormatPathLikeParsedPath() {
        assertEquals(GraphQlError.formatPath(List.of("books", 1, "title")), "books[1].title");
        assertEquals(GraphQlError.formatPath(List.of()), "");
    }
}
