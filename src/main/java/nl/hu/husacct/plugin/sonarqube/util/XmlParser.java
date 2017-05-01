package nl.hu.husacct.plugin.sonarqube.util;

import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XmlParser {

    private static final String HUSACCTMAVENPLUGINARTIFACT = "husacct-maven-plugin";
    private static final String HUSACCTWORKSPACENODE = "workspacePath";

    public String getHUSACCTWorkspaceFileFromPom(File xmlFile) {
        Document document = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(xmlFile);
        } catch (IOException | ParserConfigurationException | SAXException e ) {
            Loggers.get(getClass()).error(String.format("Error reading pom.xml: %s", e.getMessage()));
            throw new RuntimeException(e);
        }
        String workspacePath;
        Element mavenPlugin = findMavenPlugin(document);
        Element mavenProperties = findMavenProperties(document);
        if(mavenPlugin != null) {
            workspacePath = findWorkspacePath(mavenPlugin);
            return FileFormatter.formatWorkspaceFile(workspacePath);
        }
        if(mavenProperties != null) {
            workspacePath = findWorkspacePath(mavenProperties);
            return FileFormatter.formatWorkspaceFile(workspacePath);
        }
        Loggers.get(getClass()).warn("Cannot find HUSACCT architecture file location inside pom.xml");
        return null;
    }

    private  String findWorkspacePath(Element parentElement) {
        NodeList workspacePath =  parentElement.getElementsByTagName(HUSACCTWORKSPACENODE);
        if(workspacePath.getLength() == 0) {
            Loggers.get(getClass()).warn(String.format("Cannot find workspace path node inside %s", parentElement.getTagName()));
            throw new WorkspaceFileException(String.format("Cannot find workspace path node inside %s", parentElement.getTagName()));
        }
        Loggers.get(getClass()).info("Found HUSACCT architecture file in pom.xml");
        return workspacePath.item(0).getTextContent();
    }

    private Element findMavenProperties(Document document) {
        Loggers.get(getClass()).info("Start searching for HUSACCT architecture file in maven properties");
        NodeList allProperties = document.getElementsByTagName("properties");

        if(allProperties.getLength() == 1 ) {
            return (Element) allProperties.item(0);
        }
        if(allProperties.getLength() > 1) {
            for(int counter = 0; counter < allProperties.getLength(); counter++) {
                Node currentItem = allProperties.item(counter);
                if(currentItem.getParentNode().getNodeName().equals("project")) {
                    return (Element) currentItem;
                }
            }
        }
        Loggers.get(getClass()).warn("Cannot find maven properties!");
        return null;
    }

    private Element findMavenPlugin(Document document) {
        Loggers.get(getClass()).info("Start searching for HUSACCT architecture file in maven plug-in");

        NodeList allPlugins = document.getElementsByTagName("plugin");
        for(int counter = 0; counter < allPlugins.getLength(); counter++) {
            Element plugin = (Element) allPlugins.item(counter);
            Node artifact = plugin.getElementsByTagName("artifactId").item(0);
            if(artifact.getTextContent().equals(HUSACCTMAVENPLUGINARTIFACT)) {
                return plugin;
            }
        }
        String errorMessage = String.format("Cannot find the HUSACCT Maven plugin within the pom.xml file. artifactId: %s", HUSACCTMAVENPLUGINARTIFACT);
        Loggers.get(getClass()).warn(errorMessage);
        return null;
    }
}
