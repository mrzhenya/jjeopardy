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
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Represents a table header cell where game category name
 * (likely a multi-line string) is rendered.
 *
 * @author Yevgeny Nyden
 */
public class EditHeaderCell extends JPanel {

  /** Background color to use for print. */
  private static final Color PRINT_BACKGROUND_COLOR = new Color(210, 233, 248);

  /** An invisible text pane to help determining the text areas sizes (not thread safe!). */
  private static final JTextPane HELPER_TEXT_PANE = UiService.createDefaultTextPane();

  /** Text area where category name is rendered. */
  private final JTextPane textPane;

  /**
   * Ctor.
   * @param name header cell text (category name)
   */
  public EditHeaderCell(String name) {
    // Layout helps to vertically center the text content.
    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL}})); // rows

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.textPane = UiService.createDefaultTextPane();
    this.textPane.setFont(lafTheme.getEditTableHeaderFont());
    HELPER_TEXT_PANE.setFont(lafTheme.getEditTableHeaderFont());
    if (!StringUtils.isBlank(name)) {
      this.textPane.setText(name.toUpperCase());
    }

    this.add(this.textPane, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
    this.activateViewStyle();
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
