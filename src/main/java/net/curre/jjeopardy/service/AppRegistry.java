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

import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.ui.LandingUi;
import net.curre.jjeopardy.ui.dialog.QuestionDialog;
import net.curre.jjeopardy.ui.game.MainWindow;

import java.util.logging.Logger;

/**
 * This is the central place for most of the app UI and data resources.
 *
 * @author Yevgeny Nyden
 */
public class AppRegistry implements Registry {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(AppRegistry.class.getName());

  /** Reference to the singelton instance of this class. */
  private static Registry instance;

  /** Reference to the main window. */
  private MainWindow mainWindow;

  /** Reference to the game data service. */
  private final GameDataService gameDataService;

  /** Reference to the game settings service. */
  private final SettingsService settingsService;

  /** Reference to the locale service. */
  private final LocaleService localeService;

  /** Reference to the question dialog. */
  private QuestionDialog questionDialog;

  /** Reference to the main landing UI. */
  private LandingUi landingUi;

  /**
   * Returns the singleton instance of this class.
   * @return The singleton instance of this class
   */
  public static Registry getInstance() {
    if (instance == null) {
      throw new RuntimeException("App registry is not initialized!");
    }
    return instance;
  }

  /**
   * Initializes the static registry reference to either a new
   * instance of this class (default) or a passed registry reference (e.g. test).
   * @param registryOrNull reference to the (test) registry or null to initialize the default registry
   */
  public static void initialize(Registry registryOrNull) {
    LOGGER.info("Initializing registry with a " +
      (registryOrNull == null ? "default" : "passed") + " one.");
    instance = registryOrNull == null ? new AppRegistry() : registryOrNull;
  }

  /**
   * Private constructor. Sets settingsFilePath with
   * the result of the getSettingsFilePathHelper() method.
   */
  public AppRegistry() {
    this.gameDataService = new GameDataService();
    this.settingsService = new SettingsService(null);
    this.localeService = new LocaleService();
  }

  /**
   * Getter for the main window reference.
   * @return The reference to the main window object
   */
  public MainWindow getMainWindow() {
    return this.mainWindow;
  }

  /**
   * Setter for the main window reference.
   * @param mainWindow Reference to the main window object
   */
  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  /**
   * Gets the game data service.
   * @return a reference to the game data service
   */
  public GameDataService getGameDataService() {
    return this.gameDataService;
  }

  /**
   * Gets the game settings service.
   * @return a reference to the game settings service
   */
  public SettingsService getSettingsService() {
    return this.settingsService;
  }

  /**
   * Gets the locale service.
   * @return a reference to the locale service
   */
  public LocaleService getLocaleService() {
    return this.localeService;
  }

  /**
   * Gets a reference to the game data.
   * @return a reference to the game data
   */
  public GameData getGameData() {
    return this.gameDataService.getGameData();
  }

  /**
   * Gets a reference to the question dialog.
   * @return a reference to the question dialog
   */
  public QuestionDialog getQuestionDialog() {
    return this.questionDialog;
  }

  /**
   * Sets the reference to the question dialog.
   * @param questionDialog reference to the question dialog
   */
  public void setQuestionDialog(QuestionDialog questionDialog) {
    this.questionDialog = questionDialog;
  }

  /**
   * Gets a reference to the main Landing UI.
   * @return a reference to the main Landing UI
   */
  public LandingUi getLandingUi() {
    return this.landingUi;
  }

  /**
   * Sets the reference to the main Landing UI.
   * @param landingUi reference to the main landing UI
   */
  public void setLandingUi(LandingUi landingUi) {
    this.landingUi = landingUi;
  }
}
