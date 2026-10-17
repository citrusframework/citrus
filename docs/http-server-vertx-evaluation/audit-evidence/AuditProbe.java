package org.citrusframework.http.vertx;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.*;
import org.citrusframework.http.message.*;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.message.Message;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.Cookie;

public class AuditProbe {
  static AbstractEndpointAdapter adapter(Function<Message, Message> function) {
    return new AbstractEndpointAdapter() {
      protected Message handleMessageInternal(Message message) { return function.apply(message); }
      public Endpoint getEndpoint() { return null; }
      public EndpointConfiguration getEndpointConfiguration() { return null; }
    };
  }
  static HttpMessage ok() { return new HttpMessage("pong").status(HttpStatus.OK); }
  static VertxHttpServer server(String name, Function<Message, Message> f) {
    var s = new VertxHttpServer(); s.setName(name); s.setPort(0); s.setEndpointAdapter(adapter(f));
    s.initialize(); return s;
  }
  static HttpResponse<String> get(int port) throws Exception {
    {
      var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
      return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/fp"))
          .timeout(Duration.ofSeconds(2)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
  }
  static void protocols() throws Exception {
    int port;
    try (var socket = new ServerSocket(0)) { port = socket.getLocalPort(); }
    var jetty = new HttpServer(); jetty.setName("auditJetty"); jetty.setPort(port);
    jetty.setEndpointAdapter(adapter(m -> ok())); jetty.initialize(); jetty.start();
    try { System.out.println("AUDIT protocol jetty=" + get(port).version()); }
    finally { jetty.stop(); jetty.destroy(); }
    var vertx = server("auditProtocol", m -> ok()); vertx.start();
    try { System.out.println("AUDIT protocol vertx=" + get(vertx.getActualPort()).version()); }
    finally { vertx.stop(); vertx.destroy(); }
  }
  static void stoppedWorkers() throws Exception {
    var s = server("auditStoppedWorkers", m -> ok()); s.start();
    try {
      get(s.getActualPort()); s.stop();
      var workers = Thread.getAllStackTraces().keySet().stream()
          .filter(t -> t.getName().startsWith("auditStoppedWorkers-exchange") && t.isAlive())
          .map(t -> t.getName() + ":" + t.getState()).toList();
      System.out.println("AUDIT workers after stop=" + workers);
    } finally { s.destroy(); }
  }
  static Object field(Object o, String name) throws Exception {
    var f = o.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(o);
  }
  static void repeatedStart() throws Exception {
    var s = server("auditRepeatedStart", m -> ok()); s.start();
    int firstPort = s.getActualPort();
    var firstVertx = (io.vertx.core.Vertx) field(s, "vertx");
    var firstServer = (io.vertx.core.http.HttpServer) field(s, "vertxServer");
    try {
      s.start(); int secondPort = s.getActualPort(); s.stop(); s.destroy();
      try (var socket = new Socket()) {
        socket.connect(new InetSocketAddress("127.0.0.1", firstPort), 1000);
        System.out.println("AUDIT repeated start firstPort=" + firstPort + " secondPort=" + secondPort
            + " first listener accepts connection after stop+destroy=true");
      }
    } finally {
      firstServer.close().toCompletionStage().toCompletableFuture().get();
      firstVertx.close().toCompletionStage().toCompletableFuture().get();
      s.stop(); s.destroy();
    }
  }
  static void streamingLimit() throws Exception {
    var s = server("auditBodyLimit", m -> ok()); s.setMaxBodySizeBytes(4); s.start();
    try (var socket = new Socket("127.0.0.1", s.getActualPort())) {
      socket.setSoTimeout(700);
      var out = socket.getOutputStream(); var in = socket.getInputStream();
      out.write(("POST /fp HTTP/1.1\r\nHost: localhost\r\nTransfer-Encoding: chunked\r\n\r\n"
          + "8\r\n12345678\r\n").getBytes(StandardCharsets.US_ASCII)); out.flush();
      try { System.out.println("AUDIT bodyLimit response before end=" + in.read()); }
      catch (SocketTimeoutException expected) { System.out.println("AUDIT bodyLimit no rejection after 8 bytes with limit 4 while body incomplete=true"); }
      out.write("0\r\n\r\n".getBytes(StandardCharsets.US_ASCII)); out.flush();
      var bytes = new byte[4096]; int n = in.read(bytes);
      System.out.println("AUDIT bodyLimit response after end=" + new String(bytes, 0, n, StandardCharsets.US_ASCII).split("\r\n")[0]);
    } finally { s.stop(); s.destroy(); }
  }
  static void outboundError() throws Exception {
    var s = server("auditOutbound", m -> { var r = ok(); r.setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, "bad"); return r; }); s.start();
    try {
      try { System.out.println("AUDIT outbound invalid status response=" + get(s.getActualPort()).statusCode()); }
      catch (HttpTimeoutException expected) { System.out.println("AUDIT outbound invalid status causes client timeout=true"); }
    } finally { s.stop(); s.destroy(); }
  }
  static void configAndCookie() throws Exception {
    var s = server("auditConfig", m -> ok());
    s.getHttpEndpointConfiguration().setCharset("ISO-8859-1"); s.start();
    try { System.out.println("AUDIT configured charset=ISO-8859-1 active charset=" + s.getHttpEndpointConfiguration().getCharset()); }
    finally { s.stop(); s.destroy(); }
    var c = new HttpEndpointConfiguration(); c.setHandleCookies(true);
    var cookie = new Cookie("token", "value"); cookie.setMaxAge(0); cookie.setSecure(true); cookie.setHttpOnly(true); cookie.setAttribute("SameSite", "Strict");
    var reply = ok(); reply.cookie(cookie);
    System.out.println("AUDIT cookie=" + VertxHttpMessageMapper.toResponsePlan(reply, c, false).headers().get("Set-Cookie"));
  }
  static void framing() throws Exception {
    var s = server("auditFraming", m -> { var r = ok(); r.setHeader("Transfer-Encoding", "chunked"); return r; }); s.start();
    try (var socket = new Socket("127.0.0.1", s.getActualPort())) {
      socket.setSoTimeout(1500);
      socket.getOutputStream().write("GET /fp HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
      byte[] b = new byte[4096]; int n = socket.getInputStream().read(b);
      System.out.println("AUDIT framing=" + new String(b, 0, n, StandardCharsets.US_ASCII).replace("\r\n", " | "));
    } finally { s.stop(); s.destroy(); }
  }
  public static void main(String[] args) throws Exception {
    System.out.println("AUDIT java=" + System.getProperty("java.version"));
    if (args.length > 0) { generationIsolation(); optionSnapshot(); return; }
    protocols(); stoppedWorkers(); repeatedStart(); streamingLimit(); outboundError(); configAndCookie(); framing();
  }
  static void generationIsolation() throws Exception {
    var s = new VertxHttpServer(); s.setName("auditGenerations"); s.setPort(0);
    s.setExecutorThreads(1); s.setMaxConcurrentExchanges(2); s.setReplyTimeoutMs(5000); s.initialize(); s.start();
    var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    try {
      String uri = "http://127.0.0.1:" + s.getActualPort() + "/fp";
      client.sendAsync(HttpRequest.newBuilder(URI.create(uri)).timeout(Duration.ofSeconds(10))
          .POST(HttpRequest.BodyPublishers.ofString("first-old")).build(), HttpResponse.BodyHandlers.ofString());
      var oldAdapter = (org.citrusframework.endpoint.direct.DirectEndpointAdapter) s.getEndpointAdapter();
      var first = oldAdapter.getEndpointConfiguration().getQueue().receive(2000);
      if (first == null) throw new AssertionError("first request was not dispatched");
      client.sendAsync(HttpRequest.newBuilder(URI.create(uri)).timeout(Duration.ofSeconds(10))
          .POST(HttpRequest.BodyPublishers.ofString("second-old")).build(), HttpResponse.BodyHandlers.ofString());
      var executor = (VertxExchangeExecutor) field(s, "exchangeExecutor");
      long deadline = System.nanoTime() + 2_000_000_000L;
      while (executor.availablePermits() != 0 && System.nanoTime() < deadline) Thread.sleep(10);
      if (executor.availablePermits() != 0) throw new AssertionError("second request was not queued");
      s.stop(); s.start();
      ((org.citrusframework.message.MessageQueue) first.getHeader(org.citrusframework.endpoint.direct.DirectMessageHeaders.REPLY_QUEUE)).send(ok());
      var freshAdapter = (org.citrusframework.endpoint.direct.DirectEndpointAdapter) s.getEndpointAdapter();
      var crossed = freshAdapter.getEndpointConfiguration().getQueue().receive(2000);
      System.out.println("AUDIT generation current=" + s.getGeneration() + " fresh queue received="
          + (crossed == null ? "nothing" : crossed.getPayload() + " from generation=" + crossed.getHeader(VertxHttpMessageMapper.GENERATION_HEADER)));
      if (crossed != null) ((org.citrusframework.message.MessageQueue) crossed.getHeader(org.citrusframework.endpoint.direct.DirectMessageHeaders.REPLY_QUEUE)).send(ok());
    } finally { s.stop(); s.destroy(); }
  }
  static void optionSnapshot() throws Exception {
    var s = server("auditOptions", m -> ok()); s.setMaxConcurrentExchanges(1); s.start();
    try {
      System.out.println("AUDIT limit active options=" + s.getActiveOptions().getMaxConcurrentExchanges()
          + " executor permits=" + ((VertxExchangeExecutor) field(s, "exchangeExecutor")).availablePermits());
      s.setAutoStart(true); s.destroy(); s.initialize();
      System.out.println("AUDIT after direct destroy+autoStart initialize running=" + s.isRunning() + " actualPort=" + s.getActualPort());
    } finally { s.stop(); s.destroy(); }
  }
}
