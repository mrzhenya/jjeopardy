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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.event.EditTableMouseListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JTextPane;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Represents a table header cell where game category name
 * (likely a multi-line string) is rendered.
 *
 * @author Yevgeny Nyden
 */
public class EditHeaderCell extends JLayeredPane implements EditableCell {

  /** Background color to use for print. */
  private static final Color PRINT_BACKGROUND_COLOR = new Color(210, 233, 248);

  /** An invisible text pane to help determining the text areas sizes (not thread safe!). */
  private static final JTextPane HELPER_TEXT_PANE = UiService.createDefaultTextPane();

  /** Category index (zero based). */
  private int categoryIndex;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Text area where category name is rendered. */
  private final JTextPane textPane;

  /** Overlay with buttons to move or delete this category. */
  private final CategoryOverlay editOverlay;

  /**
   * Ctor.
   * @param name header cell text (category name)
   * @param categoryIndex category index (zero based)
   * @param editTable reference to the edit table; not nullable
   */
  public EditHeaderCell(String name, int categoryIndex, EditTable editTable) {
    this.categoryIndex = categoryIndex;
    this.editTable = editTable;

    this.setOpaque(true);

    // Layout helps to vertically center the text content.
    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL}})); // rows

    EditTableMouseListener mouseListener = this.editTable.getTableMouseListener();

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.textPane = UiService.createDefaultTextPane();
    this.textPane.setFont(lafTheme.getEditTableHeaderFont());
    HELPER_TEXT_PANE.setFont(lafTheme.getEditTableHeaderFont());
    if (!StringUtils.isBlank(name)) {
      this.textPane.setText(name.toUpperCase());
    }
    this.textPane.addMouseListener(mouseListener);
    this.textPane.addMouseMotionListener(mouseListener);

    this.add(this.textPane, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));

    this.editOverlay = new CategoryOverlay(categoryIndex, editTable);
    this.editOverlay.setVisible(false);
    if (categoryIndex == 0) {
      this.editOverlay.setLeftMoveEnabled(false);
    } else if (categoryIndex == this.editTable.getGameData().getCategoriesCount() - 1) {
      this.editOverlay.setRightMoveEnabled(false);
    }
    if (this.editTable.getGameData().getCategoriesCount() <= JjDefaults.MIN_NUMBER_OF_CATEGORIES) {
      this.editOverlay.setRemoveEnabled(false);
    }

    this.add(this.editOverlay, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.TOP), 3);
    this.moveToFront(this.editOverlay);

    this.activateViewStyle();

    this.addMouseMotionListener(mouseListener);
    this.addMouseListener(mouseListener);
  }

  /** @inheritDoc */
  public void showEditDialog() {
    EditCategoryDialog dialog = new EditCategoryDialog(this);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }

  /** @inheritDoc */
  public void decorateHoverState(boolean isHovered) {
    this.editOverlay.setVisible(isHovered);
    Color background = EditTable.decorateHoverStateHelper(this, isHovered);
    this.textPane.setBackground(background);
    this.repaint();
  }

  /**
   * Updates the relative index of this cell and its overlay. Note that the left button
   * will be disabled by default on the cell with index 0.
   * @param newIndex the new index of this cell
   * @param rightEnabled true to enable the right arrow button
   * @param removeEnabled true to enable the remove button
   */
  public void updateIndexAndOverlay(int newIndex, boolean rightEnabled, boolean removeEnabled) {
    this.categoryIndex = newIndex;
    this.editOverlay.updateState(newIndex, rightEnabled, removeEnabled);
  }

  /**
   * Gets the category name.
   * @return the current category name
   */
  protected String getCategoryName() {
    return this.editTable.getGameData().getCategories().get(this.categoryIndex).getName();
  }

  /**
   * Gets the category index.
   * @return the current category index (zero based)
   */
  protected int getCategoryIndex() {
    return this.categoryIndex;
  }

  /**
   * Updates the cell's corresponding category name in the UI and in the game data.
   * @param name new category name (assume not blank).
   */
  protected void updateCategoryName(@NotNull String name) {
    Category category = this.editTable.getGameData().getCategories().get(this.categoryIndex);
    if (category.setName(name)) {
      this.editTable.updateDataChanged(true);
    }
    this.textPane.setText(name.toUpperCase());
  }

  /**
   * Determines the preferred height of this cell taking into consideration
   * the current view mode and the provided column width. Size/dimension of the cell
   * component is not affected by calling this method.
   * @param columnWidth the width of this cell
   * @return preferred height of this cell
   */
  protected int getPreferredCellHeight(int columnWidth) {
    HELPER_TEXT_PANE.setSize(new Dimension(columnWidth, 500));
    HELPER_TEXT_PANE.setText(this.textPane.getText());
    return Math.max(HELPER_TEXT_PANE.getPreferredSize().height, JjDefaults.GAME_TABLE_HEADER_HEIGHT);
  }

  /** Activates the cell's view style/presentation. */
  protected void activateViewStyle() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.textPane.setForeground(lafTheme.getGameTableHeaderColor());
    this.textPane.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.setBorder(BorderFactory.createLineBorder(lafTheme.getGameTableBorderColor(), 2));
  }

  /** Activates the cell's print style/presentation. */
  protected void activatePrintStyle() {
    this.setBackground(PRINT_BACKGROUND_COLOR);
    this.textPane.setForeground(Color.BLACK);
    this.textPane.setBackground(PRINT_BACKGROUND_COLOR);
    this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
  }
}
