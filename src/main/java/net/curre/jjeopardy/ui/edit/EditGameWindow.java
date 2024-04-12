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
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.validation.constraints.NotNull;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Edit game frame that displays the game detailed information such as
 * questions and answers, offers editing and printing capabilities.
 *
 * @author Yevgeny Nyden
 */
public class EditGameWindow extends JDialog {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(EditGameWindow.class.getName());

  /** Reference to the game data we are editing or printing. */
  private final GameData gameData;

  /** Indicates that game data has been changed. */
  private boolean dataChanged;

  /** The game table to render game data in. */
  private final EditTable table;

  /** Reference to the scroll pane where the table is rendered. */
  private final JScrollPane scrollPane;

  /** Reference to the edit settings panel. */
  private final EditSettingPanel editSettingsPanel;

  /**
   * Ctor.
   * @param gameData game data to edit or print
   * @param editEnabled true if the editing is enabled.
   * @param editTableMode view mode (questions, answers, all)
   */
  public EditGameWindow(@NotNull GameData gameData, boolean editEnabled, EditTableMode editTableMode) {
    this.gameData = gameData;
    this.dataChanged = false;

    this.setTitle(gameData.getGameName());
    this.addWindowListener(new ClosingWindowListener(this::handleWindowClosing));
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        EditGameWindow.this.table.refreshAndResize();
      }
    });

    Settings settings = AppRegistry.getInstance().getSettingsService().getSettings();
    this.setSize(settings.getEditGameWindowWidth(), settings.getEditGameWindowHeight());
    this.setLocationRelativeTo(null);

    // Setting the main content pane layout.
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL, 15, TableLayout.PREFERRED, 15}})); // rows

    // Game table wrapped in a scroll pane.
    this.table = new EditTable(gameData, editEnabled, editTableMode, this::scrollToTop, this::enableSaveButton);
    // Note that the table assumes its direct parent is the scroll pane.
    this.scrollPane = new JScrollPane(this.table);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(10);
    this.scrollPane.setBorder(BorderFactory.createLineBorder(this.getBackground()));

    contentPane.add(this.scrollPane, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));

    // Print and edit settings panels centered horizontally.
    JPanel panelWrap = new JPanel();
    PrintSettingPanel printPanel = new PrintSettingPanel(this.table, editTableMode);
    panelWrap.add(printPanel);
    this.editSettingsPanel = new EditSettingPanel(this, editEnabled);
    panelWrap.add(this.editSettingsPanel);
    contentPane.add(panelWrap, new TableLayoutConstraints(
        0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));

    SwingUtilities.invokeLater(this.table::refreshAndResize);
  }

  /**
   * Sets the editing enabled mode on the edit table.
   * @param editEnabled true if editing is enabled; false if disabled
   */
  public void setEditEnabled(boolean editEnabled) {
    this.table.setEditEnabled(editEnabled);
  }

  /**
   * Saves the game data
   */
  protected void saveGameData() {
    logger.info("Saving game data");
    this.dataChanged = false;
    AppRegistry.getInstance().getGameDataService().saveGameData(this.gameData, this);
  }

  /**
   * Scrolls the scroll panel to the very top.
   */
  private void scrollToTop() {
    this.scrollPane.getVerticalScrollBar().setValue(0);
  }

  /**
   * Enables the Save button in the edit settings panel.
   */
  private void enableSaveButton() {
    this.dataChanged = true;
    this.editSettingsPanel.enableSaveButton();
  }

  /** Saves dimensions of the edit window when window closes. */
  private void handleWindowClosing() {
    logger.info("Closing the edit game window");
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();
    settingsService.updateEditGameWindowSize(this.getWidth(), this.getHeight());
    this.table.refreshAndResize();
    settingsService.persistSettings();

    if (this.dataChanged) {
      registry.getUiService().showConfirmationDialog(
          LocaleService.getString("jj.editdialog.unsaved.title"),
          LocaleService.getString("jj.editdialog.unsaved.message"),
          this::saveGameData, this);
    }
  }
}
