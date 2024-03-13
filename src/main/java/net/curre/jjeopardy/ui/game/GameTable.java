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

import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.event.GameTableListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Represents the Game table.
 *
 * @see GameTableCellRenderer
 * @see GameTableHeaderRenderer
 * @author Yevgeny Nyden
 */
public class GameTable extends JTable {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Reference to the table cell renderer. */
  private final TableCellRenderer renderer;

  /** Reference to the table data model. */
  private final GameTableModel model;

  /** Constructs a new game table. */
  public GameTable() {
    logger.info("Creating game table");

    // Initializing game data.
    GameData data = AppRegistry.getInstance().getGameDataService().getCurrentGameData();
    this.model = new GameTableModel(data);
    this.setModel(this.model);

    // Creating renderer and setting up the UI.
    this.renderer = new GameTableCellRenderer(this.model);
    setPreferredScrollableViewportSize(
        new Dimension(JjDefaults.GAME_TABLE_MIN_WIDTH, JjDefaults.GAME_TABLE_MIN_HEIGHT));

    resizeAndRefreshTable();

    this.setDragEnabled(false);
    this.setSurrendersFocusOnKeystroke(true);
    this.setRowSelectionAllowed(false);

    // Configuring the table header.
    JTableHeader header = this.getTableHeader();
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setDefaultRenderer(new GameTableHeaderRenderer());
    header.setPreferredSize(
        new Dimension(this.getColumnModel().getTotalColumnWidth(), JjDefaults.GAME_TABLE_HEADER_HEIGHT));

    // Adding resize and mouse-click listener.
    final GameTableListener gtListener = new GameTableListener(this);
    this.addComponentListener(gtListener);
    this.addMouseListener(gtListener);
    
    // Ignore the tooltips.
    ToolTipManager.sharedInstance().unregisterComponent(this);
    ToolTipManager.sharedInstance().unregisterComponent(this.getTableHeader());
  }

  /**
   * Prepares the game for a new round.
   */
  public void prepareGame() {
    this.model.prepareGame();
  }

    /** {@inheritDoc} */
  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    return this.renderer;
  }

  /**
   * Resizes and refreshes table - row height are computed
   * and set here according to the current table height,
   * and column widths are resized automatically.
   */
  public void resizeAndRefreshTable() {
    final double rowNum = this.model.getRowCount();
    final double height = this.getSize().getHeight() / rowNum;
    final double heightToSet = Math.max(height, JjDefaults.GAME_TABLE_MIN_ROW_HEIGHT);
    for (int i = 0; i < rowNum; ++i) {
      setRowHeight(i, (int) heightToSet);
    }
  }

  /**
   * Enables font anti-aliasing in the current graphics context.
   * @param g Graphics context
   */
  @Override
  public void paint(Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
    super.paint(g);
  }
}
