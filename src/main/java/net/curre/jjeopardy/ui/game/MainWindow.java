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
import net.curre.jjeopardy.event.GameTableMouseListener;
import net.curre.jjeopardy.event.GameWindowListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.dialog.QuestionDialog;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.SoftBevelBorder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * Main Game window UI that contains the game table, player scores, and some action buttons.
 * After creating this object, it must be initialized by calling #prepareGame.
 * This method should also be used to reset the game data for a new game.
 *
 * @author Yevgeny Nyden
 */
public class MainWindow extends JFrame {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());

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
  public MainWindow() {
    LOGGER.info("Creating the main Game UI.");
    this.gameTable = new GameTable();
    this.actionsEnabled = true;

    final Settings settings = AppRegistry.getInstance().getSettingsService().getSettings();
    this.setPreferredSize(new Dimension(settings.getGameWindowWidth(), settings.getGameWindowHeight()));
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new GameWindowListener());

    initComponents();

    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this);

    Registry registry = AppRegistry.getInstance();
    if (registry.getQuestionDialog() == null) {
      QuestionDialog dialog = new QuestionDialog();
      registry.setQuestionDialog(dialog);
    }
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
    GameData gameData = AppRegistry.getInstance().getGameDataService().getGameData();
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
   * Determines if the actions are enabled on the main game window.
   * @return true if actions are enabled; false if otherwise
   */
  public boolean isActionsEnabled() {
    return this.actionsEnabled;
  }

  /**
   * Enables actions on the main game window (this) dialog.
   */
  public void enableActions() {
    this.actionsEnabled = true;
    GameData gameData = AppRegistry.getInstance().getGameDataService().getGameData();
    this.bonusQuestionsButton.setEnabled(gameData.bonusQuestionsHaveBeenAsked());
    this.resetButton.setEnabled(true);
    this.quitButton.setEnabled(true);
  }

  /**
   * Disables actions on the main game window (this) dialog.
   */
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
    GameTableMouseListener mouseListener = new GameTableMouseListener(this.gameTable);
    this.gameTable.addMouseMotionListener(mouseListener);
    this.gameTable.addMouseListener(mouseListener);
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
  private JPanel initPlayerButtonsPanel() {
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
    this.bonusQuestionsButton.setFont(buttonFont);
    this.bonusQuestionsButton.setAction(new BonusQuestionsAction());
    this.bonusQuestionsButton.setText(LocaleService.getString("jj.game.buttons.bonus.name"));
    buttonsPanel.add(this.bonusQuestionsButton, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Restart the game button.
    this.resetButton = new JButton();
    this.resetButton.setFont(buttonFont);
    this.resetButton.setAction(new RestartGameAction());
    this.resetButton.setText(LocaleService.getString("jj.game.buttons.restart.name"));
    buttonsPanel.add(this.resetButton, new TableLayoutConstraints(
      3, 1, 3, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Quit the game button.
    this.quitButton = new JButton();
    this.quitButton.setFont(buttonFont);
    this.quitButton.setAction(new EndGameAction());
    this.quitButton.setText(LocaleService.getString("jj.game.buttons.quit.name"));
    buttonsPanel.add(this.quitButton, new TableLayoutConstraints(
      5, 1, 5, 1, TableLayout.FULL, TableLayout.FULL));

    return buttonsPanel;
  }

  /** Handler for the Bonus question button. */
  private class BonusQuestionsAction extends AbstractAction {

    /** Ctor. */
    private BonusQuestionsAction() {}

    @Override
    public void actionPerformed(ActionEvent e) {
      Registry registry = AppRegistry.getInstance();
      registry.getQuestionDialog().startAskingBonusQuestions();
      MainWindow.this.bonusQuestionsButton.setEnabled(false);
    }
  }

  /** Handler for the Restart game button. */
  private static class RestartGameAction extends AbstractAction {

    /** Ctor. */
    private RestartGameAction() {}

    @Override
    public void actionPerformed(ActionEvent e) {
      AppRegistry.getInstance().getMainService().startGame();
    }
  }

  /**
   * Action to handle ending the game.
   */
  private class EndGameAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
      // End the game.
      Registry registry = AppRegistry.getInstance();
      registry.getSoundService().startMusic(SoundEnum.FINAL, 1);
      Player winner = registry.getGameDataService().getWinner();
      registry.getUiService().showEndGameDialog(
        LocaleService.getString("jj.game.enddialog.header", winner.getName()),
        LocaleService.getString("jj.game.enddialog.message", winner.getName(), String.valueOf(winner.getScore())));
      MainWindow.this.setVisible(false);
      registry.getLandingUi().setVisible(true);
    }
  }
}
