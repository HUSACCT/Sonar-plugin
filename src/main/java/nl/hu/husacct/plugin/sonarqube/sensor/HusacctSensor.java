package nl.hu.husacct.plugin.sonarqube.sensor;

import husacct.externalinterface.ExternalServiceProvider;
import husacct.externalinterface.SaccCommandDTO;
import husacct.externalinterface.ViolationImExportDTO;
import nl.hu.husacct.plugin.sonarqube.rules.HUSACCTRulesDefinitionFromXML;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.FilePredicates;
import nl.hu.husacct.plugin.sonarqube.util.xmlparser.XmlParser;
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
import java.util.List;

import static nl.hu.husacct.plugin.sonarqube.util.FileFormatter.formatFilePath;
import static nl.hu.husacct.plugin.sonarqube.util.Log4JPropertiesMaker.getLog4JProperties;


public abstract class HusacctSensor implements Sensor {

    protected static final FileFinder fileFinder = new FileFinder();

    protected XmlParser xmlParser;

    protected abstract String getFileSuffix();

    protected abstract List<String> getSourcePaths(SensorContext context);

    protected abstract String getHusacctSaccFile(SensorContext context);

    protected abstract String getLanguageKey();

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("HUSACCT violations export to SonarQube");
        // optimisation to disable execution of sensor if project does
        // not contain Java files or if the example rule is not activated
        // in the Quality profile
        descriptor.onlyOnLanguage(getLanguageKey());
        descriptor.createIssuesForRuleRepositories(HUSACCTRulesDefinitionFromXML.REPOSITORY+getLanguageKey());
    }

    @Override
    /* ignore sonar exception logging issue, because the exception gets logged. Sonar doesn't seem to figure that out...
     also ignore the fact that sonar wants to specific Exception to  be caught. All exceptions need to get caught here
     so the scan can go on without this plug-in.
    */
    @java.lang.SuppressWarnings({"squid:S1166", "squid:S2221"})
    public void execute(SensorContext context) {
        try {
            Loggers.get(getClass()).info("Starting Husacct analysis.");
            doExecute(context);
        } catch (Exception e) {
            Loggers.get(getClass()).error(String.format("error during HUSACCT analysis: %s: %s", e.getClass().toString(), e.getMessage()));
            Loggers.get(getClass()).info("Skipping HUSACCT analysis.");
        }
    }

    private void doExecute(SensorContext context) {
        SaccCommandDTO saccCommandDTO = createSacCommand(context);
        ExternalServiceProvider externalServiceProvider = ExternalServiceProvider.getInstance(getLog4JProperties());
        ViolationImExportDTO[] allViolations = externalServiceProvider.performSoftwareArchitectureComplianceCheck(saccCommandDTO)
                .getAllViolations();


        Loggers.get(getClass()).info("HUSACCT finished, starting to creating sonar issues.");
        Loggers.get(getClass()).info(String.format("Found %d issues", allViolations.length));

        for (ViolationImExportDTO violation : allViolations) {
            createIssueFromViolation(context, violation);
        }
    }

    /**
     * Create a sacCommandDTO from the sensorcontext.
     */
    private SaccCommandDTO createSacCommand(SensorContext context) {
        List<String> sourcePaths = getSourcePaths(context);
        Loggers.get(getClass()).debug(String.format("Found %d sourcepaths basedir: %s", sourcePaths.size(), context.fileSystem().baseDir().getAbsolutePath()));

        String husacctFile = getHusacctSaccFile(context);
        File saccfile = fileFinder.getHUSACCTFile(context, husacctFile).file();

        SaccCommandDTO saccCommandDTO = new SaccCommandDTO();
        saccCommandDTO.setHusacctWorkspaceFile(saccfile.getAbsolutePath());
        saccCommandDTO.setSourceCodePaths((ArrayList<String>) sourcePaths);
        return saccCommandDTO;
    }

    private void createIssueFromViolation(SensorContext context, ViolationImExportDTO violation) {
        // getFrom needs formatting before it can be used to find an InputFile.
        String violationFile = formatFilePath(violation.getFrom()) + getFileSuffix();
        InputFile violationInputFile = context.fileSystem().inputFile(new FilePredicates.fileWithPath(violationFile, getFileSuffix()));

        if (violationInputFile != null) {
            NewIssue issue = context.newIssue().forRule(RuleKey.of(HUSACCTRulesDefinitionFromXML.REPOSITORY, violation.getRuleType()));

            NewIssueLocation location = issue.newLocation()
                    .on(violationInputFile)
                    .at(violationInputFile.selectLine(violation.getLine()))
                    .message(violation.getMessage());
            issue.at(location);
            issue.save();
        }
    }
}
