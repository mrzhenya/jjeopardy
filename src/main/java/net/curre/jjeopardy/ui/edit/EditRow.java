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

package net.curre.jjeopardy.ui.edit;

import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

/**
 * Represents a row in the game edit table.
 *
 * @author Yevgeny Nyden
 */
public class EditRow extends JPanel {

  /** Edit row border width. */
  protected static final int BORDER_WIDTH = 1;

  /** Ordered list of cells for this row. */
  private final ArrayList<EditCell> cells;

  /** Current row height. */
  private int rowHeight;

  /**
   * Ctor.
   * @param rowIndex row's index
   * @param editTable reference to the edit table
   */
  public EditRow(int rowIndex, EditTable editTable) {
    this.rowHeight = 0;

    this.setLayout(new GridLayout(1, 0));
    this.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, 0, BORDER_WIDTH));

    GameData gameData = editTable.getGameData();
    this.cells = new ArrayList<>();
    for (int columnIndex = 0; columnIndex < gameData.getCategoriesCount(); columnIndex++) {
      Category category = gameData.getCategories().get(columnIndex);
      EditCell cell = new EditCell(category.getQuestion(rowIndex), editTable);
      cell.setColumnAndRowIndexes(columnIndex, rowIndex);
      this.cells.add(cell);
      this.add(cell);
    }
  }

  /** Gets the current row height. */
  public int getRowHeight() {
    return this.rowHeight;
  }

  /**
   * Updates the size of all row cells. The height is determined based on the tallest cell.
   * @param columnWidth total width of a column
   * @param rowWidth all available row width
   * @return the determined row height
   */
  public int refreshAndResize(int columnWidth, int rowWidth) {
    int maxHeight = 0;
    for (EditCell cell : this.cells) {
      int height = cell.refreshAndResize(columnWidth);
      if (height > maxHeight) {
        maxHeight = height;
      }
    }
    this.setRowSize(rowWidth, maxHeight);
    for (EditCell cell : this.cells) {
      cell.maximizeImages(columnWidth, maxHeight);
    }
    return maxHeight;
  }

  /**
   * Sets the row preferred size.
   * @param width row width
   * @param height row height
   */
  public void setRowSize(int width, int height) {
    this.rowHeight = height;
    Dimension size = new Dimension(width, height);
    super.setPreferredSize(size);
    super.setSize(size);
  }

  /** Activates the row's view style/presentation. */
  protected void activateViewStyle() {
    for (EditCell cell : this.cells) {
      cell.activateViewStyle();
    }
  }

  /** Activates the row's print style/presentation. */
  protected void activatePrintStyle() {
    for (EditCell cell : this.cells) {
      cell.activatePrintStyle();
    }
  }
}
