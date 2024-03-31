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
import net.curre.jjeopardy.ui.game.GameTable;
import net.curre.jjeopardy.ui.game.GameTableCellRenderer;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Game table mouse event handler.
 * This class is responsible for highlighting and "graying out" the cells.
 *
 * @author Yevgeny Nyden
 */
public class GameTableMouseMotionListener extends MouseAdapter implements MouseMotionListener {

  /** Reference to the game table. */
  private final GameTable gameTable;

  /**
   * Ctor.
   * @param gameTable reference to the game table
   */
  public GameTableMouseMotionListener(GameTable gameTable) {
    this.gameTable = gameTable;
  }

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
}
