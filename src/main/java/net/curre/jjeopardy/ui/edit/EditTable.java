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

package net.curre.jjeopardy.ui.edit;

import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.event.EditTableMouseListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.PrintUtilities;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Represents the edit table (though not a JTable) that is also used
 * during printing to preview the game and select print options before printing.
 *
 * @author Yevgeny Nyden
 */
public class EditTable extends JPanel implements Printable {

  /** Thickness of line printed at the top of the table for more emphasis. */
  private static final int EMPHASIS_LINE_HEIGHT = 1;

  /** Edit table header (to display game categories). */
  private final EditHeader header;

  /** List of edit table rows. */
  private final ArrayList<EditRow> rows;

  /** A game data for this table. */
  private final GameData gameData;

  /** Current view mode (answers only, question and answers, etc.). */
  private EditTableMode editTableMode;

  /**  Print page header text or null if no header should be printed. */
  private MessageFormat printHeader;

  /**  Print page footer text or null if no header should be printed. */
  private MessageFormat printFooter;

  /** Runnable to scroll to the top of the scrollable table area. */
  private final Runnable scrollToTopFn;

  /** Runnable to enable the Save button in the edit settings panel. */
  private final Runnable enableSaveFn;

  /** Reference to the edit table mouse listener. */
  private final EditTableMouseListener tableMouseListener;

  /**
   * Ctor.
   * @param gameData game data for the table
   * @param editEnabled true if the editing is enabled.
   * @param editTableMode current view mode
   * @param scrollToTopFn to run for scrolling to the top of the scrollable table area
   * @param enableSaveFn to run to enable the Save button
   */
  public EditTable(@NotNull GameData gameData, boolean editEnabled, EditTableMode editTableMode,
                   Runnable scrollToTopFn, Runnable enableSaveFn) {
    this.gameData = gameData;
    this.editTableMode = editTableMode;
    this.scrollToTopFn = scrollToTopFn;
    this.enableSaveFn = enableSaveFn;

    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    this.header = new EditHeader(gameData.getCategories());
    this.add(this.header);

    this.tableMouseListener = new EditTableMouseListener(editEnabled);

    this.rows = new ArrayList<>();
    for (int ind = 0; ind < gameData.getCategoryQuestionsCount(); ind++) {
      EditRow row = new EditRow(ind, this);
      this.rows.add(row);
      this.add(row);
    }

    this.activateViewStyle();
  }

  /**
   * Gets the game name.
   * @return game data game name
   */
  public String getGameName() {
    return this.gameData.getGameName();
  }

  /**
   * Sets the view/print mode (questions, answers, all).
   * @param editTableMode view mode to set
   */
  public void setViewMode(EditTableMode editTableMode) {
    this.editTableMode = editTableMode;
    this.refreshAndResize();
    this.repaint();
    SwingUtilities.invokeLater(EditTable.this.scrollToTopFn);
  }

  /**
   * Sets the print page header (note that {0} sequences will be replaced with page numbers).
   * @param headerText header text or null if no header should be printed
   */
  public void setPrintHeader(String headerText) {
    if (headerText == null) {
      this.printHeader = null;
    } else {
      this.printHeader = new MessageFormat(headerText);
    }
  }

  /**
   * Sets the print page footer (note that {0} sequences will be replaced with page numbers).
   * @param footerText footer text or null if no footer should be printed
   */
  public void setPrintFooter(String footerText) {
    if (footerText == null) {
      this.printFooter = null;
    } else {
      this.printFooter = new MessageFormat(footerText);
    }
  }

  /**
   * Refreshes and resizes table - row height are computed based on the
   * content of each cell (row gets the height of the tallest cell in that row).
   * Column widths are resized to fill in the available table width.
   */
  public void refreshAndResize() {
    final int columnsCount = this.gameData.getCategoriesCount();
    final int rowsCount = this.gameData.getCategoryQuestionsCount();

    // We assume here that EditTable is nested in a JScrollPane.
    JViewport viewport = (JViewport) this.getParent();
    JScrollBar scrollBar = (JScrollBar) this.getParent().getParent().getComponent(1);
    final int availableWidth = viewport.getWidth() - Math.max(scrollBar.getWidth(), 10);
    final int columnWidth = (int) (availableWidth / (double) columnsCount);

    // Update the table header row.
    int headerHeight = this.header.refreshAndResize(columnWidth, availableWidth);

    // Determine and update row heights, also update the width of all cells to the column width.
    int totalTableHeight = headerHeight;
    for (EditRow row : this.rows) {
      totalTableHeight += row.refreshAndResize(columnWidth, availableWidth);
    }

    // Increase the height of rows to fill in the table if the computed height is not large enough.
    int viewportHeight = viewport.getHeight(); // scrollable viewport height
    if (totalTableHeight <= viewportHeight) {
      // This is also the (only) case when there is no scrollbar.
      double rowHeight = (viewportHeight - headerHeight) / (double) rowsCount;
      for (EditRow row : this.rows) {
        row.setRowSize(availableWidth, (int) rowHeight);
      }
    }
    this.repaint();
  }

