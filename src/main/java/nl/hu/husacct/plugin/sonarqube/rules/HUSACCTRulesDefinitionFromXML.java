package nl.hu.husacct.plugin.sonarqube.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class HUSACCTRulesDefinitionFromXML implements RulesDefinition {

    public static final String REPOSITORY = "HUSACCT";
    private static final String PATH_TO_RULES_XML = "/HUSACCTRules.xml";

    protected String rulesDefinitionFilePath() {
        return PATH_TO_RULES_XML;
    }

    private void defineRulesForLanguage(Context context, String repositoryKey, String repositoryName, String... languageKeys) {
        for (String language : languageKeys) {
            NewRepository repository = context.createRepository(repositoryKey + language, language).setName(repositoryName);
            InputStream rulesXml = this.getClass().getResourceAsStream(rulesDefinitionFilePath());
            if (rulesXml != null) {
                RulesDefinitionXmlLoader rulesLoader = new RulesDefinitionXmlLoader();
                rulesLoader.load(repository, rulesXml, StandardCharsets.UTF_8.name());
            }
            repository.done();
        }
    }

    @Override
    public void define(Context context) {
        defineRulesForLanguage(context, REPOSITORY, REPOSITORY, "java", "cs");
    }

}
