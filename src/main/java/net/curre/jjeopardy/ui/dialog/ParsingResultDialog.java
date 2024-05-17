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

package net.curre.jjeopardy.ui.dialog;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.FileParsingResult;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.validation.constraints.NotNull;
import java.awt.Component;
import java.awt.Font;

/**
 * Dialog to display file parsing results.
 *
 * @author Yevgeny Nyden
 */
public class ParsingResultDialog extends BasicDialog {

  /** Game file parsing result. */
  private final FileParsingResult result;

  /**
   * Creates an instance of a dialog to show results of parsing a game file.
   * @param result file parsing result
   */
  public ParsingResultDialog(@NotNull FileParsingResult result) {
    this.result = result;
    ImageEnum icon = result.getGameData().isGameDataUsable() ? ImageEnum.SUCCESS_64 : ImageEnum.FAILURE_64;
    this.initializeDialog(result.getResulTitleShort(), icon);
  }

  /** @inheritDoc */
  @Override
  public Component getHeaderComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    JPanel headerPanel = new JPanel(new TableLayout(new double[][] {
      {TableLayout.PREFERRED}, {TableLayout.PREFERRED, TableLayout.PREFERRED}}));
    final JLabel label1 = new JLabel(this.result.getResulTitleLong());
    label1.setFont(lafTheme.getDialogHeaderFont());
    headerPanel.add(label1, new TableLayoutConstraints(
      0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));
    final JLabel label2 = new JLabel(this.result.getFileOrBundlePath());
    label2.setFont(lafTheme.getDialogHeaderFont());
    headerPanel.add(label2, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.CENTER, TableLayout.CENTER));
    return headerPanel;
  }

  /** @inheritDoc */
  @Override
  public Component getContentComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    Font textFont = lafTheme.getDialogTextFont();
    for (String msg : this.result.getErrorMessages()) {
      contentPane.add(this.createLabelWithIcon(msg, textFont, ImageEnum.ERROR_24));
    }
    for (String msg : this.result.getWarningMessages()) {
      contentPane.add(this.createLabelWithIcon(msg, textFont, ImageEnum.WARN_24));
    }
    for (String msg : this.result.getInfoMessages()) {
      contentPane.add(this.createLabelWithIcon(msg, textFont, ImageEnum.INFO_24));
    }
    contentPane.add(new JLabel("\n\n")); // add a couple extra new lines
    return contentPane;
  }

  /**
   * Gets the button panel.
   * @return panel with buttons for this dialog
   */
  @Override
  public Component getButtonComponent() {
    Registry registry = AppRegistry.getInstance();
    LafTheme lafTheme = registry.getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    JPanel panel = new JPanel(new TableLayout(new double[][] {
        {TableLayout.PREFERRED, padding, TableLayout.PREFERRED}, // columns
        {TableLayout.PREFERRED}})); // rows

    JButton defaultButton = createDefaultButton(null);
    panel.add(defaultButton, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));

    JButton addButton = new JButton();
    ClickAndKeyAction.createAndAddAction(addButton, this::addGameToLibrary);
    addButton.setText(LocaleService.getString("jj.dialog.addtolibrary.button"));
    addButton.setFont(lafTheme.getButtonFont());
    panel.add(addButton, new TableLayoutConstraints(
        2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));
    if (this.result.getGameData().isGameDataUsable() &&
        !registry.getGameDataService().gameExistsInLibrary(this.result.getGameData())) {
      SwingUtilities.invokeLater(addButton::requestFocus);
    } else {
      addButton.setEnabled(false);
    }
    return panel;
  }

  /**
   * Adds the game to the game Library.
   */
  private void addGameToLibrary() {
    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().copyAndAddGameToLibrary(this.result.getGameData());
    registry.getLandingUi().updateLibrary(null);
    this.setVisible(false);
    this.dispose();
  }

  /**
   * Creates a label for a parsing result message.
   * @param text message text
   * @param font font to use
   * @param icon icon to show next to the label
   * @return created label component
   */
  private @NotNull JLabel createLabelWithIcon(String text, Font font, @NotNull ImageEnum icon) {
    JLabel label = new JLabel(icon.toImageIcon());
    label.setFont(font);
    label.setText(text);
    return label;
  }
}
