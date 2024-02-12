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
import net.curre.jjeopardy.ui.laf.LafService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

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

  /** Reference to the table cell renderer. */
  private final javax.swing.table.TableCellRenderer renderer;

  /** Preferred table header height. */
  private static final int PREFERRED_HEADER_HEIGHT = 86;

  /** Reference to the table data model. */
  private final GameTableModel model;

  /** Constructs a new game table. */
  public GameTable() {
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    GameData data = AppRegistry.getInstance().getGameData();
    this.model = new GameTableModel(data);
    this.setModel(this.model);

    this.renderer = new GameTableCellRenderer(this.model);
    setPreferredScrollableViewportSize(
      new Dimension(LafService.DEFAULT_GAME_TABLE_WIDTH, LafService.DEFAULT_GAME_TABLE_HEIGHT));

    resizeAndRefreshTable();

    this.setDragEnabled(false);
    this.setSurrendersFocusOnKeystroke(true);
    this.setRowSelectionAllowed(false);

    // configuring the table header
    JTableHeader header = this.getTableHeader();
    Dimension size = header.getSize();
    size.setSize(size.getWidth(), 50);
    header.setPreferredSize(size);
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setDefaultRenderer(new GameTableHeaderRenderer());
    header.setPreferredSize(new Dimension(this.getColumnModel().getTotalColumnWidth(), PREFERRED_HEADER_HEIGHT));

    // adding resize and mouse-click listener
    final GameTableListener gtListener = new GameTableListener(this);
    this.addComponentListener(gtListener);
    this.addMouseListener(gtListener);
    
    // ignoring the tooltips
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
    final double heightToSet = Math.max(height, LafService.GAME_TABLE_MIN_ROW_HEIGHT);
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
