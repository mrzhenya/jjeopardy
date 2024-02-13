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

/**
 * Represents a registry interface to set and retrieve application
 * services.
 *
 * @author Yevgeny Nyden
 */
public interface Registry {

  /**
   * Getter for the main window reference.
   * @return The reference to the main window object
   */
  MainWindow getMainWindow();

  /**
   * Setter for the main window reference.
   * @param mainWindow Reference to the main window object
   */
  void setMainWindow(MainWindow mainWindow);

  /**
   * Gets the game data service.
   * @return a reference to the game data service
   */
  GameDataService getGameDataService();

  /**
   * Gets the game settings service.
   * @return a reference to the game settings service
   */
  SettingsService getSettingsService();

  /**
   * Gets a reference to the game data.
   * @return a reference to the game data
   */
  GameData getGameData();

  /**
   * Gets a reference to the question dialog.
   * @return a reference to the question dialog
   */
  QuestionDialog getQuestionDialog();

  /**
   * Sets the reference to the question dialog.
   * @param questionDialog reference to the question dialog
   */
  void setQuestionDialog(QuestionDialog questionDialog);

  /**
   * Gets a reference to the main Landing UI.
   * @return a reference to the main Landing UI
   */
  LandingUi getLandingUi();

  /**
   * Sets the reference to the main Landing UI.
   * @param landingUi reference to the main landing UI
   */
  void setLandingUi(LandingUi landingUi);
}
