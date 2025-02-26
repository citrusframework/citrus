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

package org.citrusframework.jbang;

/**
 * Printer interface used by commands to write output to given print stream. By default, uses System out print stream,
 * but unit tests for instance may use a different print stream.
 */
public interface Printer {

    void println();

    void println(String line);

    void print(String output);

    void printf(String format, Object... args);

    default void printErr(String message) {
        printf("ERROR: %s%n", message);
    }

    default void printErr(String message, Exception e) {
        printErr("%s - %s".formatted(message, e.getMessage()));
    }

    default void printErr(Exception e) {
        printErr(e.getMessage());
    }

    /**
     * Default printer uses System out print stream.
     */
    class SystemOutPrinter implements Printer {
        public void println() {
            System.out.println();
        }

        public void println(String line) {
            System.out.println(line);
        }

        public void print(String output) {
            System.out.print(output);
        }

        public void printf(String format, Object... args) {
            System.out.printf(format, args);
        }
    }

}
