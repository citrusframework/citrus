package com.consol.citrus.admin.spring.model;

import javax.xml.bind.annotation.*;

/**
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "propertyType", propOrder = {
    "description",
    "any",
    "list",
    "map",
    "propertyTypeValue",
    "propertyTypeRef",
    "bean"
})
@XmlRootElement(name = "property")
public class Property {

    protected Description description;
    @XmlAnyElement(lax = true)
    protected Object any;
    @XmlElement(name = "list")
    protected List list;
    @XmlElement(name = "map")
    protected Map map;
    @XmlElement(name = "value")
    protected Value propertyTypeValue;
    @XmlElement(name = "ref")
    protected Ref propertyTypeRef;
    protected SpringBean bean;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "ref")
    protected String ref;
    @XmlAttribute(name = "value")
    protected String value;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

    /**
     * Gets the value of the propertyTypeValue property.
     * 
     * @return
     *     possible object is
     *     {@link Value }
     *     
     */
    public Value getPropertyTypeValue() {
        return propertyTypeValue;
    }

    /**
     * Sets the value of the propertyTypeValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Value }
     *     
     */
    public void setPropertyTypeValue(Value value) {
        this.propertyTypeValue = value;
    }

    /**
     * Gets the value of the propertyTypeRef property.
     * 
     * @return
     *     possible object is
     *     {@link Ref }
     *     
     */
    public Ref getPropertyTypeRef() {
        return propertyTypeRef;
    }

    /**
     * Sets the value of the propertyTypeRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ref }
     *     
     */
    public void setPropertyTypeRef(Ref value) {
        this.propertyTypeRef = value;
    }

    /**
     * Gets the value of the bean property.
     * 
     * @return
     *     possible object is
     *     {@link SpringBean }
     *     
     */
    public SpringBean getBean() {
        return bean;
    }

    /**
     * Sets the value of the bean property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpringBean }
     *     
     */
    public void setBean(SpringBean value) {
        this.bean = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the list property.
     *
     * @return
     *     possible object is
     *     {@link List }
     *
     */
    public List getList() {
        return list;
    }

    /**
     * Sets the value of the list property.
     *
     * @param value
     *     allowed object is
     *     {@link List }
     *
     */
    public void setList(List value) {
        this.list = value;
    }

    /**
     * Gets the value of the map property.
     *
     * @return
     *     possible object is
     *     {@link Map }
     *
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sets the value of the map property.
     *
     * @param value
     *     allowed object is
     *     {@link Map }
     *
     */
    public void setMap(Map value) {
        this.map = value;
    }

}
