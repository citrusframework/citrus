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

package org.citrusframework.validation.context;

/**
 * Default validation context keeps track of its status to mark if this context has been processed during the validation.
 * @since 2.4
 */
public class DefaultValidationContext implements ValidationContext {

    /** The status of this context */
    private ValidationStatus status = ValidationStatus.UNKNOWN;

    /**
     * Updates the validation status if update is allowed according to the current status.
     * @param status the new status
     */
    public void updateStatus(ValidationStatus status) {
        if (updateAllowed()) {
            this.status = status;
        }
    }

    /**
     * Determine whether the status update is allowed.
     * In case the current state is FAILED the update is not allowed in order to not loose the failure state.
     * @return
     */
    private boolean updateAllowed() {
        return this.status != ValidationStatus.FAILED;
    }

    @Override
    public ValidationStatus getStatus() {
        return status;
    }
}
