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

import net.curre.jjeopardy.bean.FileParsingResult;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.landing.LandingUi;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.logging.Logger;

/**
 * Handler for the Load game action.
 *
 * @author Yevgeny Nyden
 */
public class LoadGameAction extends AbstractAction implements KeyListener {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(LoadGameAction.class.getName());

  /** Reference to the parent Landing UI dialog. */
  private final LandingUi landingUi;

  /**
   * Ctor.
   * @param landingUi reference to the parent Landing UI dialog
   */
  public LoadGameAction(LandingUi landingUi) {
    this.landingUi = landingUi;
  }

  /**
   * Handles the Load game action.
   * @param e the event to be processed
   */
  public void actionPerformed(ActionEvent e) {
    this.showLoadGameDialog();
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      this.showLoadGameDialog();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}

  /**
   * Opens file chooser dialog to load a new game file.
   */
  private void showLoadGameDialog() {
    LOGGER.info("Handling the Load button action.");
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(settingsService.getSettings().getLastCurrentDirectory()));
    final int result = fileChooser.showOpenDialog(landingUi);
    settingsService.saveLastCurrentDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
    settingsService.persistSettings();
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      GameDataService gameDataService = registry.getGameDataService();
      GameData gameData = gameDataService.parseGameData(selectedFile.getAbsolutePath());
      FileParsingResult parsingResults = gameData.generateFileParsingResult();
      registry.getUiService().showParsingResult(parsingResults, this.landingUi);

      if (gameData.isGameDataUsable()) {
        gameDataService.setCurrentGameData(gameData);
        this.landingUi.updateUiWithLoadedGameFile();
      }
    }
  }
}
