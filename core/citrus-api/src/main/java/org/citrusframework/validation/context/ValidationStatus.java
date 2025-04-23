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
 * Status that marks that the validation result for a specific validation context.
 * The validation context keeps track of its state in order to identify passed, failed or unknown state.
 * Message validators update the status on the validation context after processing with the outcome of the validation.
 * This way we can track if a validation context has been processed during validation.
 */
public enum ValidationStatus {

    OPTIONAL,
    PASSED,
    FAILED,
    UNKNOWN
}
