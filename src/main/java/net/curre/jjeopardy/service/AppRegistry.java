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
import net.curre.jjeopardy.ui.landing.LandingUi;
import net.curre.jjeopardy.ui.dialog.QuestionDialog;
import net.curre.jjeopardy.ui.game.MainWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the central registry place for most of the app UI frames/dialogs and service objects.
 *
 * @author Yevgeny Nyden
 */
public class AppRegistry implements Registry {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Reference to the singleton instance of this class. */
  private static Registry instance;

  /** Reference to the main service. */
  private final MainService mainService;

  /** Reference to the game data service. */
  private final GameDataService gameDataService;

  /** Reference to the game settings service. */
  private final SettingsService settingsService;

  /** Reference to the locale service. */
  private final LocaleService localeService;

  /** Reference to the sound service. */
  private final SoundService soundService;

  /** Reference to the UI service. */
  private final UiService uiService;

  /** Reference to the Look and Feel service. */
  private final LafService lafService;

  /** Reference to the main landing UI. */
  private LandingUi landingUi;

  /** Reference to the main window. */
  private MainWindow mainWindow;

  /** Reference to the question dialog. */
  private QuestionDialog questionDialog;

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
    logger.info("Initializing registry with a " +
      (registryOrNull == null ? "default" : "passed") + " one.");
    instance = registryOrNull == null ? new AppRegistry() : registryOrNull;
  }

  /**
   * Private constructor. Sets settingsFilePath with
   * the result of the getSettingsFilePathHelper() method.
   */
  public AppRegistry() {
    this.mainService = new MainService();
    this.gameDataService = new GameDataService();
    this.settingsService = new SettingsService(null);
    this.localeService = new LocaleService();
    this.soundService = new SoundService();
    this.uiService = new UiService();
    this.lafService = new LafService();
  }

  /** {@inheritDoc} */
  @Override
  public MainService getMainService() {
    return this.mainService;
  }

  /** {@inheritDoc} */
  @Override
  public GameDataService getGameDataService() {
    return this.gameDataService;
  }

  /** {@inheritDoc} */
  @Override
  public SettingsService getSettingsService() {
    return this.settingsService;
  }

  /** {@inheritDoc} */
  @Override
  public LocaleService getLocaleService() {
    return this.localeService;
  }

  /** {@inheritDoc} */
  @Override
  public SoundService getSoundService() {
    return this.soundService;
  }

  /** {@inheritDoc} */
  @Override
  public UiService getUiService() {
    return this.uiService;
  }

  /** {@inheritDoc} */
  @Override
  public LafService getLafService() {
    return this.lafService;
  }

  /** {@inheritDoc} */
  @Override
  public LandingUi getLandingUi() {
    return this.landingUi;
  }

  /** {@inheritDoc} */
  @Override
  public void setLandingUi(LandingUi landingUi) {
    this.landingUi = landingUi;
  }

  /** {@inheritDoc} */
  @Override
  public MainWindow getMainWindow() {
    synchronized (this) {
      // Lazy initialize the main game window UI when requested.
      if (this.mainWindow == null) {
        this.mainWindow = new MainWindow();
      }
    }
    return this.mainWindow;
  }

  /** {@inheritDoc} */
  @Override
  public QuestionDialog getQuestionDialog() {
    return this.questionDialog;
  }

  /** {@inheritDoc} */
  @Override
  public void setQuestionDialog(QuestionDialog questionDialog) {
    this.questionDialog = questionDialog;
  }
}
