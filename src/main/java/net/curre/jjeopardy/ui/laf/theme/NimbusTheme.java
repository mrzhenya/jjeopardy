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

import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.ui.laf.LafThemeId;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Image;

/**
 * LAF theme from defaults (ignored if it's not present).
 * @author Yevgeny Nyden
 */
public class NimbusTheme extends LafTheme {

  /** Theme ID. */
  public static final LafThemeId LAF_THEME_ID = LafThemeId.NIMBUS;

  /** Class name of this LAF to use with UIManager. */
  public static final String LAF_CLASS_NAME = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

  /** Game table cell text color. */
  private static final Color GAME_TABLE_CELL_TEXT_COLOR = new Color(255, 255, 250);

  /** Default game table cell background image. */
  private static final Image GAME_TABLE_CELL_BACKGROUND_DEFAULT = ImageEnum.CELL_NIMBUS.toImage();

  /** Hover game table cell background image. */
  private static final Image GAME_TABLE_CELL_BACKGROUND_HOVER = ImageEnum.CELL_NIMBUS_HOVER.toImage();

  /** Empty game table cell background image. */
  private static final Image GAME_TABLE_CELL_BACKGROUND_EMPTY = ImageEnum.CELL_NIMBUS_EMPTY.toImage();

  /** Game table header text color. */
  public static final Color GAME_TABLE_HEADER_COLOR = new Color(253, 216, 179);

  /** Game table header background color. */
  public static final Color GAME_TABLE_HEADER_BACKGROUND_COLOR = new Color(32, 45, 182);

  /** Game table score panel background color. */
  public static final Color GAME_TABLE_SCORE_PLAYER_BACKGROUND = new Color(214, 211, 231);

  /** {@inheritDoc} */
  @Override
  public LafThemeId getId() {
    return LAF_THEME_ID;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isDarkTheme() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public String getNameResourceKey() {
    return "jj.laf.theme.nimbus.name";
  }

  /** {@inheritDoc} */
  @Override
  public boolean activateTheme() throws Exception {
    UIManager.setLookAndFeel(LAF_CLASS_NAME);
    this.initializeInternals(UIManager.getDefaults());
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Color getTimerLabelColor() {
    return DARK_BLUE_COLOR;
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
    return GAME_TABLE_HEADER_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableHeaderBackgroundColor() {
    return GAME_TABLE_HEADER_BACKGROUND_COLOR;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableScorePlayerBackground() {
    return GAME_TABLE_SCORE_PLAYER_BACKGROUND;
  }

  /** {@inheritDoc} */
  @Override
  public Color getGameTableBorderColor() {
    return Color.WHITE;
  }
}
