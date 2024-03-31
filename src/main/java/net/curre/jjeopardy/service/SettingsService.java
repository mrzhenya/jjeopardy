/*
 * Copyright 2024 Yevgeny Nyden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.curre.jjeopardy.service;

import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Locale;

import static net.curre.jjeopardy.service.LafService.DEFAULT_LAF_THEME_ID;

/**
 * Settings service that assists with handling application settings.
 * Application settings stored on disk are loaded at service creation time or
 * initialized to default if no settings file exists. An instance of this service
 * object should be obtained from the AppRegistry.<br><br>
 *
 * To change settings, modify the settings object obtained via #getSettings
 * and persist them to disk via the #persistSettings method.<br><br>
 *
 * Settings file is stored in a platform specific directory,
 * <ul>
 *   <li>on Mac, it's - 'UserHome' / Library / Application Support / JJeopardy /</li>
 *   <li>on Windows, it's - 'UserHome' / AppData / Local / Temp / JJeopardy /</li>
 *   <li>on others, it's - 'UserHome' / temp / JJeopardy /</li>
 * </ul>
 *
 * @author Yevgeny Nyden
 */
public class SettingsService {

  /** Name of the temp settings file to store some info between the games. */
  private static final String SETTINGS_FILENAME = "jjeopardy-settings.ser";

  /** Directory name where the settings are going to be saved. */
  private static final String SETTINGS_DIR_NAME = "JJeopardy";

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Reference to the settings object. */
  private final Settings settings;

  /** Absolute path to the settings file. */
  private final String settingsFilePath;

  /**
   * Ctor.
   * @param settingsFilePath path to the settings file (for test) or null if default should be used
   */
  public SettingsService(String settingsFilePath) {
    if (settingsFilePath == null) {
      settingsFilePath = getVerifiedSettingsDirectoryPath() + File.separatorChar + SETTINGS_FILENAME;
    }
    this.settingsFilePath = settingsFilePath;
    this.settings = loadSettings(settingsFilePath);
  }

  /**
   * Returns application settings (current settings).
   * @return Reference to the application settings
   */
  public Settings getSettings() {
    return this.settings;
  }

  /** Persists the current settings. */
  public void persistSettings() {
    try {
      File file = new File(this.settingsFilePath);
      FileOutputStream fStream = new FileOutputStream(file);
      ObjectOutputStream oStream = new ObjectOutputStream(fStream);
      oStream.writeObject(this.settings);
    } catch (Exception e) {
      logger.log(Level.WARN, "Unable to save the settings!", e);
    }
  }

  /**
   * Saves the last known current directory in the settings.
   * @param absolutePath last known current directory
   */
  public void saveLastCurrentDirectory(String absolutePath) {
    this.settings.setLastCurrentDirectory(absolutePath);
  }

  /**
   * Saves the main game window size in the settings if it's not
   * smaller than the default game table size.
   * @param width the table width
   * @param height the table height
   */
  public void updateGameWindowSize(int width, int height) {
    if (width >= JjDefaults.GAME_TABLE_MIN_WIDTH) {
      this.settings.setGameWindowWidth(width);
    }
    if (height >= JjDefaults.GAME_TABLE_MIN_HEIGHT) {
      this.settings.setGameWindowHeight(height);
    }
  }

  /**
   * Saves the edit game dialog size in the settings if it's not
   * smaller than the default edit game dialog size.
   * @param width the width
   * @param height the height
   */
  public void updateEditDialogSize(int width, int height) {
    if (width >= JjDefaults.EDIT_GAME_DIALOG_MIN_WIDTH) {
      this.settings.setEditDialogWidth(width);
    }
    if (height >= JjDefaults.EDIT_GAME_DIALOG_MIN_HEIGHT) {
      this.settings.setEditDialogHeight(height);
    }
  }

  /**
   * Returns a platform specific absolute path to the game settings directory.
   * All custom directories in the path that don't exist, will be created.
   * @return absolute path to the game settings directory
   */
  public static String getVerifiedSettingsDirectoryPath() {
    StringBuilder path = new StringBuilder(System.getProperties().getProperty("user.home"));
    switch (Utilities.getPlatformType()) {
      case MAC_OS:
        path.append(File.separatorChar).append("Library").
            append(File.separatorChar).append("Application Support");
        break;
      case WINDOWS:
        path.append(File.separatorChar).append("AppData").
            append(File.separatorChar).append("Local").
            append(File.separatorChar).append("Temp");
        break;
      default:
        path.append(File.separatorChar).append("temp");
    }
    path.append(File.separatorChar).append(SETTINGS_DIR_NAME);
    createDirIfDoesntExist(path);
    return path.toString();
  }

  /**
   * Method to load settings stored on disk.
   * @param settingsFilePath path to the settings file
   * @return Settings, loaded from the settings file,
   *         or a new <code>Settings</code> object if no settings file is found
   */
  private Settings loadSettings(String settingsFilePath) {
    // Try loading the settings file.
    try {
      File file = new File(settingsFilePath);
      if (file.exists()) {
        FileInputStream fStream = new FileInputStream(file);
        ObjectInputStream oStream = new ObjectInputStream(fStream);
        Settings settings = (Settings) oStream.readObject();

        // Test each newer field for null values here
        // in case when an old settings file is loaded.
        verifySettings(settings);
        return settings;
      }
    } catch (Exception e) {
      logger.log(Level.WARN, "Unable to load a settings file. Creating a default one.", e);
    }
    return new Settings();
  }

  /**
   * Creates a directory if it doesn't exist (only the last one in the provided path).
   * @param path Path to the directory
   */
  private static void createDirIfDoesntExist(StringBuilder path) {
    File dir = new File(path.toString());
    if (!dir.exists()) {
      if (!dir.mkdir()) {
        logger.log(Level.WARN, "Unable to create a directory: " + path);
      }
    }
  }

  /**
   * Tests if settings values are null and sets them to the default values if they
   * are null. This is crucial when a newer application uses an older version of
   * serialized settings object.
   * @param settings settings object to verify
   */
  private static void verifySettings(Settings settings) {
    if (settings.getLafThemeId() == null) {
      settings.setLafThemeId(DEFAULT_LAF_THEME_ID);
    }
    if (settings.getLocaleId() == null) {
      LocaleService localeService = AppRegistry.getInstance().getLocaleService();
      Locale locale = localeService.findLocaleById(Locale.getDefault().toString());
      settings.setLocaleId(locale.toString());
    }
    if (settings.getLastCurrentDirectory() == null) {
      settings.setLastCurrentDirectory(System.getProperty("user.home"));
    }

    if (settings.getGameWindowWidth() < JjDefaults.GAME_TABLE_MIN_WIDTH) {
      settings.setGameWindowWidth(JjDefaults.GAME_TABLE_MIN_WIDTH);
    }
    if (settings.getGameWindowHeight() < JjDefaults.GAME_TABLE_MIN_HEIGHT) {
      settings.setGameWindowHeight(JjDefaults.GAME_TABLE_MIN_HEIGHT);
    }
    if (settings.getEditDialogWidth() < JjDefaults.EDIT_GAME_DIALOG_MIN_WIDTH) {
      settings.setEditDialogWidth(JjDefaults.EDIT_GAME_DIALOG_MIN_WIDTH);
    }
    if (settings.getEditDialogHeight() < JjDefaults.EDIT_GAME_DIALOG_MIN_HEIGHT) {
      settings.setEditDialogHeight(JjDefaults.EDIT_GAME_DIALOG_MIN_HEIGHT);
    }
  }
}
