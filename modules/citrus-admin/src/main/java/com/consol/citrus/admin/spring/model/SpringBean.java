package com.consol.citrus.admin.spring.model;

import java.util.*;

import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "properties"
})
@XmlRootElement(name = "bean")
public class SpringBean
    extends IdentifiedType
{

    protected Description description;
    
    @XmlElementRef(name = "property", namespace = "http://www.springframework.org/schema/beans", type = Property.class)
    protected List<Property> properties;
    
    @XmlAttribute(name = "name")
    protected String name;
    
    @XmlAttribute(name = "class")
    protected String clazz;
    
    @XmlAttribute(name = "parent")
    protected String parent;
    @XmlAttribute(name = "scope")
    protected String scope;
    @XmlAttribute(name = "abstract")
    protected Boolean _abstract;
    @XmlAttribute(name = "autowire")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String autowire;
    @XmlAttribute(name = "depends-on")
    protected String dependsOn;
    @XmlAttribute(name = "primary")
    protected Boolean primary;
    @XmlAttribute(name = "init-method")
    protected String initMethod;
    @XmlAttribute(name = "destroy-method")
    protected String destroyMethod;
    @XmlAttribute(name = "factory-method")
    protected String factoryMethod;
    @XmlAttribute(name = "factory-bean")
    protected String factoryBean;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

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
     * Gets the value of the properties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the properties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link MetaType }{@code >}
     * 
     * 
     */
    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<Property>();
        }
        return this.properties;
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
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParent(String value) {
        this.parent = value;
    }

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAbstract(Boolean value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the autowire property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutowire() {
        if (autowire == null) {
            return "default";
        } else {
            return autowire;
        }
    }

    /**
     * Sets the value of the autowire property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutowire(String value) {
        this.autowire = value;
    }

    /**
     * Gets the value of the dependsOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDependsOn() {
        return dependsOn;
    }

    /**
     * Sets the value of the dependsOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDependsOn(String value) {
        this.dependsOn = value;
    }

    /**
     * Gets the value of the primary property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPrimary() {
        return primary;
    }

    /**
     * Sets the value of the primary property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrimary(Boolean value) {
        this.primary = value;
    }

    /**
     * Gets the value of the initMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the value of the initMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitMethod(String value) {
        this.initMethod = value;
    }

    /**
     * Gets the value of the destroyMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the value of the destroyMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestroyMethod(String value) {
        this.destroyMethod = value;
    }

    /**
     * Gets the value of the factoryMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFactoryMethod() {
        return factoryMethod;
    }

    /**
     * Sets the value of the factoryMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFactoryMethod(String value) {
        this.factoryMethod = value;
    }

    /**
     * Gets the value of the factoryBean property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFactoryBean() {
        return factoryBean;
    }

    /**
     * Sets the value of the factoryBean property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFactoryBean(String value) {
        this.factoryBean = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
