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

package org.citrusframework.kubernetes.config;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CredentialValidatorTest {

    private static final String IS_VALID_DATA_PROVIDER = "is-valid";

    @DataProvider(name = IS_VALID_DATA_PROVIDER)
    public static Object[][] primeNumbers() {
        return new Object[][]{
                {"user1", "pass1", null, true},
                {null, null, "token1", true},
                {"user1", null, "token1", false},
                {null, "pass1", "token1", false},
                {"user1", "pass1", "token1", false},
                {null, "pass1", null, false},
                {null, null, null, true}
        };
    }

    @Test(dataProvider = IS_VALID_DATA_PROVIDER)
    public void isValid(String username, String password, String oauthToken, boolean expectedResult) {
        assertEquals(CredentialValidator.isValid(username, password, oauthToken), expectedResult);
    }
}
