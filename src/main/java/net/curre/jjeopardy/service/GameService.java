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
import net.curre.jjeopardy.ui.game.GameWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingUtilities;

/**
 * This service bean is responsible for handling game tasks such as
 * starting a new game, restarting a game, and other active game UI related tasks.<br><br>
 *
 * Game data tasks (such loading a library game, keeping scores, etc.) are handled in the
 * <code>GameDataService</code>.
 * <br><br>
 * An instance of this service object should be obtained from the AppRegistry.
 *
 * @see GameDataService
 * @author Yevgeny Nyden
 */
public class GameService {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Reference to the main window. */
  private GameWindow gameWindow;

  /** Private constructor to prevent instantiation. */
  protected GameService() {}

  /** Starts a new game. */
  public void startGame() {
    logger.info("Starting a new game...");

    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().resetPlayerScores();

    GameWindow oldGameWindow = null;
    if (this.gameWindow != null) {
      oldGameWindow = this.gameWindow;
    }

    this.gameWindow = new GameWindow();
    this.gameWindow.prepareAndStartGame();

    // Dispose the old window UI after a new game started.
    if (oldGameWindow != null) {
      SwingUtilities.invokeLater(oldGameWindow::dispose);
    }
  }

  /** Restarts an already loaded in the GameWindow game. */
  public void restartGame() {
    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().resetPlayerScores();
    this.getGameWindow().prepareAndStartGame();
  }

  /** Updates the players' scores. */
  public void updateScores() {
    this.gameWindow.updateScores();
  }

  /**
   * Determines if the actions are enabled on the main game window.
   * @return true if the actions are disabled; false if otherwise
   */
  public boolean gameActionsDisabled() {
    return this.getGameWindow().actionsDisabled();
  }

  /**
   * Enables or disabled actions on the main game window.
   * @param isEnabled true if the actions should be enabled; false if otherwise
   */
  public void setGameActionsEnabled(boolean isEnabled) {
    if (isEnabled) {
      this.getGameWindow().enableActions();
    } else {
      this.getGameWindow().disableActions();
    }
  }

  /**
   * Getter for the main game window reference.
   * @return The reference to the game window object
   */
  private GameWindow getGameWindow() {
    if (this.gameWindow == null) {
      logger.error("Game window is not created yet, call startGame");
      throw new RuntimeException("The game window is not ready, start the game first.");
    }
    return this.gameWindow;
  }
}
