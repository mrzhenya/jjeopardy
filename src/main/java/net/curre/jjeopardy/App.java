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
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.landing.LandingUi;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.SwingUtilities;
import java.io.File;

/**
 * The driver to run the JJeopardy application.
 * @author Yevgeny Nyden
 */
public class App {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Name of the log file (will be created in the settings directory). */
  private static final String LOG_FILENAME = "jjeopardy.log";

  /**
   * Main method to run the JJeopardy application.
   * @param args Argument array.
   */
  public static void main(String[] args) {

    // Add file appender to log4j configuration.
    updateLogConfiguration();
    logger.info("Starting application...");

    // Initialize the main service registry with a default (non-test) one.
    AppRegistry.initialize(null);

    // Copy default prepackaged game files to the settings game library folder.
    GameDataService.copyDefaultGamesToLibraryIfNeeded();

    // Then, load and activate the stored settings (LAF theme, locale, game board size).
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();
    Settings settings = settingsService.getSettings();
    registry.getLafService().activateLafTheme(settings.getLafThemeId());
    registry.getLocaleService().setCurrentLocale(settings.getLocaleId(), false);

    SwingUtilities.invokeLater(() -> {
      // Now, start the app by showing the landing UI.
      LandingUi landingUi = new LandingUi(); // The Landing UI will get displayed shortly.
      registry.setLandingUi(landingUi);
    });
  }

  /**
   * Adds a file appender to log4j configuration. It has to be done programmatically
   * (vs via log4j2.xml) because the log filepath needs to be determined at run time.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void updateLogConfiguration() {
    final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
    final Configuration config = loggerContext.getConfiguration();
    final Layout layout = PatternLayout.newBuilder()
        .withPattern("%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")
        .withConfiguration(config).build();
    String logFilePath = SettingsService.getVerifiedSettingsDirectoryPath() + File.separatorChar + LOG_FILENAME;
    FileAppender.Builder builder = FileAppender.newBuilder();
    builder.withFileName(logFilePath)
        .withAppend(false)
        .withLocking(false)
        .setIgnoreExceptions(true);
    builder.setName("JJeopardyLogFile");
    builder.setBufferedIo(true);
    builder.setBufferSize(4000);
    builder.setLayout(layout);
    builder.setImmediateFlush(true);
    builder.setConfiguration(config);
    FileAppender appender = builder.build();
    appender.start();

    config.getRootLogger().addAppender(appender, Level.INFO, null);
    loggerContext.updateLoggers();
  }
}
