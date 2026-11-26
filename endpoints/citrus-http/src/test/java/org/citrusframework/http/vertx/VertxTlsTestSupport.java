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

package org.citrusframework.http.vertx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/** Fresh, short-lived test certificates; uses the JDK running the tests. */
public final class VertxTlsTestSupport implements AutoCloseable {
    public static final String PASSWORD = "test-password";
    public final Path directory;
    public final Path server;
    public final Path client;
    public final Path stranger;
    public VertxTlsTestSupport() throws Exception {
        directory = Files.createTempDirectory("citrus-vertx-tls-");
        server = create("server", "localhost", "SAN=dns:localhost,ip:127.0.0.1");
        client = create("client", "client", "EKU=clientAuth");
        stranger = create("stranger", "wrong.invalid", "SAN=dns:wrong.invalid");
    }
    private Path create(String name, String subject, String extension) throws Exception {
        Path store = directory.resolve(name + ".p12");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", name, "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
                "-dname", "CN=" + subject, "-ext", extension, "-storetype", "PKCS12",
                "-keystore", store.toString(), "-storepass", PASSWORD, "-keypass", PASSWORD, "-noprompt")
                .redirectErrorStream(true).redirectOutput(directory.resolve(name + ".log").toFile()).start();
        if (!process.waitFor(20, java.util.concurrent.TimeUnit.SECONDS)) {
            process.destroyForcibly(); throw new IllegalStateException("keytool timed out");
        }
        if (process.exitValue() != 0) throw new IllegalStateException("Unable to generate test certificate");
        return store;
    }
    private static KeyStore load(Path path) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (var stream = Files.newInputStream(path)) { store.load(stream, PASSWORD.toCharArray()); }
        return store;
    }
    public SSLContext context(Path trusted, Path identity) throws Exception {
        var trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(load(trusted));
        KeyManagerFactory keys = null;
        if (identity != null) {
            keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keys.init(load(identity), PASSWORD.toCharArray());
        }
        var context = SSLContext.getInstance("TLS");
        context.init(keys == null ? null : keys.getKeyManagers(), trust.getTrustManagers(), null);
        return context;
    }
    @Override public void close() throws Exception {
        try (var files = Files.walk(directory)) {
            for (Path file : files.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(file);
        }
    }
}
