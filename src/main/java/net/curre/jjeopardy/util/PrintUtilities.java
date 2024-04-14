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

package net.curre.jjeopardy.util;

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.edit.EditTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;

/**
 * Printing utilities mostly for printing <code>EditTable</code>.
 *
 * @author Yevgeny Nyden
 */
public class PrintUtilities {

  /** Print page header font. */
  public static final Font PAGE_HEADER_FONT = new Font("Lucida Sans", Font.BOLD, 18);

  /** Print page footer font. */
  public static final Font PAGE_FOOTER_FONT = new Font("Lucida Sans", Font.PLAIN, 10);

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(PrintUtilities.class.getName());

  /** Vertical space to leave between the table and the header/footer text. */
  private static final int PRINT_Y_SPACING = 4;

  /**
   * Prints the edit table.
   * @param table game table to print
   */
  public static void printEditTable(EditTable table) {
    String title = LocaleService.getString("jj.printdialog.title");

    PrinterJob job = PrinterJob.getPrinterJob();

    // First, show the page format dialog to determine page format.
    PageFormat pageFormat = job.pageDialog(job.defaultPage());

    job.setPrintable(table, pageFormat);
    boolean proceed = job.printDialog();
    if (proceed) {
      try {
        job.print();
      } catch (PrinterException e) {
        // Printing failed.
        table.activateViewStyle();
        logger.warn("Printing failed", e);
        String reason = e.getMessage() == null ? "Unknown" : e.getMessage();
        AppRegistry.getInstance().getUiService().showErrorDialog(
            title, LocaleService.getString("jj.printdialog.failure.message", reason), null);
      }
    }
  }

  /**
   * Helper method to determine the print page header height.
   * @param g2d graphics context where header is printed, non null
   * @param message header message to print or null if there is no header
   * @param messageArgs header message arguments (to replace {0} sequences), non null
   * @return the allocated vertical space for the header (including the padding)
   */
  public static int computeHeaderHeight(Graphics2D g2d, MessageFormat message, Object[] messageArgs) {
    if (message == null) {
      return 0;
    }
    String headerText = message.format(messageArgs);
    g2d.setFont(PAGE_FOOTER_FONT);
    Rectangle2D hRect = g2d.getFontMetrics().getStringBounds(headerText, g2d);
    return (int) Math.ceil(hRect.getHeight()) + PRINT_Y_SPACING;
  }

  /**
   * Helper method to print the page header. Note that graphics context is
   * affected as a result of calling this method (translates at the point below the header).
   * @param g2d graphics context where header is printed, non null
   * @param message header message to print or null if there is no header
   * @param messageArgs header message arguments (to replace {0} sequences), non null
   * @param pageFormat page format to use, non null
   * @return the allocated vertical space for the header (including the padding)
   */
  public static int printHeader(Graphics2D g2d, MessageFormat message, Object[] messageArgs, PageFormat pageFormat) {
    if (message == null) {
      return 0;
    }
    final int printWidth = (int) pageFormat.getImageableWidth();
    String headerText = message.format(messageArgs);
    g2d.setFont(PAGE_HEADER_FONT);
    Rectangle2D hRect = g2d.getFontMetrics().getStringBounds(headerText, g2d);
    int headerHeight = computeHeaderHeight(g2d, message, messageArgs);

    // Print the header at the top of the imageable area and then translate downwards.
    printText(g2d, headerText, hRect, PAGE_HEADER_FONT, printWidth);

    g2d.translate(0, pageFormat.getImageableY() + headerHeight);
    return headerHeight;
  }

  /**
   * Helper method to determine the print page footer height.
   * @param g2d graphics context where footer is printed, non null
   * @param message footer message to print or null if there is no footer
   * @param messageArgs footer message arguments (to replace {0} sequences), non null
   * @return the allocated vertical space for the footer
   */
  public static int computeFooterHeight(Graphics2D g2d, MessageFormat message, Object[] messageArgs) {
    if (message == null) {
      return 0;
    }
    String footerText = message.format(messageArgs);
    g2d.setFont(PAGE_FOOTER_FONT);
    Rectangle2D fRect = g2d.getFontMetrics().getStringBounds(footerText, g2d);
    return (int) Math.ceil(fRect.getHeight());
  }

  /**
   * Helper method to print the page footer. Note that graphics context is not
   * affected as a result of calling this method.
   * @param g2d graphics context where footer is printed, non null
   * @param message footer message to print, non null
   * @param messageArgs footer message arguments (to replace {0} sequences), non null
   * @param pageFormat page format to use, non null
   * @return the allocated vertical space for the footer (including the padding)
   */
  public static int printFooter(Graphics2D g2d, MessageFormat message, Object[] messageArgs, PageFormat pageFormat) {
    if (message == null) {
      return 0;
    }
    AffineTransform oldTrans = g2d.getTransform();
    final int printWidth = (int) pageFormat.getImageableWidth();
    final int printHeight = (int) pageFormat.getImageableHeight();

    // if there's footer text, print it at the bottom of the imageable area
    // fetch the formatted footer text, if any
    String footerText = message.format(messageArgs);
    g2d.setFont(PAGE_FOOTER_FONT);
    Rectangle2D fRect = g2d.getFontMetrics().getStringBounds(footerText, g2d);
    int footerHeight = computeFooterHeight(g2d, message, messageArgs);

    // Print the footer at the very bottom of the page.
    g2d.translate(0, printHeight - footerHeight);
    printText(g2d, footerText, fRect, PAGE_FOOTER_FONT, printWidth);

    // Restore the previous transforms.
    g2d.setTransform(oldTrans);

    return footerHeight;
  }

  /**
   * Helps to render the header and footer text (copy of <code>TablePrintable</code>).
   * @param g2d the graphics to draw into
   * @param text the text to draw, non null
   * @param rect the bounding rectangle for this text, as calculated at the given font, non null
   * @param font the font to draw the text in, non null
   * @param imgWidth the width of the area to draw into
   */
  private static void printText(Graphics2D g2d, String text, @NotNull Rectangle2D rect, Font font, int imgWidth) {
    // If the text is small enough to fit, center it.
    int tx = 0;
    if (rect.getWidth() < imgWidth) {
      tx = (int) ((imgWidth - rect.getWidth()) / 2);
    }
    int ty = (int) Math.ceil(Math.abs(rect.getY()));
    g2d.setColor(Color.BLACK);
    g2d.setFont(font);
    g2d.drawString(text, tx, ty);
  }
}
