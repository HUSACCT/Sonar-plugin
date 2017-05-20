package nl.hu.husacct.plugin.sonarqube.sensor;


import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.xmlparser.HusacctPropertiesXmlParser;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Loggers;

import java.util.ArrayList;

public class HusacctSensorCs extends HusacctSensor{

    @Override
    protected String getFileSuffix() {
        return ".cs";
    }

    @Override
    protected ArrayList<String> getSourcePaths(SensorContext context) {
        return fileFinder.getAllCsSourcePaths(context.fileSystem());
    }

    @Override
    protected String findHUSACCTFile(SensorContext context ) {
        xmlParser = new HusacctPropertiesXmlParser();
        String return_value = null;
        FileFinder fF = new FileFinder();
        Iterable<InputFile> allXmlFiles = fF.getAllXmlFiles(context);
        for(InputFile xmlFile : allXmlFiles) {
            if(xmlFile.file().getName().equals("HUSACCT.properties.xml")) {
                return_value = xmlParser.getHussactWorkspaceFile(xmlFile.file());
            }
        }
        if(return_value != null) {
            return return_value;
        }
        Loggers.get(getClass()).error("Cannot find HUSACCT file!");
        throw new WorkspaceFileException("Cannot find HUSACCT file!");
    }

    @Override
    protected String getLanguageKey() {
        return "cs";
    }
}
