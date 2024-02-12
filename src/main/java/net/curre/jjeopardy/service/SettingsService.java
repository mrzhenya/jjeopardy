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

import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.ui.laf.LafService;
import net.curre.jjeopardy.util.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a service bean that assists with
 * handling application settings.
 *
 * @author Yevgeny Nyden
 */
public class SettingsService {

  /** Name of the temp settings file to store some info between the games. */
  private static final String SETTINGS_FILENAME = "jjeopardy-settings.ser";

  /** Directory name where the settings are going to be saved. */
  private static final String SETTINGS_DIR_NAME = "JJeopardy";

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());

  /** Reference to the settings object. */
  private static final Settings settings;

  static {
    // initializing the settings object
    settings = loadSettings();
  }

  /**
   * Returns application settings (current settings).
   * @return Reference to the application settings bean
   */
  public static Settings getSettings() {
    return settings;
  }

  /**
   * Method to load settings stored on disk.
   * @return Settings, loaded from the settings file,
   *         or a new <code>Settings</code> object if no settings file is found
   */
  public static Settings loadSettings() {
    // Try loading the settings file.
    try {
      File file = new File(getVerifiedSettingsFilePath());
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
      LOGGER.log(Level.WARNING, "Unable to load a settings file. Creating a default one.", e);
    }
    return new Settings();
  }

  /**
   * Method to save user settings. Application settings are
   * saved in the place and under the name specified in the AppRegistry bean.
   */
  public static void saveSettings() {
    settings.setLocaleId(Locale.getDefault().toString());

    // Saving the LAF theme.
    settings.setLafThemeId(LafService.getInstance().getCurrentLafThemeId());

    // settings LAF should be already set
    persistSettings();
  }

  /**
   * Method to reset user settings.
   * The settings are recreated and saved.
   * @throws ServiceException If there was an error when resetting settings
   */
  public static void resetSettings() throws ServiceException {
    persistSettings();
  }

  /**
   * Method to persist the current settings.
   */
  public static void persistSettings() {
    try {
      File file = new File(getVerifiedSettingsFilePath());
      FileOutputStream fStream = new FileOutputStream(file);
      ObjectOutputStream oStream = new ObjectOutputStream(fStream);
      oStream.writeObject(settings);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to save the settings!", e);
    }
  }

  /**
   * Saves the last known current directory in the settings.
   * @param absolutePath last known current directory
   */
  public static void saveLastCurrentDirectory(String absolutePath) {
    settings.setLastCurrentDirectory(absolutePath);
    SettingsService.persistSettings();
  }

  /**
   * Saves the main game window size in the settings if it's not
   * smaller than the default game table size.
   * @param width the table width
   * @param height the table height
   */
  public static void updateMainWindowSize(int width, int height) {
    if (width >= LafService.DEFAULT_GAME_TABLE_WIDTH) {
      settings.setMainFrameWidth(width);
    }
    if (height >= LafService.GAME_TABLE_MIN_ROW_HEIGHT) {
      settings.setMainFrameHeight(height);
    }
    SettingsService.persistSettings();
  }

  /**
   * Returns a platform specific absolute path to the settings file including
   * the file name. All custom directories in the path that don't exist,
   * will be created.
   * @return Absolute path to the settings file including the file name
   */
  private static String getVerifiedSettingsFilePath() {
    StringBuilder path = new StringBuilder();
    switch (Utilities.getPlatformType()) {
      case MAC_OS:
        path.append(System.getProperties().getProperty("user.home")).
          append(File.separatorChar).append("Library").
          append(File.separatorChar).append("Application Support");
        break;
      case WINDOWS:
        path.append(System.getProperties().getProperty("user.home")).
          append(File.separatorChar).append("AppData").
          append(File.separatorChar).append("Local").
          append(File.separatorChar).append("Temp");
        break;
      default:
        path.append(File.separatorChar).append("temp");
    }
    // Settings file is nested in the game directory.
    path.append(File.separatorChar).append(SETTINGS_DIR_NAME);
    createDirIfDoesntExist(path);
    path.append(File.separatorChar).append(SETTINGS_FILENAME);
    return path.toString();
  }

  /**
   * Creates a directory if it doesn't exist (only the last one in the provided path).
   * @param path Path to the directory
   */
  private static void createDirIfDoesntExist(StringBuilder path) {
    File dir = new File(path.toString());
    if (!dir.exists()) {
      dir.mkdir();
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
      settings.setLafThemeId(LafService.getInstance().getDefaultLafThemeId());
    }
    if (settings.getLocaleId() == null) {
      Locale locale = LocaleService.findLocaleById(Locale.getDefault().toString());
      settings.setLocaleId(locale.toString());
    }
    if (settings.getLastCurrentDirectory() == null) {
      settings.setLastCurrentDirectory(System.getProperty("user.home"));
    }
  }
}
