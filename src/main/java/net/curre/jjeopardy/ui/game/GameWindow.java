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

package net.curre.jjeopardy.ui.game;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.event.ClosingWindowListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.SoftBevelBorder;
import javax.validation.constraints.NotNull;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Main Game window UI that contains the game table, player scores, and some action buttons.
 * After creating this object, it must be initialized by calling #prepareGame.
 * This method should also be used to reset the game data for a new game.
 *
 * @author Yevgeny Nyden
 */
public class GameWindow extends JFrame {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(GameWindow.class.getName());

  /** Reference to the panel that contains player scores. */
  private PlayerScoresPanel playerScoresPanel;

  /** Reference to the game table. */
  private final GameTable gameTable;

  /** Reference to the Bonus Questions button. */
  private JButton bonusQuestionsButton;

  /** Reference to the Restart game button. */
  private JButton resetButton;

  /** Reference to the Quit game button. */
  private JButton quitButton;

  /** Flag to indicate that actions are enabled/disabled on the main game window. */
  private boolean actionsEnabled;

  /**
   * Ctor.
   */
  public GameWindow() {
    logger.info("Creating the main Game UI.");
    this.gameTable = new GameTable();
    this.actionsEnabled = true;

    final Settings settings = AppRegistry.getInstance().getSettingsService().getSettings();
    this.setPreferredSize(new Dimension(settings.getGameWindowWidth(), settings.getGameWindowHeight()));
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new ClosingWindowListener(this::handleWindowClosing));

