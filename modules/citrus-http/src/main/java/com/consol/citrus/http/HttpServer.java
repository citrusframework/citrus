package com.consol.citrus.http;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.Server;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.http.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.http.util.HttpConstants;
import com.consol.citrus.http.util.HttpUtils;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.ShutdownThread;

/**
 * Simple http server accepting client connections on a server uri and port. The
 * received messages are published to a configurable JMS queue, so the messages
 * can be consumed by any validating resource.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class HttpServer implements Server, InitializingBean {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    /**
     * mode to shutdown the server
     */
    private static final String SHUTDOWN_COMMAND = "quit";

    /** Name of this server (will be injected through Spring) */
    private String name;

    /** Running flag */
    private boolean running = false;

    /** Thread running the server in non deamon mode */
    private Thread thread;

    /** server socket accepting client conections */
    private ServerSocket serverSocket;

    /** Host */
    private String host = "localhost";

    /** Port to listen on */
    private int port = 8080;

    /** URI to listen on */
    private String uri;

    /** MessageHandler handling incoming requests and providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseProducingMessageHandler();
    
    /** Should server start in deamon mode */
    private boolean deamon = false;
    
    /** Autostart server after properties are set */
    private boolean autoStart = false;
    
    public void run() {
        log.info("[HttpServer] Listening for client connections on "
                + serverSocket.getInetAddress().getHostName() + ":" + port + uri);

        Socket clientSocket = null;

        synchronized (this) {
            running = true;
        }

        while (running && !serverSocket.isClosed()) {
            BufferedReader in = null;
            Writer out = null;
            
            try {
                clientSocket = serverSocket.accept();

                log.info("[HttpServer] Accepted client connection on " + serverSocket.getInetAddress().getHostName() + ":" + port + uri);

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new OutputStreamWriter(clientSocket.getOutputStream());

                log.info("[HttpServer] parsing request ...");

                final Message request;
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
                    String postLine = "";
                    char buf[] = new char[512];
                    int read = in.read(buf);
                    while (read >= 0 && size > 0 && !postLine.endsWith(HttpConstants.LINE_BREAK)) {
                        size -= read;
                        postLine += String.valueOf(buf, 0, read);
                        if (size > 0) {
                            read = in.read(buf);
                        }
                    }
                    postLine = postLine.trim();
                    
                    request = MessageBuilder.withPayload(postLine).copyHeaders(requestHeaders).build();
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

                Message response = messageHandler.handleMessage(request);
                
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

    public void start() throws TestSuiteException {
        log.info("[HttpServer] Starting ...");
        try {
            InetAddress addr = InetAddress.getByName(host);
            this.serverSocket = new ServerSocket(port, 0, addr);

            thread = new Thread(this);
            thread.setDaemon(false);
            thread.start();

            if(deamon == false) {
                new ShutdownThread(this);
            }
        } catch (IOException e) {
            log.error("[HttpServer] failed to listen on port " + port, e);
            log.info("[HttpServer] Startup failed");
            throw new TestSuiteException(e);
        } catch (Exception e) {
            log.info("[HttpServer] Startup failed");
            throw new TestSuiteException(e);
        }
        log.info("[HttpServer] Started sucessfully");
    }

    public void stop() throws TestSuiteException {
        //TODO: ensure shutdown
        synchronized (this) {
            log.info("[HttpServer] Stopping Http server '" + getName() + "'");
            try {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                log.error("Error while closing server socket", e);
            } finally {
                running = false;
                thread = null;
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
            server.setHost("localhost");
            server.setPort(8080);
            server.setUri("request");
            
            server.setMessageHandler(new EmptyResponseProducingMessageHandler());
            server.setDeamon(true);
        }

        if (command.equals(HttpServer.SHUTDOWN_COMMAND)) {
            try {
                server.quit();
            } catch (TestSuiteException e) {
                log.error("Error during shutdown", e);
            }
        } else {
            try {
                server.start();
            } catch (TestSuiteException e) {
                log.error("Error during startup", e);
            }
            
            if(server.isDeamon() == false) {
                server.join();
            }
        }
    }

    public void setDeamon(boolean deamon) {
        this.deamon = deamon;
    }

    public boolean isDeamon() {
        return deamon;
    }

    public void quit() throws TestSuiteException {
        Writer writer = null;

        try {
            InetAddress addr = InetAddress.getByName(host);

            Socket socket = new Socket(addr, port);

            Message httpRequest;
            
            httpRequest = MessageBuilder.withPayload("quit")
                            .setHeader("HTTPVersion", HttpConstants.HTTP_VERSION)
                            .setHeader("HTTPMethod", HttpConstants.HTTP_POST)
                            .setHeader("HTTPUri", host + ":" + port + uri)
                            .setHeader("HTTPHost", host)
                            .setHeader("HTTPPort", new Integer(port).toString())
                            .build();

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
            writer.write(HttpUtils.generateRequest(httpRequest));
            writer.flush();
        } catch (UnknownHostException e) {
            throw new TestSuiteException(e);
        } catch (ConnectException e) {
            log.warn("Could not connect to HttpStub - maybe server is already stopped");
        } catch (IOException e) {
            throw new TestSuiteException(e);
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
    
    public void afterPropertiesSet() throws Exception {
        if(autoStart) {
            start();
        }
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        synchronized (this) {
            return running;
        }
    }
    
    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Error occured", e);
        }
    }
    
    public void setBeanName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

}
