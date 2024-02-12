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

import net.curre.jjeopardy.ui.laf.LafThemeId;

import java.awt.*;

/**
 * Interface to represent a jjeopardy Look and Feel theme.
 *
 * @author Yevgeny Nyden
 */
public interface LafThemeInterface {

  /**
   * Gets a unique ID of this theme.
   *
   * @return theme ID enum
   */
  LafThemeId getId();

  /**
   * Returns the skin resource key, which
   * also the skin's unique identifier.
   *
   * @return the skin resource key and it's unique ID.
   */
  String getNameResourceKey();

  /**
   * Loads the theme in the UIManager.
   * @return true if activation was successful; false if otherwise.
   * @throws Exception on error
   */
  boolean activateTheme() throws Exception;

  /**
   * Gets default background color.
   * @return default background color
   */
  Color getDefaultBackgroundColor();

  /**
   * Gets the default dialog header font.
   * @return dialog header font
   */
  Font getDialogHeaderFont();

  /**
   * Gets the default dialog text font.
   * @return dialog text font
   */
  Font getDialogTextFont();

  /**
   * Width of the spacing to use between the buttons.
   * @return button spacing
   */
  int getButtonSpacing();

  /**
   * Panel padding (gap between content and panel edge).
   * @return panel padding
   */
  int getPanelPadding();

  /**
   * Gets the Font to use on action buttons.
   * @return button Font
   */
  Font getButtonFont();

  /**
   * Gets Font to use on Labels on the Landing UI.
   * @return label Font for landing UI
   */
  Font getLandingLabelFont();

  /**
   * Gets the Font to use on labels on the Question UI title.
   * @return label Font for question UI title
   */
  Font getQuestionTitleFont();

  /**
   * Gets the Font to use on labels on the Question UI text.
   * @return label Font for question UI text
   */
  Font getQuestionTextFont();

  /**
   * Gets the timer label font color.
   * @return timer label color
   */
  Color getTimerLabelColor();

  /**
   * Gets the timer label font.
   * @return timer label font
   */
  Font getTimerLabelFont();

  /**
   * Gets the game table cell label color.
   * @return game table cell text color
   */
  Color getGameTableCellTextColor();

  /**
   * Gets the game table cell default background image.
   * @return default game table cell background image
   */
  Image getGameTableCellBackgroundDefault();

  /**
   * Gets the game table hover cell background image.
   * @return hover game table cell background image
   */
  Image getGameTableCellBackgroundHover();

  /**
   * Gets the game table empty cell background image.
   * @return empty game table cell background image
   */
  Image getGameTableCellBackgroundEmpty();

  /**
   * Gets the game table header font.
   * @return game table header font
   */
  Font getGameTableHeaderFont();

  /**
   * Gets the game table header font color.
   * @return game table header label color
   */
  Color getGameTableHeaderColor();

  /**
   * Gets the game table header background font color.
   * @return game table header background color
   */
  Color getGameTableHeaderBackgroundColor();

  /**
   * Gets the game table text (cell tables) font.
   * @return game table text font
   */
  Font getGameTableTextFont();

  /**
   * Gets the game table border color.
   * @return game table border color
   */
  Color getGameTableBorderColor();

  /**
   * Gets the game table score font.
   * @return game table score font
   */
  Font getGameTableScoreFont();

  /**
   * Gets the game table score player text font.
   * @return game table score player text font
   */
  Font getGameTableScorePlayerFont();

  /**
   * Gets the game table score player background color.
   * @return game table score player background color
   */
  Color getGameTableScorePlayerBackground();
}
