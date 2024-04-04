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

package net.curre.jjeopardy.ui.game;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Represents a Game table header cell.
 *
 * @see GameTableCellRenderer
 * @author Yevgeny Nyden
 */
public class GameTableHeaderRenderer extends JPanel implements TableCellRenderer {

  /** Text area where category name is rendered. */
  private final JTextPane textPane;

  /** An invisible text pane to help determining the text areas sizes (not thread safe!). */
  private static final JTextPane HELPER_TEXT_PANE = UiService.createDefaultTextPane();

  /** Padding for the cell content. */
  private static final int CELL_PADDING = 5;

  /** Ctor. */
  public GameTableHeaderRenderer() {
    // Layout helps to vertically center the text content.
    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL}})); // rows

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    HELPER_TEXT_PANE.setFont(lafTheme.getGameTableHeaderFont());
    this.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.textPane = UiService.createDefaultTextPane();
    this.textPane.setFont(lafTheme.getGameTableHeaderFont());
    this.textPane.setForeground(lafTheme.getGameTableHeaderColor());
    this.textPane.setBackground(lafTheme.getGameTableHeaderBackgroundColor());

    this.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(lafTheme.getGameTableBorderColor(), 4),
      BorderFactory.createEmptyBorder(0, CELL_PADDING, 0, CELL_PADDING)));

    this.add(this.textPane, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
  }

  /** {@inheritDoc} */
  @Override
  public Component getTableCellRendererComponent(
    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String text = value.toString().toUpperCase();

    int textWidth = table.getColumnModel().getColumn(column).getWidth() - 2 * CELL_PADDING;
    this.setSize(new Dimension(textWidth, JjDefaults.GAME_TABLE_HEADER_HEIGHT));
    this.textPane.setText(text);

    // Using a non-rendered helper text pane, determine the preferred height of the text.
    HELPER_TEXT_PANE.setSize(textWidth, 500);
    HELPER_TEXT_PANE.setText(text);
    int textHeight = HELPER_TEXT_PANE.getPreferredSize().height;

    // Now, set the preferred height on the real text pane.
    this.textPane.setPreferredSize(new Dimension(textWidth, textHeight));
    this.textPane.setSize(new Dimension(textWidth, textHeight));
    return this;
  }
}
