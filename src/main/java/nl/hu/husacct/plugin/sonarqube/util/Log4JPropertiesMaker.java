package nl.hu.husacct.plugin.sonarqube.util;

import java.util.Properties;

public class Log4JPropertiesMaker {


    public static Properties getLog4JProperties() {
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
}
