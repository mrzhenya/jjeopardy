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
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.validation.constraints.NotNull;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dialog to edit data for a single question.
 *
 * @author Yevgeny Nyden
 */
public class EditQuestionDialog extends JDialog {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(EditQuestionDialog.class.getName());

  /** String to prefix an error with. */
  private static final String ERROR_PREFIX = "  · ";

  /** Width of the question points text pane. */
  private static final int POINTS_PANE_WIDTH = 120;

  /** Edit cell that's being edited. */
  private final EditCell cell;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Text pane with the question points. */
  private JTextPane pointsPane;

  /** Text pane with the question text. */
  private JTextPane questionPane;

  /** Question image picker. */
  private ImagePickerPanel questionImagePicker;

  /** Text pane with the answer text. */
  private JTextPane answerPane;

  /** Answer image picker. */
  private ImagePickerPanel answerImagePicker;

  /**
   * Ctor.
   * @param cell cell/question to edit
   * @param editTable reference to the edit table
   */
  public EditQuestionDialog(@NotNull EditCell cell, EditTable editTable) {
    logger.info("Creating EditQuestionDialog for cell col=" + cell.getColumnIndex() + "; row=" + cell.getRowIndex());
    this.cell = cell;
    this.editTable = editTable;

    this.setModal(true);
    this.setAlwaysOnTop(true);
    this.setResizable(false);
    this.setPreferredSize(new Dimension(JjDefaults.EDIT_QUESTION_DIALOG_MIN_WIDTH, JjDefaults.EDIT_QUESTION_DIALOG_MIN_HEIGHT));
    this.setTitle(LocaleService.getString("jj.editdialog.title"));

    this.initComponents();

    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty( "apple.awt.windowTitleVisible", false);
    }

