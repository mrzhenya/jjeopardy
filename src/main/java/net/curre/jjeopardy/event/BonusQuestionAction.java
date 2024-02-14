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
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SoundService;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.dialog.QuestionDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for the Bonus questions button.
 *
 * @author Yevgeny Nyden
 */
public class BonusQuestionAction extends AbstractAction implements KeyListener {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(BonusQuestionAction.class.getName());

  /** Reference to the parent Question dialog. */
  private final QuestionDialog questionDialog;

  /** True if this is a Yes (correct answer) action; false if otherwise. */
  private final boolean isYes;

  /**
   * Ctor.
   * @param questionDialog reference to the parent question dialog
   * @param isYes true if this is a Yes (correct answer) action; false if otherwise
   */
  public BonusQuestionAction(QuestionDialog questionDialog, boolean isYes) {
    this.questionDialog = questionDialog;
    this.isYes = isYes;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    this.performAction();
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      this.performAction();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}

  /** Performs the action. */
  private void performAction() {
    final SoundService soundService = AppRegistry.getInstance().getSoundService();
    if (this.isYes) {
      soundService.startMusic(SoundEnum.getRandomHooray(), 1);
    } else {
      soundService.startMusic(SoundEnum.getRandomBoo(), 1);
    }

    final Registry registry = AppRegistry.getInstance();
    final int cost = (this.isYes ? 1 : -1) * this.questionDialog.getCurrentQuestionCost();
    GameDataService dataService = registry.getGameDataService();
    dataService.addToPlayerScore(this.questionDialog.getCurrentBonusPlayerIndex(), cost);
    registry.getMainWindow().updateScores();
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      LOGGER.log(Level.WARNING, "Sleeping thread was interrupted", e);
    }

    this.questionDialog.continueAskingBonusQuestions();
  }
}
