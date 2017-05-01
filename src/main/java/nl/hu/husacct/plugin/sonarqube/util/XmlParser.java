package nl.hu.husacct.plugin.sonarqube.util;

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
    private static final String LOGGERNAME = "XmlParser";

    public static String getHUSACCTWorkspaceFileFromPom(File xmlFile) {
        Document document = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(xmlFile);
        } catch (IOException | ParserConfigurationException | SAXException e ) {
            Loggers.get(LOGGERNAME).error(String.format("Error reading pom.xml: %s", e.getMessage()));
            throw new RuntimeException(e);
        }

        Element mavenPlugin = findMavenPlugin(document);

        String workspacePath = findWorkspacePath(mavenPlugin);

        return FileFormatter.formatWorkspaceFile(workspacePath);

    }

    private static String findWorkspacePath(Element mavenPlugin) {
        Node workspacePath =  mavenPlugin.getElementsByTagName(HUSACCTWORKSPACENODE).item(0);
        return workspacePath.getTextContent();
    }

    private static Element findMavenPlugin(Document document) {
        NodeList allPlugins = document.getElementsByTagName("plugin");
        for(int counter = 0; counter < allPlugins.getLength(); counter++) {
            Element plugin = (Element) allPlugins.item(counter);
            Node artifact = plugin.getElementsByTagName("artifactId").item(0);
            if(artifact.getTextContent().equals(HUSACCTMAVENPLUGINARTIFACT)) {
                return plugin;
            }
        }
        String errorMessage = String.format("Cannot find the HUSACCT Maven plugin within the pom.xml file. artifactId", HUSACCTMAVENPLUGINARTIFACT);
        Loggers.get(LOGGERNAME).error(errorMessage);
        throw new RuntimeException(errorMessage);
    }
}
