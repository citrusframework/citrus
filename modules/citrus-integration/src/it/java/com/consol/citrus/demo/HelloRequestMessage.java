package com.consol.citrus.demo;

public class HelloRequestMessage {
    private String messageId;
    private String correlationId;
    private String text;
    private String user;
    
    String xmlns = "http://www.consol.de/schemas/samples/sayHello.xsd";
    
    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }
    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }
    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    /**
     * @return the text
     */
    public String getText() {
        return text;
    }
    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }
    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }
    /**
     * @return the xmlns
     */
    public String getXmlns() {
        return xmlns;
    }
    /**
     * @param xmlns the xmlns to set
     */
    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }
    
}
