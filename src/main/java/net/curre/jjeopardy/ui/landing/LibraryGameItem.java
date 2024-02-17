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
import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author Yevgeny Nyden
 */
public class LibraryGameItem extends JPanel {

  /**
   * Creates a new game library item. Assumes the game data is valid.
   * @param gameData valid game data
   */
  public LibraryGameItem(GameData gameData) {
    this.setLayout(new TableLayout(new double[][] {
        {10, 200, 5, 500, TableLayout.FILL, 40, 10}, // columns
        {3, 20, 3}})); // rows
    String gameName = gameData.getGameName();
    List<Category> categories = gameData.getCategories();
    String questionsString = categories.size() + " x " + categories.get(0).getQuestionsCount();
    this.setToolTipText(gameData.getGameDescription());

    this.add(new JLabel(gameName), new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER));
    JLabel descriptionLabel = new JLabel(gameData.getGameDescription());
    this.add(descriptionLabel, new TableLayoutConstraints(
        3, 1, 3, 1, TableLayout.LEFT, TableLayout.CENTER));
    this.add(new JLabel(String.valueOf(questionsString)), new TableLayoutConstraints(
        5, 1, 5, 1, TableLayout.CENTER, TableLayout.CENTER));

    this.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent evt) {
        LibraryGameItem.this.setBackground(Color.GREEN);
      }

      public void mouseExited(MouseEvent evt) {
        LibraryGameItem.this.setBackground(UIManager.getColor("control"));
      }

      public void mousePressed(MouseEvent evt) {
        LibraryGameItem.this.setBackground(Color.YELLOW);
      }

      public void mouseReleased(MouseEvent e) {
        LibraryGameItem.this.setBackground(Color.GREEN);
      }
    });
  }
}
