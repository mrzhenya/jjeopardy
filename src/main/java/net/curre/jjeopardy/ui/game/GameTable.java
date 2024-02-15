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

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.event.GameTableListener;
import net.curre.jjeopardy.util.Utilities;

import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Game table.
 *
 * @see GameTableCellRenderer
 * @see GameTableHeaderRenderer
 * @author Yevgeny Nyden
 */
public class GameTable extends JTable {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(GameTable.class.getName());

  /** Reference to the table cell renderer. */
  private final TableCellRenderer renderer;

  /** Minimum table row height. */
  private final int minRowHeight;

  /** Reference to the table data model. */
  private final GameTableModel model;

  /** Constructs a new game table. */
  public GameTable() {
    // Loading essential default properties.
    int rowHeight = 0, preferredHeaderHeight = 0, defaultTableWidth = 0, defaultTableHeight = 0;
    try {
      defaultTableWidth = Utilities.getDefaultIntProperty("jj.defaults.game.table.width");
      defaultTableHeight = Utilities.getDefaultIntProperty("jj.defaults.game.table.height");
      rowHeight = Utilities.getDefaultIntProperty("jj.defaults.min.row.height");
      preferredHeaderHeight = Utilities.getDefaultIntProperty("jj.defaults.preferred.header.height");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to initialize default properties", e);
      System.exit(1);
    }
    this.minRowHeight = rowHeight;

    // Initializing game data.
    GameData data = AppRegistry.getInstance().getGameData();
    this.model = new GameTableModel(data);
    this.setModel(this.model);

    // Creating renderer and setting up the UI.
    this.renderer = new GameTableCellRenderer(this.model);
    setPreferredScrollableViewportSize(new Dimension(defaultTableWidth, defaultTableHeight));

    resizeAndRefreshTable();

    this.setDragEnabled(false);
    this.setSurrendersFocusOnKeystroke(true);
    this.setRowSelectionAllowed(false);

    // Configuring the table header.
    JTableHeader header = this.getTableHeader();
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setDefaultRenderer(new GameTableHeaderRenderer());
    header.setPreferredSize(new Dimension(this.getColumnModel().getTotalColumnWidth(), preferredHeaderHeight));

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
    final double heightToSet = Math.max(height, this.minRowHeight);
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
