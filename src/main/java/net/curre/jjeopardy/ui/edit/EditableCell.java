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

import java.awt.Color;

/**
 * Common interface for editable table cell or header cell.
 *
 * @author Yevgeny Nyden
 */
public interface EditableCell {

  /**
   * Decorates the state of this cell as hovered or not hovered.
   * @param isHovered true if the cell is hovered; false if not
   * @param isSingleCellHover true if this is a single cell hover (vs row or columns hover effect)
   */
  void decorateHoverState(boolean isHovered, boolean isSingleCellHover);

  /**
   * Shows edit question dialog for this cell.
   */
  void showEditDialog();

  /**
   * Gets the column index of this cell (which is also the category index in the game data).
   * @return column index
   */
  int getColumnIndex();

  /**
   * Sets the background color on the cell.
   * @param color new background color for the cell
   */
  void setBackground(Color color);
}
