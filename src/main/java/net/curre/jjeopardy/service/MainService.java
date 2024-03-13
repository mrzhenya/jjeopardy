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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This service bean is responsible for handling various general tasks such as quitting,
 * starting a new game, and other misc. tasks that don't have its own service.
 *
 * @author Yevgeny Nyden
 */
public class MainService {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Private constructor to prevent instantiation. */
  protected MainService() {}

  /**
   * Starts a new game.
   */
  public void startGame() {
    logger.info("Starting a new game...");

    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().resetPlayerScores();
    registry.getMainWindow().prepareAndStartGame();
  }

  /** Disposes all frames and quits the application. */
  public void quitApp() {
    logger.info("Handling application exit...");
    System.exit(0);
  }
}
