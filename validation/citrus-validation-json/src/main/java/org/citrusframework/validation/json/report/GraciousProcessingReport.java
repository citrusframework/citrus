/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.json.report;

import com.networknt.schema.ValidationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class implements a report that represents a gracious interpretation of a list of JSON schema validation messages
 * (e.g. a {@link List<ValidationMessage>}).
 * <p>
 * Its main use-case is this: We cannot sincerely determine the matching JSON schema when validating messages in
 * {@link org.citrusframework.validation.json.schema.JsonSchemaValidation}. Therefore, if at least one schema validates
 * the message without any exceptions, the message will be marked as valid and thus accepted by this class.
 * </p>
 */
public class GraciousProcessingReport {

    private boolean success;
    private final List<ValidationMessage> validationMessages = new ArrayList<>();

    /**
     * Creates a new {@link GraciousProcessingReport} with the initial success state being {@code false}.
     */
    public GraciousProcessingReport() {
        this(false);
    }

    /**
     * Creates a new {@link GraciousProcessingReport} with the given initial success state.
     *
     * @param success the default success state
     */
    public GraciousProcessingReport(boolean success) {
        this.success = success;
    }

    /**
     * Creates a new {@link GraciousProcessingReport} considering the initial validation messages.
     *
     * @param validationMessages The list of validation messages to merge the existing report
     */
    public GraciousProcessingReport(Set<ValidationMessage> validationMessages) {
        this(false);
        mergeWith(validationMessages);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    /**
     * Merges this {@link GraciousProcessingReport} with the status information from the existing one.
     *
     * @param validationMessages the new validation messages to consider
     */
    public void mergeWith(Set<ValidationMessage> validationMessages) {
        success = success || validationMessages.isEmpty();
        this.validationMessages.addAll(validationMessages);
    }
}
