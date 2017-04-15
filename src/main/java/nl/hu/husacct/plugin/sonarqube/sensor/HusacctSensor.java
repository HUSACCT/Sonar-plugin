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
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Generates issues on all java files at line 1. This rule
 * must be activated in the Quality profile.
 */
public class HusacctSensor implements Sensor {
    // for demo purpose
    private final static String TEMPHUSACCTFILE = "HUSACCT_Workspace_Current_Architecture.xml";
    private final static String LOG4JFILE = "log4j.properties";

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("HUSACCT violations export to SonarQube");
        // optimisation to disable execution of sensor if project does
        // not contain Java files or if the example rule is not activated
        // in the Quality profile
        // descriptor.onlyOnLanguage("java");
        descriptor.createIssuesForRuleRepositories(HUSACCTRulesDefinitionFromXML.REPOSITORY);
    }

    @Override
    public void execute(SensorContext context) {
        System.out.println("Dikzak");
        FileSystem fs = context.fileSystem();
        System.out.println("Tok TOk TOk ");
        SaccCommandDTO saccCommandDTO = createSacCommand(context);
        ExternalServiceProvider externalServiceProvider;
        System.out.println("SacCommand is er");
        externalServiceProvider = ExternalServiceProvider.getInstance(getLog4JProperties());
        System.out.println("Yay!! :D ");
        externalServiceProvider.performSoftwareArchitectureComplianceCheck(saccCommandDTO);

        // example code
        Iterable<InputFile> javaFiles = fs.inputFiles(fs.predicates().hasLanguage("java"));
        for (InputFile javaFile : javaFiles) {
            NewIssue newIssue = context.newIssue().forRule(RuleKey.of("HUSACCT", "MustUse"));
            NewIssueLocation primaryLocation = newIssue.newLocation()
                    .on(javaFile)
                    .at(javaFile.selectLine(1))
                    .message("You can't do anything. This is first line!");
            newIssue.at(primaryLocation);
            newIssue.save();
        }
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

    private SaccCommandDTO createSacCommand(SensorContext context) {
        ArrayList<String> javaPaths = getAllSourcePaths(context.fileSystem());
        SaccCommandDTO saccCommandDTO = new SaccCommandDTO();
        getAllXmlFiles(context);
        saccCommandDTO.setHusacctWorkspaceFile(getHUSACCTFile(context).file().getAbsolutePath());
        saccCommandDTO.setSourceCodePaths(javaPaths);
        return saccCommandDTO;
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

        return fs.inputFile(new HUSACCTPredicate(TEMPHUSACCTFILE));
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

    class HUSACCTPredicate implements FilePredicate {
        private String fileName;
        public HUSACCTPredicate(String fileName) {
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

