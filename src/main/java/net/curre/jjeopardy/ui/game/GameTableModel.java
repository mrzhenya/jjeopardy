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

import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Question;

import javax.swing.table.AbstractTableModel;

/**
 * Game table model.
 * After creating this object, it must be initialized by calling #prepareGame.
 * This method should also be used to reset the game data for a new game.
 *
 * @see GameWindow
 * @see GameTable
 * @author Yevgeny Nyden
 */
public class GameTableModel extends AbstractTableModel {

  /** What's displayed in a game cell for questions that have been asked. */
  private static final String ANSWERED_QUESTION_STRING = "";

  /** Reference to the game data. */
  private final GameData gameData;

  /** Record of the empty cells on the table. */
  private boolean[][] emptyCells;

  /**
   * Ctor.
   * @param gameData reference to the game data
   */
  public GameTableModel(GameData gameData) {
    this.gameData = gameData;
  }

  /**
   * Prepares the game model for a new game.
   */
  public void prepareGame() {
    int columnCount = gameData.getCategories().size();
    int rowCount = 0;
    if (columnCount > 0) {
      // This shouldn't happen, but just in case we ensure there are categories.
      // Also, we assume here that the number of questions is the same across all categories.
      rowCount = gameData.getCategories().get(0).getQuestionsCount();
    }
    this.emptyCells = new boolean[rowCount][columnCount];
    this.gameData.resetGameData();
  }

  /**
   * Gets column count.
   * @return column count
   */
  public int getColumnCount() {
    return this.gameData.getCategoriesCount();
  }

  /**
   * Gets the row count.
   * @return the number of questions in each category
   */
  public int getRowCount() {
    return this.gameData.getCategoryQuestionsCount();
  }

  /** {@inheritDoc} */
  @Override
  public String getColumnName(int column) {
    return this.gameData.getCategories().get(column).getName();
  }

  /**
   * Gets value for the given row and the given column.
   * @param row item's row
   * @param column item's column
   * @return item's value
   */
  public Object getValueAt(int row, int column) {
    final Category category = this.gameData.getCategories().get(column);
    final Question question = category.getQuestion(row);
    if (question.isHasBeenAsked() || question.isNotAskable()) {
      this.emptyCells[row][column] = true;
      return ANSWERED_QUESTION_STRING;
    }
    return Integer.toString(question.getPoints());
  }

  /** {@inheritDoc} */
  @Override
  public Class<?> getColumnClass(int column) {
    // Every value in the table is a string.
    return String.class;
  }

  /**
   * This method always returns false.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  public boolean isCellEditable(int row, int column) {
    return false;
  }

  /**
   * Determines if a given cell is empty (the question has been answered).
   * @param row row of the cell to check
   * @param column column of the cell to check
   * @return true if the cell is empty; false if otherwise
   */
  public boolean isCellEmpty(int row, int column) {
    return this.emptyCells[row][column];
  }
}
