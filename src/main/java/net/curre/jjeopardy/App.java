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

package net.curre.jjeopardy;

import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.LandingUi;
import net.curre.jjeopardy.ui.laf.LafService;

import javax.swing.SwingUtilities;
import java.util.logging.Logger;

/**
 * The driver to run the JJeopardy application.
 * @author Yevgeny Nyden
 */
public class App {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(App.class.getName());

  /**
   * Main method to run the JJeopardy application.
   * @param args Argument array.
   */
  public static void main(String[] args) {
    LOGGER.info("Starting application...");

    // First, initialize the main service registry with a default (non-test) one.
    AppRegistry.initialize(null);

    // Second, initialize the Look and Feel service, before showing any UI.
    LafService.getInstance().initialize();

    // Then, load and activate the stored settings (LAF theme, locale, game board size).
    SettingsService settingsService = AppRegistry.getInstance().getSettingsService();
    Settings settings = settingsService.getSettings();
    LafService.getInstance().activateLafTheme(settings.getLafThemeId());
    LocaleService.setCurrentLocale(settings.getLocaleId(), false);

    SwingUtilities.invokeLater(() -> {
      // Now, start the app by showing the landing UI.
      LandingUi landingUi = new LandingUi(); // The Landing UI will get displayed shortly.
      AppRegistry.getInstance().setLandingUi(landingUi);
    });
  }
}
