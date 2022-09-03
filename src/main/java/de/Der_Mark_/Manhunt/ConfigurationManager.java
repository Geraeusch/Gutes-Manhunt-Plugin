package de.Der_Mark_.Manhunt;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigurationManager {

    File m_DataFolder;
    String m_FileName;
    ManhuntMain m_PluginData;

    public ConfigurationManager(File DataFolder, String name, ManhuntMain PluginData) {
        m_DataFolder = DataFolder;
        m_FileName = name;
        m_PluginData = PluginData;

        FileConfiguration config = m_PluginData.getConfig();

        // Pfade sollen mit . seperiert werden
        config.options().pathSeparator('.');
        // Falls ein Eintrag fehlt, soll dieser ergänzt werden
        config.options().copyDefaults(true);
    }

    /* FUNCTION load
     *
     * lädt Konfiguration
     *
     */

    public void load() {
        try {
            // Ordner erzeugen falls nicht existent
            if (!m_DataFolder.exists()) {
                m_DataFolder.mkdirs();
            }
            File temp = new File(m_DataFolder, "database.yml");
            if (!temp.exists()) {
                temp.getParentFile().mkdirs();
                m_PluginData.saveResource("database.yml", false);
            }

            // Neue Datei vorbereiten
            File file = new File(m_DataFolder, m_FileName);
            // Falls Datei nicht existiert eine neue erstellen
            if (!file.exists()) {
                m_PluginData.getLogger().info(m_FileName + " not found, creating new one!");
                m_PluginData.saveDefaultConfig();
            } else {
                m_PluginData.getLogger().info(m_FileName + " found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* FUNCTION save
     *
     * Speichert konfiguration
     *
     */

    public void save() {
        m_PluginData.saveConfig();
    }

}