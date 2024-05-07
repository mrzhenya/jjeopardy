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

package net.curre.jjeopardy.ui.landing;

import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.LafService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.edit.EditGameWindow;
import net.curre.jjeopardy.ui.edit.EditInfoDialog;
import net.curre.jjeopardy.ui.edit.EditTableMode;
import net.curre.jjeopardy.ui.laf.theme.LafThemeInterface;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.validation.constraints.NotNull;
import java.awt.event.KeyEvent;
import java.util.Locale;

/**
 * Represents the menu displayed on the Landing UI.
 * @author Yevgeny Nyden
 */
public class LandingUiMenu extends JMenuBar {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(LandingUiMenu.class.getName());

  /** Reference to the landing UI. */
  private final LandingUi landingUi;

  /** Reference to the print answers menu item. */
  private JMenuItem printMenuItem;

  /** Reference to the EditInfoDialog. */
  private EditInfoDialog gameEditInfoDialog;

  /** Ctor. */
  public LandingUiMenu(LandingUi landingUi) {
    this.landingUi = landingUi;
    JMenu settingsMenu = new JMenu(LocaleService.getString("jj.landing.menu.title"));
    settingsMenu.add(createThemeMenu());
    settingsMenu.add(createSoundMenu());
    settingsMenu.add(createLocaleMenu());
    settingsMenu.addSeparator();
    settingsMenu.add(createAboutItem());
    settingsMenu.addSeparator();
    settingsMenu.add(createExitItem());
    this.add(settingsMenu);

    JMenu gameMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.game"));
    gameMenu.add(createNewGameMenuItem());
    gameMenu.add(createPrintGameMenuItem());
    this.add(gameMenu);
  }

  /**
   * Updates the enabled/disabled status of the print answer menu item.
   * @param enabled true if the menu item should be enabled; false if otherwise
   */
  public void updatePrintMenuItem(boolean enabled) {
    this.printMenuItem.setEnabled(enabled);
  }

  /**
   * Creates the LAF Themes settings menu group.
   * @return themes menu
   */
  private @NotNull JMenu createThemeMenu() {
    JMenu themeMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.themes"));
    ButtonGroup themesGroup = new ButtonGroup();
    LafService lafService = AppRegistry.getInstance().getLafService();
    for (LafThemeInterface lafTheme : lafService.getSupportedThemes()) {
      JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(
          LocaleService.getString(lafTheme.getNameResourceKey()));
      if (lafService.getCurrentLafTheme().equals(lafTheme)) {
        themeItem.setSelected(true);
      }
      themesGroup.add(themeItem);
      themeMenu.add(themeItem);
      themeItem.addActionListener(evt -> {
        try {
          lafService.activateLafTheme(lafTheme.getId());
          Registry registry = AppRegistry.getInstance();
          SettingsService settingsService = registry.getSettingsService();
          settingsService.getSettings().setLafThemeId(lafService.getCurrentLafThemeId());
          settingsService.persistSettings();
          registry.getLandingUi().updateLandingUi();
          registry.getUiService().showRestartGameDialog();
        } catch (Exception e) {
          logger.log(Level.WARN,"Unable to save settings.", e);
        }
      });
    }
    return themeMenu;
  }

  /**
   * Creates the Sound settings menu group.
   * @return sounds menu
   */
  private @NotNull JMenu createSoundMenu() {
    SettingsService settingsService = AppRegistry.getInstance().getSettingsService();
    Settings settings = settingsService.getSettings();
    JMenu soundMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.sound"));
    ButtonGroup soundGroup = new ButtonGroup();

    // Play all sounds menu item.
    JRadioButtonMenuItem allItem = new JRadioButtonMenuItem(
        LocaleService.getString("jj.landing.menu.item.sound.all"));
    if (settings.isAllSoundOn()) {
      allItem.setSelected(true);
    }
    allItem.addActionListener(evt -> {
      settingsService.getSettings().enableAllSound();
      settingsService.persistSettings();
    });
    soundGroup.add(allItem);
    soundMenu.add(allItem);

    // Play FX sounds menu item.
    JRadioButtonMenuItem fxItem = new JRadioButtonMenuItem(
        LocaleService.getString("jj.landing.menu.item.sound.fx"));
    if (settings.isSoundEffectsOnly()) {
      fxItem.setSelected(true);
    }
    fxItem.addActionListener(evt -> {
      settingsService.getSettings().enableSoundEffectsOnly();
      settingsService.persistSettings();
    });
    soundGroup.add(fxItem);
    soundMenu.add(fxItem);

    // Play FX sounds menu item.
    JRadioButtonMenuItem noItem = new JRadioButtonMenuItem(
        LocaleService.getString("jj.landing.menu.item.sound.no"));
    if (settings.isAllSoundOff()) {
      noItem.setSelected(true);
    }
    noItem.addActionListener(evt -> {
      AppRegistry.getInstance().getSoundService().stopAllMusic();
      settings.disableAllSound();
      settingsService.persistSettings();
    });
    soundGroup.add(noItem);
    soundMenu.add(noItem);

    return soundMenu;
  }

