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
import net.curre.jjeopardy.ui.game.GameTable;
import net.curre.jjeopardy.ui.game.GameTableCellRenderer;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Handler for various game table mouse actions (clicking on cells, moving over cells).
 * This class is responsible for highlighting and "graying out" the cells.
 *
 * @author Yevgeny Nyden
 */
public class GameTableMouseListener extends MouseAdapter implements MouseListener, MouseMotionListener {

  /** Reference to the main Game table UI. */
  private final GameTable gameTable;

  /**
   * Ctor.
   * @param gameTable reference to the game table
   */
  public GameTableMouseListener(GameTable gameTable) {
    this.gameTable = gameTable;
  }

  /** Does nothing. */
  @Override
  public void mouseClicked(MouseEvent e) {}

  /** Does nothing. */
  @Override
  public void mousePressed(MouseEvent e) {}

  /**
   * Opens the question dialog for the cell over which the mouse is released if
   * that cell contains a question that has not been asked yet.<br><br>
   *
   * @inheritDoc
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    if (AppRegistry.getInstance().getGameService().gameActionsDisabled()) {
      return;
    }
    Point p = e.getPoint();
    int row = this.gameTable.rowAtPoint(p);
    int column = this.gameTable.columnAtPoint(p);
    if (row >= 0 && column >= 0) {
      maybeOpenQuestionDialog(column, row);
    }
  }

  /** Does nothing. */
  @Override
  public void mouseEntered(MouseEvent e) {}

  /**
   * If the mouse moved into a cell with a non-asked question, highlights the cell.<br><br>
   *
   * @inheritDoc
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    if (AppRegistry.getInstance().getGameService().gameActionsDisabled()) {
      return;
    }
    Point p = e.getPoint();
    int row = this.gameTable.rowAtPoint(p);
    int column = this.gameTable.columnAtPoint(p);

    GameTableCellRenderer cellRenderer = (GameTableCellRenderer) this.gameTable.getCellRenderer(row, column);
    if (cellRenderer.setCellHovered(row, column)) {
      // Repainting the table if the hovered cell changed.
      this.gameTable.repaint();
    }
  }

  /**
   * Clears highlighted cell when it exited.<br><br>
   *
   * @inheritDoc
   */
  @Override
  public void mouseExited(MouseEvent e) {
    if (AppRegistry.getInstance().getGameService().gameActionsDisabled()) {
      return;
    }
    // Clearing out the currently hovered cell state and repainting the table.
    GameTableCellRenderer cellRenderer = (GameTableCellRenderer) this.gameTable.getCellRenderer(0, 0);
    cellRenderer.setCellHovered(-1, -1);
    this.gameTable.repaint();
  }

  /**
   * Displays the Question dialog for a given question if it's askable and if it hasn't been asked yet.
   * @param catIndex question category index
   * @param questIndex question index
   */
  private void maybeOpenQuestionDialog(int catIndex, int questIndex) {
    final Registry registry = AppRegistry.getInstance();
    GameDataService dataService = registry.getGameDataService();
    final Question question = dataService.getQuestion(catIndex, questIndex);
    if (!question.isHasBeenAsked() && !question.isNotAskable()) {
      AppRegistry.getInstance().getSoundService().stopAllMusic();

      // marking the questions answered on the game board
      question.setHasBeenAsked();
      this.gameTable.refreshAndResizeTable();

      // opening the question dialog
      this.gameTable.openQuestionDialogForQuestion(question);
    }
  }
}
