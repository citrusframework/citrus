package com.consol.citrus.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter to replace resource elements in test case with data inline elements.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2008
 */
public class FileResourceSerializerFilter implements LSSerializerFilter {

    /**
     * (non-Javadoc)
     * @see org.w3c.dom.traversal.NodeFilter#acceptNode(org.w3c.dom.Node)
     */
    public short acceptNode(Node node) {

        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("resource")) {

            if (node.getParentNode().getNodeName().equals("message") == false) {
                return LSSerializerFilter.FILTER_ACCEPT;
            }

            Element resourceElement = (Element)node;
            Text newLineElement = (Text)node.getPreviousSibling();

            if (newLineElement == null) {
                newLineElement = node.getOwnerDocument().createTextNode("");
            }

            Element dataElement = node.getOwnerDocument().createElement("data");

            StringBuffer buf = new StringBuffer();
            BufferedReader  reader = null;
            
            try {
                String filePath = resourceElement.getAttribute("file");
                Resource file;

                if (filePath.startsWith("classpath:")) {
                    file = new ClassPathResource(filePath.substring("classpath:".length()));
                } else if (filePath.startsWith("file:")) {
                    file = new FileSystemResource(filePath.substring("file:".length()));
                } else {
                    file = new FileSystemResource(filePath);
                }

                reader = new BufferedReader(new FileReader(file.getFile()));

                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(newLineElement.getTextContent() + "\t" + line);
                }

                dataElement.appendChild(newLineElement.cloneNode(true));
                dataElement.appendChild(resourceElement.getOwnerDocument().createCDATASection(buf.toString()));
                dataElement.appendChild(newLineElement.cloneNode(true));

                node.getParentNode().insertBefore(dataElement, node.getNextSibling());

                return LSSerializerFilter.FILTER_REJECT;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

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