  /**
   * Creates the Locale settings menu group.
   * @return locale menu
   */
  private @NotNull JMenu createLocaleMenu() {
    JMenu localeMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.locales"));
    ButtonGroup localesGroup = new ButtonGroup();
    LocaleService localeService = AppRegistry.getInstance().getLocaleService();
    for (Locale locale : localeService.getAvailableLocales()) {
      JRadioButtonMenuItem localeItem = new JRadioButtonMenuItem(locale.getDisplayName());
      if (locale.equals(LocaleService.getCurrentLocale())) {
        localeItem.setSelected(true);
      }
      localesGroup.add(localeItem);
      localeMenu.add(localeItem);
      localeItem.addActionListener(evt -> {
        localeService.setCurrentLocale(locale.toString(), true);
        SettingsService settingsService = AppRegistry.getInstance().getSettingsService();
        settingsService.getSettings().setLocaleId(locale.toString());
        settingsService.persistSettings();
      });
    }
    return localeMenu;
  }

  /**
   * Creates the About menu item.
   * @return about menu item
   */
  private @NotNull JMenuItem createAboutItem() {
    final JMenuItem aboutItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.about"));
    aboutItem.setMnemonic(KeyEvent.VK_A);
    aboutItem.addActionListener(evt -> AppRegistry.getInstance().getUiService().showInfoDialog(
        LocaleService.getString("jj.landing.menu.about.title"),
        LocaleService.getString("jj.landing.menu.about.message"),
        null));
    return aboutItem;
  }

  /**
   * Creates the Exit menu item.
   * @return Exit menu item
   */
  private @NotNull JMenuItem createExitItem() {
    final JMenuItem exitItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.exit"));
    exitItem.setMnemonic(KeyEvent.VK_Q);
    exitItem.addActionListener(evt -> this.landingUi.quitApp());
    return exitItem;
  }

  /**
   * Creates the New Game menu item.
   * @return new game menu item
   */
  private @NotNull JMenuItem createNewGameMenuItem() {
    final JMenuItem newGameItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.newgame"));
    newGameItem.setMnemonic(KeyEvent.VK_N);
    newGameItem.addActionListener(evt -> createNewGame());
    return newGameItem;
  }

  /**
   * Creates the Print Game menu item.
   * @return print game menu item
   */
  private @NotNull JMenuItem createPrintGameMenuItem() {
    this.printMenuItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.print"));
    this.printMenuItem.setMnemonic(KeyEvent.VK_P);
    this.printMenuItem.addActionListener(evt -> printAnswers());
    this.printMenuItem.setEnabled(AppRegistry.getInstance().getGameDataService().hasCurrentGameData());
    return this.printMenuItem;
  }

  /**
   * Creates a new game and opens the edit game window.
   */
  private void createNewGame() {
    // First, ask user for the game name and description.
    this.gameEditInfoDialog = new EditInfoDialog(() -> {
      // Then, create a default game in the library folder.
      String gameName = LandingUiMenu.this.gameEditInfoDialog.getGameName();
      String gameDescription = LandingUiMenu.this.gameEditInfoDialog.getGameDescription();
      GameDataService gameDataService = AppRegistry.getInstance().getGameDataService();
      GameData gameData = gameDataService.createNewGame(gameName, gameDescription);

      // Open the new game in the edit game window.
      EditGameWindow frame = new EditGameWindow(gameData, true, EditTableMode.ALL);
      frame.enableSaveButton();
      frame.setVisible(true);
    });
    this.gameEditInfoDialog.setLocationRelativeTo(null);
    this.gameEditInfoDialog.showDialog("", "");
  }

  /**
   * Displays the edit dialog to print answers.
   */
  private void printAnswers() {
    GameData gameData = AppRegistry.getInstance().getGameDataService().getCurrentGameData();
    EditGameWindow frame = new EditGameWindow(gameData, false, EditTableMode.ANSWERS);
    frame.setVisible(true);
  }
}
