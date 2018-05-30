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

package com.consol.citrus.ftp.client;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpEndpointConfiguration extends FtpEndpointConfiguration {

    /** Path to private key of user */
    private String privateKeyPath;

    /** Password for private key */
    private String privateKeyPassword;

    /** Whether strict host checking should be performed */
    private boolean strictHostChecking = false;

    /** If strict host checking is used, path to the 'known_hosts' file */
    private String knownHosts;

    /**
     * Gets the strictHostChecking.
     *
     * @return
     */
    public boolean isStrictHostChecking() {
        return strictHostChecking;
    }

    /**
     * Sets the strictHostChecking.
     *
     * @param strictHostChecking
     */
    public void setStrictHostChecking(boolean strictHostChecking) {
        this.strictHostChecking = strictHostChecking;
    }

    /**
     * Gets the knownHosts.
     *
     * @return
     */
    public String getKnownHosts() {
        return knownHosts;
    }

    /**
     * Sets the knownHosts.
     *
     * @param knownHosts
     */
    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    /**
     * Gets the privateKeyPath.
     *
     * @return
     */
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    /**
     * Sets the privateKeyPath.
     *
     * @param privateKeyPath
     */
    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * Gets the privateKeyPassword.
     *
     * @return
     */
    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    /**
     * Sets the privateKeyPassword.
     *
     * @param privateKeyPassword
     */
    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }
}
