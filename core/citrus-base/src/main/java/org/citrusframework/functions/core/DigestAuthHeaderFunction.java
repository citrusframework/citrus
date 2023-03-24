/*
 * Copyright 2006-2012 the original author or authors.
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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

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
 * 
 * @author Christoph Deppisch
 */
public class DigestAuthHeaderFunction implements Function {
    
    /** Nonce is valid for 60 seconds */
    private Long nonceValidity = 60000L;
    
    /**
      * {@inheritDoc}
      */
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.size() < 8) {
            throw new InvalidFunctionUsageException("Function parameters not set correctly - need parameters: username,password,realm,noncekey,method,uri,opaque,algorithm");
        }

        StringBuilder authorizationHeader = new StringBuilder();

        String username = parameterList.get(0);
        String password = parameterList.get(1);
        String realm = parameterList.get(2);
        String noncekey = parameterList.get(3);
        String method = parameterList.get(4);
        String uri = parameterList.get(5);
        String opaque = parameterList.get(6);
        String algorithm = parameterList.get(7);
        
        String digest1 = username + ":" + realm + ":" + password;
        String digest2 = method + ":" + uri;
        
        Long expirationTime = System.currentTimeMillis() + nonceValidity;
        String nonce = Base64.encodeBase64String((expirationTime + ":" + getDigestHex(algorithm, expirationTime + ":" + noncekey)).getBytes());
        
        authorizationHeader.append("Digest username=");
        authorizationHeader.append(username);
        authorizationHeader.append(",realm=");
        authorizationHeader.append(realm);
        authorizationHeader.append(",nonce=");
        authorizationHeader.append(nonce);
        authorizationHeader.append(",uri=");
        authorizationHeader.append(uri);
        authorizationHeader.append(",response=");
        authorizationHeader.append(getDigestHex(algorithm, getDigestHex(algorithm, digest1) + ":" + nonce + ":" + getDigestHex(algorithm, digest2)));
        authorizationHeader.append(",opaque=");
        authorizationHeader.append(getDigestHex(algorithm, opaque));
        authorizationHeader.append(",algorithm=");
        authorizationHeader.append(algorithm);
        
        return authorizationHeader.toString();
    }

    /**
     * Generates digest hexadecimal string representation of a key with given
     * algorithm.
     * 
     * @param algorithm
     * @param key
     * @return
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
    
}
