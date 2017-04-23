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
import nl.hu.husacct.plugin.sonarqube.rules.HUSACCTRulesDefinitionFromXML;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.FilePredicates;
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
    // for demo purpose
    private final static String TEMPHUSACCTFILE = "HUSACCT_Workspace_Current_Architecture.xml";
    private final static FileFinder fileFinder = new FileFinder();

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
        String emptyImportFile = createImportFile(context);
        SaccCommandDTO saccCommandDTO = createSacCommand(context, emptyImportFile);
        ExternalServiceProvider externalServiceProvider;
        externalServiceProvider = ExternalServiceProvider.getInstance(getLog4JProperties());
        ViolationImExportDTO[] allViolations = externalServiceProvider.performSoftwareArchitectureComplianceCheck(saccCommandDTO).getAllViolations();
        Loggers.get(getClass()).info("Number of violations found:  " + allViolations.length);

        Loggers.get(getClass()).info("HUSACCT finished, starting to creating sonar issues");
        for (ViolationImExportDTO violation : allViolations) {
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
        try {
            Files.delete(Paths.get(emptyImportFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SaccCommandDTO createSacCommand(SensorContext context, String emptyImportFile) {
        ArrayList<String> javaPaths = fileFinder.getAllSourcePaths(context.fileSystem());
        File SACCFile = fileFinder.getHUSACCTFile(context, TEMPHUSACCTFILE).file();

        SaccCommandDTO saccCommandDTO = new SaccCommandDTO();
        saccCommandDTO.setHusacctWorkspaceFile(SACCFile.getAbsolutePath());
        saccCommandDTO.setSourceCodePaths(javaPaths);
        saccCommandDTO.setImportFilePreviousViolations(emptyImportFile);
        return saccCommandDTO;
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
            // do something
        }

        Loggers.get(getClass()).info("Created dummy import file");
        return baserDir + fileName;

    }
}

