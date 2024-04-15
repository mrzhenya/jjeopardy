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
import javax.validation.constraints.NotNull;
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

  /** Row padding. */
  private static final int PADDING = EditRow.BORDER_WIDTH;

  /** Ordered list of cells for this row. */
  private final ArrayList<EditCell> cells;

  /** Current row height. */
  private int rowHeight;

  /**
   * Ctor.
   * @param rowIndex row's index
   * @param editTable reference to the edit table
   */
  public EditRow(int rowIndex, @NotNull EditTable editTable) {
    this.rowHeight = 0;

    this.setLayout(new GridLayout(1, 0));
    this.setBorder(new EmptyBorder(PADDING, PADDING, 0, PADDING));

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
   * Removes a cell from the row.
   * @param removeInd index of the cell to remove
   */
  protected void removeCell(int removeInd) {
    EditCell cell = this.cells.remove(removeInd);
    this.remove(cell);
    this.updateIndexesAndOverlays();
  }

  /**
   * Moves a cell in the row.
   * @param cellInd index of the cell to move
   * @param toRight true if the index of the cell should be increased; false if decreased
   */
  protected void moveCell(int cellInd, boolean toRight) {
    final int newInd = cellInd + (toRight ? 1 : -1);
    EditCell cell = this.cells.remove(cellInd);
    this.cells.add(newInd, cell);

    this.remove(cell);
    this.add(cell, newInd);
    this.updateIndexesAndOverlays();
  }

  /**
   * Updates the size of all row cells. The height is determined based on the tallest cell.
   * @param columnWidth total width of a column
   * @param rowWidth all available row width
   * @return the determined row height
   */
  protected int refreshAndResize(int columnWidth, int rowWidth) {
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
  protected void setRowSize(int width, int height) {
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

  /**
   * Updates the relative index of each cell and their overlays. Depending on the
   * position of the cell, some move buttons will be disabled.
   */
  private void updateIndexesAndOverlays() {
    final int cellCount = this.cells.size();
    for (int ind = 0; ind < cellCount; ind++) {
      this.cells.get(ind).updateColumnIndex(ind);
    }
  }
}
