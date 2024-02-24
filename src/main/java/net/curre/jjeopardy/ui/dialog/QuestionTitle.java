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

package net.curre.jjeopardy.ui.dialog;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;

/**
 * Question title rendered in the question dialog.
 *
 * @see QuestionDialog
 * @author Yevgeny Nyden
 */
public class QuestionTitle extends JPanel {

  /** Label for the Question category. */
  private final JLabel categoryLabel;

  /** Label for the points. */
  private final JLabel pointsLabel;

  /**
   * Ctor.
   */
  public QuestionTitle() {
    this.categoryLabel = new JLabel();
    this.pointsLabel = new JLabel();

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int spacing = lafTheme.getButtonSpacing();

    this.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, spacing, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {TableLayout.FILL}}));  // rows

    final Font labelFont = lafTheme.getQuestionTitleFont();
    this.categoryLabel.setFont(labelFont);
    this.add(this.categoryLabel, new TableLayoutConstraints(
      1, 0, 1, 0, TableLayout.CENTER, TableLayout.CENTER));

    final Font pointsFont = labelFont.deriveFont(labelFont.getStyle() | Font.BOLD, labelFont.getSize() + 4f);
    this.pointsLabel.setFont(pointsFont);
    this.pointsLabel.setForeground(lafTheme.getTimerLabelColor());
    JPanel wrapPanel = new JPanel();
    wrapPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    wrapPanel.add(this.pointsLabel);
    this.add(wrapPanel, new TableLayoutConstraints(
      3, 0, 3, 0, TableLayout.CENTER, TableLayout.CENTER));
  }

  /**
   * Resets text on all labels.
   */
  protected void reset() {
    this.categoryLabel.setText("");
    this.pointsLabel.setText("");
  }

  /**
   * Updates the title for the given question.
   * @param question question to use for this title
   */
  protected void updateTitle(Question question) {
    this.categoryLabel.setText(question.getParentName());
    this.pointsLabel.setText(" " + question.getPoints() + " ");
  }
}