    this.pack();
  }

  /** Initializes the UI components. */
  private void initComponents() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    Font pointsLabelFont = lafTheme.getEditTableCellFont().deriveFont(Font.BOLD, 30);

    // Settings the main layout.
    this.setLayout(new TableLayout(new double[][] {
        {padding, TableLayout.FILL, padding}, // columns
        {padding, TableLayout.PREFERRED, padding, TableLayout.FILL, padding, TableLayout.PREFERRED, padding,
            TableLayout.FILL, padding, TableLayout.PREFERRED, padding}})); // rows

    // ******* Question points text pane.
    this.pointsPane = new JTextPane();
    UiService.addAlignCenterToTextPane(this.pointsPane);
    this.pointsPane.setText(String.valueOf(this.cell.getQuestion().getPoints()));
    this.pointsPane.setFont(pointsLabelFont);
    this.pointsPane.setPreferredSize(new Dimension(POINTS_PANE_WIDTH, this.pointsPane.getPreferredSize().height));
    this.add(this.pointsPane, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Question section panel.
    JPanel questionPanel = this.createQuestionPanel();
    this.add(questionPanel, new TableLayoutConstraints(
        1, 3, 1, 3, TableLayout.FULL, TableLayout.FULL));

    // ******* Separator label icon.
    JLabel mainSeparator = new JLabel();
    mainSeparator.setIcon(ImageEnum.VERTICAL_SPACER_24.toImageIcon());
    mainSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.add(mainSeparator, new TableLayoutConstraints(
        1, 5, 1, 5, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Answer section panel.
    JPanel answerPanel = this.createAnswerPanel();
    this.add(answerPanel, new TableLayoutConstraints(
        1, 7, 1, 7, TableLayout.FULL, TableLayout.FULL));

    // ******* Buttons panel.
    JPanel buttonPanel = this.createButtonPanel();
    this.add(buttonPanel, new TableLayoutConstraints(
        1, 9, 1, 9, TableLayout.CENTER, TableLayout.CENTER));
  }

  /**
   * Creates and initializes the question section with question edit text pane
   * and an input field for the question image.
   * @return the question panel to add to the edit question dialog
   */
  private @NotNull JPanel createQuestionPanel() {
    Question question = this.cell.getQuestion();
    this.questionPane = new JTextPane();
    this.questionPane.setText(question.getQuestion());
    this.questionImagePicker = new ImagePickerPanel(question.getQuestionImage(), this.editTable.getGameBundlePath());
    return createQuestionAnswerPanelHelper(
        "jj.editdialog.question.title", this.questionPane, this.questionImagePicker);
  }

  /**
   * Creates and initializes the answer section with answer edit text pane
   * and an input field for the answer image.
   * @return the answer panel to add to the edit question dialog
   */
  private @NotNull JPanel createAnswerPanel() {
    Question question = this.cell.getQuestion();
    this.answerPane = new JTextPane();
    this.answerPane.setText(question.getAnswer());
    this.answerImagePicker = new ImagePickerPanel(question.getAnswerImage(), this.editTable.getGameBundlePath());
    return createQuestionAnswerPanelHelper(
        "jj.editdialog.answer.title", this.answerPane, this.answerImagePicker);
  }

  /**
   * Helper method to create answer/question panel with answer/question text and image information.
   * @param titleProp panel title localized property name
   * @param textPane reference to the text pane for the question/answer text
   * @param imagePicker reference to the question/answer image picker
   * @return created and initialized panel for question or answer information
   */
  private static @NotNull JPanel createQuestionAnswerPanelHelper(
      String titleProp, @NotNull JTextPane textPane, ImagePickerPanel imagePicker) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    TitledBorder border = BorderFactory.createTitledBorder(LocaleService.getString(titleProp));
    Font oldFont = border.getTitleFont();
    border.setTitleFont(oldFont.deriveFont(Font.BOLD, oldFont.getSize() + 2));
    panel.setBorder(border);

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    int panelWidth = JjDefaults.EDIT_QUESTION_DIALOG_MIN_WIDTH - 2 * lafTheme.getPanelPadding();
    textPane.setPreferredSize(new Dimension(panelWidth - 20, 75));
    JScrollPane scrollPane = new JScrollPane(textPane);
    scrollPane.setPreferredSize(new Dimension(panelWidth - 20, 75));
    scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel.add(scrollPane);

    panel.add(imagePicker);

    return panel;
  }

  /**
   * Creates and initializes the button panel that's displayed at the bottom of the dialog.
   * @return panel that contains the action buttons
   */
  private @NotNull JPanel createButtonPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int spacing = lafTheme.getButtonSpacing();
    JPanel panel = new JPanel();

    // ******* Save button.
    final JButton okButton = new JButton();
    ClickAndKeyAction.createAndAddAction(okButton, this::handleOkQuestionAction);
    okButton.setFont(lafTheme.getButtonFont());
    okButton.setText(LocaleService.getString("jj.dialog.button.ok"));
    panel.add(okButton);
    panel.add(Box.createRigidArea(new Dimension(spacing, 1)));

    // ******* Cancel button.
    final JButton cancelButton = new JButton();
    ClickAndKeyAction.createAndAddAction(cancelButton, this::handleCancelQuestionAction);
    cancelButton.setFont(lafTheme.getButtonFont());
    cancelButton.setText(LocaleService.getString("jj.dialog.button.cancel"));
    panel.add(cancelButton);

    return panel;
  }

  /**
   * Handles cancelling the changes action and closes the dialog.
   */
  private void handleCancelQuestionAction() {
    logger.info("Cancelling the changes and closing the dialog.");
    this.setVisible(false);
    this.dispose();
  }

  /**
   * Handles saving the changes action and closes the dialog.
   */
  private void handleOkQuestionAction() {
    logger.info("Saving the question changes and closing the dialog.");

    List<String> errors = new ArrayList<>();
    int points = this.validatePointsValue(errors);

    // Validating image paths.
    String questionImagePath = this.validateQuestionImage(errors);
    String answerImagePath = this.validateAnswerImage(errors);

    // If there are errors, stop here.
    if (!errors.isEmpty()) {
      this.showErrorDialog(errors);
      return;
    }

    // Validate the question and answer data.
    String questionText = StringUtils.trimToNull(this.questionPane.getText());
    if (questionText == null && questionImagePath == null) {
      errors.add(ERROR_PREFIX + LocaleService.getString("jj.editdialog.invalid.question") + "\n");
    }
    String answerText = StringUtils.trimToNull(this.answerPane.getText());
    if (answerText == null && answerImagePath == null) {
      errors.add(ERROR_PREFIX + LocaleService.getString("jj.editdialog.invalid.answer") + "\n");
    }

    // Check again for errors, stop if there are any.
    if (!errors.isEmpty()) {
      this.showErrorDialog(errors);
      return;
    }

    // Warn the user that the points will be updated on all questions in the row.
    boolean isDataChanged = false;
    if (points != this.cell.getQuestion().getPoints()) {
      isDataChanged = true;
      this.setModal(false);
      this.setAlwaysOnTop(false);
      GameData gameData = this.editTable.getGameData();
      AppRegistry.getInstance().getUiService().showInfoDialog(
          LocaleService.getString("jj.editdialog.points.update.title"),
          LocaleService.getString("jj.editdialog.points.update.msg"), this);
      for (Category category : gameData.getCategories()) {
        category.getQuestion(this.cell.getRowIndex()).setPoints(points);
      }
    }

    // Update the rest of the question data.
    Question question = this.cell.getQuestion();
    isDataChanged |= question.setQuestion(questionText);
    isDataChanged |= question.setQuestionImage(questionImagePath);
    isDataChanged |= question.setAnswer(answerText);
    isDataChanged |= question.setAnswerImage(answerImagePath);

    // Update the table and dispose the edit dialog.
    this.editTable.updateDataChanged(isDataChanged);
    this.editTable.refreshAndResize();
    this.setVisible(false);
    this.dispose();
  }

  /**
   * Shows the error dialog to the user.
   * @param errors errors to show to the user
   */
  private void showErrorDialog(List<String> errors) {
    this.setModal(false);
    this.setAlwaysOnTop(false);
    AppRegistry.getInstance().getUiService().showErrorDialog(
        LocaleService.getString("jj.editdialog.error.title"),
        LocaleService.getString("jj.editdialog.error.header") + errors, this);
    this.setModal(true);
    this.setAlwaysOnTop(true);
  }

  /**
   * Validates the currently specified points value.
   * @param errors errors list to add an error to
   * @return parsed points value
   */
  private int validatePointsValue(List<String> errors) {
    int points = 0;
    String pointsStr = StringUtils.trimToNull(this.pointsPane.getText());
    if (pointsStr != null) {
      try {
        points = Integer.parseInt(pointsStr);
      } catch (NumberFormatException e) {
        // Ignore parsing exceptions.
      }
    }
    if (points <= 0) {
      errors.add(ERROR_PREFIX + LocaleService.getString("jj.editdialog.invalid.points") + "\n");
    }
    return points;
  }

  /**
   * Validates the question image path.
   * @param errors errors list to add an error to
   * @return parsed question image path or null if no image path is specified
   */
  private String validateQuestionImage(List<String> errors) {
    String path = null;
    if (this.questionImagePicker.isInvalidImage()) {
      errors.add(ERROR_PREFIX + LocaleService.getString("jj.editdialog.invalid.question.image") + "\n");
    } else {
      path = this.questionImagePicker.getImagePathOrNull();
    }
    return path;
  }

  /**
   * Validates the answer image path.
   * @param errors errors list to add an error to
   * @return parsed answer image path or null if no image path is specified
   */
  private String validateAnswerImage(List<String> errors) {
    String path = null;
    if (this.answerImagePicker.isInvalidImage()) {
      errors.add(ERROR_PREFIX + LocaleService.getString("jj.editdialog.invalid.answer.image") + "\n");
    } else {
      path = this.answerImagePicker.getImagePathOrNull();
    }
    return path;
  }
}
