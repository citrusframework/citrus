package com.consol.citrus.admin.spring.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "listItems"
})
@XmlRootElement(name = "list")
public class List {

    @XmlElementRefs({
        @XmlElementRef(name = "bean", namespace = "http://www.springframework.org/schema/beans", type = SpringBean.class),
        @XmlElementRef(name = "ref", namespace = "http://www.springframework.org/schema/beans", type = Ref.class)
    })
    protected java.util.List<Object> listItems;

    /**
     * Gets the list items.
     */
    public java.util.List<Object> getListItems() {
        if (listItems == null) {
            listItems = new ArrayList<Object>();
        }
        return this.listItems;
    }

}
