package nl.hu.husacct.plugin.sonarqube.util.xmlparser;

import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public abstract class XmlParser {
    private static final String HUSACCTWORKSPACENODE = "workspacePath";

    protected abstract String findWorkspaceFile(Element document);

    public String getHussactWorkspaceFile(File xmlFile) {
        Document document = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(xmlFile);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            Loggers.get(getClass()).error(String.format("Error reading %s: %s", xmlFile.getName(), e.getMessage()));
            throw new WorkspaceFileException(e);
        }

        return findWorkspaceFile(document.getDocumentElement());
    }

    protected String getWorkspaceContent(Element parentElement) {
        NodeList workspacePath = parentElement.getElementsByTagName(HUSACCTWORKSPACENODE);
        if (workspacePath.getLength() == 0) {
            Loggers.get(getClass()).warn(String.format("Cannot find workspace path node inside %s", parentElement.getTagName()));
            throw new WorkspaceFileException(String.format("Cannot find workspace path node inside %s", parentElement.getTagName()));
        }
        String returnValue = workspacePath.item(0).getTextContent();
        Loggers.get(getClass()).info(String.format("Found name of HUSACCT architecture file %s", returnValue));
        return returnValue;
    }
}
