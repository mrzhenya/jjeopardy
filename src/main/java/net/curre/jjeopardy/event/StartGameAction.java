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

package net.curre.jjeopardy.event;

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.ui.LandingUi;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Logger;

/**
 * Handler for the Start game button.
 *
 * @author Yevgeny Nyden
 */
public class StartGameAction extends AbstractAction implements KeyListener {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(StartGameAction.class.getName());

  /** Reference to the parent Landing UI dialog. */
  private final LandingUi landingUi;

  /**
   * Ctor.
   * @param landingUi reference to the parent Landing UI dialog
   */
  public StartGameAction(LandingUi landingUi) {
    this.landingUi = landingUi;
  }

  /**
   * Handles the Start game action.
   *
   * @param e the event to be processed
   */
  public void actionPerformed(ActionEvent e) {
    this.startGame();
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      this.startGame();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  /**
   * Starts a new game (assuming all data is ready and valid).
   */
  private void startGame() {
    LOGGER.info("Handling the Start Game button action.");
    AppRegistry.getInstance().getSoundService().stopAllMusic();
    landingUi.setVisible(false);

    AppRegistry.getInstance().getMainService().startGame();
  }
}
