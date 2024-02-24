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
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
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
  public LibraryGameItem(GameData gameData) {
    this.gameData = gameData;
    this.lafService = AppRegistry.getInstance().getLafService();
    LafTheme lafTheme = this.lafService.getCurrentLafTheme();
    Font font = lafTheme.getDialogTextFont();
    this.setLayout(new TableLayout(new double[][] {
        {10, 30, 10, 200, 5, 450, TableLayout.FILL, 40, 10}, // columns
        {3, 30, 3}})); // rows
    this.setToolTipText(gameData.getGameDescription());
    this.setMaximumSize(new Dimension(JjDefaults.LANDING_UI_WIDTH, 36));

    // Game size icon.
    ImageEnum sizeIcon = gameData.getGameSizeIconSmall();
    this.add(new JLabel(sizeIcon.toImageIcon()), new TableLayoutConstraints(
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

    // Game size text label.
    JLabel sizeLabel = new JLabel(gameData.getCategoriesCount() + "x" + gameData.getCategoryQuestionsCount());
    sizeLabel.setFont(font);
    this.add(sizeLabel, new TableLayoutConstraints(
        7, 1, 7, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Adding mouse hover and click actions.
    this.addMouseListener(new MouseAdapter() {
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
    });
  }
}
