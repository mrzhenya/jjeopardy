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

package net.curre.jjeopardy.ui.landing;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.FileParsingResult;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.event.QuitAppAction;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.service.SoundService;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.ui.player.PlayerDialog;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Main landing UI that's shown to the user after application start.
 * @author Yevgeny Nyden
 */
public class LandingUi extends JFrame {

  /** Image filename used by the dialog. */
  private static final String BACKGROUND_IMAGE_FILE = "jeopardy.jpg";

  /** Id for the Library card in the card layout. */
  private static final String CARD_LIBRARY_ID = "LibraryCardId";

  /** Id for the Background card in the card layout. */
  private static final String CARD_BACKGROUND_ID = "BackgroundCardId";

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(LandingUi.class.getName());

  /** Reference to the PlayerDialog. */
  private final PlayerDialog playerDialog;

  /** Current game label. */
  private JLabel currGameLabel;

  /** Reference to the Library button. */
  private JButton libraryButton;

  /** Start game button. */
  private JButton startGameButton;

  /** Reference to the container of the players list component. */
  private JPanel playersPanel;

  /** Reference to the bottom panel (where background image or library is displayed). */
  private JPanel bottomPanel;

  /**
   * Represents the main landing UI displayed to the user on application start.
   */
  public LandingUi() {
    this.setTitle(LocaleService.getString("jj.app.name"));
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new QuitAppAction());
    this.initComponents();