    initComponents();

    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this);
  }

  /** {@inheritDoc} */
  @Override
  public void setVisible(boolean isVisible) {
    super.setVisible(isVisible);

    // Helps to avoid a glitch with first cell background image render not showing up.
    SwingUtilities.invokeLater(this::repaint);
  }

  /**
   * Prepares the game for a new round and starts it by showing the main game window UI.
   */
  public void prepareAndStartGame() {
    GameData gameData = AppRegistry.getInstance().getGameDataService().getCurrentGameData();
    this.setTitle(gameData.getGameName());

    this.actionsEnabled = true;
    this.gameTable.prepareGame();
    this.playerScoresPanel.prepareGame();
    this.bonusQuestionsButton.setEnabled(gameData.bonusQuestionsHaveBeenAsked());
    this.repaint();

    this.pack();
    this.setLocationRelativeTo(null);
    this.setVisible(true);
  }

  /**
   * Updates the players' scores.
   */
  public void updateScores() {
    this.playerScoresPanel.updateScores();
  }

  /**
   * Determines if the actions are disabled on the main game window.
   * @return true if actions are disabled; false if otherwise
   */
  public boolean actionsDisabled() {
    return !this.actionsEnabled;
  }

  /** Enables actions on this game window. */
  public void enableActions() {
    this.actionsEnabled = true;
    GameDataService gameService = AppRegistry.getInstance().getGameDataService();
    if (gameService.hasCurrentGameData()) {
      GameData gameData = gameService.getCurrentGameData();
      this.bonusQuestionsButton.setEnabled(gameData.bonusQuestionsHaveBeenAsked());
      this.resetButton.setEnabled(true);
      this.quitButton.setEnabled(true);
    } else {
      this.bonusQuestionsButton.setEnabled(false);
      this.resetButton.setEnabled(false);
      this.quitButton.setEnabled(false);
    }
  }

  /** Disables actions on this game window. */
  public void disableActions() {
    this.actionsEnabled = false;
    this.bonusQuestionsButton.setEnabled(false);
    this.resetButton.setEnabled(false);
    this.quitButton.setEnabled(false);
  }

  /**
   * Initializes UI components.
   */
  private void initComponents() {
    // Setting the frame layout.
    Container contentPane = getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
      {3, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 3}, // columns
      {3, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED,
        TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 3}})); // rows

    // ******* Game table is mostly the layout.
    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BorderLayout());

    // Game table with most of the game data.
    this.gameTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    this.gameTable.setColumnSelectionAllowed(false);
    this.gameTable.setRowSelectionAllowed(false);
    this.gameTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.gameTable.setPreferredScrollableViewportSize(new Dimension(450, 200));

    tablePanel.add(this.gameTable.getTableHeader(), BorderLayout.NORTH);
    tablePanel.add(this.gameTable, BorderLayout.CENTER);
    contentPane.add(tablePanel, new TableLayoutConstraints(
      2, 2, 2, 2, TableLayout.FULL, TableLayout.FULL));

    // ******* Players scores pane, below the game questions table.
    this.playerScoresPanel = new PlayerScoresPanel();
    this.playerScoresPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
    contentPane.add(this.playerScoresPanel, new TableLayoutConstraints(
      2, 4, 2, 4, TableLayout.FULL, TableLayout.FULL));

    // ******* Action buttons panel.
    JPanel buttonsPanel = this.initPlayerButtonsPanel();
    contentPane.add(buttonsPanel, new TableLayoutConstraints(
      2, 6, 2, 6, TableLayout.FULL, TableLayout.FULL));

    this.setLocationRelativeTo(null);
  }

  /**
   * Initializes the panel with the game buttons (bonus questions, quit, etc.).
   * @return player buttons panel
   */
  private @NotNull JPanel initPlayerButtonsPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();

    JPanel buttonsPanel = new JPanel();
    final int buttonSpacing = lafTheme.getButtonSpacing();
    final int padding = lafTheme.getPanelPadding();
    buttonsPanel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, buttonSpacing,
        TableLayout.PREFERRED, buttonSpacing, TableLayout.PREFERRED, TableLayout.FILL}, // columns
      {padding, TableLayout.PREFERRED, padding}})); // rows

    // Bonus questions button.
    this.bonusQuestionsButton = new JButton();
    Font buttonFont = lafTheme.getButtonFont();
    ClickAndKeyAction.createAndAddAction(this.bonusQuestionsButton, this::handleBonusQuestionsAction);
    this.bonusQuestionsButton.setFont(buttonFont);
    this.bonusQuestionsButton.setText(LocaleService.getString("jj.game.buttons.bonus.name"));
    buttonsPanel.add(this.bonusQuestionsButton, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Restart the game button.
    this.resetButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.resetButton, this::handleRestartGameAction);
    this.resetButton.setFont(buttonFont);
    this.resetButton.setText(LocaleService.getString("jj.game.buttons.restart.name"));
    buttonsPanel.add(this.resetButton, new TableLayoutConstraints(
      3, 1, 3, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Quit the game button.
    this.quitButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.quitButton, this::handleEndGameAction);
    this.quitButton.setFont(buttonFont);
    this.quitButton.setText(LocaleService.getString("jj.game.buttons.quit.name"));
    buttonsPanel.add(this.quitButton, new TableLayoutConstraints(
      5, 1, 5, 1, TableLayout.FULL, TableLayout.FULL));

    return buttonsPanel;
  }

  /** Handles the Bonus question button action. */
  private void handleBonusQuestionsAction() {
    this.bonusQuestionsButton.setEnabled(false);
    this.gameTable.openBonusQuestionsDialog();
  }

  /** Handles the Restart game button action. */
  private void handleRestartGameAction() {
    AppRegistry.getInstance().getGameService().restartGame();
  }

  /** Handles the End game button action. */
  private void handleEndGameAction() {
    Registry registry = AppRegistry.getInstance();
    Player winner = registry.getGameDataService().getWinner();
    if (winner.getScore() > 0) {
      registry.getSoundService().startMusic(SoundEnum.FINAL, 1);
      registry.getUiService().showEndGameDialog(winner.getName(), winner.getScore());
    }
    GameWindow.this.handleWindowClosing();
  }

  /**
   * Saves game window size in the settings, closes game window,
   * and switches to the landing UI.
   */
  private void handleWindowClosing() {
    // Saving dimensions of the main window.
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();
    settingsService.updateGameWindowSize(this.getWidth(), this.getHeight());
    settingsService.persistSettings();

    // Hide the main game window and show the landing UI.
    this.setVisible(false);
    registry.getLandingUi().setVisible(true);
  }
}
