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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.validation.constraints.NotNull;

import static net.curre.jjeopardy.images.ImageEnum.*;

/**
 * Represents an overlay displayed over the edit table row cell to
 * offer some question editing options such as moving or erasing.
 *
 * @author Yevgeny Nyden
 */
public class CellOverlay extends JPanel {

  /** Question's category index (zero based). */
  private int categoryIndex;

  /** Question index (zero based). */
  private int questionIndex;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Up arrow/move action label. */
  private final OverlayActionLabel upArrowLabel;

  /** Remove row action label. */
  private final OverlayActionLabel removeLabel;

  /** Down arrow/move action label. */
  private final OverlayActionLabel downArrowLabel;

  /** Add a new row action label. */
  private final OverlayActionLabel addRowLabel;

  /**
   * Ctor.
   * @param questionIndex question index (zero based)
   * @param editTable reference to the edit table; not nullable
   */
  public CellOverlay(int categoryIndex, int questionIndex, @NotNull EditTable editTable) {
    this.categoryIndex = categoryIndex;
    this.questionIndex = questionIndex;
    this.editTable = editTable;

    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    this.setOpaque(false);
    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED}, // columns
        {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}})); // rows

    // ******* Initializing the Up arrow action label in the default enabled state.
    this.upArrowLabel = new OverlayActionLabel(ARROW_UP_32, ARROW_UP_32_HOVER, ARROW_UP_32_DISABLED,
        "jj.edit.question.move.up", this::moveQuestionUp, false, false);
    this.add(this.upArrowLabel, new TableLayoutConstraints(
        2, 1, 2, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Initializing the left arrow action label in the default enabled state.
    this.removeLabel = new OverlayActionLabel(REMOVE_32, REMOVE_32_HOVER, REMOVE_32_DISABLED,
        "jj.edit.question.remove.tooltip", this::removeQuestionRow, false, true);
    this.add(this.removeLabel, new TableLayoutConstraints(
        2, 2, 2, 2, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Initializing the left arrow action label in the default enabled state.
    this.downArrowLabel = new OverlayActionLabel(ARROW_DOWN_32, ARROW_DOWN_32_HOVER, ARROW_DOWN_32_DISABLED,
        "jj.edit.question.move.down", this::moveQuestionDown, false, false);
    this.add(this.downArrowLabel, new TableLayoutConstraints(
        2, 3, 2, 3, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Initializing the left arrow action label in the default enabled state.
    this.addRowLabel = new OverlayActionLabel(PLUS_ONE_32, PLUS_ONE_32_HOVER, PLUS_ONE_32_DISABLED,
        "jj.edit.category.add.row", this::addQuestionRow, false, true);
    this.add(this.addRowLabel, new TableLayoutConstraints(
        1, 2, 1, 2, TableLayout.CENTER, TableLayout.CENTER));


    // This listener is needed for the non-interrupted hover effect on the parent header cell.
    EditTableMouseListener mouseListener = this.editTable.getTableMouseListener();
    this.upArrowLabel.addMouseListener(mouseListener);
    this.removeLabel.addMouseListener(mouseListener);
    this.downArrowLabel.addMouseListener(mouseListener);
    this.addRowLabel.addMouseListener(mouseListener);
  }

  /**
   * Enables/disables the Up arrow/move action on this overlay.
   * @param isEnabled true if the Up move should be enabled
   */
  public void setUpMoveEnabled(boolean isEnabled) {
    this.upArrowLabel.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the remove row action on this overlay.
   * @param isEnabled true if remove action is enabled
   */
  public void setRemoveEnabled(boolean isEnabled) {
    this.removeLabel.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the Down arrow/move action on this overlay.
   * @param isEnabled true if Down move should be enabled
   */
  public void setDownMoveEnabled(boolean isEnabled) {
    this.downArrowLabel.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the Add row action on this overlay.
   * @param isEnabled true if Add row should be enabled
   */
  public void setAddRowEnabled(boolean isEnabled) {
    this.addRowLabel.setEnabled(isEnabled);
  }

  /**
   * Updates the category (column) index of this overlay.
   * @param categoryIndex the new category index
   */
  protected void updateCategoryIndex(int categoryIndex) {
    this.categoryIndex = categoryIndex;
  }

  /**
   * Updates the index of the question this overlay adds action to and the state of the
   * overlay buttons. Note that the Up button will be disabled by default on the cell with index 0.
   * @param newIndex the new index of the question
   * @param downEnabled true to enable the Down arrow button
   * @param removeEnabled true to enable the Remove row action
   * @param addRowEnabled true to enable the Add row action
   */
  protected void updateState(int newIndex, boolean downEnabled, boolean removeEnabled, boolean addRowEnabled) {
    this.questionIndex = newIndex;
    this.setUpMoveEnabled(newIndex != 0);
    this.setDownMoveEnabled(downEnabled);
    this.setRemoveEnabled(removeEnabled);
    this.setAddRowEnabled(addRowEnabled);
  }

  /**
   * Moves the question up (w/o user confirmation).
   */
  private void moveQuestionUp() {
    this.editTable.moveQuestionUp(this.categoryIndex, this.questionIndex);
  }

  /**
   * Removes the current row of questions (with a user confirmation).
   */
  private void removeQuestionRow() {
    AppRegistry.getInstance().getUiService().showConfirmationDialog(
        LocaleService.getString("jj.edit.question.remove.title"),
        LocaleService.getString("jj.edit.question.remove.message"),
        () -> this.editTable.removeQuestionRow(this.questionIndex), null);
  }

  /**
   * Adds a new row of questions.
   */
  private void addQuestionRow() {
    this.editTable.addQuestionRow(this.questionIndex);
  }

  /**
   * Moves the question down (w/o user confirmation).
   */
  private void moveQuestionDown() {
    this.editTable.moveQuestionDown(this.categoryIndex, this.questionIndex);
  }
}
