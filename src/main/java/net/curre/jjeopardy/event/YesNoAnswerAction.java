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

/**
 * Handler for the Yes/No buttons (Correct/Wrong answers).
 * @author Yevgeny Nyden
 */
public class YesNoAnswerAction extends AbstractAction implements KeyListener {

  /** Reference to the main Question dialog UI. */
  private final QuestionDialog questionDialog;

  /** True if this is a Yes (correct answer) action; false if otherwise. */
  private final boolean isYes;

  /** Player's index for which this action is created. */
  private final int playerIndex;

  /**
   * Ctor.
   * @param questionDialog reference to the main question dialog
   * @param playerIndex player's index
   * @param isYes true if this is a Yes (correct answer) action; false if otherwise
   */
  public YesNoAnswerAction(QuestionDialog questionDialog, int playerIndex, boolean isYes) {
    this.questionDialog = questionDialog;
    this.playerIndex = playerIndex;
    this.isYes = isYes;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    this.handleAction();
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  /**
   * Handles the key press event and handles action when Enter is pressed.
   * @param e the event to be processed
   */
  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      this.handleAction();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  /** Handles the action. */
  private void handleAction() {
    this.questionDialog.stopTimer();
    final Registry registry = AppRegistry.getInstance();
    final SoundService soundService = registry.getSoundService();
    if (this.isYes) {
      soundService.startMusic(SoundEnum.getRandomHooray(), 1);
    } else {
      soundService.startMusic(SoundEnum.getRandomBoo(), 1);
    }

    GameDataService dataService = registry.getGameDataService();
    final int cost = (this.isYes ? 1 : -1) * this.questionDialog.getCurrentQuestionCost();
    dataService.addToPlayerScore(this.playerIndex, cost);
    registry.getMainWindow().updateScores();

    if (this.isYes) {
      this.questionDialog.showAnswer();
    }
  }
}
