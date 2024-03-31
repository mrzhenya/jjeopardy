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
import net.curre.jjeopardy.util.PrintUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Represents a print settings UI displayed on the game table.
 *
 * @author Yevgeny Nyden
 */
public class PrintSettingPanel extends JPanel {

  /** Reference to the game table data. */
  private final EditTable gameTable;

  /** Print mode for the game data. */
  private EditTableMode printMode;

  /** Include header option selection box. */
  private JCheckBox headerBox;

  /** Include footer option selection box. */
  private JCheckBox footerBox;

  /** Header text value text field. */
  private JTextField headerField;

  /** Footer text value text field. */
  private JTextField footerField;

  /** Print option for answers only. */
  private JRadioButton radioOptionAnswers;

  /** Print option for all (questions and answers). */
  private JRadioButton radioOptionAll;

  /** Print option for questions only. */
  private JRadioButton radioOptionQuestions;

  /** Print action button. */
  private JButton printButton;

  /**
   * Ctor.
   * @param gameTable game table data that's being printed
   * @param printMode print mode (answers only, all, etc.)
   */
  public PrintSettingPanel(EditTable gameTable, EditTableMode printMode) {
    this.gameTable = gameTable;
    this.printMode = printMode;

    // Initialize fields.
    initializeHeaderFields();
    initializeFooterFields();
    initializePrintButton();
    initializeViewModeGroup();

    // Construct the main UI after all fields are initialized.
    initialize();
  }

  /** Initializes the header printing options components. */
  private void initializeHeaderFields() {
    this.headerBox = new JCheckBox(LocaleService.getString("jj.print.header.option.title"), true);
    this.headerBox.addActionListener(ae -> this.headerField.setEnabled(this.headerBox.isSelected()));
    this.headerBox.setToolTipText(LocaleService.getString("jj.print.header.option.tooltip"));
    this.headerField = new JTextField(this.gameTable.getGameName());
    this.headerField.setToolTipText(LocaleService.getString("jj.print.header.field.tooltip"));
  }

  /** Initializes the footer printing options components. */
  private void initializeFooterFields() {
    this.footerBox = new JCheckBox(LocaleService.getString("jj.print.footer.option.title"), true);
    this.footerBox.addActionListener(ae -> this.footerField.setEnabled(this.footerBox.isSelected()));
    this.footerBox.setToolTipText(LocaleService.getString("jj.print.footer.option.tooltip"));
    this.footerField = new JTextField(LocaleService.getString("jj.print.footer.field"));
    this.footerField.setToolTipText(LocaleService.getString("jj.print.footer.field.tooltip"));
  }

  /**
   * Initializes the editing/printing mode options components that determine what
   * question information to view/print - answers only, questions and answers, etc.
   */
  private void initializeViewModeGroup() {
    ButtonGroup group = new ButtonGroup();
    PrintOptionActionListener actionListener = new PrintOptionActionListener();

    this.radioOptionAnswers = new JRadioButton();
    this.radioOptionAnswers.setText(EditTableMode.ANSWERS.getMessage());
    this.radioOptionAnswers.setToolTipText(EditTableMode.ANSWERS.getTooltip());
    this.radioOptionAnswers.addActionListener(actionListener);
    group.add(this.radioOptionAnswers);

    this.radioOptionAll = new JRadioButton();
    this.radioOptionAll.setText(EditTableMode.ALL.getMessage());
    this.radioOptionAll.setToolTipText(EditTableMode.ALL.getTooltip());
    this.radioOptionAll.addActionListener(actionListener);
    group.add(this.radioOptionAll);

    this.radioOptionQuestions = new JRadioButton();
    this.radioOptionQuestions.setText(EditTableMode.QUESTIONS.getMessage());
    this.radioOptionQuestions.setToolTipText(EditTableMode.QUESTIONS.getTooltip());
    this.radioOptionQuestions.addActionListener(actionListener);
    group.add(this.radioOptionQuestions);

    switch (this.printMode) {
      case ANSWERS:
        this.radioOptionAnswers.setSelected(true);
        break;
      case QUESTIONS:
        this.radioOptionQuestions.setSelected(true);
        break;
      default:
        this.radioOptionAll.setSelected(true);
    }
  }

  /** Initializes the print button component. */
  private void initializePrintButton() {
    this.printButton = new JButton(LocaleService.getString("jj.print.button.message"));
    this.printButton.setToolTipText(LocaleService.getString("jj.print.button.tooltip"));
    this.printButton.addActionListener(ae -> {
      PrintSettingPanel thisRef = PrintSettingPanel.this;
      thisRef.gameTable.setPrintHeader(thisRef.headerBox.isSelected() ? thisRef.headerField.getText().trim() : null);
      thisRef.gameTable.setPrintFooter(thisRef.footerBox.isSelected() ? thisRef.footerField.getText().trim() : null);
      PrintUtilities.printEditTable(thisRef.gameTable);
    });
  }

  /** Initializes the main layout and its components for this printing settings panel. */
  private void initialize() {
    this.setBorder(BorderFactory.createTitledBorder(LocaleService.getString("jj.print.setting.message")));
    GroupLayout panelLayout = new GroupLayout(this);
    this.setLayout(panelLayout);
    panelLayout.setHorizontalGroup(
        panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(headerBox)
                    .addComponent(footerBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(footerField)
                    .addComponent(headerField, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(this.radioOptionAll)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(printButton))
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(this.radioOptionAnswers)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(this.radioOptionQuestions))
                )
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    panelLayout.setVerticalGroup(
        panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(headerBox)
                    .addComponent(headerField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(this.radioOptionQuestions)
                    .addComponent(this.radioOptionAnswers)
                )
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(footerBox)
                    .addComponent(footerField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(this.radioOptionAll)
                    .addComponent(printButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }

  /**
   * Action listener to handle view/print options change (whether to print answers only
   * or questions with answers, etc.).
   */
  private class PrintOptionActionListener implements ActionListener {

    /** @inheritDoc */
    @Override
    public void actionPerformed(ActionEvent e) {
      EditTableMode newMode;
      if (PrintSettingPanel.this.radioOptionAnswers.isSelected()) {
        newMode = EditTableMode.ANSWERS;
      } else if (PrintSettingPanel.this.radioOptionQuestions.isSelected()) {
        newMode = EditTableMode.QUESTIONS;
      } else {
        newMode = EditTableMode.ALL;
      }
      if (newMode != PrintSettingPanel.this.printMode) {
        PrintSettingPanel.this.printMode = newMode;
        PrintSettingPanel.this.gameTable.setViewMode(newMode);
      }
    }
  }
}
