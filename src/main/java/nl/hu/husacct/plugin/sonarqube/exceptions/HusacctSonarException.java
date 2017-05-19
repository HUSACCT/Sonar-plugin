package nl.hu.husacct.plugin.sonarqube.exceptions;

public class HusacctSonarException extends RuntimeException{

    public HusacctSonarException(Throwable throwable) {
        super(throwable);
    }

    public HusacctSonarException(String message) {
        super(message);
    }
}
