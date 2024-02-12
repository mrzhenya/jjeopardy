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

import net.curre.jjeopardy.ui.game.MainWindow;

import java.util.logging.Logger;

/**
 * This service bean is responsible for
 * handling various general tasks such as quitting, printing, etc.
 *
 * @author Yevgeny Nyden
 */
public class MainService {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(MainService.class.getName());

  /** Private constructor to prevent instantiation. */
  private MainService() {}

  /**
   * Starts a new game.
   */
  public static void startGame() {
    LOGGER.info("Starting a new game...");

    // Create the main window if it doesn't exist.
    Registry registry = AppRegistry.getInstance();
    MainWindow mainWindow = registry.getMainWindow();
    if (mainWindow == null) {
      mainWindow = new MainWindow();
      registry.setMainWindow(mainWindow);
    }
    AppRegistry.getInstance().getGameDataService().resetPlayerScores();

    mainWindow.prepareGame();
    mainWindow.pack();
    mainWindow.setLocationRelativeTo(null);
    mainWindow.setVisible(true);
  }

  /** Disposes all frames and quits the application. */
  public static void quitApp() {
    LOGGER.info("Handling application exit...");
    System.exit(0);
  }
}
