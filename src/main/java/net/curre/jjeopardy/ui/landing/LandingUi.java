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
import net.curre.jjeopardy.event.ClosingWindowListener;
import net.curre.jjeopardy.service.*;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.ui.player.PlayerDialog;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
  private static final Logger logger = LogManager.getLogger(LandingUi.class.getName());

  /** Reference to the menu bar. */
  private LandingUiMenu menuBar;

  /** Reference to the PlayerDialog. */
  private final PlayerDialog playerDialog;

  /** Current game label. */
  private JLabel currGameLabel;

  /** Start game button. */
  private JButton startGameButton;

  /** Reference to the container of the players list component. */
  private JPanel playersPanel;

  /** Reference to the bottom panel (where background image or library is displayed). */
  private JPanel bottomPanel;

  /** Reference to the library panel that contains library game items. */
  private JPanel libraryPanel;

  /**
   * Represents the main landing UI displayed to the user on application start.
   */
  public LandingUi() {
    this.setTitle(LocaleService.getString("jj.app.name"));
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new ClosingWindowListener(this::quitApp));
    this.initComponents();

    SwingUtilities.invokeLater(() -> {
      logger.info("Displaying the main Landing UI...");
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

    this.playerDialog = new PlayerDialog(this::handleSavePlayersAction);
    this.playerDialog.setLocationRelativeTo(this);
    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this);
    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this.playerDialog);
  }

  /**
   * Displays the player dialog.
   */
  public void showPlayerDialog() {
    this.playerDialog.showDialog(
        AppRegistry.getInstance().getGameDataService().getCurrentPlayerNames());
  }

  /**
   * Updates the Landing UI according to the loaded game data.
   */
  public void updateUiWithLoadedGameFile() {
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
        LocaleService.getString("jj.playerdialog.addplayers.label.intro")).append("  ");
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
    boolean printEnabled = false;
    String currGameName = null;
    if (gameDataService.hasCurrentGameData()) {
      GameData gameData = gameDataService.getCurrentGameData();
      if (gameData.isGameDataUsable()) {
        currGameName = gameData.getGameName();
        printEnabled = true;
      }
    }
    this.menuBar.updatePrintMenuItem(printEnabled);
    this.currGameLabel.setEnabled(printEnabled);
    currGameName = printEnabled ? currGameName : LocaleService.getString("jj.playerdialog.game.default.name");
    this.currGameLabel.setText(currGameName);

    // Check if the Start game button should be enabled.
    if (gameDataService.isGameReady()) {
      this.startGameButton.setEnabled(true);
      this.startGameButton.requestFocus();
    }
  }

  /**
   * Updates game library UI after new games are added.
   */
  public void updateLibrary() {
    List<GameData> games = AppRegistry.getInstance().getGameDataService().getLibraryGames();

    // First, check if any items should be removed.
    List<LibraryGameItem> itemsToRemove = new ArrayList<>();
    for (Component component : this.libraryPanel.getComponents()) {
      LibraryGameItem item = (LibraryGameItem) component;
      boolean gameExist = false;
      for (GameData game : games) {
        if (item.gameEquals(game)) {
          gameExist = true;
          break;
        }
      }
      if (!gameExist) {
        itemsToRemove.add(item);
      }
    }
    for (LibraryGameItem item : itemsToRemove) {
      this.libraryPanel.remove(item);
    }

    // Now, check if any new items should be added; assume library
    // games and game library items on the panel are both sorted.
    for (int ind = 0; ind < games.size(); ind++) {
      GameData game = games.get(ind);
      LibraryGameItem item = (LibraryGameItem) this.libraryPanel.getComponent(ind);
      if (!item.gameEquals(game)) {
        this.libraryPanel.add(new LibraryGameItem(game), ind);
      }
    }

    this.libraryPanel.revalidate();
    this.libraryPanel.repaint();
  }

  /**
   * Shows the next card in the bottom panel - the Library UI card or the background card.
   */
  public void switchBetweenLibraryAndBackgroundCard() {
    CardLayout cardLayout = (CardLayout) this.bottomPanel.getLayout();
    cardLayout.next(this.bottomPanel);
  }

  /** Quits the application. */
  public void quitApp() {
    logger.info("Handling application exit...");
    System.exit(0);
  }

  /** Updates the current game players after they have been updated in the players' dialog. */
  private void handleSavePlayersAction() {
    List<String> playerNames = this.playerDialog.getPlayerNames();
    AppRegistry.getInstance().getGameDataService().updateCurrentPlayers(playerNames);
    this.updateLandingUi();
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
    this.menuBar = new LandingUiMenu(this);
    this.setJMenuBar(menuBar);
  }

  /**
   * Creates the main content panel with action text and buttons.
   * @return initialized main content panel
   */
  private @NotNull JPanel createMainContentPanel() {
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
  private @NotNull JPanel createGamePanel() {
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
    JButton libraryButton = new JButton();
    ClickAndKeyAction.createAndAddAction(libraryButton, this::handleShowLibraryAction);
    libraryButton.setFont(buttonFont);
    libraryButton.setText(LocaleService.getString("jj.playerdialog.button.library"));
    gamePanel.add(libraryButton, new TableLayoutConstraints(
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
  private @NotNull JPanel createPlayersInfoPanel(String playersText, String buttonText, boolean arePlayersSet) {
    JPanel panel = new JPanel(new TableLayout(new double[][] {
      {TableLayout.FILL,
        // Text area width.
        (arePlayersSet ? (int) (JjDefaults.LANDING_UI_WIDTH * .75) : TableLayout.PREFERRED),
        TableLayout.PREFERRED, 15, TableLayout.PREFERRED, TableLayout.FILL},  // columns
      {5, TableLayout.PREFERRED, 5}})); // rows

    // Players list rendered as un-editable text pane.
    JTextPane textPane = UiService.createDefaultTextPane();
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    textPane.setFont(lafTheme.getLandingLabelFont());
    textPane.setBackground(lafTheme.getDefaultBackgroundColor());
    textPane.setText(playersText);
    panel.add(textPane, new TableLayoutConstraints(
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
  private @NotNull Component createLibraryPanel() {
    this.libraryPanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane(this.libraryPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(10);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    boolean loadSuccess = AppRegistry.getInstance().getGameDataService().loadLibraryGames();
    if (!loadSuccess) {
      // TODO: add an error message to the returned panel.
      return scrollPane;
    }
    scrollPane.setPreferredSize(new Dimension(JjDefaults.LANDING_UI_WIDTH, JjDefaults.LANDING_UI_LIBRARY_HEIGHT));
    this.libraryPanel.setLayout(new BoxLayout(this.libraryPanel, BoxLayout.Y_AXIS));

    // Populating library with the library games.
    for (GameData game : AppRegistry.getInstance().getGameDataService().getLibraryGames()) {
      // Assume the data has already been validated to be usable.
      this.libraryPanel.add(new LibraryGameItem(game));
    }
    return scrollPane;
  }

  /**
   * Creates image background (label with image icon) to use
   * for the landing UI background.
   * @return label with an ImageIcon representing the background
   */
  private static @NotNull JLabel createImageBackground() {
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
    logger.info("Handling the Load button action.");
    Registry registry = AppRegistry.getInstance();
    SettingsService settingsService = registry.getSettingsService();

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileChooser.addChoosableFileFilter(new GameFileFilter());
    fileChooser.setCurrentDirectory(new File(settingsService.getSettings().getLastCurrentDirectory()));

    final int result = fileChooser.showOpenDialog(this);
    settingsService.saveLastCurrentDirectory(fileChooser.getCurrentDirectory().getAbsolutePath());
    settingsService.persistSettings();
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      GameDataService gameDataService = registry.getGameDataService();
      GameData gameData = gameDataService.parseGameFileOrBundle(selectedFile.getAbsolutePath());
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
    logger.info("Handling the Start Game button action.");
    Registry registry = AppRegistry.getInstance();
    registry.getSoundService().stopAllMusic();
    this.setVisible(false);
    registry.getGameService().startGame();
  }

  /** Handles the Show library button action. */
  private void handleShowLibraryAction() {
    this.switchBetweenLibraryAndBackgroundCard();
  }

  /** File selection filter for the JJeopardy game bundles. */
  private static class GameFileFilter extends FileFilter {

    /**
     * Recognizes either directories or xml/html files.
     * @param f the File to test
     * @return true to accept the file
     */
    @Override
    public boolean accept(@NotNull File f) {
      return f.isDirectory() || (f.isFile() &&
          (StringUtils.endsWithIgnoreCase(f.getName(), ".xml") ||
              StringUtils.endsWithIgnoreCase(f.getName(), ".html")));
    }

    @Override
    public @NotNull String getDescription() {
      return "JJeopardy game files or file bundles";
    }
  }
}
