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

import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SoundService;
import net.curre.jjeopardy.ui.game.GameTable;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Handler for various game table actions.
 *
 * @author Yevgeny Nyden
 */
public class GameTableListener implements ComponentListener, MouseListener {

  /** Reference to the main Game table UI. */
  private final GameTable gameTable;

  public GameTableListener(GameTable gameTable) {
    this.gameTable = gameTable;
  }

  @Override
  public void componentResized(ComponentEvent e) {
    this.gameTable.resizeAndRefreshTable();
  }

  @Override
  public void componentMoved(ComponentEvent e) {}

  @Override
  public void componentShown(ComponentEvent e) {}

  @Override
  public void componentHidden(ComponentEvent e) {}

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {
    Point p = e.getPoint();
    int row = this.gameTable.rowAtPoint(p);
    int column = this.gameTable.columnAtPoint(p);
    if (row >= 0 && column >= 0) {
      openQuestionDialog(column, row);
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  /**
   * Displays the Question dialog for a given question.
   * @param catIndex question category index
   * @param questIndex question index
   */
  private void openQuestionDialog(int catIndex, int questIndex) {
    final Registry registry = AppRegistry.getInstance();
    GameDataService dataService = registry.getGameDataService();
    final Question question = dataService.getQuestion(catIndex, questIndex);
    if (!question.isHasBeenAsked()) {
      SoundService.getInstance().stopAllMusic();

      // marking the questions answered on the game board
      question.setHasBeenAsked();
      this.gameTable.resizeAndRefreshTable();

      // opening the question dialog
      registry.getQuestionDialog().askQuestion(question, false);
    }
  }
}
