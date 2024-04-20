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
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.event.EditTableMouseListener;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.images.ImageUtilities;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Individual cell where a question data is rendered for the edit/print table.
 *
 * @author Yevgeny Nyden
 */
public class EditCell extends JLayeredPane implements EditableCell {

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
  private static final JTextPane HELPER_TEXT_PANE = UiService.createDefaultTextPane();

  /** The question to use for this cell. */
  private final Question question;

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Column index of this cell (do not assume it's always the same!). */
  private int columnIndex;

  /** Row index of this cell (do not assume it's always the same!). */
  private int rowIndex;

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

  /** Overlay with buttons to move or erase this question. */
  private final QuestionOverlay editOverlay;

  /**
   * Ctor. Note that cell's column and row index has to be initialized by calling the
   * <code>setColumnAndRowIndexes</code> method after a row is created. These indexes could
   * change while the game data is being edited.
   * @param columnIndex column index (the index of the category in the game data)
   * @param rowIndex row index (the index of the question on the category questions list)
   * @param editTable reference to the edit table; not nullable
   */
  public EditCell(int columnIndex, int rowIndex, EditTable editTable) {
    this.columnIndex = columnIndex;
    this.rowIndex = rowIndex;
    Category category = editTable.getGameData().getCategories().get(columnIndex);
    this.question = category.getQuestion(rowIndex);
    this.editTable = editTable;
    EditTable.hoveredCell = null;

    this.setOpaque(true);
    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL}})); // rows
    JPanel wrapPanel = new JPanel();
    wrapPanel.setOpaque(false);
    wrapPanel.setLayout(new BoxLayout(wrapPanel, BoxLayout.PAGE_AXIS));
    this.add(wrapPanel, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.TOP));

    // Initialize the layout and the default, view border.
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
    wrapPanel.add(this.pointsLabel);

    // Initialize the question text pane, which may be hidden for some view modes or if there is no question text.
    EditTableMouseListener mouseListener = this.editTable.getTableMouseListener();
    this.qTextPane = UiService.createDefaultTextPane();
    this.qTextPane.setFont(lafTheme.getEditTableCellFont());
    this.qTextPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.qTextPane.addMouseListener(mouseListener);
    this.qTextPane.addMouseMotionListener(mouseListener);
    wrapPanel.add(this.qTextPane);

    // Initialize the question image label, which may be hidden for some view modes or if there is no question image.
    this.qImageLabel = new JLabel();
    this.qImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    wrapPanel.add(this.qImageLabel);

    // Horizontal line separator.
    this.mainSeparator = new JLabel(ImageEnum.VERTICAL_SPACER_24.toImageIcon());
    this.mainSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
    wrapPanel.add(this.mainSeparator);

    // Initialize the answer text pane, which may be hidden for some view modes or if there is no answer text.
    this.aTextPane = UiService.createDefaultTextPane();
    this.aTextPane.setFont(lafTheme.getEditTableCellFont());
    this.aTextPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.aTextPane.addMouseListener(mouseListener);
    this.aTextPane.addMouseMotionListener(mouseListener);
    wrapPanel.add(this.aTextPane);

    // Initialize the answer image label, which may be hidden for some view modes or if there is no answer image.
    this.aImageLabel = new JLabel();
    this.aImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    wrapPanel.add(this.aImageLabel);
    wrapPanel.add(Box.createRigidArea(new Dimension(0, BOX_PADDING)));

    this.addMouseMotionListener(mouseListener);
    this.addMouseListener(mouseListener);

    this.editOverlay = new QuestionOverlay(columnIndex, rowIndex, editTable);
    this.editOverlay.setVisible(false);
    int questionsCount = this.editTable.getGameData().getCategories().get(columnIndex).getQuestionsCount();
    if (rowIndex == 0) {
      this.editOverlay.setUpMoveEnabled(false);
    } else if (rowIndex == questionsCount - 1) {
      this.editOverlay.setDownMoveEnabled(false);
    }
    if (questionsCount <= JjDefaults.MIN_NUMBER_OF_QUESTIONS) {
      this.editOverlay.setRemoveEnabled(false);
    }
    this.add(this.editOverlay, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.RIGHT, TableLayout.CENTER), 3);
    this.moveToFront(this.editOverlay);

    this.activateViewStyle();
  }

  /** @inheritDoc */
  public void showEditDialog() {
    EditQuestionDialog editDialog = new EditQuestionDialog(this, this.editTable);
    editDialog.setLocationRelativeTo(null);
    editDialog.setVisible(true);
  }

  /** @inheritDoc */
  public void decorateHoverState(boolean isHovered) {
    this.editOverlay.setVisible(isHovered);
    Color background = EditTable.decorateHoverStateHelper(this, isHovered);
    this.qTextPane.setBackground(background);
    this.aTextPane.setBackground(background);
    this.repaint();
  }

  /**
   * Updates the relative row index of this cell and its overlay. Note that the up button
   * will be disabled by default on the cell with index 0.
   * @param newIndex the new index of this cell
   * @param downEnabled true to enable the Down arrow button
   * @param removeEnabled true to enable the Remove row button
   */
  public void updateRowIndexAndOverlay(int newIndex, boolean downEnabled, boolean removeEnabled) {
    this.rowIndex = newIndex;
    this.editOverlay.updateState(newIndex, downEnabled, removeEnabled);
  }

  /**
   * Gets the cell's question reference.
   * @return the Question object for this cell
   */
  protected Question getQuestion() {
    return this.question;
  }

  /**
   * Gets the current column index of this cell.
   * @return column index of this cell
   */
  protected int getColumnIndex() {
    return this.columnIndex;
  }

  /**
   * Gets the current row index of this cell.
   * @return row index of this cell
   */
  protected int getRowIndex() {
    return this.rowIndex;
  }

  /**
   * Updates the index of this cell relative to other cells in the same row.
   * @param newIndex the new index of this cell
   */
  protected void updateColumnIndex(int newIndex) {
    this.columnIndex = newIndex;
    this.editOverlay.updateCategoryIndex(newIndex);
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
    EditTableMode editTableMode = this.editTable.getEditTableMode();

    // Refresh the points value that is always displayed.
    this.pointsLabel.setText(String.valueOf(this.question.getPoints()));

    // Determine what needs to be displayed.
    boolean modeIncludesQuestions = editTableMode == EditTableMode.QUESTIONS || editTableMode == EditTableMode.ALL;
    boolean modeIncludesAnswers = editTableMode == EditTableMode.ANSWERS || editTableMode == EditTableMode.ALL;
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
    Color background = lafTheme.getGameTableHeaderBackgroundColor();
    this.pointsLabel.setForeground(lafTheme.getGameTableCellTextColor());
    this.qTextPane.setForeground(lafTheme.getGameTableCellTextColor());
    this.qTextPane.setBackground(background);
    this.aTextPane.setForeground(lafTheme.getGameTableCellTextColor());
    this.aTextPane.setBackground(background);
    this.setBackground(background);
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

    String bundlePath = this.editTable.getGameBundlePath();
    if (this.qImageLabel.isVisible()) {
      availableHeight = (this.aImageLabel.isVisible() ? availableHeight / 2 : availableHeight) - CONTENT_SPACING;
      ImageUtilities.updateLabelIconImage(
          this.qImageLabel, question.getQuestionImage(), bundlePath, availableWidth, availableHeight);
    }

    if (this.aImageLabel.isVisible()) {
      ImageUtilities.updateLabelIconImage(
          this.aImageLabel, question.getAnswerImage(), bundlePath, availableWidth, availableHeight);
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
  private int updateTextPaneHelper(@NotNull JTextPane textPane, String textOrNull, int width) {
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
          this.qImageLabel, question.getQuestionImage(), this.editTable.getGameBundlePath(), width, MAX_IMAGE_HEIGHT);
      if (!success) {
        this.qImageLabel.setIcon(ImageEnum.IMAGE_FAILURE_64.toImageIcon());
        this.qImageLabel.setToolTipText(
            LocaleService.getString("jj.print.image.failure", question.getQuestionImage()));
      }
      return this.qImageLabel.getIcon().getIconHeight();
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
          this.aImageLabel, question.getAnswerImage(), this.editTable.getGameBundlePath(), width, MAX_IMAGE_HEIGHT);
      if (!success) {
        this.aImageLabel.setIcon(ImageEnum.IMAGE_FAILURE_64.toImageIcon());
        this.aImageLabel.setToolTipText(
            LocaleService.getString("jj.print.image.failure", question.getAnswerImage()));
      }
      return this.aImageLabel.getIcon().getIconHeight();
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
    HELPER_TEXT_PANE.setSize(width, 500);
    HELPER_TEXT_PANE.setText(text);
    HELPER_TEXT_PANE.setFont(font);
    return HELPER_TEXT_PANE.getPreferredSize().height  + CONTENT_SPACING;
  }
}
