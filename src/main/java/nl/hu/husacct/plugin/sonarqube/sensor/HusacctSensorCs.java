package nl.hu.husacct.plugin.sonarqube.sensor;


import husacct.externalinterface.ExternalServiceProvider;
import husacct.externalinterface.SaccCommandDTO;
import husacct.externalinterface.ViolationImExportDTO;
import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import nl.hu.husacct.plugin.sonarqube.rules.HUSACCTRulesDefinitionFromXML;
import nl.hu.husacct.plugin.sonarqube.util.DummyImportFile;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.FilePredicates;
import nl.hu.husacct.plugin.sonarqube.util.PomParser;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.util.ArrayList;

import static nl.hu.husacct.plugin.sonarqube.util.FileFormatter.formatFilePath;
import static nl.hu.husacct.plugin.sonarqube.util.Log4JPropertiesMaker.getLog4JProperties;

public class HusacctSensorCs implements Sensor{

    private final static FileFinder fileFinder = new FileFinder();
    private final static PomParser pomParser = new PomParser();
    private final static DummyImportFile dummyImport = new DummyImportFile();

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("HUSACCT violations export to SonarQube");
        // optimisation to disable execution of sensor if project does
        // not contain Java files or if the example rule is not activated
        // in the Quality profile
        descriptor.onlyOnLanguage("cs");
        descriptor.createIssuesForRuleRepositories(HUSACCTRulesDefinitionFromXML.REPOSITORY);
    }

    @Override
    public void execute(SensorContext context) {
        // create dummy importfile otherwise HUSACCT will not work.
        // file is not needed because Sonar keeps track of all the current issues, and refreshes them accordingly.
        String emptyImportFile = dummyImport.createImportFile(context);

        SaccCommandDTO saccCommandDTO = createSacCommand(context, emptyImportFile);
        ExternalServiceProvider externalServiceProvider = ExternalServiceProvider.getInstance(getLog4JProperties());
        ViolationImExportDTO[] allViolations = externalServiceProvider.performSoftwareArchitectureComplianceCheck(saccCommandDTO)
                .getAllViolations();


        Loggers.get(getClass()).info("HUSACCT finished, starting to creating sonar issues.");
        Loggers.get(getClass()).info(String.format("Found %d issues", allViolations.length));

        for (ViolationImExportDTO violation : allViolations) {
            createIssueFromViolation(context, violation);
        }

        dummyImport.removeDummyImportFile(emptyImportFile);
    }

    private void createIssueFromViolation(SensorContext context, ViolationImExportDTO violation) {
        // getFrom needs formatting before it can be used to find an InputFile.
        String violationFile = formatFilePath(violation.getFrom()) + ".cs";
        InputFile violationInputFile = context.fileSystem().inputFile(new FilePredicates.fileWithPath(violationFile));

        NewIssue issue = context.newIssue().forRule(RuleKey.of(HUSACCTRulesDefinitionFromXML.REPOSITORY, violation.getRuleType()));
        NewIssueLocation location = issue.newLocation()
                .on(violationInputFile)
                .at(violationInputFile.selectLine(violation.getLine()))
                .message(violation.getMessage());
        issue.at(location);
        issue.save();
    }

    /**
     *  Create a sacCommandDTO from the sensorcontext and importfile path.
     *  finds all paths with java code @see {@link FileFinder#getAllJavaSourcePaths(FileSystem)}
     *  finds the HUSACCT xml file @see {@link #findHUSACCTFile(SensorContext)}
     * @param importFile absolute path to import file with old violations
     */
    //TODO: create util function.
    private SaccCommandDTO createSacCommand(SensorContext context, String importFile) {
        ArrayList<String> csPaths = fileFinder.getAllCsSourcePaths(context.fileSystem());
        String HUSACCTFile = findHUSACCTFile(context);
        File SACCFile = fileFinder.getHUSACCTFile(context, HUSACCTFile).file();

        SaccCommandDTO saccCommandDTO = new SaccCommandDTO();
        saccCommandDTO.setHusacctWorkspaceFile(SACCFile.getAbsolutePath());
        saccCommandDTO.setSourceCodePaths(csPaths);
        saccCommandDTO.setImportFilePreviousViolations(importFile);
        return saccCommandDTO;
    }

    /**
     * Searches the HUSACCT workspace file fromd different sources
     * for java: pom.xml (HUSACCT maven plugin and properties)
     * @return absolute path to the file
     */
    private String findHUSACCTFile(SensorContext context ) {
        String return_value = null;
        FileFinder fF = new FileFinder();
        Iterable<InputFile> allXmlFiles = fF.getAllXmlFiles(context);
        for(InputFile xmlFile : allXmlFiles) {
            if(xmlFile.file().getName().equals("HUSACCT.properties.xml")) {
                // TODO: create function to get workspace.
            }
        }
        if(return_value != null) {
            return return_value;
        }
        Loggers.get(getClass()).error("Cannot find HUSACCT file!");
        throw new WorkspaceFileException("Cannot find HUSACCT file!");
    }
}
