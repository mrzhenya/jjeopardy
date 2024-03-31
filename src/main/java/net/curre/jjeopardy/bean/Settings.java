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

package net.curre.jjeopardy.bean;

import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.laf.LafThemeId;
import net.curre.jjeopardy.util.JjDefaults;

import java.io.Serializable;

import static net.curre.jjeopardy.service.LafService.DEFAULT_LAF_THEME_ID;

/**
 * Object of this class represents a bean for storing
 * application settings (window size, theme, default locale, etc.).
 * The settings are stored on disk in an OS specific destination.
 *
 * @see net.curre.jjeopardy.service.SettingsService
 * @author Yevgeny Nyden
 */
public class Settings implements Serializable {

  /** Serial version number. */
  private static final long serialVersionUID = 120364529132434560L;

  /** The main game window frame width. */
  private int gameWindowWidth;

  /** The main game window frame height. */
  private int gameWindowHeight;

  /** The edit game dialog width. */
  private int editDialogWidth;

  /** The edit game dialog height. */
  private int editDialogHeight;

  /** Look and Feel theme/skin ID. */
  private LafThemeId lafThemeId;

  /** Sound FX only setting. */
  private boolean soundFxOnly;

  /** Sound Off setting. */
  private boolean soundOff;

  /** Locale unique identifier (Locale.toString()). */
  private String localeId;

  /** Last known directory where a game file was open. */
  private String lastCurrentDirectory;

  /**
   * Ctor.
   */
  public Settings() {
    this.gameWindowWidth = JjDefaults.GAME_TABLE_MIN_WIDTH;
    this.gameWindowHeight = JjDefaults.GAME_TABLE_MIN_HEIGHT;
    this.editDialogWidth = JjDefaults.EDIT_GAME_DIALOG_MIN_WIDTH;
    this.editDialogHeight = JjDefaults.EDIT_GAME_DIALOG_MIN_HEIGHT;
    this.lafThemeId = DEFAULT_LAF_THEME_ID;
    this.soundFxOnly = false;
    this.soundOff = false;
    this.localeId = LocaleService.DEFAULT_LOCALE.toString();
    this.lastCurrentDirectory = System.getProperty("user.home");
  }

  /**
   * Getter for the main window frame width.
   * @return The main window frame width
   */
  public int getGameWindowWidth() {
    return this.gameWindowWidth;
  }

  /**
   * Setter for the main game window frame width.
   * @param gameWindowWidth Main game window frame width
   */
  public void setGameWindowWidth(int gameWindowWidth) {
    this.gameWindowWidth = gameWindowWidth;
  }

  /**
   * Getter for the main game window frame height.
   * @return The main game window frame height
   */
  public int getGameWindowHeight() {
    return this.gameWindowHeight;
  }

  /**
   * Setter for the main game window frame height.
   * @param gameWindowHeight Main game window frame height
   */
  public void setGameWindowHeight(int gameWindowHeight) {
    this.gameWindowHeight = gameWindowHeight;
  }

  /**
   * Getter for the edit game dialog width.
   * @return The edit game dialog width
   */
  public int getEditDialogWidth() {
    return this.editDialogWidth;
  }

  /**
   * Setter for the edit game dialog width.
   * @param editDialogWidth edit game dialog width
   */
  public void setEditDialogWidth(int editDialogWidth) {
    this.editDialogWidth = editDialogWidth;
  }

  /**
   * Getter for the edit game dialog height.
   * @return The edit game dialog height
   */
  public int getEditDialogHeight() {
    return this.editDialogHeight;
  }

  /**
   * Setter for the edit game dialog height.
   * @param editDialogHeight edit game dialog height
   */
  public void setEditDialogHeight(int editDialogHeight) {
    this.editDialogHeight = editDialogHeight;
  }

  /**
   * Getter for the Look and Feel theme ID.
   * @return The Look and Feel theme ID
   */
  public LafThemeId getLafThemeId() {
    return this.lafThemeId;
  }

  /**
   * Setter for the Look and Feel theme/skin ID (resource key).
   * @param lafThemeId The Look and Feel theme/skin ID (resource key)
   */
  public void setLafThemeId(LafThemeId lafThemeId) {
    this.lafThemeId = lafThemeId;
  }

  /**
   * Enables all sound effects.
   */
  public void enableAllSound() {
    this.soundFxOnly = false;
    this.soundOff = false;
  }

  /**
   * Enables only sound effects.
   */
  public void enableSoundEffectsOnly() {
    this.soundFxOnly = true;
    this.soundOff = false;
  }

  /**
   * Disables all sound effects.
   */
  public void disableAllSound() {
    this.soundFxOnly = false;
    this.soundOff = true;
  }

  /**
   * Determines if all sound should be on.
   * @return true if all sound should be on
   */
  public boolean isAllSoundOn() {
    return !this.soundFxOnly && !this.soundOff;
  }

  /**
   * Determines if only the sound effects should be on.
   * @return true if only the sound effects should be on
   */
  public boolean isSoundEffectsOnly() {
    return this.soundFxOnly;
  }

  /**
   * Determines if the sound should be off.
   * @return true if all sound should be off
   */
  public boolean isAllSoundOff() {
    return this.soundOff;
  }

  /**
   * Getter for the locale identifier.
   * @return The locale ID (Locale.toString())
   */
  public String getLocaleId() {
    return this.localeId;
  }

  /**
   * Setter for the locale unique identifier.
   * @param localeId locale ID (Locale.toString())
   */
  public void setLocaleId(String localeId) {
    this.localeId = localeId;
  }

  /**
   * Gets the last known directory user visited via the file chooser dialog.
   * @return last known directory
   */
  public String getLastCurrentDirectory() {
    return this.lastCurrentDirectory;
  }

  /**
   * Sets the last known directory user visited via the file chooser dialog.
   * @param currentDirectory last known current directory
   */
  public void setLastCurrentDirectory(String currentDirectory) {
    this.lastCurrentDirectory = currentDirectory;
  }
}
