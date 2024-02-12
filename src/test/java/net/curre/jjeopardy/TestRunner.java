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

package net.curre.jjeopardy;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.laf.LafService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRunner {


  public static void main(final String[] args) {
    LafService.getInstance().initialize();
    Settings settings = SettingsService.getSettings();
    LafService.getInstance().activateLafTheme(settings.getLafThemeId());

    testDialog();
  }

  private static void testDialog() {
    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    final int CONTENT_PANE_WIDTH = 300;

    JDialog dialog = new JDialog();
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(null);
//    dialog.setResizable(false);
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
      {15, TableLayout.PREFERRED, 15, CONTENT_PANE_WIDTH, 15}, // columns
      {15, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 15}
    }));

    JLabel icon = new JLabel(ImageEnum.INFO_64.toImageIcon());
    contentPane.add(icon, new TableLayoutConstraints(
      1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));

    JLabel header = new JLabel();
    header.setFont(lafTheme.getDialogHeaderFont());
    header.setText(LocaleService.getString("jj.landing.menu.about.title"));
    contentPane.add(header, new TableLayoutConstraints(
      3, 1, 3, 1, TableLayout.CENTER, TableLayout.CENTER));

    String message = LocaleService.getString("jj.landing.menu.about.message");
    JTextArea content = createTextArea(CONTENT_PANE_WIDTH, message, dialog);
    contentPane.add(content, new TableLayoutConstraints(
      3, 3, 3, 3, TableLayout.CENTER, TableLayout.CENTER));

    JButton button = new JButton("Hit me");
    button.setFont(lafTheme.getButtonFont());
    contentPane.add(button, new TableLayoutConstraints(
      3, 5, 3, 5, TableLayout.CENTER, TableLayout.CENTER));

    dialog.pack();
    dialog.setVisible(true);
  }

  private static int countNewLineChars(String text) {
    Matcher m = Pattern.compile("\r\n|\r|\n").matcher(text);
    int count = 0;
    while (m.find()) {
      count++;
    }
    return count;
  }


  /**
   * Creates a text area component to use for displaying
   * multi-line text in the dialogs.
   * @return created and initialized JTextArea component
   */
  public static JTextArea createTextArea(int width, String message, JDialog dialog) {
    JTextArea textArea = new JTextArea();

    LafTheme lafTheme = LafService.getInstance().getCurrentLafTheme();
    final Font font = lafTheme.getDialogTextFont();
    final Color backgroundColor = lafTheme.getDefaultBackgroundColor();

    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setDragEnabled(false);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setFont(font);
    textArea.setBackground(backgroundColor);
    textArea.setOpaque(true);
    textArea.setText(message);

    // Determine the approximate minimum height of the text pane.
    int stringWidth = dialog.getFontMetrics(font).stringWidth(message);
    int newLineChars = countNewLineChars(message);
    int lineCount = (stringWidth / 300) + newLineChars + (/* add a few more */ 2);
    int textAreaHeight = lineCount * dialog.getFontMetrics(font).getHeight();
    textArea.setPreferredSize(new Dimension(width, textAreaHeight));

    return textArea;
  }
}
