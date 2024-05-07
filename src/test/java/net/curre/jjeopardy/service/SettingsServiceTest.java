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
import net.curre.jjeopardy.ui.laf.LafThemeId;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/**
 * Tests the settings service.
 *
 * @author Yevgeny Nyden
 */
public class SettingsServiceTest {

  /** Path to the test settings directory. */
  private static final String TEST_SETTINGS_PATH = "target" + File.separatorChar + "test" + File.separatorChar + "settings";

  /** Default test settings last current directory. */
  private static final String DEFAULT_LAST_CURRENT_DIR = "some" + File.separatorChar + "test" + File.separatorChar + "directory";

  /** Default test settings game window height. */
  private static final int DEFAULT_GAME_WINDOW_HEIGHT = 1444;

  /** Default test settings game window width. */
  private static final int DEFAULT_GAME_WINDOW_WIDTH = 1555;

  /** Default test settings edit game window height. */
  private static final int DEFAULT_EDIT_GAME_WINDOW_HEIGHT = 1666;

  /** Default test settings edit game window width. */
  private static final int DEFAULT_EDIT_GAME_WINDOW_WIDTH = 1777;

  /** Default test settings LAF theme ID. */
  private static final LafThemeId DEFAULT_SETTINGS_LAF = LafThemeId.NIMBUS;

  /** Default test settings locale ID. */
  private static final String DEFAULT_LOCAL_ID = "ru_RU";

  /**
   * Reference to the game data service to test on each run.
   */
  private SettingsService testSettingsService;

  /** Absolute path to the default test settings file. */
  private String testSettingsFilePath;

  /** Initializes the state before each test run. */
  @Before
  public void init() {
    // Create default test setting file.
    File testSettingsDir = new File(TEST_SETTINGS_PATH);
    testSettingsDir.mkdirs();
    File testSettingsFile = new File(TEST_SETTINGS_PATH + "testFile.ser");
    this.testSettingsFilePath = testSettingsFile.getAbsolutePath();
    persistTestSettings(this.testSettingsFilePath);
    this.testSettingsService = new SettingsService(this.testSettingsFilePath);
  }

  /** Tests loading initial settings. */
  @Test
  public void testDefaultLoadSettings() {
    Settings settings = this.testSettingsService.getSettings();
    assertEquals("Wrong current directory", DEFAULT_LAST_CURRENT_DIR, settings.getLastCurrentDirectory());
    assertEquals("Wrong game window height", DEFAULT_GAME_WINDOW_HEIGHT, settings.getGameWindowHeight());
    assertEquals("Wrong game window width", DEFAULT_GAME_WINDOW_WIDTH, settings.getGameWindowWidth());
    assertEquals("Wrong edit game window height", DEFAULT_EDIT_GAME_WINDOW_HEIGHT, settings.getEditGameWindowHeight());
    assertEquals("Wrong edit game window width", DEFAULT_EDIT_GAME_WINDOW_WIDTH, settings.getEditGameWindowWidth());
    assertEquals("Wrong LAF theme ID", DEFAULT_SETTINGS_LAF, settings.getLafThemeId());
    assertEquals("Wrong locale ID", DEFAULT_LOCAL_ID, settings.getLocaleId());
  }

  /** Tests loading initial settings. */
  @Test
  public void testPersistSettings() {
    Settings settings = this.testSettingsService.getSettings();

    settings.setLastCurrentDirectory("test/directory/2/");
    settings.setGameWindowHeight(1555);
    settings.setGameWindowWidth(1777);
    settings.setEditGameWindowHeight(1888);
    settings.setEditGameWindowWidth(1999);
    settings.setLafThemeId(LafThemeId.FLAT_DARK);
    settings.setLocaleId("en_US");

    this.testSettingsService.persistSettings();
    assertEquals("Settings should not have changed", settings, this.testSettingsService.getSettings());
    SettingsService settingsService2 = new SettingsService(this.testSettingsFilePath);
    Settings settings2 = settingsService2.getSettings();

    assertEquals("Wrong current directory", "test/directory/2/", settings2.getLastCurrentDirectory());
    assertEquals("Wrong game window height", 1555, settings2.getGameWindowHeight());
    assertEquals("Wrong game window width", 1777, settings2.getGameWindowWidth());
    assertEquals("Wrong edit game window height", 1888, settings2.getEditGameWindowHeight());
    assertEquals("Wrong edit game window width", 1999, settings2.getEditGameWindowWidth());
    assertEquals("Wrong LAF theme ID", LafThemeId.FLAT_DARK, settings2.getLafThemeId());
    assertEquals("Wrong locale ID", "en_US", settings2.getLocaleId());
  }

  /**
   * Creates and persists default test settings file in a default test directory.
   * @param settingsFilePath test settings file path
   */
  private static void persistTestSettings(String settingsFilePath) {
    Settings settings = new Settings();
    settings.setLastCurrentDirectory(DEFAULT_LAST_CURRENT_DIR);
    settings.setGameWindowHeight(DEFAULT_GAME_WINDOW_HEIGHT);
    settings.setGameWindowWidth(DEFAULT_GAME_WINDOW_WIDTH);
    settings.setEditGameWindowHeight(DEFAULT_EDIT_GAME_WINDOW_HEIGHT);
    settings.setEditGameWindowWidth(DEFAULT_EDIT_GAME_WINDOW_WIDTH);
    settings.setLafThemeId(DEFAULT_SETTINGS_LAF);
    settings.setLocaleId(DEFAULT_LOCAL_ID);

    try {
      File file = new File(settingsFilePath);
      FileOutputStream fStream = new FileOutputStream(file);
      ObjectOutputStream oStream = new ObjectOutputStream(fStream);
      oStream.writeObject(settings);
    } catch (Exception e) {
      throw new RuntimeException("Unable to persist default test settings", e);
    }
  }
}
