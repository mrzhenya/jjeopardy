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

package net.curre.jjeopardy.ui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.event.LoadGameAction;
import net.curre.jjeopardy.event.StartGameAction;
import net.curre.jjeopardy.event.UpdatePlayersAction;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.MainService;
import net.curre.jjeopardy.service.SoundService;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.laf.LafService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.ui.player.PlayerDialog;
import net.curre.jjeopardy.util.Utilities;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static net.curre.jjeopardy.service.GameDataService.MIN_NUMBER_OF_PLAYERS;

/**
 * Main landing UI that's shown to the user after application start.
 * @author Yevgeny Nyden
 */
public class LandingUi extends JFrame {

  /** Landing UI preferred width. */
  private static final int LANDING_UI_WIDTH = 800;

  /** Image filename used by the dialog. */
  private static final String BACKGROUND_IMAGE_FILE = "jeopardy.jpg";

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(LandingUi.class.getName());

  /** Reference to the PlayerDialog. */
  private final PlayerDialog playerDialog;

  /** Current game label. */
  private JLabel currGameLabel;

  /** Start game button. */
  private JButton startGameButton;

  /** Reference to the container of the players list component. */
  private JPanel playersPanel;

  /**
   * Represents the main landing UI displayed to the user on application start.
   */
  public LandingUi() {
    this.setTitle(LocaleService.getString("jj.app.name"));
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new LandingUiWindowListener());
    this.initComponents();

