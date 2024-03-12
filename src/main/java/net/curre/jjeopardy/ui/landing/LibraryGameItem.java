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
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
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
  public LibraryGameItem(GameData gameData) {
    this.gameData = gameData;
    this.lafService = AppRegistry.getInstance().getLafService();
    LafTheme lafTheme = this.lafService.getCurrentLafTheme();
    Font font = lafTheme.getDialogTextFont();
    this.setLayout(new TableLayout(new double[][] {
        {10, 30, 10, 250, 5, 470, 10, TableLayout.FILL, 5, 40, 5, 20, 5, 40, 5, 35, 10}, // columns
        {3, 30, 3}})); // rows
    this.setMaximumSize(new Dimension(JjDefaults.LANDING_UI_WIDTH, 36));

    // Game size icon.
    ImageEnum sizeIcon = gameData.getGameSizeIconSmall();
    JLabel sizeLabel = new JLabel(sizeIcon.toImageIcon());
    sizeLabel.setToolTipText(gameData.getGameSizeText());
    this.add(sizeLabel, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Game name label.
    JLabel nameLabel = new JLabel(gameData.getGameName());
    nameLabel.setFont(font);
    nameLabel.setToolTipText(gameData.getGameName());
    this.add(nameLabel, new TableLayoutConstraints(
        3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER));

    // Game description label.
    JLabel descriptionLabel = new JLabel(gameData.getGameDescription());
    descriptionLabel.setFont(font);
    descriptionLabel.setToolTipText(gameData.getGameDescription());
    this.add(descriptionLabel, new TableLayoutConstraints(
        5, 1, 5, 1, TableLayout.LEFT, TableLayout.CENTER));

    // Players number label.
    int playersCount = gameData.getPlayerNames().size();
    if (playersCount > 0) {
      JLabel playersLabel = new JLabel(ImageEnum.USER_24.toImageIcon());
      playersLabel.setFont(font);
      playersLabel.setText(String.valueOf(playersCount));
      playersLabel.setToolTipText(
          LocaleService.getString("jj.landing.library.players.message", String.valueOf(playersCount)));
      this.add(playersLabel, new TableLayoutConstraints(
          9, 1, 9, 1, TableLayout.CENTER, TableLayout.CENTER));
    }

    // Image download failure icon.
    if (gameData.isImageDownloadFailure()) {
      JLabel failureLabel = new JLabel(ImageEnum.IMAGE_FAILURE_24.toImageIcon());
      failureLabel.setToolTipText(LocaleService.getString("jj.landing.library.failed.image"));
      this.add(failureLabel, new TableLayoutConstraints(
          11, 1, 11, 1, TableLayout.CENTER, TableLayout.CENTER));
    }

    // Game size text label.
    JLabel dimensionLabel = new JLabel(gameData.getGameDimensionShortText());
    dimensionLabel.setFont(font);
    dimensionLabel.setToolTipText(gameData.getGameDimensionLongMessage());
    this.add(dimensionLabel, new TableLayoutConstraints(
        13, 1, 13, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Remove game button.
    JButton button  = new JButton();
    button.setAction(new RemoveGameAction());
    button.setText("-");
    button.setToolTipText(LocaleService.getString("jj.file.info.remove.message"));
    this.add(button, new TableLayoutConstraints(
        15, 1, 15, 1, TableLayout.CENTER, TableLayout.CENTER));

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

  /**
   * Action handler for the remove game button.
   */
  private class RemoveGameAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
      AppRegistry.getInstance().getUiService().showConfirmationDialog(
          LocaleService.getString("jj.dialog.delete.game.title"),
          LocaleService.getString("jj.dialog.delete.game.msg"),
          LibraryGameItem.this::deleteGame,
          null);
    }
  }

  /**
   * Handler for the mouse actions.
   */
  private class GameItemMouseAdapter extends MouseAdapter {
    public void mouseEntered(MouseEvent evt) {
      LafTheme lafTheme = LibraryGameItem.this.lafService.getCurrentLafTheme();
      Color highlight = LafService.createAdjustedColor(lafTheme.getDefaultBackgroundColor(), 25);
      LibraryGameItem.this.setBackground(highlight);
    }

    public void mouseExited(MouseEvent evt) {
      LibraryGameItem.this.setBackground(UIManager.getColor("control"));
    }

    public void mousePressed(MouseEvent evt) {
      LafTheme lafTheme = LibraryGameItem.this.lafService.getCurrentLafTheme();
      Color highlight = LafService.createAdjustedColor(lafTheme.getDefaultBackgroundColor(), 50);
      LibraryGameItem.this.setBackground(highlight);
    }

    public void mouseReleased(MouseEvent e) {
      LafTheme lafTheme = LibraryGameItem.this.lafService.getCurrentLafTheme();
      Color highlight = LafService.createAdjustedColor(lafTheme.getDefaultBackgroundColor(), 25);
      LibraryGameItem.this.setBackground(highlight);
      UiService uiService = AppRegistry.getInstance().getUiService();
      uiService.showGameInfoDialog(LibraryGameItem.this.gameData, null);
    }
  }
}
