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

package net.curre.jjeopardy.ui.edit;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.event.ClosingWindowListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Edit game frame that displays the game detailed information such as
 * questions and answers, offers editing and printing capabilities.
 *
 * @author Yevgeny Nyden
 */
public class EditGameWindow extends JDialog {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(EditGameWindow.class.getName());

  /** Reference to the game data we are editing or printing (a copu of the original). */
  private final GameData gameData;

  /** Reference to the original game data we are editing or printing. */
  private final GameData originalGameData;

  /** Indicates that game data has been changed. */
  private boolean dataChanged;

  /** The game table to render game data in. */
  private final EditTable table;

  /** Reference to the scroll pane where the table is rendered. */
  private final JScrollPane scrollPane;

  /** Reference to the edit settings panel. */
  private final EditSettingPanel editSettingsPanel;

  /**
   * Ctor that creates an edit game window to edit or print a provided game.
   * @param gameData game data to edit or print
   * @param editEnabled true if the editing is enabled.
   * @param editTableMode view mode (questions, answers, all)
   */
  public EditGameWindow(@NotNull GameData gameData, boolean editEnabled, EditTableMode editTableMode) {
    // Create a copy of game data in case the user cancels to save the changes.
    this.originalGameData = gameData;
    this.gameData = gameData.createCopy();
    this.dataChanged = false;

    logger.info("Creating the edit game window");
    if (!this.gameData.isNativeData()) {
      logger.info("Opened edit game window with non-native data, editing is going to be disabled");
      editEnabled = false;
    }

    this.setTitle(this.gameData.getGameName());
    this.addWindowListener(new ClosingWindowListener(this::handleWindowClosing));

    Settings settings = AppRegistry.getInstance().getSettingsService().getSettings();
    this.setSize(settings.getEditGameWindowWidth(), settings.getEditGameWindowHeight());
    this.setLocationRelativeTo(null);

    // Setting the main content pane layout.
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL, 15, TableLayout.PREFERRED, 15}})); // rows

    // Game table wrapped in a scroll pane.
    this.table = new EditTable(this.gameData, editEnabled, editTableMode, this::scrollToTop, this::enableSaveButton);
    // Note that the table assumes its direct parent is the scroll pane.
    this.scrollPane = new JScrollPane(this.table);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(10);
    this.scrollPane.setBorder(BorderFactory.createLineBorder(this.getBackground()));

    contentPane.add(this.scrollPane, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));

    // Print and edit settings panels centered horizontally.
    JPanel panelWrap = new JPanel();
    this.editSettingsPanel = new EditSettingPanel(this, editEnabled);
    panelWrap.add(this.editSettingsPanel);
    PrintSettingPanel printPanel = new PrintSettingPanel(this.table, editTableMode);
    panelWrap.add(printPanel);
    contentPane.add(panelWrap, new TableLayoutConstraints(
        0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));

    // Resize the table and add a listener to update it on window resize events.
    SwingUtilities.invokeLater(() -> {
      EditGameWindow.this.table.refreshAndResize();
      this.addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
          EditGameWindow.this.table.refreshAndResize();
        }
      });
      EditGameWindow.this.toFront();
    });
  }

  /**
   * Scrolls to the top after dialog is set visible.
   * @inheritDoc
   */
  @Override
  public void setVisible(boolean isVisible) {
    super.setVisible(isVisible);
    if (isVisible) {
      this.scrollToTop();
    }
  }

  /**
   * Sets the editing enabled mode on the edit table.
   * @param editEnabled true if editing is enabled; false if disabled
   */
  public void setEditEnabled(boolean editEnabled) {
    this.table.setEditEnabled(editEnabled);
  }

  /**
   * Gets the game data in edit.
   * @return reference to the game data we are editing
   */
  protected @NotNull GameData getGameData() {
    return this.gameData;
  }

  /**
   * Updates the title of this window with a new game name.
   */
  public void refreshGameName() {
    this.setTitle(this.gameData.getGameName());
  }

  /**
   * Enables the Save button in the edit settings panel.
   */
  public void enableSaveButton() {
    this.dataChanged = true;
    this.editSettingsPanel.enableSaveButton();
  }

  /** Installs the ESC key handler. */
  @Override
  protected JRootPane createRootPane() {
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    JRootPane rootPane = new JRootPane();

    ActionListener actionListener = actionEvent -> EditGameWindow.this.handleWindowClosing();
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    return rootPane;
  }

  /**
   * Saves the game data to the disk. The original game data is also updated.
   */
  protected void saveGameData() {
    logger.info("Saving game data");
    this.dataChanged = false;
    GameDataService gameService = AppRegistry.getInstance().getGameDataService();
    if (this.gameData.isGameDataNew()) {
      logger.info("Adding a new game to the library");
      gameService.updateLibraryGames(this.gameData);
    }
    gameService.saveGameData(this.gameData, this);
    this.originalGameData.copyFrom(this.gameData);

    // If the game is in the library, update the library.
    if (gameService.isLibraryGame(this.gameData)) {
      AppRegistry.getInstance().getLandingUi().updateLibrary(this.gameData);
    }
  }

  /**
   * Checks if the game data has not been saved to disk yet, in which case
   * deletes the game bundle directory. This is a case when user creates a new
   * game, but later changes their mind and decides not to save it.
   */
  protected void maybeRemoveGameData() {
    if (this.gameData.isGameDataNew()) {
      logger.info("Cleaning up unneeded new game: " + this.gameData.getBundlePath());
        try {
            FileUtils.deleteDirectory(new File(this.gameData.getBundlePath()));
        } catch (Exception e) {
            logger.warn("Unable to delete new game bundle", e);
        }
    }
  }

  /**
   * Scrolls the scroll panel to the very top.
   */
  private void scrollToTop() {
    SwingUtilities.invokeLater(() -> this.scrollPane.getVerticalScrollBar().setValue(0));
  }

  /** Saves dimensions of the edit window when window closes. */
  private void handleWindowClosing() {
    logger.info("Closing the edit game window");
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();
    settingsService.updateEditGameWindowSize(this.getWidth(), this.getHeight());
    settingsService.persistSettings();

    if (this.dataChanged) {
      registry.getUiService().showConfirmationDialog(
          LocaleService.getString("jj.editdialog.unsaved.title"),
          LocaleService.getString("jj.editdialog.unsaved.message"),
          this::saveGameData, this::maybeRemoveGameData, this);
    }
    this.setVisible(false);
    this.dispose();
  }
}
