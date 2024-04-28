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
import net.curre.jjeopardy.util.JjDefaults;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.validation.constraints.NotNull;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game edit table header to display game categories.
 *
 * @author Yevgeny Nyden
 */
public class EditHeaderRow extends JPanel {

  /** Minimum header height (in px). */
  private static final int MIN_HEIGHT = 50;

  /** Header padding. */
  private static final int PADDING = EditRow.BORDER_WIDTH;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Ordered list of header cells. */
  private final ArrayList<EditHeaderCell> headerCells;

  /** Current row height. */
  private int rowHeight;

  /**
   * Ctor.
   * @param categories an ordered list of categories to create a table header for
   * @param editTable reference to the edit table; not nullable
   */
  public EditHeaderRow(@NotNull List<Category> categories, EditTable editTable) {
    this.editTable = editTable;
    this.headerCells = new ArrayList<>();
    this.rowHeight = 0;

    // Items will flow left to right and will take all available panel height.
    this.setLayout(new GridLayout(1, 0));
    this.setBorder(new EmptyBorder(PADDING, PADDING, 0, PADDING));

    int ind = 0;
    for (Category category : categories) {
      EditHeaderCell cell = new EditHeaderCell(category.getName(), ind++, editTable);
      this.headerCells.add(cell);
      this.add(cell);
    }
    this.activateViewStyle();
  }

  /**
   * Gets the headers height.
   * @return the header height
   */
  public int getHeaderHeight() {
    return this.rowHeight;
  }

  /**
   * Sets the Add Category button enabled or disabled.
   * @param enabled true if the button should be enabled
   */
  public void setAddCategoryEnabled(boolean enabled) {
    for (EditHeaderCell cell : this.headerCells) {
      cell.setAddCategoryEnabled(enabled);
    }
  }

  /**
   * Sets the Remove Category button enabled or disabled.
   * @param enabled true if the button should be enabled
   */
  public void setRemoveCategoryEnabled(boolean enabled) {
    for (EditHeaderCell cell : this.headerCells) {
      cell.setRemoveCategoryEnabled(enabled);
    }
  }

  /**
   * Removes a header cell from the header.
   * @param removeInd index of the cell to remove
   */
  protected void removeCell(int removeInd) {
    EditHeaderCell cell = this.headerCells.remove(removeInd);
    this.remove(cell);
    this.updateIndexesAndActions();
  }

  /**
   * Adds a header cell ad the given index and shifts cells to the right.
   * @param ind index at which a cell needs to be added
   * @param categoryName category name for the column text
   */
  protected void addCell(int ind, String categoryName) {
    EditHeaderCell cell = new EditHeaderCell(categoryName, ind, this.editTable);
    this.headerCells.add(ind, cell);
    this.add(cell, ind);
    this.updateIndexesAndActions();
  }

  /**
   * Moves a header cell in the header row.
   * @param cellInd index of the cell to move
   * @param toRight true if the index of the cell should be increased; false if decreased
   */
  protected void moveCell(int cellInd, boolean toRight) {
    final int newInd = cellInd + (toRight ? 1 : -1);
    EditHeaderCell cell = this.headerCells.remove(cellInd);
    this.headerCells.add(newInd, cell);

    this.remove(cell);
    this.add(cell, newInd);
    this.updateIndexesAndActions();
  }

  /**
   * Updates the size of all row cells. The height is determined based on the tallest cell.
   * @param columnWidth total width of a column
   * @param rowWidth all available row width
   * @return the determined row height
   */
  protected int refreshAndResize(int columnWidth, int rowWidth) {
    int maxHeight = 0;
    // First, find the tallest cell to determine the maximum height of the row.
    for (EditHeaderCell cell : this.headerCells) {
      int height = cell.getPreferredCellHeight(columnWidth);
      if (height > maxHeight) {
        maxHeight = height;
      }
    }
    // Row has a minimum height.
    if (maxHeight < MIN_HEIGHT) {
      maxHeight = MIN_HEIGHT;
    }

    this.rowHeight = maxHeight;
    this.setPreferredSize(new Dimension(rowWidth, maxHeight));
    return maxHeight;
  }

  /** Activates the header's view style/presentation. */
  protected void activateViewStyle() {
    for (EditHeaderCell cell : this.headerCells) {
      cell.activateViewStyle();
    }
  }

  /** Activates the header's print style/presentation. */
  protected void activatePrintStyle() {
    for (EditHeaderCell cell : this.headerCells) {
      cell.activatePrintStyle();
    }
  }

  /**
   * Updates the relative index of each cell and the action button enabled/disabled
   * state overlays (right, left, remove, add).
   */
  protected void updateIndexesAndActions() {
    int categoriesCount = this.editTable.getGameData().getCategoriesCount();
    boolean addEnabled = categoriesCount < JjDefaults.MAX_NUMBER_OF_CATEGORIES;
    boolean removeEnabled = categoriesCount > JjDefaults.MIN_NUMBER_OF_CATEGORIES;
    final int cellCount = this.headerCells.size();
    for (int ind = 0; ind < cellCount; ind++) {
      boolean rightEnabled = ind + 1 < cellCount;
      this.headerCells.get(ind).updateIndexAndOverlay(ind, rightEnabled, removeEnabled, addEnabled);
    }
  }
}
