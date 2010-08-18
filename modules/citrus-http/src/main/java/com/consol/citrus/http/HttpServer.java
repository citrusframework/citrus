/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.http;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.util.HttpConstants;
import com.consol.citrus.http.util.HttpUtils;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.server.AbstractServer;
import com.consol.citrus.server.ServerShutdownThread;

/**
 * Simple Http server accepting client connections on a server URI and port. The
 * received messages are published to a configurable JMS queue, so the messages
 * can be consumed by any validating resource.
 * 
 * Server waits for reply message to arrive on a JMS reply destination. Response is sent
 * back as Http response to the calling client.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class HttpServer extends AbstractServer {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    /** Command to shutdown the server */
    private static final String SHUTDOWN_COMMAND = "quit";

    /** Server socket accepting client conections */
    private ServerSocket serverSocket;

    /** Host */
    private String host = "localhost";

    /** Port to listen on */
    private int port = 8080;

    /** URI to listen on */
    private String uri;

    /** Message handler for incoming requests, providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseProducingMessageHandler();
    
    /** Should server start in deamon mode */
    private boolean deamon = false;
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        log.info("[HttpServer] Listening for client connections on "
                + serverSocket.getInetAddress().getHostName() + ":" + port + uri);

        Socket clientSocket = null;

        while (isRunning() && !serverSocket.isClosed()) {
            BufferedReader in = null;
            Writer out = null;
            
            try {
                clientSocket = serverSocket.accept();

                log.info("[HttpServer] Accepted client connection on " + serverSocket.getInetAddress().getHostName() + ":" + port + uri);

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new OutputStreamWriter(clientSocket.getOutputStream());

                log.info("[HttpServer] parsing request ...");

                final Message<?> request;
                Map<String, Object> requestHeaders = new HashMap<String, Object>();
                
                String readLine = in.readLine();
                if (readLine == null || readLine.length() == 0) {
                    throw new RuntimeException("HTTP request header not set properly. Usage: <METHOD> <URI> <HTTP VERSION>");
                }

                StringTokenizer st = new StringTokenizer(readLine);
                if (!st.hasMoreTokens()) {
                    throw new RuntimeException("HTTP request header not set properly. Usage: <METHOD> <URI> <HTTP VERSION>");
                } else {
                    requestHeaders.put("HTTPMethod", st.nextToken().toUpperCase());
                }

                if (!st.hasMoreTokens()) {
                    throw new RuntimeException("HTTP request header not set properly. Usage: <METHOD> <URI> <HTTP VERSION>");
                } else {
                    requestHeaders.put("HTTPUri", st.nextToken());
                }

                if (!st.hasMoreTokens()) {
                    throw new RuntimeException("HTTP request header not set properly. Usage: <METHOD> <URI> <HTTP VERSION>");
                } else {
                    requestHeaders.put("HTTPVersion", st.nextToken());
                }

                String line = "";

                do {
                    line = in.readLine();
                    int p = line.indexOf(':');
                    if (p > 0) {
                        requestHeaders.put(line.substring(0, p).trim().toLowerCase(),	line.substring(p + 1).trim());
                    }
                } while (line.trim().length() > 0);

                if (requestHeaders.get("HTTPMethod").equals(HttpConstants.HTTP_POST)) {
                    long size = 0x7FFFFFFFFFFFFFFFl;
                    String contentLength = (String) requestHeaders.get("content-length");
                    if (contentLength != null) {
                        try {
                            size = Integer.parseInt(contentLength);
                        } catch (NumberFormatException ex) {
                        }
                    }
                    StringBuilder payloadBuilder = new StringBuilder();
                    char buf[] = new char[512];
                    int read = in.read(buf);
                    while (read >= 0 && size > 0 && !payloadBuilder.toString().endsWith(HttpConstants.LINE_BREAK)) {
                        size -= read;
                        payloadBuilder.append(String.valueOf(buf, 0, read));
                        if (size > 0) {
                            read = in.read(buf);
                        }
                    }
                    
                    request = MessageBuilder.withPayload(payloadBuilder.toString().trim()).copyHeaders(requestHeaders).build();
                } else {
                    //TODO implement GET method
                    request = MessageBuilder.withPayload("").copyHeaders(requestHeaders).build();
                }

                log.info("[HttpServer] received request " + HttpUtils.generateRequest(request));

                if (request.getPayload() != null && request.getPayload().equals("quit")) {
                    log.info("[HttpServer] received shuttdown call");
                    stop();
                    return;
                }

                Message<?> response = messageHandler.handleMessage(request);
                
                if(response != null) {
                    String responseStr = HttpUtils.generateResponse(response);
    
                    log.info("[HttpServer] sending response " + responseStr);
                    
                    out.write(responseStr);
                    out.flush();
                } else {
                    log.error("Did not receive any reply from message handler '" + messageHandler + "'");
                }
            } catch(SocketException e) {
                log.info("[HttpServer] ServerSocket was closed!");
            } catch (Exception e) {
                log.error("[HttpServer] ", e);
            } finally {
                try {
                    if (clientSocket != null) {
                        clientSocket.close();
                        clientSocket = null;
                    }
                    
                    if(in != null) {
                        in.close();
                        in = null;
                    }
                    
                    if(out != null) {
                        out.close();
                        out = null;
                    }
                } catch (IOException e) {
                    log.error("[HttpServer] ", e);
                }
            }
        }
    }

    /**
     * @see com.consol.citrus.server.AbstractServer#startup()
     */
    @Override
    protected void startup() {
        log.info("[HttpServer] Starting ...");
        try {
            InetAddress addr = InetAddress.getByName(host);
            this.serverSocket = new ServerSocket(port, 0, addr);

            if(!deamon) {
                new ServerShutdownThread(this);
            }
        } catch (IOException e) {
            log.error("[HttpServer] failed to listen on port " + port, e);
            log.info("[HttpServer] Startup failed");
            throw new CitrusRuntimeException(e);
        } catch (Exception e) {
            log.info("[HttpServer] Startup failed");
            throw new CitrusRuntimeException(e);
        }
        log.info("[HttpServer] Started sucessfully");
    }

    /**
     * @see com.consol.citrus.server.AbstractServer#shutdown()
     */
    @Override
    protected void shutdown() {
        synchronized (this) {
            log.info("[HttpServer] Stopping Http server '" + getName() + "'");
            try {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                log.error("Error while closing server socket", e);
            }
            
            log.info("[HttpServer] Http server '" + getName() + "' was stopped sucessfully");
        }
    }

    /**
     * Main method starting new server instance
     *
     * @param args
     */
    public static void main(String[] args) {
        String command = "";

        if (args.length > 0) {
            command = args[0];
        }

        ApplicationContext ctx = new ClassPathXmlApplicationContext("http-stub-context.xml");

        HttpServer server;
        if (ctx.containsBean("httpServer")) {
            server = (HttpServer)ctx.getBean("httpServer");
        } else {
            server = new HttpServer();
            server.setBeanName("httpServer");
            server.setHost("localhost");
            server.setPort(8080);
            server.setUri("request");
            
            server.setMessageHandler(new EmptyResponseProducingMessageHandler());
            server.setDeamon(true);
        }

        if (command.equals(HttpServer.SHUTDOWN_COMMAND)) {
            try {
                server.quit();
            } catch (CitrusRuntimeException e) {
                log.error("Error during shutdown", e);
            }
        } else {
            try {
                server.start();
            } catch (CitrusRuntimeException e) {
                log.error("Error during startup", e);
            }
            
            if(!server.isDeamon()) {
                server.join();
            }
        }
    }

    /**
     * Set server in deamon mode.
     * @param deamon
     */
    public void setDeamon(boolean deamon) {
        this.deamon = deamon;
    }

    /**
     * Is server in deamon mode.
     * @return
     */
    public boolean isDeamon() {
        return deamon;
    }

    /**
     * Stop the server instance.
     * @throws CitrusRuntimeException
     */
    public void quit() {
        Writer writer = null;

        try {
            InetAddress addr = InetAddress.getByName(host);

            Socket socket = new Socket(addr, port);

            Message<?> httpRequest;
            
            httpRequest = MessageBuilder.withPayload("quit")
                            .setHeader("HTTPVersion", HttpConstants.HTTP_VERSION)
                            .setHeader("HTTPMethod", HttpConstants.HTTP_POST)
                            .setHeader("HTTPUri", host + ":" + port + uri)
                            .setHeader("HTTPHost", host)
                            .setHeader("HTTPPort", Integer.valueOf(port).toString())
                            .build();

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
            writer.write(HttpUtils.generateRequest(httpRequest));
            writer.flush();
        } catch (UnknownHostException e) {
            throw new CitrusRuntimeException(e);
        } catch (ConnectException e) {
            log.warn("Could not connect to HttpStub - maybe server is already stopped");
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Error while closing output stream", e);
                }
            }
        }
    }
    
    /**
     * Set the Http port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Set the Http URI.
     * @param uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Set the message handler.
     * @param messageHandler
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Set the host.
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }
    
}
