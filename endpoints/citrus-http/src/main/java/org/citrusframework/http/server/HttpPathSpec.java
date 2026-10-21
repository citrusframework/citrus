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

package org.citrusframework.http.server;

/**
 * Path specification used to match request paths against secured paths. The semantics follow the
 * path specification of a servlet container:
 * <ul>
 *     <li>An exact path such as {@code /orders} matches only that path.</li>
 *     <li>A prefix path such as {@code /secured/*} matches the path itself and all paths below it,
 *     but not a path with the same prefix like {@code /securedfoo}.</li>
 *     <li>A suffix path such as {@code *.json} matches all paths ending with that suffix.</li>
 *     <li>The root path {@code /} matches all paths.</li>
 * </ul>
 * The specificity of a path specification defines the order in which multiple specifications are
 * applied. The most specific specification wins, exact paths before prefix paths, prefix paths
 * before suffix paths and suffix paths before the default path.
 *
 * @since 5.2
 */
public class HttpPathSpec {

    /** Type of the path specification. */
    public enum Type {
        /** Exact path match. */
        EXACT,
        /** Prefix path match, e.g. {@code /secured/*}. */
        PREFIX,
        /** Suffix path match, e.g. {@code *.json}. */
        SUFFIX,
        /** Default path match, the root path matching all requests. */
        DEFAULT,
        /** Root path match, matching the root path only. */
        ROOT
    }

    private final String spec;
    private final Type type;
    private final String matchPath;

    private HttpPathSpec(String spec, Type type, String matchPath) {
        this.spec = spec;
        this.type = type;
        this.matchPath = matchPath;
    }

    /**
     * Parses the given path specification.
     *
     * @param spec the path specification to parse.
     * @return the parsed path specification.
     */
    public static HttpPathSpec parse(String spec) {
        if (spec == null || spec.isEmpty()) {
            return new HttpPathSpec("", Type.ROOT, "/");
        }

        if ("/".equals(spec)) {
            return new HttpPathSpec(spec, Type.DEFAULT, "/");
        }

        if (spec.endsWith("/*")) {
            return new HttpPathSpec(spec, Type.PREFIX, spec.substring(0, spec.length() - 2));
        }

        if (spec.startsWith("*")) {
            return new HttpPathSpec(spec, Type.SUFFIX, spec.substring(1));
        }

        return new HttpPathSpec(spec, Type.EXACT, spec);
    }

    /**
     * Checks if the given request path matches this path specification.
     *
     * @param path the request path to check.
     * @return true if the path matches this specification.
     */
    public boolean matches(String path) {
        if (path == null) {
            return false;
        }

        return switch (type) {
            case ROOT -> "/".equals(path);
            case DEFAULT -> true;
            case PREFIX -> path.equals(matchPath) || path.startsWith(matchPath + "/");
            case SUFFIX -> path.endsWith(matchPath);
            case EXACT -> matchPath.equals(path);
        };
    }

    /**
     * Gets the specificity of this path specification. The higher the value the more specific the
     * specification is.
     *
     * @return the specificity of this path specification.
     */
    public int getSpecificity() {
        return switch (type) {
            case EXACT -> 1000;
            case PREFIX -> 500 + matchPath.length();
            case SUFFIX -> 400 + matchPath.length();
            case DEFAULT -> 100;
            case ROOT -> 50;
        };
    }

    /**
     * Gets the type of this path specification.
     *
     * @return the type of this path specification.
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return spec;
    }
}
