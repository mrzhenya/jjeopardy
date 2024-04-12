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

import net.curre.jjeopardy.service.LocaleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;
import java.awt.Font;

/**
 * Represents an edit settings UI displayed on the edit game table.
 *
 * @author Yevgeny Nyden
 */
public class EditSettingPanel extends JPanel {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(EditSettingPanel.class.getName());

  /** Reference to the edit game window. */
  private final EditGameWindow editGameWindow;

  /** Checkbox to enable or disable table editing. */
  private JCheckBox enableEditBox;

  /** Checkbox to add or edit players for the game. */
  private JCheckBox editPlayersBox;

  /** Checkbox to edit game name, description, and other additional information. */
  private JCheckBox editGameInfoBox;

  /** Save changes action button. */
  private JButton saveButton;

  /**
   * Ctor.
   * @param editGameWindow reference to the edit game window
   * @param editEnabled true if editing is enable; false if otherwise
   */
  public EditSettingPanel(EditGameWindow editGameWindow, boolean editEnabled) {
    this.editGameWindow = editGameWindow;

    this.initializeCheckboxes(editEnabled);
    this.initializeSaveButton();
    this.initialize();
  }

  /**
   * Enables the Save button.
   */
  protected void enableSaveButton() {
    this.saveButton.setEnabled(true);
  }

  /**
   * Initializes the checkbox action elements of the edit settings dialog.
   * @param editEnabled true if editing is enable; false if otherwise
   */
  private void initializeCheckboxes(boolean editEnabled) {
    // Checkbox to change the editing mode of the edit table.
    this.enableEditBox = new JCheckBox();
    this.enableEditBox.setText(LocaleService.getString("jj.edit.setting.edit.message"));
    this.enableEditBox.setToolTipText(LocaleService.getString("jj.edit.setting.edit.tooltip"));
    this.enableEditBox.setSelected(editEnabled);
    this.enableEditBox.addActionListener(e -> EditSettingPanel.this.editGameWindow.setEditEnabled(
        EditSettingPanel.this.enableEditBox.isSelected()));

    // Checkbox to edit additional game information such as the game name or description.
    this.editGameInfoBox = new JCheckBox();
    this.editGameInfoBox.setText(LocaleService.getString("jj.edit.settings.extra.message"));
    this.editGameInfoBox.setToolTipText(LocaleService.getString("jj.edit.settings.extra.tooltip"));
    this.editGameInfoBox.setEnabled(false);
    this.editGameInfoBox.addActionListener(e -> {
      // TODO: implement editing additional game information.
      EditSettingPanel.this.editGameInfoBox.isSelected();
    });

    // Checkbox to edit game players.
    this.editPlayersBox = new JCheckBox();
    this.editPlayersBox.setText(LocaleService.getString("jj.edit.settings.players.message"));
    this.editPlayersBox.setToolTipText(LocaleService.getString("jj.edit.settings.players.tooltip"));
    this.editPlayersBox.setEnabled(false);
    this.editPlayersBox.addActionListener(e -> {
      // TODO: implement editing player information.
      EditSettingPanel.this.editPlayersBox.isSelected();
    });
  }

  /** Initializes the Save game button component. */
  private void initializeSaveButton() {
    this.saveButton = new JButton(LocaleService.getString("jj.edit.button.save.message"));
    this.saveButton.setToolTipText(LocaleService.getString("jj.edit.button.save.tooltip"));
    this.saveButton.setEnabled(false);
    this.saveButton.addActionListener(ae -> {
      logger.info("Saving game changes...");
      this.saveButton.setEnabled(false);
      this.editGameWindow.saveGameData();
    });
  }

  /** Initializes the main layout and its components for this edit settings panel. */
  private void initialize() {
    TitledBorder titledBorder = BorderFactory.createTitledBorder(LocaleService.getString("jj.edit.setting.message"));
    Font titleFont = titledBorder.getTitleFont();
    titledBorder.setTitleFont(titleFont.deriveFont(Font.BOLD, titleFont.getSize()));
    this.setBorder(titledBorder);

    GroupLayout panelLayout = new GroupLayout(this);
    this.setLayout(panelLayout);
    panelLayout.setHorizontalGroup(
        panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(this.editGameInfoBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(this.saveButton))
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(this.enableEditBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(this.editPlayersBox))
                )
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    panelLayout.setVerticalGroup(
        panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.editPlayersBox)
                    .addComponent(this.enableEditBox)
                )
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.editGameInfoBox)
                    .addComponent(this.saveButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }
}
