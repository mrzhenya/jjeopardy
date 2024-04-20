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

import net.curre.jjeopardy.event.EditOverlayLabelMouseListener;
import net.curre.jjeopardy.event.EditTableMouseListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.validation.constraints.NotNull;

import static net.curre.jjeopardy.images.ImageEnum.*;

/**
 * Represents an overlay displayed over the edit table header cell to
 * offer some category editing options such as moving or deleting.
 *
 * @author Yevgeny Nyden
 */
public class CategoryOverlay extends JPanel {

  /** Category index (zero based). */
  private int categoryIndex;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Left arrow/move action label. */
  private final JLabel leftArrowLabel;

  /** Left arrow/move mouse listener (to handle clicks and hovers). */
  private final EditOverlayLabelMouseListener leftArrowMouseListener;

  /** Remove action label. */
  private final JLabel removeLabel;

  /** Remove mouse listener (to handle clicks and hovers). */
  private final EditOverlayLabelMouseListener removeMouseListener;

  /** Right arrow/move action label. */
  private final JLabel rightArrowLabel;

  /** Right arrow/move mouse listener (to handle clicks and hovers). */
  private final EditOverlayLabelMouseListener rightArrowMouseListener;

  /**
   * Ctor.
   * @param categoryIndex category index (zero based)
   * @param editTable reference to the edit table; not nullable
   */
  public CategoryOverlay(int categoryIndex, @NotNull EditTable editTable) {
    this.categoryIndex = categoryIndex;
    this.editTable = editTable;
    this.setOpaque(false);

    // ******* Initializing the left arrow action label in the default enabled state.
    this.leftArrowLabel = new JLabel();
    this.add(this.leftArrowLabel);
    this.leftArrowMouseListener = new EditOverlayLabelMouseListener(
        ARROW_LEFT_32, ARROW_LEFT_32_HOVER, this::moveCategoryToTheLeft);
    this.leftArrowLabel.addMouseListener(this.leftArrowMouseListener);
    this.setLeftMoveEnabled(true);

    // ******* Initializing the left arrow action label in the default enabled state.
    this.removeLabel = new JLabel();
    this.add(this.removeLabel);
    this.removeMouseListener = new EditOverlayLabelMouseListener(REMOVE_32, REMOVE_32_HOVER, this::removeCategory);
    this.removeLabel.addMouseListener(this.removeMouseListener);
    this.setRemoveEnabled(true);

    // ******* Initializing the left arrow action label in the default enabled state.
    this.rightArrowLabel = new JLabel();
    this.add(this.rightArrowLabel);
    this.rightArrowMouseListener = new EditOverlayLabelMouseListener(
        ARROW_RIGHT_32, ARROW_RIGHT_32_HOVER, this::moveCategoryToTheRight);
    this.rightArrowLabel.addMouseListener(this.rightArrowMouseListener);
    this.setRightMoveEnabled(true);

    // This listener is needed for the non-interrupted hover effect on the parent header cell.
    EditTableMouseListener mouseListener = this.editTable.getTableMouseListener();
    this.leftArrowLabel.addMouseListener(mouseListener);
    this.removeLabel.addMouseListener(mouseListener);
    this.rightArrowLabel.addMouseListener(mouseListener);
  }

  /**
   * Enables/disables the left arrow/move action on this overlay.
   * @param isEnabled true if left move is enabled
   */
  public void setLeftMoveEnabled(boolean isEnabled) {
    this.leftArrowLabel.setIcon(
        isEnabled ? ARROW_LEFT_32.toImageIcon() : ARROW_LEFT_32_DISABLED.toImageIcon());
    this.leftArrowLabel.setToolTipText(isEnabled ?
            LocaleService.getString("jj.edit.category.move.left") : null);
    this.leftArrowMouseListener.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the remove action on this overlay.
   * @param isEnabled true if remove action is enabled
   */
  public void setRemoveEnabled(boolean isEnabled) {
    this.removeLabel.setIcon(
        isEnabled ? REMOVE_32.toImageIcon() : REMOVE_32_DISABLED.toImageIcon());
    this.removeLabel.setToolTipText(isEnabled ?
            LocaleService.getString("jj.edit.category.remove.tooltip") : null);
    this.removeMouseListener.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the right arrow/move action on this overlay.
   * @param isEnabled true if right move is enabled
   */
  public void setRightMoveEnabled(boolean isEnabled) {
    this.rightArrowLabel.setIcon(
        isEnabled ? ARROW_RIGHT_32.toImageIcon() : ARROW_RIGHT_32_DISABLED.toImageIcon());
    this.rightArrowLabel.setToolTipText(isEnabled ?
        LocaleService.getString("jj.edit.category.move.right") : null);
    this.rightArrowMouseListener.setEnabled(isEnabled);
  }

  /**
   * Updates the index of the category this overlay adds action to and the state of the
   * overlay buttons. Note that the left button will be disabled by default on the cell with index 0.
   * @param newIndex the new index of the category
   * @param rightEnabled true to enable the right arrow button
   * @param removeEnabled true to enable the remove button
   */
  protected void updateState(int newIndex, boolean rightEnabled, boolean removeEnabled) {
    this.categoryIndex = newIndex;
    this.setLeftMoveEnabled(newIndex != 0);
    this.setRemoveEnabled(removeEnabled);
    this.setRightMoveEnabled(rightEnabled);
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
        () -> this.editTable.removeCategory(this.categoryIndex), null);
  }

  /**
   * Moves the category to the right (w/o user confirmation).
   */
  private void moveCategoryToTheRight() {
    this.editTable.moveCategoryToTheRight(this.categoryIndex);
  }
}
