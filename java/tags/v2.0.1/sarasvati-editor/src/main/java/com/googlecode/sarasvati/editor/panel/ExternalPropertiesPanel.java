/*
    This file is part of Sarasvati.

    Sarasvati is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Sarasvati is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Sarasvati.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009 Paul Lorenz
*/

package com.googlecode.sarasvati.editor.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.googlecode.sarasvati.editor.command.CommandStack;
import com.googlecode.sarasvati.editor.model.EditorExternal;
import com.googlecode.sarasvati.editor.model.ExternalState;
import com.googlecode.sarasvati.editor.model.Library;

/**
 * @author Paul Lorenz
 */
@SuppressWarnings({"synthetic-access", "rawtypes", "unchecked"})
public class ExternalPropertiesPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    /** Creates new form ExternalPropertiesPanel */
    public ExternalPropertiesPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        processDefinitionInput = new javax.swing.JComboBox();
        nameInput = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        processDefinitionLabel = new javax.swing.JLabel();
        customPropertiesLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        addPropertyButton = new javax.swing.JButton();
        deletePropertyButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        customPropertiesInput = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();

        processDefinitionInput.setEditable(true);
        processDefinitionInput.setModel(getProcessDefinitionsModel());

        nameInput.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.nameInput.text")); // NOI18N

        nameLabel.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.nameLabel.text")); // NOI18N

        processDefinitionLabel.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.processDefinitionLabel.text")); // NOI18N

        customPropertiesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        customPropertiesLabel.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.customPropertiesLabel.text")); // NOI18N

        addPropertyButton.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.addPropertyButton.text")); // NOI18N

        deletePropertyButton.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.deletePropertyButton.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deletePropertyButton)
                    .addComponent(addPropertyButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addPropertyButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(deletePropertyButton)
                .addContainerGap())
        );

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        customPropertiesInput.setModel(getCustomNodeProperties());
        customPropertiesInput.setFillsViewportHeight(true);
        customPropertiesInput.setMinimumSize(new java.awt.Dimension(10, 10));
        customPropertiesInput.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane3.setViewportView(customPropertiesInput);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));

        cancelButton.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.cancelButton.text")); // NOI18N
        cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
        jPanel1.add(cancelButton);

        applyButton.setText(org.openide.util.NbBundle.getMessage(ExternalPropertiesPanel.class, "ExternalPropertiesPanel.applyButton.text")); // NOI18N
        applyButton.setPreferredSize(new java.awt.Dimension(100, 25));
        jPanel1.add(applyButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(nameLabel)
                            .addComponent(processDefinitionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(processDefinitionInput, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
                    .addComponent(customPropertiesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processDefinitionLabel)
                    .addComponent(processDefinitionInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(customPropertiesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPropertyButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTable customPropertiesInput;
    private javax.swing.JLabel customPropertiesLabel;
    private javax.swing.JButton deletePropertyButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField nameInput;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox processDefinitionInput;
    private javax.swing.JLabel processDefinitionLabel;
    // End of variables declaration//GEN-END:variables

    public ComboBoxModel getProcessDefinitionsModel ()
    {
      return processDefinitionsModel;
    }

    public TableModel getCustomNodeProperties ()
    {
      return tableModel;
    }

    private final DefaultTableModel tableModel = new DefaultTableModel( new String[] { "name", "value" }, 0 );
    private final DefaultComboBoxModel processDefinitionsModel = new DefaultComboBoxModel();

    public void setup (final JDialog dialog,
                       final EditorExternal external)
    {
      customPropertiesInput.putClientProperty( "terminateEditOnFocusLost", Boolean.TRUE );

      cancelButton.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed (final ActionEvent e)
        {
          dialog.setVisible( false );
          dialog.dispose();
        }
      });

      applyButton.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed (final ActionEvent e)
        {
          Map<String,String> customProperties = new LinkedHashMap<String,String> ();

          for ( int row = 0; row < tableModel.getRowCount(); row++ )
          {
            customProperties.put( (String)tableModel.getValueAt( row, 0 ), (String)tableModel.getValueAt( row, 1 ) );
          }

          ExternalState newState =
            new ExternalState( nameInput.getText(),
                              (String)processDefinitionInput.getSelectedItem(),
                              customProperties,
                              false );

          if ( !newState.equals( external.getState() ) )
          {
            CommandStack.editGraphMember( external, newState );
          }

          dialog.setVisible( false );
          dialog.dispose();
        }
      });

      for ( final String name : Library.getInstance().getNames() )
      {
        processDefinitionsModel.addElement( name );
      }

      ExternalState state = external.getState();
      nameInput.setText( state.getName() );
      processDefinitionInput.setSelectedItem( state.getGraphName() );

      if ( state.getCustomProperties() != null )
      {
        for ( Map.Entry<String,String> entry : state.getCustomProperties().entrySet() )
        {
          tableModel.addRow( new String[] { entry.getKey(), entry.getValue() } );
        }
      }

      this.customPropertiesInput.setModel( tableModel );

      addPropertyButton.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed (final ActionEvent e)
        {
          tableModel.addRow( new String[] { "", "" } );
        }
      });

      deletePropertyButton.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed (final ActionEvent e)
        {
          int [] indices = customPropertiesInput.getSelectedRows();
          Arrays.sort( indices );
          for ( int i = indices.length - 1; i >= 0; i-- )
          {
            tableModel.removeRow( indices[i] );
          }
        }
      });

      if ( state.isNew() )
      {
        nameInput.selectAll();
        nameInput.requestFocusInWindow();
      }
    }
}
