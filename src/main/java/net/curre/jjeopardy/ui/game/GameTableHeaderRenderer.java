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

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * Represents a Game table header cell.
 *
 * @see GameTableCellRenderer
 * @author Yevgeny Nyden
 */
public class GameTableHeaderRenderer extends JPanel implements TableCellRenderer {

  /** Text area where category name is rendered. */
  private final JTextPane textPane;

  /** Ctor. */
  public GameTableHeaderRenderer() {
    this.setLayout(new GridBagLayout());

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
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

    this.add(this.textPane, new GridBagConstraints());
  }

  /** {@inheritDoc} */
  @Override
  public Component getTableCellRendererComponent(
    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    String text = value.toString().toUpperCase();
    this.textPane.setText(text);
    int width = table.getColumnModel().getColumn(column).getWidth();
    int height = UiService.getHeightOfTextArea(this, lafTheme.getGameTableHeaderFont(), text, width, 2);
    this.textPane.setPreferredSize(new Dimension(width, height));
    this.setPreferredSize(new Dimension(width, height));
    return this;
  }
}
