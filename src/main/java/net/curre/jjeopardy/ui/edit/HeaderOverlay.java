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
import net.curre.jjeopardy.event.EditTableMouseListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;

import javax.swing.JPanel;
import javax.validation.constraints.NotNull;

import static net.curre.jjeopardy.images.ImageEnum.*;

/**
 * Represents an overlay displayed over the edit table header cell to
 * offer some category editing options such as moving or deleting.
 *
 * @author Yevgeny Nyden
 */
public class HeaderOverlay extends JPanel {

  /** Category index (zero based). */
  private int categoryIndex;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Left arrow/move action label. */
  private final OverlayActionLabel leftArrowLabel;

  /** Remove action label. */
  private final OverlayActionLabel removeLabel;

  /** Right arrow/move action label. */
  private final OverlayActionLabel rightArrowLabel;

  /** Add category/column action label. */
  private final OverlayActionLabel addCategoryLabel;

  /**
   * Ctor.
   * @param categoryIndex category index (zero based)
   * @param editTable reference to the edit table; not nullable
   */
  public HeaderOverlay(int categoryIndex, @NotNull EditTable editTable) {
    this.categoryIndex = categoryIndex;
    this.editTable = editTable;
    this.setOpaque(false);
    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}, // columns
        {TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}})); // rows

    // ******* Initializing the left arrow action label in the default enabled state.
    this.leftArrowLabel = new OverlayActionLabel(ARROW_LEFT_32, ARROW_LEFT_32_HOVER, ARROW_LEFT_32_DISABLED,
        "jj.edit.category.move.left", this::moveCategoryToTheLeft, true, false);
    this.add(this.leftArrowLabel, new TableLayoutConstraints(
        1, 0, 1, 0, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Initializing the left arrow action label in the default enabled state.
    this.removeLabel = new OverlayActionLabel(REMOVE_32, REMOVE_32_HOVER, REMOVE_32_DISABLED,
        "jj.edit.category.remove.tooltip", this::removeCategory, true, false);
    this.add(this.removeLabel, new TableLayoutConstraints(
        2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Initializing the left arrow action label in the default enabled state.
    this.rightArrowLabel = new OverlayActionLabel(ARROW_RIGHT_32, ARROW_RIGHT_32_HOVER, ARROW_RIGHT_32_DISABLED,
        "jj.edit.category.move.right", this::moveCategoryToTheRight, true, false);
    this.add(this.rightArrowLabel, new TableLayoutConstraints(
        3, 0, 3, 0, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Initializing the left arrow action label in the default enabled state.
    this.addCategoryLabel = new OverlayActionLabel(PLUS_ONE_32, PLUS_ONE_32_HOVER, PLUS_ONE_32_DISABLED,
        "jj.edit.category.add.column", this::addCategory, true, false);
    this.add(this.addCategoryLabel, new TableLayoutConstraints(
        2, 2, 2, 2, TableLayout.CENTER, TableLayout.CENTER));

    // This listener is needed for the non-interrupted hover effect on the parent header cell.
    EditTableMouseListener mouseListener = this.editTable.getTableMouseListener();
    this.leftArrowLabel.addMouseListener(mouseListener);
    this.removeLabel.addMouseListener(mouseListener);
    this.rightArrowLabel.addMouseListener(mouseListener);
    this.addCategoryLabel.addMouseListener(mouseListener);
  }

  /**
   * Enables/disables the left arrow/move action on this overlay.
   * @param isEnabled true if left move should be enabled
   */
  public void setLeftMoveEnabled(boolean isEnabled) {
    this.leftArrowLabel.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the remove action on this overlay.
   * @param isEnabled true if remove action should be enabled
   */
  public void setRemoveEnabled(boolean isEnabled) {
    this.removeLabel.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the right arrow/move action on this overlay.
   * @param isEnabled true if right move should be enabled
   */
  public void setRightMoveEnabled(boolean isEnabled) {
    this.rightArrowLabel.setEnabled(isEnabled);
  }

  /**
   * Sets the Add Category button enabled or disabled.
   * @param isEnabled true if the button should be enabled
   */
  protected void setAddCategoryEnabled(boolean isEnabled) {
    this.addCategoryLabel.setEnabled(isEnabled);
  }

  /**
   * Updates the index of the category this overlay adds action to and the state of the
   * overlay buttons. Note that the left button will be disabled by default on the cell with index 0.
   * @param newIndex the new index of the category
   * @param rightEnabled true to enable the right arrow button
   * @param removeEnabled true to enable the remove button
   * @param addEnabled true to enable the add button
   */
  protected void updateState(
          int newIndex, boolean rightEnabled, boolean removeEnabled, boolean addEnabled) {
    this.categoryIndex = newIndex;
    this.setLeftMoveEnabled(newIndex != 0);
    this.setRemoveEnabled(removeEnabled);
    this.setRightMoveEnabled(rightEnabled);
    this.setAddCategoryEnabled(addEnabled);
  }

  /**
   * Moves the category to the left (w/o user confirmation).
   */
  private void moveCategoryToTheLeft() {
    this.editTable.moveCategoryToTheLeft(this.categoryIndex);
  }

  /**
   * After user confirmation, removes the category from the game data and updates the UI.
   */
  private void removeCategory() {
    AppRegistry.getInstance().getUiService().showConfirmationDialog(
        LocaleService.getString("jj.edit.category.remove.title"),
        LocaleService.getString("jj.edit.category.remove.message"),
        () -> this.editTable.removeCategory(this.categoryIndex), null, null);
  }

  /**
   * Moves the category to the right (w/o user confirmation).
   */
  private void moveCategoryToTheRight() {
    this.editTable.moveCategoryToTheRight(this.categoryIndex);
  }

  /**
   * Adds a category at the current column location.
   */
  private void addCategory() {
    this.editTable.addCategory(this.categoryIndex);
  }
}
