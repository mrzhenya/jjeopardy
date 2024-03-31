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

import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.images.ImageUtilities;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Individual cell where a question data is rendered for the edit/print table.
 *
 * @author Yevgeny Nyden
 */
public class EditCell extends JPanel {

  /** Maximum height of a question or answer image. */
  private static final int MAX_IMAGE_HEIGHT = 100;

  /** The width of the border to draw around a cell. */
  private static final int BORDER_WIDTH = 2;

  /** Panel internal padding. */
  private static final int BOX_PADDING = 5;

  /** Vertical gap to use between the content items. */
  private static final int CONTENT_SPACING = 5;

  /** The height of the separator UI that splits question and answer sections. */
  private static final int SEPARATOR_HEIGHT = 24;

  /** Border to use for print. */
  private static final Border PRINT_BORDER = BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(Color.BLACK, BORDER_WIDTH),
      BorderFactory.createLineBorder(Color.BLACK, BORDER_WIDTH));

  /** An invisible text pane to help determining the text areas sizes (not thread safe!). */
  private static final JTextPane TEST_TEXT_PANE = UiService.createDefaultTextPane();

  /** The question to use for this cell. */
  private final Question question;

  /** Current view mode (answers only, questions and answers, etc.). */
  private EditTableMode editTableMode;

  /** Absolute path to the game bundle directory or null if none. */
  private final String gameBundlePath;

  /** Reference to the question points label. */
  private final JLabel pointsLabel;

  /** The height of the question points label. */
  private final int pointsLabelHeight;

  /** Reference to the question text pane. */
  private final JTextPane qTextPane;

  /** Reference to the question image label. */
  private final JLabel qImageLabel;

  /** Reference to the main separator icon label (used to separate question and answer). */
  private final JLabel mainSeparator;

  /** Reference to the answer text pane. */
  private final JTextPane aTextPane;

  /** Reference to the answer image label. */
  private final JLabel aImageLabel;

  /** Border to use during view (not print). */
  private static Border viewBorder;

  /**
   * Ctor.
   * @param question the question for this cell; non null
   * @param editTableMode current view mode; non null
   * @param gameBundlePath absolute path to the game bundle or null
   */
  public EditCell(Question question, EditTableMode editTableMode, String gameBundlePath) {
    this.question = question;
    this.editTableMode = editTableMode;
    this.gameBundlePath = gameBundlePath;

    // Initialize the layout and the default, view border.
    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    if (viewBorder == null) {
      viewBorder = BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(lafTheme.getGameTableBorderColor(), BORDER_WIDTH),
          BorderFactory.createEmptyBorder(BOX_PADDING, BOX_PADDING, BOX_PADDING, BOX_PADDING));
    }

    // Initialize the cell UI components, most of which are going to be populated later.
    // Initialize the points label, which is always visible.
    this.pointsLabel = new JLabel();
    this.pointsLabel.setText(String.valueOf(question.getPoints()));
    this.pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    Font pointsLabelFont = lafTheme.getEditTableCellFont().deriveFont(Font.BOLD, 30);
    this.pointsLabelHeight = this.getFontMetrics(pointsLabelFont).getHeight();
    this.pointsLabel.setFont(pointsLabelFont);
    this.add(this.pointsLabel);

    // Initialize the question text pane, which may be hidden for some view modes or if there is no question text.
    this.qTextPane = UiService.createDefaultTextPane();
    this.qTextPane.setFont(lafTheme.getEditTableCellFont());
    this.qTextPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.add(this.qTextPane);

    // Initialize the question image label, which may be hidden for some view modes or if there is no question image.
    this.qImageLabel = new JLabel();
    this.qImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.add(this.qImageLabel);

    // Horizontal separator.
    this.mainSeparator = new JLabel(ImageEnum.VERTICAL_SPACER_24.toImageIcon());
    this.mainSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.add(this.mainSeparator);

    // Initialize the answer text pane, which may be hidden for some view modes or if there is no answer text.
    this.aTextPane = UiService.createDefaultTextPane();
    this.aTextPane.setFont(lafTheme.getEditTableCellFont());
    this.aTextPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.add(this.aTextPane);

    // Initialize the answer image label, which may be hidden for some view modes or if there is no answer image.
    this.aImageLabel = new JLabel();
    this.aImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.add(this.aImageLabel);
    this.add(Box.createRigidArea(new Dimension(0, BOX_PADDING)));

    this.activateViewStyle();
  }

  /**
   * Sets the view/print mode (questions, answers, all).
   * @param editTableMode view mode to set
   */
  public void setViewMode(EditTableMode editTableMode) {
    this.editTableMode = editTableMode;
  }

  /**
   * Determines the preferred height of this cell taking into consideration
   * the current view mode and the provided column width. Size/dimension of the cell
   * component is not affected by calling this method.
   * @param columnWidth the width of this cell
   * @return preferred height of this cell
   */
  protected int refreshAndResize(int columnWidth) {
    int totalHeight = 2 * BOX_PADDING + this.pointsLabelHeight + CONTENT_SPACING;
    int availableWidth = columnWidth - 2 * BOX_PADDING - 2 * BORDER_WIDTH;

    // Determine what needs to be displayed.
    boolean modeIncludesQuestions = this.editTableMode == EditTableMode.QUESTIONS || this.editTableMode == EditTableMode.ALL;
    boolean modeIncludesAnswers = this.editTableMode == EditTableMode.ANSWERS || this.editTableMode == EditTableMode.ALL;
    boolean questionVisible = modeIncludesQuestions && !StringUtils.isBlank(this.question.getQuestion());
    boolean qImageVisible = modeIncludesQuestions && this.question.getQuestionImage() != null;
    boolean answerVisible = modeIncludesAnswers && !StringUtils.isBlank(this.question.getAnswer());
    boolean aImageVisible = modeIncludesAnswers && this.question.getAnswerImage() != null;

    // Populate the question text for appropriate view modes. The question pane is
    // hidden for certain view modes or if there is no question text available.
    totalHeight += this.updateQuestionTextPane(questionVisible, availableWidth);

    // Populate the question image for appropriate view modes. The question image label
    // is hidden for certain view modes or if there is no question image available.
    totalHeight += this.updateQuestionImageLabel(qImageVisible, availableWidth);

    // Add separator between question and answer areas if both are present.
    if ((questionVisible || qImageVisible) && (answerVisible || aImageVisible)) {
      this.mainSeparator.setVisible(true);
      totalHeight += SEPARATOR_HEIGHT;
    } else {
      this.mainSeparator.setVisible(false);
    }

    // Populate the answer text pane and image label or clear them as appropriate.
    totalHeight += this.updateAnswerTextPane(answerVisible, availableWidth);
    totalHeight += this.updateAnswerImageLabel(aImageVisible, availableWidth);

    if (answerVisible || aImageVisible) {
      totalHeight += BOX_PADDING;
    }

    return totalHeight;
  }

  /** Activates the cell's view style/presentation. */
  protected void activateViewStyle() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.pointsLabel.setForeground(lafTheme.getGameTableCellTextColor());
    this.qTextPane.setForeground(lafTheme.getGameTableCellTextColor());
    this.qTextPane.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.aTextPane.setForeground(lafTheme.getGameTableCellTextColor());
    this.aTextPane.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.setBorder(viewBorder);
  }

  /** Activates the cell's print style/presentation. */
  protected void activatePrintStyle() {
    this.pointsLabel.setForeground(Color.BLUE);
    this.qTextPane.setForeground(Color.BLACK);
    this.qTextPane.setBackground(Color.WHITE);
    this.aTextPane.setForeground(Color.BLACK);
    this.aTextPane.setBackground(Color.WHITE);
    this.setBackground(Color.WHITE);
    this.setBorder(PRINT_BORDER);
  }

  /**
   * Enlarges images to fill the available space.
   * @param columnWidth total width of a column
   * @param cellHeight max height
   */
  protected void maximizeImages(int columnWidth, int cellHeight) {
    if (!this.qImageLabel.isVisible() && !this.aImageLabel.isVisible()) {
      // No images, nothing to do.
      return;
    }
    int availableHeight = cellHeight - (2 * BOX_PADDING + this.pointsLabelHeight + CONTENT_SPACING);
    int availableWidth = columnWidth - 2 * BOX_PADDING;
    if (this.qTextPane.isVisible()) {
      availableHeight -= getPreferredHeightForText(this.qTextPane.getText(), this.aTextPane.getFont(), availableWidth);
    }

    if (this.mainSeparator.isVisible()) {
      availableHeight -= SEPARATOR_HEIGHT;
    }

    if (this.aTextPane.isVisible()) {
      availableHeight -= getPreferredHeightForText(this.aTextPane.getText(), this.aTextPane.getFont(), availableWidth);
    }
    this.aTextPane.isVisible();

    if (this.qImageLabel.isVisible()) {
      availableHeight = (this.aImageLabel.isVisible() ? availableHeight / 2 : availableHeight) - CONTENT_SPACING;
      ImageUtilities.updateLabelIconImage(
          this.qImageLabel, question.getQuestionImage(), this.gameBundlePath, availableWidth, availableHeight);
    }

    if (this.aImageLabel.isVisible()) {
      ImageUtilities.updateLabelIconImage(
          this.aImageLabel, question.getAnswerImage(), this.gameBundlePath, availableWidth, availableHeight);
    }
  }

  /**
   * Updates the state of the question text pane.
   * @param isVisible true if the question pane should be visible; false if otherwise
   * @param width preferred width for the pane
   * @return height of the text pane or 0 if isVisible is false
   */
  private int updateQuestionTextPane(boolean isVisible, int width) {
    return this.updateTextPaneHelper(this.qTextPane, isVisible ? question.getQuestion() : null, width);
  }

  /**
   * Updates the state of the answer text pane.
   * @param isVisible true if the answer pane should be visible; false if otherwise
   * @param width preferred width for the pane
   * @return height of the text pane or 0 if isVisible is false
   */
  private int updateAnswerTextPane(boolean isVisible, int width) {
    return this.updateTextPaneHelper(this.aTextPane, isVisible ? question.getAnswer() : null, width);
  }

  /**
   * Updates the state of the given text pane.
   * @param textPane text pane to update
   * @param textOrNull text for the text pane or null if the question pane should not be visible
   * @param width preferred width for the pane
   * @return height of the text pane or 0 if the panel is not visible
   */
  private int updateTextPaneHelper(JTextPane textPane, String textOrNull, int width) {
    boolean isVisible = textOrNull != null;
    textPane.setVisible(isVisible);
    if (isVisible) {
      textPane.setText(textOrNull);
      // We only determine the preferred size here and let the layout handle the sizing later.
      return getPreferredHeightForText(textOrNull, textPane.getFont(), width);
    } else {
      textPane.setText(null);
      return 0;
    }
  }

  /**
   * Updates the question image label.
   * @param isVisible true if the question image label should be visible; false if otherwise
   * @param width available width to paint the image in
   * @return height of the image label or 0 if the label is not visible
   */
  private int updateQuestionImageLabel(boolean isVisible, int width) {
    this.qImageLabel.setVisible(isVisible);
    if (isVisible) {
      boolean success = ImageUtilities.updateLabelIconImage(
          this.qImageLabel, question.getQuestionImage(), this.gameBundlePath, width, MAX_IMAGE_HEIGHT);
      if (success) {
        return this.qImageLabel.getIcon().getIconHeight();
      } else {
        this.qImageLabel.setIcon(ImageEnum.IMAGE_FAILURE_64.toImageIcon());
        this.qImageLabel.setToolTipText(
            LocaleService.getString("jj.print.image.failure", question.getQuestionImage()));
        return this.qImageLabel.getIcon().getIconHeight();
      }
    } else {
      return 0;
    }
  }

  /**
   * Updates the answer image label.
   * @param isVisible true if the answer image label should be visible; false if otherwise
   * @param width available width to paint the image in
   * @return height of the image label or 0 if the label is not visible
   */
  private int updateAnswerImageLabel(boolean isVisible, int width) {
    this.aImageLabel.setVisible(isVisible);
    if (isVisible) {
      boolean success = ImageUtilities.updateLabelIconImage(
          this.aImageLabel, question.getAnswerImage(), this.gameBundlePath, width, MAX_IMAGE_HEIGHT);
      if (success) {
        return this.aImageLabel.getIcon().getIconHeight();
      } else {
        this.aImageLabel.setIcon(ImageEnum.IMAGE_FAILURE_64.toImageIcon());
        this.aImageLabel.setToolTipText(
            LocaleService.getString("jj.print.image.failure", question.getAnswerImage()));
        return this.aImageLabel.getIcon().getIconHeight();
      }
    } else {
      return 0;
    }
  }

  /**
   * Measures the height of a question or answer text rendered in a text pane.
   * @param text text being rendered
   * @param font font to use
   * @param width available width of the text pane
   * @return preferred height of the text pane for the given text
   */
  private static int getPreferredHeightForText(String text, Font font, int width) {
    TEST_TEXT_PANE.setSize(width, 500);
    TEST_TEXT_PANE.setText(text);
    TEST_TEXT_PANE.setFont(font);
    return TEST_TEXT_PANE.getPreferredSize().height  + CONTENT_SPACING;
  }
}