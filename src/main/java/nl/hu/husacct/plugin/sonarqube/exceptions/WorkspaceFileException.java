package nl.hu.husacct.plugin.sonarqube.exceptions;

public class WorkspaceFileException extends RuntimeException{

    public WorkspaceFileException(Throwable throwable) {
        super(throwable);
    }

    public WorkspaceFileException(String message) {
        super(message);
    }
}
