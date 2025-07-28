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

package org.citrusframework.openapi.testapi;

public enum ParameterStyle {

    NONE(io.swagger.v3.oas.annotations.enums.ParameterStyle.DEFAULT.toString()),
    MATRIX(io.swagger.v3.oas.annotations.enums.ParameterStyle.MATRIX.toString()),
    LABEL(io.swagger.v3.oas.annotations.enums.ParameterStyle.LABEL.toString()),
    FORM(io.swagger.v3.oas.annotations.enums.ParameterStyle.FORM.toString()),
    SPACEDELIMITED(io.swagger.v3.oas.annotations.enums.ParameterStyle.SPACEDELIMITED.toString()),
    PIPEDELIMITED(io.swagger.v3.oas.annotations.enums.ParameterStyle.PIPEDELIMITED.toString()),
    DEEPOBJECT(io.swagger.v3.oas.annotations.enums.ParameterStyle.DEEPOBJECT.toString()),
    SIMPLE(io.swagger.v3.oas.annotations.enums.ParameterStyle.SIMPLE.toString()),
    X_ENCODE_AS_JSON("x_encode_as_json");

    private final String styleString;

    ParameterStyle(String styleString) {
        this.styleString = styleString;
    }

    @Override
    public String toString() {
        return this.styleString;
    }
}
