package com.consol.citrus.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter to replace resource elements in test case with data inline elements.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2008
 */
public class MetaInfoSerializerFilter implements LSSerializerFilter {

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Map mapping;

    public MetaInfoSerializerFilter(Map mapping) {
        this.mapping = mapping;
    }

    /**
     * (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeFilter#acceptNode(org.w3c.dom.Node)
     */
    public short acceptNode(Node node) {

        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("testcase")) {
            Element testcase = ((Element)node);
            String testName = testcase.getAttribute("name");

            String authorName = (String)mapping.get(testName);

            if (authorName == null) {
                authorName = "Unknown";
            }

            if (testcase.getElementsByTagName("meta-info").getLength() > 0) {
                return LSSerializerFilter.FILTER_ACCEPT;
            }

            Element metaInfo = testcase.getOwnerDocument().createElement("meta-info");

            metaInfo.appendChild(testcase.getOwnerDocument().createTextNode("\n\t\t\t"));

            Element author = testcase.getOwnerDocument().createElement("author");
            author.setTextContent(authorName);
            metaInfo.appendChild(author);

            metaInfo.appendChild(testcase.getOwnerDocument().createTextNode("\n\t\t\t"));

            Element creationdate = testcase.getOwnerDocument().createElement("creationdate");
            creationdate.setTextContent(dateFormat.format(new Date(System.currentTimeMillis())));
            metaInfo.appendChild(creationdate);

            metaInfo.appendChild(testcase.getOwnerDocument().createTextNode("\n\t\t\t"));

            Element status = testcase.getOwnerDocument().createElement("status");
            status.setTextContent("FINAL");
            metaInfo.appendChild(status);

            metaInfo.appendChild(testcase.getOwnerDocument().createTextNode("\n\t\t\t"));

            Element lastUpdatedBy = testcase.getOwnerDocument().createElement("lastUpdatedBy");
            lastUpdatedBy.setTextContent(authorName);
            metaInfo.appendChild(lastUpdatedBy);

            metaInfo.appendChild(testcase.getOwnerDocument().createTextNode("\n\t\t\t"));

            Element lastUpdatedOn = testcase.getOwnerDocument().createElement("lastUpdatedOn");
            lastUpdatedOn.setTextContent(dateFormat.format(new Date(System.currentTimeMillis())) + "T00:00:00");
            metaInfo.appendChild(lastUpdatedOn);

            metaInfo.appendChild(testcase.getOwnerDocument().createTextNode("\n\t\t"));

            testcase.insertBefore(metaInfo, testcase.getFirstChild());
            testcase.insertBefore(testcase.getOwnerDocument().createTextNode("\n\t\t"), testcase.getFirstChild());
            testcase.insertBefore(testcase.getOwnerDocument().createTextNode("\n\t\t"), metaInfo.getNextSibling());

            return LSSerializerFilter.FILTER_ACCEPT;
        }

        return LSSerializerFilter.FILTER_ACCEPT;
    }

    /**
     * (non-Javadoc)
     * @see org.w3c.dom.ls.LSSerializerFilter#getWhatToShow()
     */
    public int getWhatToShow() {
        return NodeFilter.SHOW_ELEMENT;
    }
}
