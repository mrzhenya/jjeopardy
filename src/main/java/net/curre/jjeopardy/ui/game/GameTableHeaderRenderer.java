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
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Represents a Game table header cell.
 *
 * @see GameTableCellRenderer
 * @author Yevgeny Nyden
 */
public class GameTableHeaderRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

  /** Text area where category name is rendered. */
  private final JTextPane textPane;

  /** Ctor. */
  public GameTableHeaderRenderer() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.setLayout(new TableLayout(new double[][] {
      {10, TableLayout.FILL, 10},  // columns
      {TableLayout.FILL}})); // rows
    this.setBackground(lafTheme.getGameTableHeaderBackgroundColor());
    this.textPane = new JTextPane();
    this.textPane.setEditable(false);
    this.textPane.setOpaque(false);
    this.textPane.setFocusable(false);
    this.textPane.setFont(lafTheme.getGameTableHeaderFont());
    this.textPane.setForeground(lafTheme.getGameTableHeaderColor());
    this.textPane.setBackground(lafTheme.getGameTableHeaderBackgroundColor());

    this.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(lafTheme.getGameTableBorderColor(), 4),
      BorderFactory.createEmptyBorder(0, 5, 0, 5)));

    StyledDocument doc = this.textPane.getStyledDocument();
    SimpleAttributeSet center = new SimpleAttributeSet();
    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
    doc.setParagraphAttributes(0, doc.getLength(), center, false);

    this.add(this.textPane, new TableLayoutConstraints(
      1, 0, 1, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));  }

  /** {@inheritDoc} */
  @Override
  public Component getTableCellRendererComponent(
    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    int width = table.getColumnModel().getColumn(column).getWidth();
    this.textPane.setText(value.toString().toUpperCase());
    this.setPreferredSize(new Dimension(width - 20, getPreferredSize().height));
    return this;
  }
}