    SwingUtilities.invokeLater(() -> {
      LOGGER.info("Displaying the main Landing UI...");
      LandingUi.this.pack();
      LandingUi.this.setLocationRelativeTo(null);
      LandingUi.this.setVisible(true);

      // stating the intro music
      final SoundService sound = AppRegistry.getInstance().getSoundService();
      sound.startMusic(SoundEnum.OPENING, 1);
    });
    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
    }

    this.playerDialog = new PlayerDialog(this);
    this.playerDialog.setLocationRelativeTo(this);
    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this);
    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this.playerDialog);
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
    GameDataService gameService = AppRegistry.getInstance().getGameDataService();
    final List<Player> players = gameService.getCurrentPlayers();
    this.playerDialog.updatePlayersPane(players);

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
    List<Player> players = AppRegistry.getInstance().getGameDataService().getCurrentPlayers();
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
    String currGameName = null;
    if (gameDataService.hasCurrentGameData()) {
      GameData gameData = gameDataService.getCurrentGameData();
      if (gameData.isGameDataUsable()) {
        currGameName = gameData.getGameName();
      }
    }
    this.currGameLabel.setEnabled(currGameName != null);
    currGameName = currGameName == null ?
        LocaleService.getString("jj.playerdialog.game.default.name") : currGameName;
    this.currGameLabel.setText(currGameName);

    // Check if the Start game button should be enabled.
    if (gameDataService.isGameReady()) {
      this.startGameButton.setEnabled(true);
      this.startGameButton.requestFocus();
    }
  }

  /**
   * Shows the next card in the bottom panel - the Library UI card or the background card.
   */
  public void switchBetweenLibraryAndBackgroundCard() {
    CardLayout cardLayout = (CardLayout) this.bottomPanel.getLayout();
    cardLayout.next(this.bottomPanel);
  }

  /** Initializes UI components. */
  private void initComponents() {
    // Setting the main layout.
    Container contentPane = getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL},  // columns
      {TableLayout.PREFERRED, TableLayout.FILL}}));  // rows

    // ******* Top panel - with the current game name and buttons to load and start the game.
    JPanel topPanel = this.createMainContentPanel();
    contentPane.add(topPanel, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));

    // ********* Bottom panel - library OR background image.
    this.bottomPanel = new JPanel();
    this.bottomPanel.setLayout(new CardLayout());
    this.bottomPanel.add(createImageBackground(), CARD_BACKGROUND_ID);
    this.bottomPanel.add(createLibraryPanel(), CARD_LIBRARY_ID);
    contentPane.add(this.bottomPanel, new TableLayoutConstraints(
        0, 1, 0, 1, TableLayout.FULL, TableLayout.BOTTOM));

    // ********* Adding a menu bar
    this.setJMenuBar(new LandingUiMenu());
  }

  /**
   * Creates the main content panel with action text and buttons.
   * @return initialized main content panel
   */
  private JPanel createMainContentPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
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
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int buttonSpacing = lafTheme.getButtonSpacing();
    final Font buttonFont = lafTheme.getButtonFont();

    final JPanel gamePanel = new JPanel(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, buttonSpacing, TableLayout.PREFERRED,
        buttonSpacing, TableLayout.PREFERRED, buttonSpacing, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {TableLayout.PREFERRED}})); // rows
    this.currGameLabel = new JLabel(LocaleService.getString("jj.playerdialog.game.default.name"));
    this.currGameLabel.setFont(lafTheme.getLandingLabelFont());
    this.currGameLabel.setEnabled(false);
    gamePanel.add(this.currGameLabel, new TableLayoutConstraints(
      1, 0, 1, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

    // ******* Load game button.
    final JButton loadButton = new JButton();
    ClickAndKeyAction.createAndAddAction(loadButton, this::handleLoadGameAction);
    loadButton.setFont(buttonFont);
    loadButton.setText(LocaleService.getString("jj.playerdialog.button.load"));
    gamePanel.add(loadButton, new TableLayoutConstraints(
      3, 0, 3, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

    // ******* Game library button.
    this.libraryButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.libraryButton, this::handleShowLibraryAction);
    this.libraryButton.setFont(buttonFont);
    this.libraryButton.setText(LocaleService.getString("jj.playerdialog.button.library"));
    gamePanel.add(this.libraryButton, new TableLayoutConstraints(
      5, 0, 5, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

    // ******* Start game button.
    this.startGameButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.startGameButton, this::handleStartGameAction);
    this.startGameButton.setFont(buttonFont);
    this.startGameButton.setText(LocaleService.getString("jj.playerdialog.button.start"));
    this.startGameButton.setEnabled(false);
    gamePanel.add(this.startGameButton, new TableLayoutConstraints(
      7, 0, 7, 0, TableLayout.CENTER, TableLayoutConstraints.CENTER));

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
        (arePlayersSet ? (int) (JjDefaults.LANDING_UI_WIDTH * .75) : TableLayout.PREFERRED),
        TableLayout.PREFERRED, 15, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {5, TableLayout.PREFERRED, 5}})); // rows

    // Players list rendered as un-editable text pane.
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
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
    ClickAndKeyAction.createAndAddAction(updateButton, this::showPlayerDialog);
    updateButton.setFont(lafTheme.getButtonFont());
    updateButton.setText(buttonText);
    panel.add(updateButton, new TableLayoutConstraints(
      4, 1, 4, 1, TableLayout.LEFT, TableLayout.CENTER));
    return panel;
  }

  /**
   * Creates the library UI component.
   * @return library UI component
   */
  private static Component createLibraryPanel() {
    boolean loadSuccess = AppRegistry.getInstance().getGameDataService().loadDefaultGames();

    JPanel panel = new JPanel();
    if (!loadSuccess) {
      // TODO: add an error message to the returned panel.
      return panel;
    }
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(new Dimension(JjDefaults.LANDING_UI_WIDTH, JjDefaults.LANDING_UI_LIBRARY_HEIGHT));
    panel.setLayout(new GridLayout(0, 1));

    for (int i = 0; i < 7; i++) {
      for (GameData game : AppRegistry.getInstance().getGameDataService().getAllGames()) {
        // Assume the data has already been validated to be usable.
        panel.add(new LibraryGameItem(game));
      }
    }
    return scrollPane;
  }

  /**
   * Creates image background (label with image icon) to use
   * for the landing UI background.
   * @return label with an ImageIcon representing the background
   */
  private static JLabel createImageBackground() {
    ImageIcon backgroundIcon = new ImageIcon(Objects.requireNonNull(
        App.class.getResource("images/" + BACKGROUND_IMAGE_FILE)));
    JLabel backgroundLabel = new JLabel(backgroundIcon);
    backgroundIcon.setImage(backgroundIcon.getImage().getScaledInstance(
        JjDefaults.LANDING_UI_WIDTH, JjDefaults.LANDING_UI_LIBRARY_HEIGHT, Image.SCALE_FAST));
    return backgroundLabel;
  }

  /**
   * Opens file chooser dialog to load a new game file.
   */
  private void handleLoadGameAction() {
    LOGGER.info("Handling the Load button action.");
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(settingsService.getSettings().getLastCurrentDirectory()));
    final int result = fileChooser.showOpenDialog(this);
    settingsService.saveLastCurrentDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
    settingsService.persistSettings();
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      GameDataService gameDataService = registry.getGameDataService();
      GameData gameData = gameDataService.parseGameData(selectedFile.getAbsolutePath());
      FileParsingResult parsingResults = gameData.generateFileParsingResult();
      registry.getUiService().showParsingResult(parsingResults, this);

      if (gameData.isGameDataUsable()) {
        gameDataService.setCurrentGameData(gameData);
        this.updateUiWithLoadedGameFile();
      }
    }
  }

  /**
   * Starts a new game (assuming all data is ready and valid).
   */
  private void handleStartGameAction() {
    LOGGER.info("Handling the Start Game button action.");
    Registry registry = AppRegistry.getInstance();
    registry.getSoundService().stopAllMusic();
    this.setVisible(false);
    registry.getMainService().startGame();
  }

  /** Handles the Show library button action. */
  private void handleShowLibraryAction() {
    this.switchBetweenLibraryAndBackgroundCard();
  }
}
