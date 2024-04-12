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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents a library row item.
 *
 * @author Yevgeny Nyden
 */
public class LibraryGameItem extends JPanel {

  /** Reference to the game's data. */
  private final GameData gameData;

  /** Reference to the LAF service. */
  private final LafService lafService;

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
        {10, 30, 10, 250, 5, 435, 10, TableLayout.FILL, 5, 40, 5, 24, 5, 40, 5, 30, 5, 30, 10}, // columns
        {3, 30, 3}})); // rows
    this.setMaximumSize(new Dimension(JjDefaults.LANDING_UI_WIDTH, 36));

    // Game size icon.
    JLabel sizeLabel = new ItemIconLabel(
        null, gameData.getGameSizeIconSmall(), null, gameData.getGameSizeText(), null);
    this.add(sizeLabel, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Game name label.
    JLabel nameLabel = new JLabel(gameData.getGameName());
    nameLabel.setFont(font);
    this.add(nameLabel, new TableLayoutConstraints(
        3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER));

    // Game description label.
    JLabel descriptionLabel = new JLabel(gameData.getGameDescription());
    descriptionLabel.setFont(font);
    this.add(descriptionLabel, new TableLayoutConstraints(
        5, 1, 5, 1, TableLayout.LEFT, TableLayout.CENTER));

    // Players number label.
    int playersCount = gameData.getPlayerNames().size();
    if (playersCount > 0) {
      JLabel playersLabel = new ItemIconLabel(String.valueOf(playersCount), ImageEnum.USER_24, null,
          LocaleService.getString("jj.library.players.message", String.valueOf(playersCount)), null);
      this.add(playersLabel, new TableLayoutConstraints(
          9, 1, 9, 1, TableLayout.CENTER, TableLayout.CENTER));
    }

    // Image download failure icon.
    if (gameData.isImageDownloadFailure()) {
      JLabel failureLabel = new ItemIconLabel(null, ImageEnum.IMAGE_FAILURE_24, null,
          LocaleService.getString("jj.library.failed.image"), null);
      this.add(failureLabel, new TableLayoutConstraints(
          11, 1, 11, 1, TableLayout.CENTER, TableLayout.CENTER));
    }

    // Game dimension label.
    JLabel dimensionLabel = new JLabel(gameData.getGameDimensionShortText());
    dimensionLabel.setFont(font);
    dimensionLabel.setToolTipText(gameData.getGameDimensionLongMessage());
    this.add(dimensionLabel, new TableLayoutConstraints(
        13, 1, 13, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Questions info button (to display game questions and answers).
    JLabel infoLabel = new ItemIconLabel(null, ImageEnum.EDIT_24, ImageEnum.EDIT_24_HOVER,
        LocaleService.getString("jj.library.info.button"), () -> {
      EditGameWindow frame = new EditGameWindow(gameData, true, EditTableMode.ALL);
      frame.setVisible(true);
    });
    this.add(infoLabel, new TableLayoutConstraints(
        15, 1, 15, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Remove game button.
    JLabel trashLabel = new ItemIconLabel(null, ImageEnum.TRASH_24, ImageEnum.TRASH_24_HOVER,
        LocaleService.getString("jj.file.info.remove.message"), this::handleDeleteGameItem);
    this.add(trashLabel, new TableLayoutConstraints(
        17, 1, 17, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Adding mouse hover and click actions.
    this.addMouseListener(new GameItemMouseAdapter());
  }

  /**
   * Deletes this game from the library (memory and disk).
   */
  private void deleteGame() {
    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().deleteGameFromLibrary(this.gameData);
    registry.getLandingUi().updateLibrary();
  }

  /** Handles removing a game library item. */
  public void handleDeleteGameItem() {
    AppRegistry.getInstance().getUiService().showConfirmationDialog(
        LocaleService.getString("jj.dialog.delete.game.title"),
        LocaleService.getString("jj.dialog.delete.game.msg"),
        LibraryGameItem.this::deleteGame,
        null);
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
}
