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
import net.curre.jjeopardy.service.LocaleService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.validation.constraints.NotNull;

import java.awt.Dimension;

import static net.curre.jjeopardy.images.ImageEnum.*;

/**
 * Represents an overlay displayed over the edit table row cell to
 * offer some question editing options such as moving or erasing.
 *
 * @author Yevgeny Nyden
 */
public class QuestionOverlay extends JPanel {

  /** Question's category index (zero based). */
  private int categoryIndex;

  /** Question index (zero based). */
  private int questionIndex;

  /** Reference to the edit cell this overlay is for. */
  private final EditCell cell;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Up arrow/move action label. */
  private final JLabel upArrowLabel;

  /** Up arrow/move mouse listener (to handle clicks and hovers). */
  private final EditOverlayLabelMouseListener upArrowMouseListener;

  /** Erase action label. */
  private final JLabel eraseLabel;

  /** Erase mouse listener (to handle clicks and hovers). */
  private final EditOverlayLabelMouseListener eraseMouseListener;

  /** Down arrow/move action label. */
  private final JLabel downArrowLabel;

  /** Down arrow/move mouse listener (to handle clicks and hovers). */
  private final EditOverlayLabelMouseListener downArrowMouseListener;

  /**
   * Ctor.
   * @param questionIndex question index (zero based)
   * @param editTable reference to the edit table; not nullable
   */
  public QuestionOverlay(int categoryIndex, int questionIndex, @NotNull EditCell cell, @NotNull EditTable editTable) {
    this.categoryIndex = categoryIndex;
    this.questionIndex = questionIndex;
    this.cell = cell;
    this.editTable = editTable;

    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    this.setOpaque(false);

    // ******* Initializing the Up arrow action label in the default enabled state.
    this.upArrowLabel = new JLabel();
    this.add(this.upArrowLabel);
    this.add(Box.createRigidArea(new Dimension(1, 5)));
    this.upArrowMouseListener = new EditOverlayLabelMouseListener(
        ARROW_UP_32, ARROW_UP_32_HOVER, this::moveQuestionUp);
    this.upArrowLabel.addMouseListener(this.upArrowMouseListener);
    this.setUpMoveEnabled(true);

    // ******* Initializing the left arrow action label in the default enabled state.
    this.eraseLabel = new JLabel();
    this.add(this.eraseLabel);
    this.add(Box.createRigidArea(new Dimension(1, 5)));
    this.eraseMouseListener = new EditOverlayLabelMouseListener(REMOVE_32, REMOVE_32_HOVER, this::eraseQuestion);
    this.eraseLabel.addMouseListener(this.eraseMouseListener);
    this.setEraseEnabled(true);

    // ******* Initializing the left arrow action label in the default enabled state.
    this.downArrowLabel = new JLabel();
    this.add(this.downArrowLabel);
    this.downArrowMouseListener = new EditOverlayLabelMouseListener(
        ARROW_DOWN_32, ARROW_DOWN_32_HOVER, this::moveQuestionDown);
    this.downArrowLabel.addMouseListener(this.downArrowMouseListener);
    this.setDownMoveEnabled(true);

    // This listener is needed for the non-interrupted hover effect on the parent header cell.
    EditTableMouseListener mouseListener = this.editTable.getTableMouseListener();
    this.upArrowLabel.addMouseListener(mouseListener);
    this.eraseLabel.addMouseListener(mouseListener);
    this.downArrowLabel.addMouseListener(mouseListener);
  }

  /**
   * Enables/disables the Up arrow/move action on this overlay.
   * @param isEnabled true if the Up move is enabled
   */
  public void setUpMoveEnabled(boolean isEnabled) {
    this.upArrowLabel.setIcon(
        isEnabled ? ARROW_UP_32.toImageIcon() : ARROW_UP_32_DISABLED.toImageIcon());
    this.upArrowLabel.setToolTipText(isEnabled ?
        LocaleService.getString("jj.edit.question.move.up") : null);
    this.upArrowMouseListener.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the erase action on this overlay.
   * @param isEnabled true if erase action is enabled
   */
  public void setEraseEnabled(boolean isEnabled) {
    this.eraseLabel.setIcon(
        isEnabled ? REMOVE_32.toImageIcon() : REMOVE_32_DISABLED.toImageIcon());
    this.eraseLabel.setToolTipText(isEnabled ?
        LocaleService.getString("jj.edit.question.erase") : null);
    this.eraseMouseListener.setEnabled(isEnabled);
  }

  /**
   * Enables/disables the Down arrow/move action on this overlay.
   * @param isEnabled true if Down move is enabled
   */
  public void setDownMoveEnabled(boolean isEnabled) {
    this.downArrowLabel.setIcon(
        isEnabled ? ARROW_DOWN_32.toImageIcon() : ARROW_DOWN_32_DISABLED.toImageIcon());
    this.downArrowLabel.setToolTipText(isEnabled ?
        LocaleService.getString("jj.edit.question.move.down") : null);
    this.downArrowMouseListener.setEnabled(isEnabled);
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
   */
  protected void updateState(int newIndex, boolean downEnabled) {
    this.questionIndex = newIndex;
    this.setUpMoveEnabled(newIndex != 0);
    this.setDownMoveEnabled(downEnabled);
  }

  /**
   * Moves the question up (w/o user confirmation).
   */
  private void moveQuestionUp() {
    this.editTable.moveQuestionUp(this.categoryIndex, this.questionIndex);
  }

  /**
   * Erases the content of this question (w/o user confirmation).
   */
  private void eraseQuestion() {
    this.editTable.eraseQuestion(this.categoryIndex, this.questionIndex);
  }

  /**
   * Moves the question down (w/o user confirmation).
   */
  private void moveQuestionDown() {
    this.editTable.moveQuestionDown(this.categoryIndex, this.questionIndex);
  }
}
