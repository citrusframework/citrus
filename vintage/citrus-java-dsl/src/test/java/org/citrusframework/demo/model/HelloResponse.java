//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2020.03.04 um 04:11:49 PM CET
//


package org.citrusframework.demo.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MessageId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="CorrelationId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="User" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Text" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Fault" type="{http://www.consol.de/schemas/samples/sayHello.xsd}FaultType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "messageId",
    "correlationId",
    "user",
    "text",
    "fault"
})
@XmlRootElement(name = "HelloResponse")
public class HelloResponse {

    @XmlElement(name = "MessageId", required = true)
    protected String messageId;
    @XmlElement(name = "CorrelationId", required = true)
    protected String correlationId;
    @XmlElement(name = "User", required = true)
    protected String user;
    @XmlElement(name = "Text", required = true)
    protected String text;
    @XmlElement(name = "Fault")
    protected FaultType fault;

    /**
     * Ruft den Wert der messageId-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Legt den Wert der messageId-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Ruft den Wert der correlationId-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Legt den Wert der correlationId-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCorrelationId(String value) {
        this.correlationId = value;
    }

    /**
     * Ruft den Wert der user-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUser() {
        return user;
    }

    /**
     * Legt den Wert der user-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Ruft den Wert der text-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getText() {
        return text;
    }

    /**
     * Legt den Wert der text-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Ruft den Wert der fault-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link FaultType }
     *
     */
    public FaultType getFault() {
        return fault;
    }

    /**
     * Legt den Wert der fault-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link FaultType }
     *
     */
    public void setFault(FaultType value) {
        this.fault = value;
    }

}
