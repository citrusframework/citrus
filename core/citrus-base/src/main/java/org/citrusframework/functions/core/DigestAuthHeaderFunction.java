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

package org.citrusframework.functions.core;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Function creates digest authentication HTTP header with given security parameters:
 * username
 * password
 * realm
 * noncekey
 * method
 * uri
 * opaque
 * algorithm
 */
public class DigestAuthHeaderFunction implements ParameterizedFunction<DigestAuthHeaderFunction.Parameters> {

    /** Nonce is valid for 60 seconds */
    private Long nonceValidity = 60000L;

    @Override
    public String execute(Parameters params, TestContext context) {
        StringBuilder authorizationHeader = new StringBuilder();

        String digest1 = params.getUsername() + ":" + params.getRealm() + ":" + params.getPassword();
        String digest2 = params.getMethod() + ":" + params.getUri();

        Long expirationTime = System.currentTimeMillis() + nonceValidity;
        String nonce = Base64.encodeBase64String((expirationTime + ":" + getDigestHex(params.getAlgorithm(), expirationTime + ":" + params.getNoncekey())).getBytes());

        authorizationHeader.append("Digest username=");
        authorizationHeader.append(params.getUsername());
        authorizationHeader.append(",realm=");
        authorizationHeader.append(params.getRealm());
        authorizationHeader.append(",nonce=");
        authorizationHeader.append(nonce);
        authorizationHeader.append(",uri=");
        authorizationHeader.append(params.getUri());
        authorizationHeader.append(",response=");
        authorizationHeader.append(getDigestHex(params.getAlgorithm(), getDigestHex(params.getAlgorithm(), digest1) + ":" + nonce + ":" + getDigestHex(params.getAlgorithm(), digest2)));
        authorizationHeader.append(",opaque=");
        authorizationHeader.append(getDigestHex(params.getAlgorithm(), params.getOpaque()));
        authorizationHeader.append(",algorithm=");
        authorizationHeader.append(params.getAlgorithm());

        return authorizationHeader.toString();
    }

    /**
     * Generates digest hexadecimal string representation of a key with given
     * algorithm.
     */
    private String getDigestHex(String algorithm, String key) {
        if (algorithm.equals("md5")) {
            return DigestUtils.md5Hex(key);
        } else if (algorithm.equals("sha")) {
            return DigestUtils.shaHex(key);
        }

        throw new CitrusRuntimeException("Unsupported digest algorithm: " + algorithm);
    }

    /**
     * Sets the nonceValidity.
     * @param nonceValidity the nonceValidity to set
     */
    public void setNonceValidity(Long nonceValidity) {
        this.nonceValidity = nonceValidity;
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {

        private String username;
        private String password;
        private String realm;
        private String noncekey;
        private String method;
        private String uri;
        private String opaque;
        private String algorithm;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.size() < 8) {
                throw new InvalidFunctionUsageException("Function parameters not set correctly - need parameters: " +
                        "username,password,realm,noncekey,method,uri,opaque,algorithm");
            }

            setUsername(parameterList.get(0));
            setPassword(parameterList.get(1));
            setRealm(parameterList.get(2));
            setNoncekey(parameterList.get(3));
            setMethod(parameterList.get(4));
            setUri(parameterList.get(5));
            setOpaque(parameterList.get(6));
            setAlgorithm(parameterList.get(7));
        }

        public String getUsername() {
            return username;
        }

        @SchemaProperty(required = true, description = "The username.")
        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        @SchemaProperty(required = true, description = "The password.")
        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealm() {
            return realm;
        }

        @SchemaProperty(required = true, description = "The realm.")
        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getNoncekey() {
            return noncekey;
        }

        @SchemaProperty(required = true, description = "The noncekey.")
        public void setNoncekey(String noncekey) {
            this.noncekey = noncekey;
        }

        public String getMethod() {
            return method;
        }

        @SchemaProperty(required = true, description = "The method.")
        public void setMethod(String method) {
            this.method = method;
        }

        public String getUri() {
            return uri;
        }

        @SchemaProperty(required = true, description = "The uri.")
        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getOpaque() {
            return opaque;
        }

        @SchemaProperty(required = true, description = "The opaque.")
        public void setOpaque(String opaque) {
            this.opaque = opaque;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        @SchemaProperty(required = true, description = "The algorithm.")
        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }
}
