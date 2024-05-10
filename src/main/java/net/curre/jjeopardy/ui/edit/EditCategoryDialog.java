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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.event.TabKeyListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.dialog.EditBaseDialog;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.Font;

/**
 * A dialog to edit the category name. This dialog is not designed
 * to be reused (a new instance should be created each time it is needed).
 *
 * @author Yevgeny Nyden
 */
public class EditCategoryDialog extends EditBaseDialog {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(EditCategoryDialog.class.getName());

  /** Reference to the header cell corresponding to this category. */
  private final EditHeaderCell editHeaderCell;

  /** Header label for the input text field. */
  private final JLabel headerLabel;

  /** Game name text input pane. */
  private final JTextPane categoryNamePane;

  /**
   * Ctor.
   * @param editHeaderCell reference to the header cell containing this category
   */
  public EditCategoryDialog(EditHeaderCell editHeaderCell) {
    this.editHeaderCell = editHeaderCell;

    this.categoryNamePane = new JTextPane();
    this.categoryNamePane.setText(editHeaderCell.getCategoryName());
    this.categoryNamePane.addKeyListener(new TabKeyListener());
    this.headerLabel = new JLabel();
    this.headerLabel.setText(
        LocaleService.getString("jj.editinfo.category.message",
            String.valueOf(editHeaderCell.getColumnIndex() + 1)));

    this.initializeDialog(LocaleService.getString("jj.editinfo.category.title"),
        JjDefaults.EDIT_INFO_DIALOG_MIN_WIDTH, JjDefaults.EDIT_INFO_DIALOG_MIN_HEIGHT / 2);
  }

  /** @inheritDoc */
  @Override
  protected void handleCancelAction() {
    logger.info("Cancelling the changes and disposing the dialog.");
    this.setVisible(false);
    this.dispose();
  }

  /** @inheritDoc */
  @Override
  protected void handleOkAction() {
    logger.info("Saving the changes and closing the dialog.");

    String nameText = StringUtils.trimToNull(this.categoryNamePane.getText());
    // If there are errors, stop here.
    if (nameText == null) {
      this.showNameErrorDialog();
      return;
    }

    this.editHeaderCell.updateCategoryName(nameText);

    this.setVisible(false);
    this.dispose();
  }

  /** @inheritDoc */
  @Override
  protected JPanel getDialogBodyComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    Font labelFont = lafTheme.getButtonFont();
    JPanel bodyPanel = new JPanel();
    bodyPanel.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {10, TableLayout.PREFERRED, 3, TableLayout.PREFERRED}})); // rows

    this.headerLabel.setFont(labelFont);
    bodyPanel.add(this.headerLabel, new TableLayoutConstraints(
        0, 1, 0, 1, TableLayout.FULL, TableLayout.CENTER));
    bodyPanel.add(this.categoryNamePane, new TableLayoutConstraints(
        0, 3, 0, 3, TableLayout.FULL, TableLayout.CENTER));

    return bodyPanel;
  }

  /**
   * Shows the name error dialog to the user.
   */
  private void showNameErrorDialog() {
    super.showErrorDialog(
        LocaleService.getString("jj.editinfo.error.category.title"),
        LocaleService.getString("jj.editinfo.error.category.message"));
  }
}
