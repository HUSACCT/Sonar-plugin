# Sonar-plugin

## Requirements
- SonarQube version 5.6 or higher
- SonarXML (plug-in installed on SonarQube) version 1.4.2 or higher
- In case of Java source code: Maven version 3.0 or higher

## Installation guide

### Installing the plug-in.
1. Shutdown your SonarQube instance.
2. Download the latest version of the plug-in from the versions folder.
3. Move the downloaded jar to the extensions/plugins folder of your SonarQube installation.
4. Start SonarQube.

### Adding the Husacct rules to your quality profile within SonarQube
1. Login with permissions to change or add quality profiles.
2. Navigate to the quality profile page
3. Click on the arrow next to the quality profile you want to add the rules to.
4. Select activate more rules.
5. Select the Repository HUSACCT. This will filter away all other inactive rules.
6. Do a bulk change and select "activate in <profile name>".
7. The plug-in will now run with each project that uses this profile.

### Running a scan with Husacct

#### C#
1. Make sure you don't have the SonarQube paramater "sonar.language" set to cs. Otherwise, SonarQube will ignore all XML files.
2. Create a file called "HUSACCT.properties.xml".
3. Create an element called "workspacePath" with the name of your husacct sac file inside. (example: workspace.xml)
4. Run the sonar-scanner.

#### Java (with Maven)
1. Make sure you don't have the SonarQube paramater "sonar.language" set to java. Otherwise, SonarQube will ignore all XML files.
2. If you're not using the Husacct maven plug-in, add an element "workspacePath", with the name of your husacct sacc file inside, to your maven properties. (example: workspace.xml)
3. Add the SonarQube [maven plugin](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven)
4. Run the following command to start the scan: mvn sonar:sonar 


