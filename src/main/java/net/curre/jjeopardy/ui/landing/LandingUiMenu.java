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

import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LafService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.laf.theme.LafThemeInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.KeyEvent;
import java.util.Locale;

/**
 * Represents the menu displayed on the Landing UI.
 * @author Yevgeny Nyden
 */
public class LandingUiMenu extends JMenuBar {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Ctor. */
  public LandingUiMenu() {
    JMenu menu = new JMenu(LocaleService.getString("jj.landing.menu.title"));
    menu.add(createThemeMenu());
    menu.add(createSoundMenu());
    menu.add(createLocaleMenu());
    menu.addSeparator();
    menu.add(createAboutItem());
    menu.addSeparator();
    menu.add(createExitItem());
    this.add(menu);
  }

  /**
   * Creates the LAF Themes settings menu group.
   * @return themes menu
   */
  private JMenu createThemeMenu() {
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
  private JMenu createSoundMenu() {
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
  private JMenu createLocaleMenu() {
    JMenu localeMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.locales"));
    ButtonGroup localesGroup = new ButtonGroup();
    LocaleService localeService = AppRegistry.getInstance().getLocaleService();
    for (Locale locale : localeService.getAvailableLocales()) {
      JRadioButtonMenuItem localeItem = new JRadioButtonMenuItem(locale.getDisplayName());
      if (locale.equals(Locale.getDefault())) {
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
  private JMenuItem createAboutItem() {
    // Creating the About menu item.
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
  private JMenuItem createExitItem() {
    // Creating the Exit menu item.
    final JMenuItem exitItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.exit"));
    exitItem.setMnemonic(KeyEvent.VK_Q);
    exitItem.addActionListener(evt -> AppRegistry.getInstance().getMainService().quitApp());
    return exitItem;
  }
}
