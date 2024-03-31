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
import net.curre.jjeopardy.service.SettingsService;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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

  /** Reference to the game data we are editing or printing. */
  private final GameData gameData;

  /** The game table to render game data in. */
  private final EditTable table;

  /** Reference to the scroll pane where the table is rendered. */
  private final JScrollPane scrollPane;

  /**
   * Ctor.
   * @param gameData game data to edit or print
   * @param editTableMode view mode (questions, answers, all)
   */
  public EditGameWindow(GameData gameData, EditTableMode editTableMode) {
    this.gameData = gameData;

    this.setTitle(gameData.getGameName());
    this.setModal(true);
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
    this.table = new EditTable(gameData, editTableMode, this::scrollToTop);
    // Note that the table assumes its direct parent is the scroll pane.
    this.scrollPane = new JScrollPane(this.table);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(10);
    this.scrollPane.setBorder(BorderFactory.createLineBorder(this.getBackground()));

    contentPane.add(this.scrollPane, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));

    // Print settings panel centered horizontally.
    PrintSettingPanel printPanel = new PrintSettingPanel(table, editTableMode);
    contentPane.add(printPanel, new TableLayoutConstraints(
        0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));

    SwingUtilities.invokeLater(this.table::refreshAndResize);
  }

  /**
   * Scrolls the scroll panel to the very top.
   */
  private void scrollToTop() {
    this.scrollPane.getVerticalScrollBar().setValue(0);
  }

  /** Saves dimensions of the edit window when window closes. */
  private void handleWindowClosing() {
    SettingsService settingsService = AppRegistry.getInstance().getSettingsService();
    settingsService.updateEditGameWindowSize(this.getWidth(), this.getHeight());
    this.table.refreshAndResize();
    settingsService.persistSettings();
  }
}
