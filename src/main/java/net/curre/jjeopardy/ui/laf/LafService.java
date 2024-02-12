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

package net.curre.jjeopardy.ui.laf;

import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.ServiceException;
import net.curre.jjeopardy.ui.laf.theme.*;
import net.curre.jjeopardy.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsible for some common Look and Feel support.
 * Use #getInstance method to obtain an instance of this service, and
 * make sure to call #initialize before creating any UI.
 *
 * @author Yevgeny Nyden
 */
public class LafService {

  /** Default width for the table. */
  public static final int DEFAULT_GAME_TABLE_WIDTH = 500;

  /** Default height for the table. */
  public static final int DEFAULT_GAME_TABLE_HEIGHT = 600;

  /** Rows' height will not be smaller than this value. */
  public static final double GAME_TABLE_MIN_ROW_HEIGHT = 50;

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(LafService.class.getName());

  /** Default LAF theme. */
  public static final LafThemeId DEFAULT_LAF_THEME_ID = LafThemeId.FLAT_LIGHT;

  /** Holds a reference to the singleton instance. */
  private static final LafService INSTANCE = new LafService();

  /** Array of available themes/skins. */
  private static ArrayList<LafTheme> SUPPORTED_LAF_THEMES;

  /** Current theme ID. */
  private LafThemeId currentLafThemeId;

  /**
   * Reference to the known Window UI components, UI tree roots to update.
   * We handle it "manually" because Frame.getFrames doesn't have them all.
   */
  private static final ArrayList<Window> componentsRegistry = new ArrayList<>();

  /** Private constructor. */
  private LafService() {
    this.currentLafThemeId = DEFAULT_LAF_THEME_ID;
  }

  /**
   * Returns an instance of this class to use.
   * @return singleton instance of this class to use
   */
  public static LafService getInstance() {
    return INSTANCE;
  }

  /**
   * Gets verified, supported LAF themes that could be used by the app.
   * @return supported LAF themes
   */
  public ArrayList<LafTheme> getSupportedThemes() {
    synchronized (this) {
      if (SUPPORTED_LAF_THEMES == null) {
        SUPPORTED_LAF_THEMES = new ArrayList<>();
        SUPPORTED_LAF_THEMES.add(new DefaultTheme());
        SUPPORTED_LAF_THEMES.add(new FlatLightTheme());
        SUPPORTED_LAF_THEMES.add(new FlatDarkTheme());
        if (this.systemLafThemePresent(NimbusTheme.LAF_CLASS_NAME)) {
          SUPPORTED_LAF_THEMES.add(new NimbusTheme());
        }
        if (Utilities.isMacOs()) {
          SUPPORTED_LAF_THEMES.add(new FlatMacDarkTheme());
        }
      }
    }
    return SUPPORTED_LAF_THEMES;
  }

  /**
   * Initializes the service before any UI is rendered.
   */
  public void initialize() {
    // For Mac OS, do a few extra things.
    // https://www.formdev.com/flatlaf/macos/
    if (Utilities.isMacOs()) {
      // Moves the menu bar to the top of the screen.
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("apple.awt.application.name", LocaleService.getString("jj.app.name"));

      // Support of the system dark/light theme.
      System.setProperty( "apple.awt.application.appearance", "system" );
    }
  }

  /**
   * Determines the default LAF theme based on the current OS.
   * @return default LAF theme
   */
  public LafThemeId getDefaultLafThemeId() {
    LafThemeId lafThemeId = FlatLightTheme.LAF_THEME_ID;
    if (Utilities.isMacOs()) {
      if (this.systemLafThemePresent(DefaultTheme.LAF_CLASS_NAME)) {
        lafThemeId = DefaultTheme.LAF_THEME_ID;
      }
    }
    return lafThemeId;
  }

  /**
   * Gets the current LAF theme ID.
   * @return current LAF theme ID
   */
  public LafThemeId getCurrentLafThemeId() {
    return this.currentLafThemeId;
  }

  /**
   * Gets the current LAF theme.
   * @return current LAF theme
   */
  public LafTheme getCurrentLafTheme() {
    try {
      return findLafThemeById(this.currentLafThemeId);
    } catch (ServiceException e) {
      // This should never occur, only at development time.
      LOGGER.log(Level.SEVERE, "Unable to set LAF theme: " + this.currentLafThemeId, e);
      System.exit(1);
    }
    return null;
  }

  /**
   * UI tree of the window components registered here will be updated on
   * LAF theme changes.
   * @param component window UI component to register
   */
  public static void registerUITreeForUpdates(Window component) {
    componentsRegistry.add(component);
  }

  /**
   * Sets the Look and Feel to the passed theme and updates the UI.
   * @param lafThemeId Theme ID to set the LaF to.
   */
  public void activateLafTheme(final LafThemeId lafThemeId) {
    JFrame.setDefaultLookAndFeelDecorated(true);
    try {
      LOGGER.info("Activating LAF theme " + lafThemeId);
       if (findLafThemeById(lafThemeId).activateTheme()) {
        this.currentLafThemeId = lafThemeId;

        // Updating the UI of registered components (fyi, Frame.getFrames doesn't have them all).
         for (Window component : componentsRegistry) {
           SwingUtilities.updateComponentTreeUI(component);
           component.pack();
         }
      } else {
        LOGGER.warning("Unable to set LAF theme: " + lafThemeId);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to set LAF theme: " + lafThemeId, e);
    }
  }

  /**
   * Checks if the passed LAF theme is present in the UIManager.
   * @param lafClassName class name of the theme class
   * @return true if the passed theme is installed in UIManager; false if otherwise
   */
  private boolean systemLafThemePresent(String lafClassName) {
    for (UIManager.LookAndFeelInfo installedLaf : UIManager.getInstalledLookAndFeels()) {
      if (lafClassName.equals(installedLaf.getClassName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the LAF theme given its ID.
   * @param lafThemeId theme ID
   * @return The them with the given ID.
   * @throws ServiceException If theme with given ID was not found.
   */
  private static LafTheme findLafThemeById(LafThemeId lafThemeId) throws ServiceException {
    for (LafTheme theme : LafService.getInstance().getSupportedThemes()) {
      if (theme.getId().equals(lafThemeId)) {
        return theme;
      }
    }
    throw new ServiceException("Theme with id \"" + lafThemeId + "\" was not found!");
  }
}
