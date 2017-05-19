/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package nl.hu.husacct.plugin.sonarqube.sensor;

import husacct.externalinterface.ExternalServiceProvider;
import husacct.externalinterface.SaccCommandDTO;
import husacct.externalinterface.ViolationImExportDTO;
import nl.hu.husacct.plugin.sonarqube.exceptions.HusacctSonarException;
import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import nl.hu.husacct.plugin.sonarqube.rules.HUSACCTRulesDefinitionFromXML;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.FilePredicates;
import nl.hu.husacct.plugin.sonarqube.util.XmlParser;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static nl.hu.husacct.plugin.sonarqube.util.FileFormatter.formatFilePath;
import static nl.hu.husacct.plugin.sonarqube.util.Log4JPropertiesMaker.getLog4JProperties;

/**
 * must be activated in the Quality profile.
 */
public class HusacctSensor implements Sensor {

    private final static FileFinder fileFinder = new FileFinder();
    private final static XmlParser xmlParser = new XmlParser();

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("HUSACCT violations export to SonarQube");
        // optimisation to disable execution of sensor if project does
        // not contain Java files or if the example rule is not activated
        // in the Quality profile
        descriptor.onlyOnLanguage("java");
        descriptor.createIssuesForRuleRepositories(HUSACCTRulesDefinitionFromXML.REPOSITORY);
    }

    @Override
    public void execute(SensorContext context) {
        // create dummy importfile otherwise HUSACCT will not work.
        // file is not needed because Sonar keeps track of all the current issues, and refreshes them accordingly.
        String emptyImportFile = createImportFile(context);

        SaccCommandDTO saccCommandDTO = createSacCommand(context, emptyImportFile);
        ExternalServiceProvider externalServiceProvider = ExternalServiceProvider.getInstance(getLog4JProperties());
        ViolationImExportDTO[] allViolations = externalServiceProvider.performSoftwareArchitectureComplianceCheck(saccCommandDTO)
                .getAllViolations();


        Loggers.get(getClass()).info("HUSACCT finished, starting to creating sonar issues");

        for (ViolationImExportDTO violation : allViolations) {
            createIssueFromViolation(context, violation);
        }

        removeDummyImportFile(emptyImportFile);
    }

    private void createIssueFromViolation(SensorContext context, ViolationImExportDTO violation) {
        // getFrom needs formatting before it can be used to find an InputFile.
        String violationFile = formatFilePath(violation.getFrom()) + ".java";
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
    private SaccCommandDTO createSacCommand(SensorContext context, String importFile) {
        ArrayList<String> javaPaths = fileFinder.getAllJavaSourcePaths(context.fileSystem());
        String HUSACCTFile = findHUSACCTFile(context);
        File SACCFile = fileFinder.getHUSACCTFile(context, HUSACCTFile).file();

        SaccCommandDTO saccCommandDTO = new SaccCommandDTO();
        saccCommandDTO.setHusacctWorkspaceFile(SACCFile.getAbsolutePath());
        saccCommandDTO.setSourceCodePaths(javaPaths);
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
            if(xmlFile.file().getName().equals("pom.xml")) {
                return_value = xmlParser.getHUSACCTWorkspaceFileFromPom(xmlFile.file());
            }
        }
        if(return_value != null) {
            return return_value;
        }
        Loggers.get(getClass()).error("Cannot find HUSACCT file!");
        throw new WorkspaceFileException("Cannot find HUSACCT file!");
    }


    /*
        This function is neede because, HUSACCT expects an import file.
     */
    private String createImportFile(SensorContext context) {
        String baserDir = context.fileSystem().baseDir().getAbsolutePath();
        String fileName = "DummyImportFile.xml";

        try {
            PrintWriter writer = new PrintWriter(baserDir + fileName, "UTF-8");
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<report></report>");
            writer.close();
        } catch (IOException e) {
            throw new HusacctSonarException("Cannot create dummy import file." + e.getCause());
        }

        Loggers.get(getClass()).info("Created dummy import file");
        return baserDir + fileName;

    }

    private void removeDummyImportFile(String emptyImportFile) {
        try {
            Files.delete(Paths.get(emptyImportFile));
            Loggers.get(getClass()).info("Dummy import file has successfully deleted.");
        } catch (IOException e) {
            Loggers.get(getClass()).warn("Could not find dummy import file. Maybe it has been deleted manually?");
        }
    }
}

