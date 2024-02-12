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

import net.curre.jjeopardy.ui.LandingUi;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Logger;

/**
 * Handler for the Add/Update action.
 *
 * @author Yevgeny Nyden
 */
public class UpdatePlayersAction extends AbstractAction implements KeyListener {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(UpdatePlayersAction.class.getName());

  /** Reference to the parent Landing UI dialog. */
  private final LandingUi landingUi;

  /**
   * Ctor.
   * @param landingUi reference to the parent Landing UI dialog
   */
  public UpdatePlayersAction(LandingUi landingUi) {
    this.landingUi = landingUi;
  }

  /**
   * Handles the Add/Update action.
   * @param e the event to be processed
   */
  public void actionPerformed(ActionEvent e) {
    this.showPlayerDialog();
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      this.showPlayerDialog();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}

  /**
   * Displays the player dialog UI.
   */
  private void showPlayerDialog() {
    LOGGER.info("Handling the Update Players button action.");
    landingUi.showPlayerDialog();
  }
}
