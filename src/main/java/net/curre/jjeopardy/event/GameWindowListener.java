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

package net.curre.jjeopardy.event;

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.game.MainWindow;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Main Game window listener. Mostly to store the game board dimensions
 * in the game settings.
 */
public class GameWindowListener implements WindowListener {

  @Override
  public void windowOpened(WindowEvent e) {
  }

  @Override
  public void windowClosing(WindowEvent e) {
    Registry registry = AppRegistry.getInstance();
    MainWindow mainWindow = registry.getMainWindow();

    // Saving dimensions of the main window.
    SettingsService settingsService = registry.getSettingsService();
    settingsService.updateMainWindowSize(mainWindow.getWidth(), mainWindow.getHeight());
    settingsService.persistSettings();

    // Hide the main game window and show the landing UI.
    mainWindow.setVisible(false);
    registry.getLandingUi().setVisible(true);
  }

  @Override
  public void windowClosed(WindowEvent e) {
  }

  @Override
  public void windowIconified(WindowEvent e) {
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowActivated(WindowEvent e) {
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }
}
