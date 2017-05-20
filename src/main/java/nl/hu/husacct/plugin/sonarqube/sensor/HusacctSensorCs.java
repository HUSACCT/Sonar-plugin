package nl.hu.husacct.plugin.sonarqube.sensor;


import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.xmlparser.HusacctPropertiesXmlParser;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Loggers;

import java.util.List;

public class HusacctSensorCs extends HusacctSensor{

    @Override
    protected String getFileSuffix() {
        return ".cs";
    }

    @Override
    protected List<String> getSourcePaths(SensorContext context) {
        return fileFinder.getAllCsSourcePaths(context.fileSystem());
    }

    @Override
    protected String findHUSACCTFile(SensorContext context ) {
        xmlParser = new HusacctPropertiesXmlParser();
        String returnValue = null;
        FileFinder fF = new FileFinder();
        Iterable<InputFile> allXmlFiles = fF.getAllXmlFiles(context);
        for(InputFile xmlFile : allXmlFiles) {
            if("HUSACCT.properties.xml".equals(xmlFile.file().getName())) {
                returnValue = xmlParser.getHussactWorkspaceFile(xmlFile.file());
            }
        }
        if(returnValue != null) {
            return returnValue;
        }
        Loggers.get(getClass()).error("Cannot find HUSACCT file!");
        throw new WorkspaceFileException("Cannot find HUSACCT file!");
    }

    @Override
    protected String getLanguageKey() {
        return "cs";
    }
}
