package nu.studer.gradle.credentials;

import nu.studer.gradle.credentials.domain.CredentialsEncryptor;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static nu.studer.gradle.util.Credentials.getCredentialsFile;

/**
 * <p>
 * Plugin to store and access encrypted credentials using password-based encryption (PBE). The credentials are stored in the Gradle home directory in a separate file for each
 * passphrase. If no passphrase is provided, a default passphrase is used and the credentials are stored in the default credentials file 'gradle.encrypted.properties'. While
 * running a build, only one passphrase is active per project.
 * </p>
 * <p>
 * The plugin provides a credentials container through the 'credentials' property that is available from the Gradle project. This allows access to credentials in the form of
 * <code>project.myCredentialKey</code>. The already persisted credentials can be accessed through the credentials container, and new credentials can be added to the container
 * ad-hoc while the build is executed. Credentials added ad-hoc are not available beyond the lifetime of the build.
 * </p>
 * The plugin adds a task to add credentials and a task to remove credentials.
 */
public class CredentialsPlugin implements Plugin<Project> {

    public static final String DEFAULT_PASSPHRASE = ">>Default passphrase to encrypt passwords!<<";
    private static final String CREDENTIALS_CONTAINER_PROPERTY = "credentials";

    static final String CREDENTIALS_KEY_PROPERTY = "credentialsKey";
    static final String CREDENTIALS_VALUE_PROPERTY = "credentialsValue";

    private static final String ADD_CREDENTIALS_TASK_NAME = "addCredentials";
    private static final String REMOVE_CREDENTIALS_TASK_NAME = "removeCredentials";

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    @Override
    public void apply(Project project) {
        // create credentials encryptor for the given passphrase
        CredentialsEncryptor credentialsEncryptor = CredentialsEncryptor.withPassphrase(CredentialsPlugin.DEFAULT_PASSPHRASE.toCharArray());
        Map<String, Object> orderedProperties = new LinkedHashMap<String, Object>();

        try {
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder = setupConfigurationBuilder(project);
            Configuration config = setupConfiguration(builder, orderedProperties);

            addCredentialsProperty(project, config);
            addAddCredentialsTask(project, credentialsEncryptor, builder);
            addRemoveCredentialsTask(project, builder);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    private FileBasedConfigurationBuilder<FileBasedConfiguration> setupConfigurationBuilder(Project project) {
        Parameters params = new Parameters();
        return new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFile(getCredentialsFile(project)));
    }

    private Configuration setupConfiguration(FileBasedConfigurationBuilder<FileBasedConfiguration> builder, Map<String, Object> orderedProperties) throws ConfigurationException {
        return builder.setParameters(orderedProperties).getConfiguration();
    }

    /**
     * Add a new 'credentials' property and transiently store the persisted credentials for access in build scripts
     *
     * @param project Gradle project
     * @param config Configuration that can be used to access config files
     */
    private void addCredentialsProperty(Project project, Configuration config) {
        project.getExtensions().getExtraProperties().set(CREDENTIALS_CONTAINER_PROPERTY, config);
        LOGGER.debug("Registered property '" + CREDENTIALS_CONTAINER_PROPERTY + "'");
    }

    /**
     * Add a task instance that stores new credentials through the configuration
     *
     * @param project Gradle project
     * @param credentialsEncryptor Helper class to encrypt with our secret key
     * @param builder Configuration Builder that can be used to persist changes
     */
    private void addAddCredentialsTask(Project project, CredentialsEncryptor credentialsEncryptor, FileBasedConfigurationBuilder<FileBasedConfiguration> builder) {
        AddCredentialsTask addCredentials = project.getTasks().create(ADD_CREDENTIALS_TASK_NAME, AddCredentialsTask.class);
        addCredentials.setDescription("Adds the credentials specified through the project properties 'credentialsKey' and 'credentialsValue'.");
        addCredentials.setGroup("Credentials");
        addCredentials.setCredentialsEncryptor(credentialsEncryptor);
        addCredentials.setConfigBuilder(builder);
        LOGGER.debug(String.format("Registered task '%s'", addCredentials.getName()));
    }

    /**
     * Add a task instance that removes credentials through the configuration
     *
     * @param project Gradle project
     * @param builder Configuration Builder that can be used to persist changes
     */
    private void addRemoveCredentialsTask(Project project, FileBasedConfigurationBuilder<FileBasedConfiguration> builder) {
        RemoveCredentialsTask removeCredentials = project.getTasks().create(REMOVE_CREDENTIALS_TASK_NAME, RemoveCredentialsTask.class);
        removeCredentials.setDescription("Removes the credentials specified through the project property 'credentialsKey'.");
        removeCredentials.setGroup("Credentials");
        removeCredentials.setConfigBuilder(builder);
        LOGGER.debug(String.format("Registered task '%s'", removeCredentials.getName()));
    }

}
