package info.makarov.s3.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import info.makarov.s3.core.entity.preferences.Preferences;
import info.makarov.s3.core.entity.preferences.Profile;
import info.makarov.s3.core.exception.S3PluginException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
public class PreferencesService {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public Preferences getPreferences() {
        File file = getPrefFile();
        if (file.exists()) {
            try {
                return objectMapper.readValue(file, Preferences.class);
            } catch (IOException e) {
                throw new S3PluginException("Cannot to read file " + file, e);
            }
        }
        Preferences preferences = createPrefs();
        save(preferences);
        return preferences;
    }

    public void save(Preferences preferences) {

        File file = getPrefFile();
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("File {} wasn't deleted", file);
            }
        }
        try {
            boolean isCreated = file.createNewFile();
            if (!isCreated) {
                throw new S3PluginException("Cannot to create file " + file);
            }
        } catch (IOException e) {
            throw new S3PluginException("Cannot to read file " + file, e);
        }
        try {
            objectMapper.writeValue(file, preferences);
        } catch (IOException e) {
            throw new S3PluginException("Cannot to write file " + file, e);
        }
    }

    public File getPrefFile() {
        return Paths.get(System.getProperty("user.home"), ".s3-plugin.cfg").toFile();
    }

    private Preferences createPrefs() {
        Preferences preferences = new Preferences();
        Profile firstProfile = preferences.getDefaultProfile().toBuilder().build();
        preferences.setProfiles(Lists.newArrayList(firstProfile));
        return preferences;
    }

}
