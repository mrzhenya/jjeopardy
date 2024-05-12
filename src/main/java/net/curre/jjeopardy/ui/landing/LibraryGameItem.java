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
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LafService;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.edit.EditGameWindow;
import net.curre.jjeopardy.ui.edit.EditTableMode;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * Represents a library row item.
 *
 * @author Yevgeny Nyden
 */
public class LibraryGameItem extends JPanel {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(LibraryGameItem.class.getName());

  /** Reference to the game's data. */
  private final GameData gameData;

  /** Reference to the LAF service. */
  private final LafService lafService;

  /** Reference to the size label. */
  private final ItemIconLabel sizeLabel;

  /** Reference to the game name label. */
  private final JLabel nameLabel;

  /** Reference to the game description label. */
  private final JLabel descriptionLabel;

  /** Reference to the players label. */
  private final ItemIconLabel playersLabel;

  /** Reference to the failed download image label. */
  private final ItemIconLabel imageFailureLabel;

  /** Reference to the game dimension label. */
  private final JLabel dimensionLabel;

  /**
   * Creates a new game library item. Assumes the game data is valid.
   * @param gameData valid game data
   */
  public LibraryGameItem(@NotNull GameData gameData) {
    this.gameData = gameData;
    this.lafService = AppRegistry.getInstance().getLafService();
    LafTheme lafTheme = this.lafService.getCurrentLafTheme();
    Font font = lafTheme.getDialogTextFont();
    this.setLayout(new TableLayout(new double[][] {
        {10, 30, 10, 240, 5, 410, 10, TableLayout.FILL, 5, 40, 5, 24, 5, 40, 5, 30, 5, 30, 5, 30, 10}, // columns
        {3, 30, 3}})); // rows
    this.setMaximumSize(new Dimension(JjDefaults.LANDING_UI_WIDTH, 36));

    // Game size icon.
    this.sizeLabel = new ItemIconLabel(null, gameData.getGameSizeIconSmall(), gameData.getGameSizeText());
    this.add(this.sizeLabel, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Game name label.
    this.nameLabel = new JLabel(gameData.getGameName());
    this.nameLabel.setFont(font);
    this.add(nameLabel, new TableLayoutConstraints(
        3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER));

    // Game description label.
    this.descriptionLabel = new JLabel(gameData.getGameDescription());
    this.descriptionLabel.setFont(font);
    this.add(this.descriptionLabel, new TableLayoutConstraints(
        5, 1, 5, 1, TableLayout.LEFT, TableLayout.CENTER));

    // Players number label.
    int playersCount = gameData.getPlayerNames().size();
    this.playersLabel = new ItemIconLabel();
        this.add(this.playersLabel, new TableLayoutConstraints(
            9, 1, 9, 1, TableLayout.CENTER, TableLayout.CENTER));
    if (playersCount > 0) {
      this.playersLabel.updateContent(String.valueOf(playersCount), ImageEnum.USER_24,
          LocaleService.getString("jj.library.players.message", String.valueOf(playersCount)));
    }

    // Image download failure icon.
    this.imageFailureLabel = new ItemIconLabel();
    this.add(this.imageFailureLabel, new TableLayoutConstraints(
        11, 1, 11, 1, TableLayout.CENTER, TableLayout.CENTER));
    if (gameData.isImageDownloadFailure()) {
      this.imageFailureLabel.updateContent(null, ImageEnum.IMAGE_FAILURE_24,
          LocaleService.getString("jj.library.failed.image"));
    }

    // Game dimension label.
    this.dimensionLabel = new JLabel(gameData.getGameDimensionShortText());
    this.dimensionLabel.setFont(font);
    this.dimensionLabel.setToolTipText(gameData.getGameDimensionLongMessage());
    this.add(this.dimensionLabel, new TableLayoutConstraints(
        13, 1, 13, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Show game directory button.
    JButton dirLabel = new ItemIconButton(null, ImageEnum.OPEN_FILE_24, ImageEnum.OPEN_FILE_24_HOVER,
        LocaleService.getString("jj.library.dir.button"),this::showGameDirectory);
    this.add(dirLabel, new TableLayoutConstraints(
        15, 1, 15, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Game edit button (to edit game information).
    JButton editLabel = new ItemIconButton(null, ImageEnum.EDIT_24, ImageEnum.EDIT_24_HOVER,
        LocaleService.getString("jj.library.info.button"), () -> {
      EditGameWindow frame = new EditGameWindow(this.gameData, true, EditTableMode.ALL);
      frame.setVisible(true);
    });
    this.add(editLabel, new TableLayoutConstraints(
        17, 1, 17, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Remove game button.
    JButton trashLabel = new ItemIconButton(null, ImageEnum.TRASH_24, ImageEnum.TRASH_24_HOVER,
        LocaleService.getString("jj.file.info.remove.message"), this::handleDeleteGameItem);
    this.add(trashLabel, new TableLayoutConstraints(
        19, 1, 19, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Adding mouse hover and click actions.
    this.addMouseListener(new GameItemMouseAdapter());
  }

  /** Refreshes this library game item with (possibly) the new data. */
  public void updateContent() {
    this.sizeLabel.updateContent(null, this.gameData.getGameSizeIconSmall(), this.gameData.getGameSizeText());
    this.nameLabel.setText(this.gameData.getGameName());
    this.descriptionLabel.setText(this.gameData.getGameDescription());
    int playersCount = this.gameData.getPlayerNames().size();
    if (playersCount > 0) {
      this.playersLabel.updateContent(String.valueOf(playersCount), ImageEnum.USER_24,
          LocaleService.getString("jj.library.players.message", String.valueOf(playersCount)));
    } else {
      this.playersLabel.clearContent();
    }
    if (this.gameData.isImageDownloadFailure()) {
      this.imageFailureLabel.updateContent(null, ImageEnum.IMAGE_FAILURE_24,
          LocaleService.getString("jj.library.failed.image"));
    } else {
      this.imageFailureLabel.clearContent();
    }
    this.dimensionLabel.setText(this.gameData.getGameDimensionShortText());
    this.dimensionLabel.setToolTipText(this.gameData.getGameDimensionLongMessage());

    this.revalidate();
    this.repaint();
  }

  /**
   * Deletes this game from the library (memory and disk).
   */
  private void deleteGame() {
    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().deleteGameFromLibrary(this.gameData);
    registry.getLandingUi().updateLibrary(null);
  }

  /** Handles removing a game library item. */
  public void handleDeleteGameItem() {
    AppRegistry.getInstance().getUiService().showConfirmationDialog(
        LocaleService.getString("jj.dialog.delete.game.title"),
        LocaleService.getString("jj.dialog.delete.game.msg"),
        LibraryGameItem.this::deleteGame, null, null);
  }

  /**
   * Checks if the given game is the one contained in this library item.
   * @param game game to check
   * @return true if this game library item contains the passed game
   */
  public boolean gameEquals(GameData game) {
    return this.gameData.equals(game);
  }

  /**
   * Handler for the mouse actions.
   */
  private class GameItemMouseAdapter extends MouseAdapter {

    /** @inheritDoc */
    @Override
    public void mouseEntered(MouseEvent evt) {
      LafTheme lafTheme = LibraryGameItem.this.lafService.getCurrentLafTheme();
      Color highlight = LafService.createAdjustedColor(lafTheme.getDefaultBackgroundColor(), 25);
      LibraryGameItem.this.setBackground(highlight);
    }

    /** @inheritDoc */
    @Override
    public void mouseExited(MouseEvent evt) {
      LibraryGameItem.this.setBackground(UIManager.getColor("control"));
    }

    /** @inheritDoc */
    @Override
    public void mousePressed(MouseEvent evt) {
      LafTheme lafTheme = LibraryGameItem.this.lafService.getCurrentLafTheme();
      Color highlight = LafService.createAdjustedColor(lafTheme.getDefaultBackgroundColor(), 50);
      LibraryGameItem.this.setBackground(highlight);
    }

    /** @inheritDoc */
    @Override
    public void mouseReleased(MouseEvent e) {
      LafTheme lafTheme = LibraryGameItem.this.lafService.getCurrentLafTheme();
      Color highlight = LafService.createAdjustedColor(lafTheme.getDefaultBackgroundColor(), 25);
      LibraryGameItem.this.setBackground(highlight);
      UiService uiService = AppRegistry.getInstance().getUiService();
      uiService.showGameInfoDialog(LibraryGameItem.this.gameData, null);
    }
  }

  /**
   * Opens the game bundle directory in the system file browser.
   */
  private void showGameDirectory() {
    File directory = new File(this.gameData.getBundlePath());
      try {
          Desktop.getDesktop().open(directory);
      } catch (IOException e) {
          logger.error("Unable to show game directory", e);
      }
  }
}