    SwingUtilities.invokeLater(() -> {
      LOGGER.info("Displaying the main Landing UI...");
      LandingUi.this.pack();
      LandingUi.this.setLocationRelativeTo(null);
      LandingUi.this.setVisible(true);

      // stating the intro music
      final SoundService sound = SoundService.getInstance();
      sound.startMusic(SoundEnum.OPENING, 1);
    });
    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
    }

    this.playerDialog = new PlayerDialog(this);
    this.playerDialog.setLocationRelativeTo(this);
    LafService.registerUITreeForUpdates(this);
    LafService.registerUITreeForUpdates(this.playerDialog);
  }

  /**
   * Displays the player dialog.
   */
  public void showPlayerDialog() {
    playerDialog.setVisible(true);
  }

  /**
   * Updates the Landing UI according to the loaded game data.
   */
  public void updateUiWithLoadedGameFile() {
    GameDataService gameDataService = AppRegistry.getInstance().getGameDataService();
    GameData gameFileData = gameDataService.getGameFileData();
    final List<String> playerNames = gameFileData.getPlayerNames();
    // Updating game players from the ones parsed from the file.
    if (playerNames.size() >= MIN_NUMBER_OF_PLAYERS) {
      // Update the PlayerDialog values.
      this.playerDialog.updatePlayersPane(playerNames);

      // Update the Game "real" players data.
      gameDataService.updatePlayersFromNames(playerNames);
    }

    // Update the players list label in the Landing UI.
    this.updateLandingUi();
  }

  /**
   * Updates the UI according to the current game data and available players.
   * If there is enough data to start the game, the Start button is enabled.
   */
  public void updateLandingUi() {
    // Updating current game players list label.
    String playersText;
    String buttonText;
    boolean arePlayersSet;
    playersPanel.removeAll();
    List<Player> players = AppRegistry.getInstance().getGameDataService().getPlayers();
    if (players.isEmpty()) {
      playersText = LocaleService.getString("jj.playerdialog.label.addplayers");
      buttonText = LocaleService.getString("jj.playerdialog.button.add");
      arePlayersSet = false;
    } else {
      StringBuilder playersString = new StringBuilder(
        LocaleService.getString("jj.playerdialog.addplayers.label.intro"));
      for (int ind = 0; ind < players.size(); ind++) {
        Player player = players.get(ind);
        playersString.append(player.getName());
        if (ind + 1 < players.size()) {
          playersString.append(", ");
        }
      }
      playersText = playersString.toString();
      buttonText = LocaleService.getString("jj.playerdialog.button.update");
      arePlayersSet = true;
    }
    playersPanel.add(this.createPlayersInfoPanel(playersText, buttonText, arePlayersSet));
    this.pack();

    // Updating the game name label.
    GameDataService gameDataService = AppRegistry.getInstance().getGameDataService();
    GameData gameFileData = gameDataService.getGameFileData();
    if (gameFileData.haveEnoughGameData()) {
      this.currGameLabel.setText(gameFileData.getGameName());
      this.currGameLabel.setEnabled(true);
    } else {
      this.currGameLabel.setText(LocaleService.getString("jj.playerdialog.game.default.name"));
      this.currGameLabel.setEnabled(false);
    }

    // Check if the Start game button should be enabled.
    if (gameDataService.isGameReady()) {
      this.startGameButton.setEnabled(true);
      this.startGameButton.requestFocus();
    }
  }

  /** Initializes UI components. */
  private void initComponents() {
    // Setting the main layout.
    Container contentPane = getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL},  // columns
      {TableLayout.PREFERRED, TableLayout.FILL}}));  // rows

    // ******* Panel with the current game name and buttons to load and start the game.
    JPanel mainPanel = this.createMainContentPanel();
    contentPane.add(mainPanel, new TableLayoutConstraints(
      0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));

    // ********* Background image.
    ImageIcon backgroundIcon = new ImageIcon(Objects.requireNonNull(
      App.class.getResource("images/" + BACKGROUND_IMAGE_FILE)));
    JLabel picLabel = new JLabel(backgroundIcon);
    double imageWidth = backgroundIcon.getIconWidth();
    double imageHeight = backgroundIcon.getIconHeight();
    int scaledHeight = (int) (LANDING_UI_WIDTH / (imageWidth / imageHeight));
    backgroundIcon.setImage(backgroundIcon.getImage().getScaledInstance(
      LANDING_UI_WIDTH, scaledHeight, Image.SCALE_FAST));
    contentPane.add(picLabel, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.FULL, TableLayout.BOTTOM));

    // ********* Adding a menu bar
    this.setJMenuBar(new LandingUiMenu());
  }

  /**
   * Creates the main content panel with action text and buttons.
   * @return initialized main content panel
   */
  private JPanel createMainContentPanel() {
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    final int buttonSpacing = lafTheme.getButtonSpacing();

    final JPanel mainPanel = new JPanel(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {padding, TableLayout.PREFERRED, buttonSpacing, TableLayout.PREFERRED, padding}})); // rows

    // ******* Panel with the current game name and buttons to load and start the game.
    JPanel gamePanel = this.createGamePanel();
    mainPanel.add(gamePanel, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ********* Panel with players list and a button to add/update players.
    this.playersPanel = new JPanel();
    String playersText = LocaleService.getString("jj.playerdialog.label.addplayers");
    String buttonText = LocaleService.getString("jj.playerdialog.button.add");
    this.playersPanel.add(this.createPlayersInfoPanel(playersText, buttonText, false));
    mainPanel.add(this.playersPanel, new TableLayoutConstraints(
      1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));

    mainPanel.setBorder(BorderFactory.createLoweredBevelBorder());

    return mainPanel;
  }

  /**
   * Creates a component where current game name is displayed and
   * buttons to load or update the current game.
   * @return initialized JPanel component
   */
  private JPanel createGamePanel() {
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    final int buttonSpacing = lafTheme.getButtonSpacing();
    final Font buttonFont = lafTheme.getButtonFont();

    final JPanel gamePanel = new JPanel(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, buttonSpacing, TableLayout.PREFERRED,
        buttonSpacing, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {TableLayout.PREFERRED}})); // rows
    this.currGameLabel = new JLabel(LocaleService.getString("jj.playerdialog.game.default.name"));
    this.currGameLabel.setFont(lafTheme.getLandingLabelFont());
    this.currGameLabel.setEnabled(false);
    gamePanel.add(this.currGameLabel, new TableLayoutConstraints(
      1, 0, 1, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

    // ******* Load game button.
    final JButton loadButton = new JButton();
    loadButton.setFont(buttonFont);
    LoadGameAction loadGameAction = new LoadGameAction(this);
    loadButton.setAction(loadGameAction);
    loadButton.addKeyListener(loadGameAction);
    loadButton.setText(LocaleService.getString("jj.playerdialog.button.load"));
    gamePanel.add(loadButton, new TableLayoutConstraints(
      3, 0, 3, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

    // ******* Start game button.
    this.startGameButton = new JButton();
    this.startGameButton.setFont(buttonFont);
    StartGameAction startGameAction = new StartGameAction(this);
    this.startGameButton.setAction(startGameAction);
    this.startGameButton.addKeyListener(startGameAction);
    this.startGameButton.setText(LocaleService.getString("jj.playerdialog.button.start"));
    this.startGameButton.setEnabled(false);
    gamePanel.add(this.startGameButton, new TableLayoutConstraints(
      5, 0, 5, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

    return gamePanel;
  }

  /**
   * Creates the UI with a list of current players and a button to add or update players.
   * @param playersText String to display in the text area
   * @param buttonText Action button label
   * @param arePlayersSet true if there are players, false if we are showing default UI
   * @return panel with created UI
   */
  private JPanel createPlayersInfoPanel(String playersText, String buttonText, boolean arePlayersSet) {
    JPanel panel = new JPanel(new TableLayout(new double[][] {
      {TableLayout.FILL,
        // Text area width.
        (arePlayersSet ? (int) (LANDING_UI_WIDTH * .75) : TableLayout.PREFERRED),
        TableLayout.PREFERRED, 15, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {5, TableLayout.PREFERRED, 5}})); // rows

    // Players list rendered as uneditable text pane.
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    Component labelOrText;
    if (arePlayersSet) {
      JTextPane textPane = new JTextPane();
      textPane.setFont(lafTheme.getLandingLabelFont());
      textPane.setEditable(false);
      textPane.setFocusable(false);
      textPane.setDragEnabled(false);
      textPane.setBackground(lafTheme.getDefaultBackgroundColor());
      textPane.setText(playersText);

      StyledDocument doc = textPane.getStyledDocument();
      SimpleAttributeSet center = new SimpleAttributeSet();
      StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
      doc.setParagraphAttributes(0, doc.getLength(), center, false);
      labelOrText = textPane;
    } else {
      // JLabel avoid undesirable background color when label is disabled on light themes.
      JLabel textLabel = new JLabel();
      textLabel.setFont(lafTheme.getLandingLabelFont());
      textLabel.setEnabled(false);
      textLabel.setText(playersText);
      labelOrText = textLabel;
    }

    panel.add(labelOrText, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Add or Update players button.
    final JButton updateButton = new JButton();
    updateButton.setFont(lafTheme.getButtonFont());
    UpdatePlayersAction playersAction = new UpdatePlayersAction(this);
    updateButton.setAction(playersAction);
    updateButton.addKeyListener(playersAction);
    updateButton.setText(buttonText);
    panel.add(updateButton, new TableLayoutConstraints(
      4, 1, 4, 1, TableLayout.LEFT, TableLayout.CENTER));
    return panel;
  }

  /**
   * Window listener class for this Landing dialog.
   */
  private static class LandingUiWindowListener implements WindowListener {
    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
      MainService.quitApp();
    }

    public void windowClosed(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}
  }

}
