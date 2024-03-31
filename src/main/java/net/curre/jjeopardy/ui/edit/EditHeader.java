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

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import static net.curre.jjeopardy.ui.edit.EditRow.BORDER_WIDTH;

/**
 * Represents a game edit table header to display game categories.
 *
 * @author Yevgeny Nyden
 */
public class EditHeader extends JPanel {

  /** Minimum header height (in px). */
  private static final int MIN_HEIGHT = 50;

  /** Ordered list of header cells. */
  private final ArrayList<EditHeaderCell> headerCells;

  /** Current row height. */
  private int rowHeight;

  /**
   * Ctor.
   * @param categories an ordered list of categories to create a table header for
   */
  public EditHeader(List<Category> categories) {
    this.headerCells = new ArrayList<>();
    this.rowHeight = 0;

    // Items will flow left to right and will take all available panel height.
    this.setLayout(new GridLayout(1, 0));
    this.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, 0, BORDER_WIDTH));

    for (Category category : categories) {
      EditHeaderCell cell = new EditHeaderCell(category.getName());
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
}
