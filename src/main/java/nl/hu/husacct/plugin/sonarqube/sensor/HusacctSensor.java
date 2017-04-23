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
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * Generates issues on all java files at line 1. This rule
 * must be activated in the Quality profile.
 */
public class HusacctSensor implements Sensor {
    // for demo purpose
    private final static String TEMPHUSACCTFILE = "HUSACCT_Workspace_Current_Architecture.xml";

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
        ViolationImExportDTO[] allViolations =  externalServiceProvider.performSoftwareArchitectureComplianceCheck(saccCommandDTO).getAllViolations();
        Loggers.get(getClass()).info("Number of violations found:  " + allViolations.length);

        Loggers.get(getClass()).info("HUSACCT finished, starting to creating sonar issues");
        for (ViolationImExportDTO violation : allViolations) {
            String violationFile = formatFilePath(violation.getFrom()) + ".java";

            InputFile violationInputFile = context.fileSystem().inputFile(new fileWithPath(violationFile));
            NewIssue issue = context.newIssue().forRule(RuleKey.of(HUSACCTRulesDefinitionFromXML.REPOSITORY,violation.getRuleType()));
            NewIssueLocation location = issue.newLocation()
                    .on(violationInputFile)
                    .at(violationInputFile.selectLine(violation.getLine()))
                    .message(violation.getMessage());
            issue.at(location);
            issue.save();
        }
    }

    private String formatFilePath(String file) {
        String returnValue = file.replace('.', File.separatorChar);
        returnValue = returnValue.replace('\\', File.separatorChar);
        returnValue = returnValue.replace('/', File.separatorChar);
        return returnValue;
    }

    private Properties getLog4JProperties() {
        Properties properties = new Properties();
        properties.setProperty("log4j.rootLogger", "debug, stdout, R");
        properties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        properties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%5p [%t] (%F:%L) - %m%n");
        properties.setProperty("log4j.appender.R", "org.apache.log4j.RollingFileAppender");
        properties.setProperty("log4j.appender.R.File", "log/husacct.log");
        properties.setProperty("log4j.appender.R.MaxFileSize", "100KB");
        properties.setProperty("log4j.appender.R.MaxBackupIndex", "1");
        properties.setProperty("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.R.layout.ConversionPattern", "%p %t %c - %m%n");

        return properties;
    }

    private SaccCommandDTO createSacCommand(SensorContext context, String emptyImportFile) {
        ArrayList<String> javaPaths = getAllSourcePaths(context.fileSystem());
        File SACCFile = getHUSACCTFile(context).file();
        SaccCommandDTO saccCommandDTO = new SaccCommandDTO();
        Loggers.get(getClass()).info(String.format("Found architecture file: %s", SACCFile));
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

        try{
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

    private ArrayList<String> getAllSourcePaths(FileSystem fs) {
        ArrayList<String> javaPaths = new ArrayList<>();
        for(InputFile file : fs.inputFiles(fs.predicates().hasLanguage("java"))) {
            String path = file.path().toString().substring(0, file.path().toString().lastIndexOf(File.separator));
            if(!javaPaths.contains(path)) {
                javaPaths.add(path);
                Loggers.get(getClass()).info(String.format("Found path: %s", path));
            }
        }
        return javaPaths;
    }

    private InputFile getHUSACCTFile(SensorContext context) {
        FileSystem fs = context.fileSystem();

        return fs.inputFile(new fileWithName(TEMPHUSACCTFILE));
    }

    private Iterable<InputFile> getAllXmlFiles(SensorContext context) {
        FileSystem fs = context.fileSystem();
        Iterable<InputFile> xmlFiles = fs.inputFiles(new XmlPredicate());
        for (InputFile input : xmlFiles) {
            Loggers.get(getClass()).info(String.format("Found xml file: %s", input.file().getName()));
        }
        return xmlFiles;
    }



    class XmlPredicate implements FilePredicate {

        @Override
        public boolean apply(InputFile inputFile) {
            if(inputFile.file().getName().endsWith(".xml")) {
                return true;
            }
            return false;
        }
    }

    class fileWithPath implements  FilePredicate {
        private String absolutePath;
        public fileWithPath(String absolutePath) { this.absolutePath = absolutePath;}

        @Override
        public boolean apply(InputFile inputFile) {
            String inputFileAbsolutePath = formatFilePath(
                    inputFile.absolutePath().substring(0, inputFile.absolutePath().length() -5))
                    + ".java";
            if(inputFileAbsolutePath.endsWith(absolutePath)) {
                return true;
            }
            return false;
        }
    }

    class fileWithName implements FilePredicate {
        private String fileName;
        public  fileWithName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public boolean apply(InputFile inputFile) {
            if(inputFile.file().getName().equals(fileName)) {
                return true;
            }
            return false;
        }
    }
}

