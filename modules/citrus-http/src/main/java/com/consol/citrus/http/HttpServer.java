package com.consol.citrus.http;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.DOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.consol.citrus.Server;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.ShutdownThread;
import com.consol.citrus.util.XMLUtils;

/**
 * Simple http server accepting client connections on a server uri and port. The
 * received messages are published to a configurable JMS queue, so the messages
 * can be consumed by any validating resource.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class HttpServer implements Server {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    /**
     * mode using the test suite the dummy will forward the received messages to
     * the test suite over jms and generate the acknowledgement itself
     */
    public static final int MODE_USE_TESTSUITE = 0;

    /**
     * mode standalone simple the dummy will swallow all messages and generate a
     * acknowledgement
     */
    public static final int MODE_STANDALONE = 1;

    /**
     * mode to shutdown the server
     */
    private static final String SHUTDOWN_COMMAND = "quit";

    /** Mode to declare the behaviour of the dummy */
    private int mode = MODE_USE_TESTSUITE;

    /** Name of this server (will be injected through Spring) */
    String name;

    /** Running flag */
    boolean running = false;

    /** Thread running the server in non deamon mode */
    Thread thread;

    /** server socket accepting client conections */
    private ServerSocket serverSocket;

    /** Host */
    private String host;

    /** Port to listen on */
    private int port;

    /** URI to listen on */
    private String uri;

    /** JMS destinations */
    private String sendDestination;

    private String replyDestination;

    private String messageTypeElement;

    private String messageHandlerDefinition;

    private MessageHandler defaultMessageHandler;
    
    private boolean deamon = false;
    
    private JmsTemplate jmsTemplate;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        log.info("[HttpServer] Listening for client connections on "
                + serverSocket.getInetAddress().getHostName() + ":" + port + uri);

        Socket clientSocket = null;

        synchronized (this) {
            running = true;
        }

        while (running && !serverSocket.isClosed()) {
            try {
                clientSocket = serverSocket.accept();

                log.info("[HttpServer] Accepted client connection on " + serverSocket.getInetAddress().getHostName() + ":" + port + uri);

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Writer out = new OutputStreamWriter(clientSocket.getOutputStream());

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
                    shutdown();
                    return;
                }

                Message response;
                Map<String, Object> responseHeaders = new HashMap<String, Object>();

                if (mode == MODE_USE_TESTSUITE) {
                    log.info("[HttpServer] Now sending to jms queue " + sendDestination);
                    if(log.isDebugEnabled()) {
                        log.debug("[HttpServer] Message is: " + request.getPayload());
                    }

                    log.info("[HttpServer] forwarding message to test suite");

                    jmsTemplate.send(sendDestination, new MessageCreator() {
                        public javax.jms.Message createMessage(Session session) throws JMSException {
                            TextMessage sendMessage = session.createTextMessage(request.getPayload().toString());

                            for (Iterator iter = request.getHeaders().keySet().iterator(); iter.hasNext();) {
                                String key = (String) iter.next();
                                sendMessage.setStringProperty(key, request.getHeaders().get(key).toString());
                            }

                            Destination replyQueue = session.createQueue(replyDestination);
                            sendMessage.setJMSReplyTo(replyQueue);
                            
                            return sendMessage;
                        }
                    });
                    
                    log.info("[HttpServer] waiting for reply of test suite ...");

                    TextMessage replyMessage = (TextMessage) jmsTemplate.receive(replyDestination);

                    if (replyMessage != null) {
                        log.info("[HttpServer] received message from test suite");
                        
                        Enumeration headerProperties = replyMessage.getPropertyNames();
                        while (headerProperties.hasMoreElements()) {
                            String property = (String)headerProperties.nextElement();
                            log.info("[HttpServer] handling header property: " + property);

                            responseHeaders.put(property, replyMessage.getStringProperty(property));
                        }
                        
                        response = MessageBuilder.withPayload(replyMessage.getText()).copyHeaders(responseHeaders).build();
                    } else if (defaultMessageHandler != null) {
                        response = defaultMessageHandler.handleMessage(request);
                    } else {
                        //TODO verify if this is a problem
                        response = MessageBuilder.withPayload("").build();
                    }

                    String responseStr = HttpUtils.generateResponse(response);

                    log.info("[HttpServer] sending response " + HttpUtils.generateResponse(response));

                    out.write(responseStr);
                    out.flush();
                } else if (mode == MODE_STANDALONE) {
                    try {
                        if (request.getPayload() != null) {
                            final Reader reader = new StringReader(request.getPayload().toString());
                            DOMParser parser = new DOMParser();
                            parser.setFeature("http://xml.org/sax/features/validation", false);

                            parser.parse(new InputSource(reader));


                            Node matchingElement;
                            if (messageTypeElement != null) {
                                matchingElement = XMLUtils.findNodeByXPath(DOMUtil.getFirstChildElement(parser.getDocument()), messageTypeElement);
                            } else {
                                matchingElement = DOMUtil.getFirstChildElement(parser.getDocument());
                            }

                            if (matchingElement == null) {
                                throw new TestSuiteException("Could not find matching element " + messageTypeElement + " in message");
                            }

                            if (messageHandlerDefinition != null) {
                                //TODO support FileSystemContext
                                ApplicationContext ctx = new ClassPathXmlApplicationContext(messageHandlerDefinition);
                                MessageHandler handler = (MessageHandler)ctx.getBean(matchingElement.getNodeName(), MessageHandler.class);

                                if (handler != null) {
                                    response = handler.handleMessage(request);
                                } else {
                                    throw new TestSuiteException("Could not find message handler for message type" + matchingElement.getNodeName());
                                }
                            } else if (defaultMessageHandler != null) {
                                response = defaultMessageHandler.handleMessage(request);
                            } else {
                                response = MessageBuilder.withPayload("").build();
                            }
                        } else {
                            if (defaultMessageHandler != null) {
                                response = defaultMessageHandler.handleMessage(request);
                            } else {
                                response = MessageBuilder.withPayload("").build();
                            }
                        }
                    } catch (SAXException e) {
                        throw new TestSuiteException(e);
                    } catch (IOException e) {
                        throw new TestSuiteException(e);
                    }

                    log.info("[HttpServer] sending response " + HttpUtils.generateResponse(response));

                    out.write(HttpUtils.generateResponse(response));
                    out.flush();
                }
            } catch(SocketException e) {
                log.info("[HttpServer] ServerSocket was closed!");
            } catch (Exception e) {
                log.error("[HttpServer] ", e);
            } finally {
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        log.error("[HttpServer] ", e);
                    }
                    clientSocket = null;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Server#startup()
     */
    public void startup() throws TestSuiteException {
        log.info("[HttpServer] Starting in mode: " + mode  + " (0=TestSuite, 1=Standalone)");
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

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Server#shutdown()
     */
    public void shutdown() throws TestSuiteException {
        //TODO: ensure shutdown
        synchronized (this) {
            log.info("[HttpServer] Shutting down");
            try {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                running = false;
                thread = null;
            }
            log.info("[HttpServer] Shutdown sucessfully");
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
            
            server.setJmsTemplate(new JmsTemplate(new ActiveMQConnectionFactory("tcp://localhost:61616")));
            
            server.setMode(MODE_STANDALONE);
            server.setDeamon(true);
        }

        if (command.equals(HttpServer.SHUTDOWN_COMMAND)) {
            try {
                server.quit();
            } catch (TestSuiteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                server.startup();
            } catch (TestSuiteException e) {
                e.printStackTrace();
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

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Server#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.consol.citrus.Server#isRunning()
     */
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
    
    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setReplyDestination(String replyDestination) {
        this.replyDestination = replyDestination;
    }

    public void setSendDestination(String sendDestination) {
        this.sendDestination = sendDestination;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setMessageTypeElement(String messageTypeElement) {
        this.messageTypeElement = messageTypeElement;
    }

    public void setMessageHandlerDefinition(String messageHandlerDefinition) {
        this.messageHandlerDefinition = messageHandlerDefinition;
    }

    public void setDefaultMessageHandler(MessageHandler defaultMessageHandler) {
        this.defaultMessageHandler = defaultMessageHandler;
    }

    public int getMode() {
        return mode;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * @return the jmsTemplate
     */
    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }
}