  /** Prints the table. */
  @Override
  public int print(Graphics graphics, @NotNull PageFormat pageFormat, int pageIndex) throws PrinterException {
    Dimension panelSize = this.getSize();
    final int printHeight = (int) pageFormat.getImageableHeight();
    final int printWidth = (int) pageFormat.getImageableWidth();
    if (printWidth <= 0) {
      throw new PrinterException("Width of printable area is too small.");
    }

    double scaleFactor = printWidth / (double) panelSize.width;
    Object[] messageArgs = new Object[] {pageIndex + 1};
    // Create a copy of the graphics so we don't affect the one given to us.
    Graphics2D g2d = (Graphics2D) graphics.create();
    if (pageIndex >= this.computeNumberOfPrintPages(g2d, messageArgs, printHeight, scaleFactor)) {
      return NO_SUCH_PAGE;
    }

    // The amount of vertical space available for printing the table.
    int availableHeight = printHeight;

    // Move the drawing pivot to the start of imageable (printable) area.
    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

    // First, print the footer if specified, which is printed at the bottom
    // of the imageable area w/o any affect on the current g2d context.
    availableHeight -= PrintUtilities.printFooter(g2d, this.printFooter, messageArgs, pageFormat);

    // Next, print the header if specified, which is printed at the top of the
    // imageable area and the g2d context is translated to the next printing position.
    availableHeight -= PrintUtilities.printHeader(g2d, this.printHeader, messageArgs, pageFormat);

    // Draw a line at the top of the table printed area for more emphasis.
    printEmphasisLine(printWidth, g2d);

    // Now, print the rows, selecting only the rows for the current page.
    AffineTransform oldTrans = g2d.getTransform();
    g2d.scale(scaleFactor, scaleFactor); // Resize the table panel to fit the imageable area.
    this.activatePrintStyle();

    this.header.print(g2d);
    availableHeight -= this.header.getHeaderHeight();
    g2d.translate(0, this.header.getHeaderHeight());

    int currPageIndex = 0;
    int currAvailableHeight = availableHeight;
    for (EditRow row : this.rows) {
      int rowHeight = (int) (row.getRowHeight() * scaleFactor);
      if (currAvailableHeight - rowHeight < 0) {
        currAvailableHeight = availableHeight;
        currPageIndex++;
      }
      if (currPageIndex == pageIndex) {
        row.print(g2d);
        g2d.translate(0, row.getRowHeight());
      }
      currAvailableHeight -= rowHeight;
    }
    // Draw a line at the top of the table printed area for more emphasis.
    g2d.translate(0, 1);
    printEmphasisLine((int) (printWidth / scaleFactor), g2d);

    g2d.setTransform(oldTrans);

    this.activateViewStyle();
    g2d.dispose();

    // Tell the caller that this page is part of the printed document.
    return PAGE_EXISTS;
  }

  /**
   * Sets the editing enabled mode on the edit table.
   * @param editEnabled true if editing is enabled; false if disabled
   */
  public void setEditEnabled(boolean editEnabled) {
    this.tableMouseListener.setEditEnabled(editEnabled);
  }

  /**
   * Gets the current view mode (answers only, question and answers, etc.).
   * @return the current view mode
   */
  protected EditTableMode getEditTableMode() {
    return this.editTableMode;
  }

  /**
   * Gets the game data we are editing.
   * @return reference to the game data
   */
  protected GameData getGameData() {
    return this.gameData;
  }

  /**
   * Gets the game bundle path.
   * @return path to the game bundle or null if the game is not in a bundle
   */
  protected String getGameBundlePath() {
    return this.gameData.getBundlePath();
  }

  /**
   * Gets the table mouse listener.
   * @return mouse listener to attach to handle click and hover events
   */
  protected EditTableMouseListener getTableMouseListener() {
    return this.tableMouseListener;
  }

  /**
   * Updates the data changed bit (the new value is OR-ed to the existing value).
   * @param isDataChanged true if the current table data has been updated
   */
  protected void updateDataChanged(boolean isDataChanged) {
    if (isDataChanged) {
      this.enableSaveFn.run();
    }
  }

  /** Activates the table's view style/presentation. */
  private void activateViewStyle() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.setBackground(lafTheme.getGameTableBorderColor());
    this.header.activateViewStyle();
    for (EditRow row : this.rows) {
      row.activateViewStyle();
    }
  }

  /** Activates the table's print style/presentation. */
  private void activatePrintStyle() {
    this.setBackground(Color.WHITE);
    this.header.activatePrintStyle();
    for (EditRow row : this.rows) {
      row.activatePrintStyle();
    }
  }

  /**
   * Computes the number of pages this table to print into.
   * @param g2d graphic context
   * @param messageArgs message arguments for the header and footer text
   * @param printHeight print header height
   * @param scaleFactor scale factor for the table content
   * @return the number of printable pages
   */
  private int computeNumberOfPrintPages(Graphics2D g2d, Object[] messageArgs, int printHeight, double scaleFactor) {
    int headerHeight = PrintUtilities.computeHeaderHeight(g2d, this.printHeader, messageArgs);
    int footerHeight = PrintUtilities.computeFooterHeight(g2d, this.printFooter, messageArgs);
    int pageCount = 1;
    int availableHeight = printHeight - headerHeight - footerHeight - EMPHASIS_LINE_HEIGHT;

    // Compute the size of the game categories row, which is printed on every page.
    availableHeight -= this.header.getHeaderHeight();

    // Compute the size of the game question rows.
    for (EditRow row : this.rows) {
      int rowHeight = (int) (row.getRowHeight() * scaleFactor);
      if (availableHeight - rowHeight < 0) {
        availableHeight = printHeight - headerHeight - footerHeight;
        pageCount++;
      }
      availableHeight -= rowHeight;
    }
    return pageCount;
  }

  /**
   * Prints the emphasis line.
   * @param pageWidth page width
   * @param g2d graphic context
   */
  private static void printEmphasisLine(int pageWidth, Graphics2D g2d) {
    JPanel line = new JPanel();
    line.setSize(new Dimension(pageWidth, EMPHASIS_LINE_HEIGHT));
    line.setBackground(Color.BLACK);
    line.print(g2d);
    g2d.translate(0, EMPHASIS_LINE_HEIGHT);
  }
}
