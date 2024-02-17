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

package net.curre.jjeopardy.ui.player;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.ui.landing.LandingUi;
import net.curre.jjeopardy.util.JjDefaults;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.logging.Logger;

/**
 * Dialog to add or update players joining the game.
 * @author Yevgeny Nyden
 */
public class PlayerDialog extends JDialog {

  /** Preferred width for the dialog. */
  private static final int DIALOG_WIDTH = 460;

  /** Preferred height for the dialog. */
  private static final int DIALOG_HEIGHT = 450;

  /** Class logger. */
  private static final Logger LOGGER = Logger.getLogger(PlayerDialog.class.getName());

  /** Reference to the main Landing dialog. */
  private final LandingUi landingUi;

  /** Container of the players' names/data. */
  private PlayersPane playersPane;

  /**
   * Ctor.
   * @param landingUi reference to the main Landing dialog.
   */
  public PlayerDialog(LandingUi landingUi) {
    this.landingUi = landingUi;

    this.setTitle(LocaleService.getString("jj.playerdialog.addplayers.title"));
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new PlayerDialogWindowEventsHandler());

    this.initComponents();
  }

  /**
   * Updates the players pane.
   * @param players list of players
   */
  public void updatePlayersPane(List<Player> players) {
    this.playersPane.updatePlayersPane(players);
  }

  /**
   * Initializes the dialog's UI components.
   */
  private void initComponents() {
    // Setting the main layout.
    this.setLayout(new TableLayout(new double[][] {
      {TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED},  // columns
      {15, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 20}}));  // rows

    Font font = new Font("Garamond", Font.PLAIN , 14);

    // ******* Top panel label.
    final JLabel label = new JLabel();
    label.setFont(font);
    label.setText(LocaleService.getString("jj.playerdialog.addplayers.header"));
    this.add(label, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Players pane.
    this.playersPane = new PlayersPane();
    this.add(this.playersPane, new TableLayoutConstraints(
      1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Save button.
    final JButton saveButton = new JButton();
    saveButton.setFont(font);
    saveButton.setAction(new SavePlayersAction());
    saveButton.setText("Save");
    this.add(saveButton, new TableLayoutConstraints(
      1, 5, 1, 5, TableLayout.CENTER, TableLayout.CENTER));

    this.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
    this.pack();
  }

  /**
   * Simple handler for the dialog's window events.
   */
  private class PlayerDialogWindowEventsHandler implements WindowListener {

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {
      PlayerDialog.this.setVisible(false);
      PlayerDialog.this.playersPane.cleanEmptyPlayers();
    }

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
  }
  /**
   * Action to handle the Save players action.
   */
  private class SavePlayersAction extends AbstractAction {

    /**
     * Handles the Save players action.
     * @param e the event to be processed
     */
    public void actionPerformed(ActionEvent e) {
      LOGGER.info("Saving players.");
      Registry registry = AppRegistry.getInstance();
      GameDataService gameService = registry.getGameDataService();

      PlayerDialog.this.playersPane.cleanEmptyPlayers();
      List<String> playerNames = playersPane.getPlayerNames();
      if (playerNames.size() < JjDefaults.MIN_NUMBER_OF_PLAYERS) {
        LOGGER.info("Not enough non-blank player names");
        PlayerDialog.this.setVisible(false);
        registry.getUiService().showWarningDialog(
            LocaleService.getString("jj.playerdialog.addplayers.warn.title"),
            LocaleService.getString("jj.playerdialog.addplayers.warn.msg",
                String.valueOf(JjDefaults.MIN_NUMBER_OF_PLAYERS)),
            PlayerDialog.this);
        PlayerDialog.this.setVisible(true);
        return;
      }
      gameService.updateCurrentPlayers(playerNames);
      PlayerDialog.this.landingUi.updateLandingUi();
      PlayerDialog.this.setVisible(false);
    }
  }
}
