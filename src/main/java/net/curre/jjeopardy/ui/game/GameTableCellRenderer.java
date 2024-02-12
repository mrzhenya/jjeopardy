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

package net.curre.jjeopardy.ui.game;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

/**
 * Represents a Game table cell.
 *
 * @see GameTableHeaderRenderer
 * @author Yevgeny Nyden
 */
public class GameTableCellRenderer extends TableCell implements TableCellRenderer {

  /** Reference to the table data model. */
  private final GameTableModel model;

  /** Hovered cell row. */
  private static int hoveredCellRow = -1;

  /** Hovered cell column. */
  private int hoveredCellColumn = -1;

  /**
   * Ctor.
   * @param model reference to the game table model
   */
  public GameTableCellRenderer(GameTableModel model) {
    this.model = model;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {
    if (this.model.isCellEmpty(row, column)) {
      this.setToEmptyState();
    } else if (hoveredCellRow == row && hoveredCellColumn == column) {
      this.setToHoveredState();
    } else {
      this.setToDefaultState();
    }
    this.setValue(value);

    return this;
  }

  /**
   * Sets the currently hovered cell.
   * @param row row of the hovered cell
   * @param column column of the hovered cell
   * @return true if the hovered cell location has changed; false if otherwise
   */
  public boolean setCellHovered(int row, int column) {
    if (hoveredCellRow == row && hoveredCellColumn == column) {
      return false;
    }
    hoveredCellRow = row;
    hoveredCellColumn = column;
    return true;
  }
}
