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

import net.curre.jjeopardy.ui.laf.LafService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * Represents a table cell component, which is a panel with an
 * image background and a text label rendered on top of it.
 *
 * @author Yevgeny Nyden
 */
public class TableCell extends JPanel {

  /** Cell border width. */
  private static final int BORDER_WIDTH = 4;

  /** Default cell label font. */
  private static Font defaultFont;

  /** Hovered cell label font. */
  private static Font hoveredFont;

  /** Flag to track font initialization. */
  private static boolean fontsInitialized = false;

  /** Helps to track the first paint. */
  private static boolean firstPaint = true;

  /** Reference to the cell text label. */
  private final JLabel label;

  /** Current cell background image. */
  private Image cellBackgroundImage;

  /** Ctor. */
  public TableCell() {
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();

    this.setLayout(new BorderLayout());
    this.label = new JLabel();
    this.cellBackgroundImage = lafTheme.getGameTableCellBackgroundDefault();
    this.label.setForeground(lafTheme.getGameTableCellTextColor());
    this.label.setHorizontalAlignment(SwingConstants.CENTER);
    this.setBackground(lafTheme.getGameTableBorderColor());

    // Initializing font 'constants' (ok not to be synchronized).
    if (!fontsInitialized) {
      Font font = lafTheme.getGameTableTextFont();
      Map attributes = font.getAttributes();
      attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DASHED);
      attributes.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
      defaultFont = font.deriveFont(attributes);

      Font font2 = font.deriveFont(font.getStyle(), font.getSize() + 2f);
      Map attributes2 = font2.getAttributes();
      attributes2.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DASHED);
      attributes2.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
      hoveredFont = font2.deriveFont(attributes2);

      fontsInitialized = true;
    }
    this.label.setFont(defaultFont);

    this.add(this.label, BorderLayout.CENTER);
  }

  @Override
  protected void paintComponent(Graphics g) {
    this.label.repaint();
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();

    // Hack to avoid first new image painted as black (ok not to be synchronized).
    if (firstPaint) {
      g.drawImage(lafTheme.getGameTableCellBackgroundHover(), BORDER_WIDTH, BORDER_WIDTH,
        this.getWidth() - 2*BORDER_WIDTH, this.getHeight() - 2*BORDER_WIDTH, null);
      g.drawImage(lafTheme.getGameTableCellBackgroundEmpty(), BORDER_WIDTH, BORDER_WIDTH,
        this.getWidth() - 2*BORDER_WIDTH, this.getHeight() - 2*BORDER_WIDTH, null);
      firstPaint = false;
    }
    g.drawImage(this.cellBackgroundImage, BORDER_WIDTH, BORDER_WIDTH,
      this.getWidth() - 2*BORDER_WIDTH, this.getHeight() - 2*BORDER_WIDTH, null);
  }

  /**
   * Sets the value for this cell.
   * @param value value to set
   */
  protected void setValue(Object value) {
    this.label.setText(String.valueOf(value));
  }

  /**
   * Sets this cell to the default render state.
   */
  protected void setToDefaultState() {
    this.label.setFont(defaultFont);
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    this.cellBackgroundImage = lafTheme.getGameTableCellBackgroundDefault();
  }

  /**
   * Sets this cell to the hovered render state.
   */
  protected void setToHoveredState() {
    this.label.setFont(hoveredFont);
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    this.cellBackgroundImage = lafTheme.getGameTableCellBackgroundHover();
  }

  /**
   * Sets this cell to the empty render state.
   */
  protected void setToEmptyState() {
    this.label.setFont(defaultFont);
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    this.cellBackgroundImage = lafTheme.getGameTableCellBackgroundEmpty();
  }
}
