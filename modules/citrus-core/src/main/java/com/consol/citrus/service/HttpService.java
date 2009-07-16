package com.consol.citrus.service;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.http.HttpConstants;
import com.consol.citrus.http.HttpUtils;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.XMLMessage;

/**
 * Class enables to send or receive messages via http
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class HttpService implements Service {
    /** Http url as service detination */
    private String urlPath;

    /** Http host */
    private String host;

    /** Port */
    private int port;

    /** Request method */
    private String requestMethod = HttpConstants.HTTP_POST;

    /** Http socket */
    private Socket socket;

    private String response;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpService.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#sendMessage(java.lang.String)
     */
    public void sendMessage(Message message) throws TestSuiteException {
        Writer writer = null;
        BufferedReader reader = null;

        try {
            log.info("Sending message to: " + getServiceDestination());

            if (log.isDebugEnabled()) {
                log.debug("Message to be sent:");
                log.debug(message.getMessagePayload());
            }

            Message request = new XMLMessage();

            request.addHeaderElement("HTTPVersion", HttpConstants.HTTP_VERSION);
            request.addHeaderElement("HTTPMethod", requestMethod);
            request.addHeaderElement("HTTPUri", urlPath);
            request.addHeaderElement("HTTPHost", host);
            request.addHeaderElement("HTTPPort", Integer.valueOf(port).toString());

            /* before sending set header values */
            Iterator it = message.getHeader().keySet().iterator();
            while (it.hasNext())
            {
                Object o = it.next();
                final String key = (String) o;
                final String value = (String) message.getHeader().get(key);

                if (log.isDebugEnabled()) {
                    log.debug("Setting message property: " + key + " to: " + value);
                }

                request.getHeader().put(key, value);
            }

            if (requestMethod.equals(HttpConstants.HTTP_POST)) {
                request.setMessagePayload(message.getMessagePayload());
            } else if (requestMethod.equals(HttpConstants.HTTP_GET)) {
                //TODO: implement GET method
            }

            InetAddress addr = InetAddress.getByName(host);
            socket = new Socket(addr, port);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
            writer.write(HttpUtils.generateRequest(request));
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(HttpConstants.LINE_BREAK);
            }

            response = buffer.toString();

        } catch (IOException e) {
            throw new TestSuiteException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Error while closing OutputStream", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing InputStream", e);
                }
            }
        }
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#receiveMessage()
     */
    public Message receiveMessage() throws TestSuiteException {
        log.info("Receiving message from: " + getServiceDestination());

        if (log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(response);
        }

        Message httpResponse = new XMLMessage();
        try {
            BufferedReader reader = new BufferedReader(new StringReader(response));

            String readLine = null;
            readLine = reader.readLine();

            if (readLine == null || readLine.length() == 0) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            }

            StringTokenizer st = new StringTokenizer(readLine);
            if (!st.hasMoreTokens()) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            } else {
                httpResponse.addHeaderElement("HTTPVersion", st.nextToken().toUpperCase());
            }

            if (!st.hasMoreTokens()) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            } else {
                httpResponse.addHeaderElement("HTTPStatusCode", st.nextToken());
            }

            if (!st.hasMoreTokens()) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            } else {
                httpResponse.addHeaderElement("HTTPReasonPhrase", st.nextToken());
            }

            // Read the request headers
            String line;
            do {
                line = reader.readLine();

                if(line != null) {
                    int p = line.indexOf(':');
                    if (p > 0) {
                        httpResponse.getHeader().put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                    }
                }
            } while (line != null && line.trim().length() > 0);

            httpResponse.getHeader().put("HTTPVersion", httpResponse.getHeader().get("HTTPVersion"));
            httpResponse.getHeader().put("HTTPStatusCode", httpResponse.getHeader().get("HTTPStatusCode"));
            httpResponse.getHeader().put("HTTPReasonPhrase", httpResponse.getHeader().get("HTTPReasonPhrase"));

            StringBuffer contentBuffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                contentBuffer.append(line).append(HttpConstants.LINE_BREAK);
            }

            httpResponse.setMessagePayload(contentBuffer.toString());
        } catch (IOException e) {
            throw new TestSuiteException(e);
        }

        return httpResponse;
    }

    /**
     * Getter for service destination
     */
    public String getServiceDestination() {
        return "http://" + host + ":" + port + urlPath;
    }

    /**
     * Setter for http host
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Setter for port
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Setter for urlPath
     * @param urlPath
     */
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    /**
     * Setter for request method
     * @param requestMethod
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.service.Service#changeServiceDestination(java.lang.String)
     */
    public void changeServiceDestination(String destination) throws TestSuiteException {
        try  {
            setUrlPath(destination);
        } catch (Exception e) {
            throw new TestSuiteException(e);
        }
    }
}
