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

package org.citrusframework.openapi.validation;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.ValidationReport;
import org.citrusframework.openapi.model.OperationPathAdapter;

public abstract class OpenApiValidator {

    protected final OpenApiInteractionValidator openApiInteractionValidator;

    protected boolean enabled;

    protected OpenApiValidator(OpenApiInteractionValidator openApiInteractionValidator, boolean enabled) {
        this.openApiInteractionValidator = openApiInteractionValidator;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected abstract String getType();

    /**
     * Constructs the error message of a failed validation based on the processing report passed
     * from {@link ValidationReport}.
     *
     * @param report The report containing the error message
     * @return A string representation of all messages contained in the report
     */
    protected String constructErrorMessage(OperationPathAdapter operationPathAdapter,
        ValidationReport report) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OpenApi ");
        stringBuilder.append(getType());
        stringBuilder.append(" validation failed for operation: ");
        stringBuilder.append(operationPathAdapter);
        report.getMessages().forEach(message -> stringBuilder.append("\n\t").append(message));
        return stringBuilder.toString();
    }
}
