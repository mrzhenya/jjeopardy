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

package net.curre.jjeopardy.ui;

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.MainService;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.LafService;
import net.curre.jjeopardy.ui.laf.theme.LafThemeInterface;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the menu displayed on the Landing UI.
 * @author Yevgeny Nyden
 */
public class LandingUiMenu extends JMenuBar {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(LandingUiMenu.class.getName());

  /** Ctor. */
  public LandingUiMenu() {
    // Creating the Themes menu items.
    JMenu themeMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.themes"));
    ButtonGroup themesGroup = new ButtonGroup();
    LafService lafService = LafService.getInstance();
    for (LafThemeInterface lafTheme : lafService.getSupportedThemes()) {
      JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(
        LocaleService.getString(lafTheme.getNameResourceKey()));
      if (LafService.getInstance().getCurrentLafTheme().equals(lafTheme)) {
        themeItem.setSelected(true);
      }
      themesGroup.add(themeItem);
      themeMenu.add(themeItem);
      themeItem.addActionListener(evt -> {
        try {
          lafService.activateLafTheme(lafTheme.getId());
          SettingsService.saveSettings();
          AppRegistry.getInstance().getLandingUi().updateLandingUi();
          UiService.getInstance().showRestartGameDialog();
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Unable to save settings.", e);
        }
      });
    }

    // Creating the Locale menu.
    JMenu localeMenu = new JMenu(LocaleService.getString("jj.landing.menu.item.locales"));
    ButtonGroup localesGroup = new ButtonGroup();
    for (Locale locale : LocaleService.getAvailableLocales()) {
      JRadioButtonMenuItem localeItem = new JRadioButtonMenuItem(locale.getDisplayName());
      if (locale.equals(Locale.getDefault())) {
        localeItem.setSelected(true);
      }
      localesGroup.add(localeItem);
      localeMenu.add(localeItem);
      localeItem.addActionListener(evt -> {
        LocaleService.setCurrentLocale(locale.toString(), true);
      });
    }

    // Creating the About menu item.
    final JMenuItem aboutItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.about"));
    aboutItem.setMnemonic(KeyEvent.VK_A);
    aboutItem.addActionListener(evt -> UiService.getInstance().showInfoDialog(
      LocaleService.getString("jj.landing.menu.about.title"),
      LocaleService.getString("jj.landing.menu.about.message"),
    null));

    // Creating the Exit menu item.
    final JMenuItem exitItem = new JMenuItem(LocaleService.getString("jj.landing.menu.item.exit"));
    exitItem.setMnemonic(KeyEvent.VK_Q);
    exitItem.addActionListener(evt -> MainService.quitApp());

    JMenu menu = new JMenu(LocaleService.getString("jj.landing.menu.title"));
    menu.add(themeMenu);
    menu.add(localeMenu);
    menu.addSeparator();
    menu.add(aboutItem);
    menu.addSeparator();
    menu.add(exitItem);
    this.add(menu);
  }
}
