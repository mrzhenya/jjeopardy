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

import net.curre.jjeopardy.ui.landing.LandingUi;

/**
 * Represents a registry interface to set and retrieve application
 * services and main UI components.
 *
 * @author Yevgeny Nyden
 */
public interface Registry {

  /**
   * Gets the game service (to handle tasks like starting a new game,
   * game restart, and other active game tasks).
   * @return a reference to the game service
   */
  GameService getGameService();

  /**
   * Gets the game data service. This service is responsible for loading and handling
   * game data - categories, questions, players.
   * @return a reference to the game data service
   */
  GameDataService getGameDataService();

  /**
   * Gets the game settings service to handle game settings (that are stored to disk).
   * @return a reference to the game settings service
   */
  SettingsService getSettingsService();

  /**
   * Gets the locale service.
   * @return reference to the locale service.
   */
  LocaleService getLocaleService();

  /**
   * Gets the sound service, which is responsible for playing sound clips for the app.
   * @return reference to the sound service
   */
  SoundService getSoundService();

  /**
   * Gets the UI service, which is responsible for common UI tasks like opening dialogs.
   * @return reference to the UI service
   */
  UiService getUiService();

  /**
   * Gets the service responsible for common Look and Feel support.
   * @return reference to the LAF service
   */
  LafService getLafService();

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
