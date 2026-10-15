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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.http.vertx.VertxHttpMessageMapper.RequestSnapshot;
import org.citrusframework.http.vertx.VertxHttpMessageMapper.ResponsePlan;

/** Gzip processing on the owned exchange worker, with bounded request expansion. */
final class VertxHttpGzip {
    private VertxHttpGzip() { }

    static final class Failure extends RuntimeException {
        final int status;
        Failure(int status, String message) { super(message); this.status = status; }
    }

    static String header(Map<String, List<String>> headers, String name) {
        return headers.entrySet().stream().filter(e -> name.equalsIgnoreCase(e.getKey()))
                .flatMap(e -> e.getValue().stream()).reduce((a, b) -> a + "," + b).orElse("");
    }

    static RequestSnapshot decode(RequestSnapshot request, long limit) {
        String encoding = header(request.headers(), "Content-Encoding").trim();
        if (encoding.isEmpty() || encoding.equalsIgnoreCase("identity")) return request;
        if (!encoding.equalsIgnoreCase("gzip")) throw new Failure(415, "Unsupported request content coding");
        var output = new ByteArrayOutputStream();
        try (var input = new GZIPInputStream(new ByteArrayInputStream(request.body()))) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if ((long) output.size() + count > limit) throw new Failure(413, "Expanded request exceeds body limit");
                output.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new Failure(400, "Invalid gzip request");
        }
        return new RequestSnapshot(request.method(), request.uri(), request.path(), request.query(), request.version(),
                request.headers(), output.toByteArray(), request.exchangeId(), request.generation(),
                request.attributes());
    }

    static boolean acceptsGzip(String accept) {
        Double explicit = null;
        double wildcard = 0;
        for (String item : accept.split(",")) {
            String[] parts = item.trim().split(";");
            double quality = 1;
            for (int i = 1; i < parts.length; i++) {
                String[] parameter = parts[i].trim().split("=", 2);
                if (parameter[0].equalsIgnoreCase("q")) {
                    try { quality = Double.parseDouble(parameter.length == 2 ? parameter[1].trim() : ""); }
                    catch (NumberFormatException e) { quality = 0; }
                    if (!Double.isFinite(quality) || quality < 0 || quality > 1) quality = 0;
                }
            }
            if (parts[0].equalsIgnoreCase("gzip")) explicit = explicit == null ? quality : Math.min(explicit, quality);
            else if (parts[0].equals("*")) wildcard = quality;
        }
        return (explicit == null ? wildcard : explicit) > 0;
    }

    static ResponsePlan encode(ResponsePlan plan, RequestSnapshot request) throws IOException {
        if (plan.suppressBody() || plan.body().length == 0 || plan.statusCode() < 200
                || plan.statusCode() == 204 || plan.statusCode() == 205 || plan.statusCode() == 304
                || plan.statusCode() == 206 || !header(plan.headers(), "Content-Range").isEmpty()
                || !header(plan.headers(), "Content-Encoding").isEmpty()) return plan;
        Map<String, List<String>> headers = new LinkedHashMap<>(plan.headers());
        String vary = header(headers, "Vary");
        boolean varies = List.of(vary.split(",")).stream().map(String::trim)
                .anyMatch(value -> value.equals("*") || value.equalsIgnoreCase("Accept-Encoding"));
        if (!varies) {
            headers.keySet().removeIf(name -> name.equalsIgnoreCase("Vary"));
            headers.put("Vary", List.of(vary.isBlank() ? "Accept-Encoding" : vary + ", Accept-Encoding"));
        }
        if (!acceptsGzip(header(request.headers(), "Accept-Encoding")))
            return new ResponsePlan(plan.statusCode(), headers, plan.body(), false);
        var output = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(output)) { gzip.write(plan.body()); }
        headers.keySet().removeIf(name -> name.equalsIgnoreCase("Content-Length") || name.equalsIgnoreCase("Transfer-Encoding"));
        headers.put("Content-Encoding", List.of("gzip"));
        headers.put("Content-Length", List.of(Integer.toString(output.size())));
        return new ResponsePlan(plan.statusCode(), headers, output.toByteArray(), false);
    }
}
