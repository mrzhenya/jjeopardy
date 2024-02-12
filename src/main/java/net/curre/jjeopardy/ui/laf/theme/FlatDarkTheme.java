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

import com.formdev.flatlaf.FlatDarkLaf;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.ui.laf.LafThemeId;

import java.awt.Color;
import java.awt.Image;

/**
 * LAF theme from <a href="https://www.formdev.com/flatlaf/">formdev</a>.
 * @author Yevgeny Nyden
 */
public class FlatDarkTheme extends LafTheme {

  /** Theme ID. */
  public static final LafThemeId LAF_THEME_ID = LafThemeId.FLAT_DARK;

  /** Class name of this LAF to use with UIManager. */
  public static final String LAF_CLASS_NAME = "com.formdev.flatlaf.FlatDarkLaf";

  /** Game table cell text color. */
  private static final Color GAME_TABLE_CELL_TEXT_COLOR = new Color(255, 255, 204);

  /** Default game table cell background image. */
  private static final Image GAME_TABLE_CELL_BACKGROUND_DEFAULT = ImageEnum.CELL_DEFAULT.toImage();

  /** Hover game table cell background image. */
  private static final Image GAME_TABLE_CELL_BACKGROUND_HOVER = ImageEnum.CELL_DEFAULT_HOVER.toImage();

  /** Empty game table cell background image. */
  private static final Image GAME_TABLE_CELL_BACKGROUND_EMPTY = ImageEnum.CELL_MACDARK_EMPTY.toImage();

  /** Game table header background color. */
  private static final Color GAME_TABLE_HEADER_BACKGROUND_COLOR = new Color(39, 41, 58);

  /** Game table border color. */
  public static final Color GAME_TABLE_BORDER_COLOR = new Color(70, 73, 75);

  /** {@inheritDoc} */
  @Override
  public LafThemeId getId() {
    return LAF_THEME_ID;
  }

  /** {@inheritDoc} */
  @Override
  public String getNameResourceKey() {
    return "jj.laf.theme.flatdark.name";
  }

  /** {@inheritDoc} */
  @Override
  public boolean activateTheme() {
    FlatDarkLaf theme = new FlatDarkLaf();
    boolean result = FlatDarkLaf.setup(theme);
    this.initializeInternals(theme.getDefaults());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public Color getTimerLabelColor() {
    return LIGHT_ORANGE_TEXT_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableCellTextColor() {
    return GAME_TABLE_CELL_TEXT_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Image getGameTableCellBackgroundDefault() {
    return GAME_TABLE_CELL_BACKGROUND_DEFAULT;
  }

  /** {@inheritDoc} */
  @Override
  public Image getGameTableCellBackgroundHover() {
    return GAME_TABLE_CELL_BACKGROUND_HOVER;
  }

  /** {@inheritDoc} */
  @Override
  public Image getGameTableCellBackgroundEmpty() {
    return GAME_TABLE_CELL_BACKGROUND_EMPTY;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableHeaderColor() {
    return LIGHT_ORANGE_TEXT_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableHeaderBackgroundColor() {
    return GAME_TABLE_HEADER_BACKGROUND_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableBorderColor() {
    return GAME_TABLE_BORDER_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableScorePlayerBackground() {
    return GAME_TABLE_HEADER_BACKGROUND_COLOR;
  }
}
