package nu.studer.gradle.util;

import nu.studer.gradle.credentials.CredentialsPlugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static nu.studer.gradle.credentials.CredentialsPlugin.DEFAULT_PASSPHRASE;

public class Credentials {

    private static final String CREDENTIALS_PASSPHRASE_PROPERTY = "credentialsPassphrase";
    private static final String DEFAULT_PASSPHRASE_CREDENTIALS_FILE = "gradle.encrypted.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsPlugin.class);

    private static String deriveFileNameFromPassphrase(String passphrase) {
        // derive the name of the file that contains the credentials from the given passphrase
        String credentialsFileName;
        if (passphrase.equals(DEFAULT_PASSPHRASE)) {
            credentialsFileName = DEFAULT_PASSPHRASE_CREDENTIALS_FILE;
            LOGGER.debug("No explicit passphrase provided. Using default credentials file name: " + credentialsFileName);
        } else {
            credentialsFileName = "gradle." + MD5.generateMD5Hash(passphrase) + ".encrypted.properties";
            LOGGER.debug("Custom passphrase provided. Using credentials file name: " + credentialsFileName);
        }
        return credentialsFileName;
    }

    public static File getCredentialsFile(Project project) {
        String passphrase = getPassphrase(project);
        String credentialsFileName = deriveFileNameFromPassphrase(passphrase);
        File gradleUserHomeDir = project.getGradle().getGradleUserHomeDir();
        return new File(gradleUserHomeDir, credentialsFileName);
    }

    private static String getPassphrase(Project project) {
        return getProjectProperty(CREDENTIALS_PASSPHRASE_PROPERTY, DEFAULT_PASSPHRASE, project);
    }

    private static String getProjectProperty(String key, String defaultValue, Project project) {
        Map<String, ?> properties = project.getProperties();
        return properties.containsKey(key) ? (String) properties.get(key) : defaultValue;
    }
}
