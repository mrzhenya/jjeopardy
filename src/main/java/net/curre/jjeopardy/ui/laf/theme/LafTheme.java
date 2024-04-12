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

package net.curre.jjeopardy.ui.laf.theme;

import javax.swing.UIDefaults;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Font;

/**
 * Base class for all LAF theme classes.
 * @author Yevgeny Nyden
 */
public abstract class LafTheme implements LafThemeInterface {

  /** Dark blue color constant. */
  protected static final Color DARK_BLUE_COLOR = new Color(0, 0, 102);

  /** Text color for timer label and game table header text. */
  protected static final Color LIGHT_ORANGE_TEXT_COLOR = new Color(252, 223, 194);

  /** Default button spacing (gap between the buttons). */
  private static final int DEFAULT_BUTTON_SPACING = 10;

  /** Default panel padding (gap between content and panel edge). */
  private static final int DEFAULT_PANEL_PADDING = 15;

  /** Default background color. */
  private Color defaultBackgroundColor;

  /** Font to use for dialogs headers. */
  private Font dialogHeaderFont;

  /** Font to use for dialog text. */
  private Font dialogTextFont;

  /** Font to use for Action buttons. */
  private Font buttonFont;

  /** Font to use for Landing UI labels. */
  private Font landingLabelFont;

  /** Font to use for Question UI title. */
  private Font questionTitleFont;

  /** Font to use for Question UI text. */
  private Font questionTextFont;

  /** Font to use for the Timer counter. */
  private Font timerLabelFont;

  /** Font to use for the Game table header labels. */
  private Font gameTableHeaderFont;

  /** Game table border color. */
  private Color gameTableBorderColor;

  /** Font to use for the Game table text (cell labels). */
  private Font gameTableTextFont;

  /** Font to use for the Game table scores. */
  private Font gameTableScoreFont;

  /** Font to use for the Game table score player text labels. */
  private Font gameTableScorePlayerFont;

  /** View font to use for editing game dialog table header. */
  private Font editTableHeaderFont;

  /** View font to use for editing game dialog table cell. */
  private Font editTableCellFont;

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object other) {
    if (other instanceof LafTheme) {
      return this.getId() == ((LafTheme) other).getId();
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Color getDefaultBackgroundColor() {
    return this.defaultBackgroundColor;
  }

  /** {@inheritDoc} */
  @Override
  public Font getDialogHeaderFont() {
    return this.dialogHeaderFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getDialogTextFont() {
    return this.dialogTextFont;
  }

  /** {@inheritDoc} */
  @Override
  public int getButtonSpacing() {
    return DEFAULT_BUTTON_SPACING;
  }

  /** {@inheritDoc} */
  @Override
  public int getPanelPadding() {
    return DEFAULT_PANEL_PADDING;
  }

  /** {@inheritDoc} */
  @Override
  public Font getButtonFont() {
    return this.buttonFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getLandingLabelFont() {
    return this.landingLabelFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getQuestionTitleFont() {
    return this.questionTitleFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getQuestionTextFont() {
    return this.questionTextFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getTimerLabelFont() {
    return this.timerLabelFont;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableBorderColor() {
    return this.gameTableBorderColor;
  }

  /** {@inheritDoc} */
  @Override
  public Font getGameTableHeaderFont() {
    return this.gameTableHeaderFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getGameTableTextFont() {
    return this.gameTableTextFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getGameTableScoreFont() {
    return this.gameTableScoreFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getGameTableScorePlayerFont() {
    return this.gameTableScorePlayerFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getEditTableHeaderFont() {
    return this.editTableHeaderFont;
  }

  /** {@inheritDoc} */
  @Override
  public Font getEditTableCellFont() {
    return this.editTableCellFont;
  }

  /**
   * Initializes internal style values.
   * @param defaults UI defaults
   */
  protected void initializeInternals(@NotNull UIDefaults defaults) {
    // Convert DerivedColor to simple Color to avoid side effects.
    this.defaultBackgroundColor = new Color(((Color) defaults.get("Panel.background")).getRGB());
    Color gridColor = (Color) defaults.get("Table.gridColor");
    if (gridColor == null) {
      this.gameTableBorderColor = this.defaultBackgroundColor;
    } else {
      this.gameTableBorderColor = new Color(gridColor.getRGB());
    }

    Font currLabelFont = (Font) defaults.get("Label.font");
    this.dialogHeaderFont = currLabelFont.deriveFont(Font.BOLD, currLabelFont.getSize() + 1f);
    this.dialogTextFont = currLabelFont.deriveFont(currLabelFont.getStyle(), currLabelFont.getSize() + 1f);

    Font currButtonFont = (Font) defaults.get("Button.font");
    final int buttonStyle = currButtonFont.getStyle() | Font.BOLD;
    this.buttonFont = currButtonFont.deriveFont(buttonStyle, currButtonFont.getSize() + 1f);

    final int labelStyle = currLabelFont.getStyle() | Font.BOLD;
    this.landingLabelFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 3f);

    this.questionTitleFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 5f);

    this.questionTextFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 8f);

    this.timerLabelFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 30f);

    this.gameTableHeaderFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 8f);

    this.gameTableTextFont = new Font("Arial Black", Font.BOLD, 40);

    this.gameTableScoreFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 18f);

    this.gameTableScorePlayerFont = currLabelFont.deriveFont(labelStyle, currLabelFont.getSize() + 4f);

    this.editTableCellFont = currLabelFont.deriveFont(Font.PLAIN, 16f);
    this.editTableHeaderFont = currLabelFont.deriveFont(Font.BOLD, 20f);
  }
}
