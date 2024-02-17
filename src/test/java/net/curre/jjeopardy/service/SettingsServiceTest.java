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
  private static final String SETTINGS_PATH = "target/test/settings/";

  /** Default test settings last current directory. */
  private static final String DEFAULT_LAST_CURRENT_DIR = "some/test/directory/";

  /** Default test settings main frame height. */
  private static final int DEFAULT_MAIN_FRAME_HEIGHT = 444;

  /** Default test settings main frame width. */
  private static final int DEFAULT_MAIN_FRAME_WIDTH = 555;

  /** Default test settings LAF theme ID. */
  private static final LafThemeId DEFAULT_MAIN_FRAME_LAF = LafThemeId.NIMBUS;

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
    File testSettingsDir = new File(SETTINGS_PATH);
    testSettingsDir.mkdirs();
    File testSettingsFile = new File(SETTINGS_PATH + "testFile.ser");
    this.testSettingsFilePath = testSettingsFile.getAbsolutePath();
    persistTestSettings(this.testSettingsFilePath);
    this.testSettingsService = new SettingsService(this.testSettingsFilePath);
  }

  /** Tests loading initial settings. */
  @Test
  public void testDefaultLoadSettings() {
    Settings settings = this.testSettingsService.getSettings();
    assertEquals("Wrong current directory", DEFAULT_LAST_CURRENT_DIR, settings.getLastCurrentDirectory());
    assertEquals("Wrong main frame height", DEFAULT_MAIN_FRAME_HEIGHT, settings.getGameWindowHeight());
    assertEquals("Wrong main frame width", DEFAULT_MAIN_FRAME_WIDTH, settings.getGameWindowWidth());
    assertEquals("Wrong LAF theme ID", DEFAULT_MAIN_FRAME_LAF, settings.getLafThemeId());
    assertEquals("Wrong locale ID", DEFAULT_LOCAL_ID, settings.getLocaleId());
  }

  /** Tests loading initial settings. */
  @Test
  public void testPersistSettings() {
    Settings settings = this.testSettingsService.getSettings();

    settings.setLastCurrentDirectory("test/directory/2/");
    settings.setGameWindowHeight(345);
    settings.setGameWindowWidth(456);
    settings.setLafThemeId(LafThemeId.FLAT_DARK);
    settings.setLocaleId("en_US");

    this.testSettingsService.persistSettings();
    assertEquals("Settings should not have changed", settings, this.testSettingsService.getSettings());
    SettingsService settingsService2 = new SettingsService(this.testSettingsFilePath);
    Settings settings2 = settingsService2.getSettings();

    assertEquals("Wrong current directory", "test/directory/2/", settings2.getLastCurrentDirectory());
    assertEquals("Wrong main frame height", 345, settings2.getGameWindowHeight());
    assertEquals("Wrong main frame width", 456, settings2.getGameWindowWidth());
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
    settings.setGameWindowHeight(DEFAULT_MAIN_FRAME_HEIGHT);
    settings.setGameWindowWidth(DEFAULT_MAIN_FRAME_WIDTH);
    settings.setLafThemeId(DEFAULT_MAIN_FRAME_LAF);
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
