/**
Copyright (C) 2017 VONGSALAT Anousone

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.imagej;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;


public class TablePatientsMouseListener extends MouseAdapter {

	private JFrame frame;
	private JTable tableau;
	private JTable tableauStudies;
	private TableDataPatients modele;
	private TableDataStudies modeleStudies;
	private TableDataSeries modeleSeries;
	private ListSelectionModel listSelection;

	public TablePatientsMouseListener(JFrame frame, JTable tableau, TableDataPatients modele, 
			JTable tableauStudies, TableDataStudies modeleStudies, TableDataSeries modeleSeries, 
			ListSelectionModel listSelection) {
		this.frame = frame;
		this.tableau = tableau;
		this.tableauStudies = tableauStudies;
		this.modele = modele;
		this.modeleStudies = modeleStudies;
		this.modeleSeries = modeleSeries;
		this.listSelection = listSelection;
	}

	@Override
	public void mousePressed(MouseEvent event) {
		this.modeleStudies.clear();
		this.modeleSeries.clear();
		if(!event.isControlDown()){
			// selects the row at which point the mouse is clicked
			Point point = event.getPoint();
			int currentRow = tableau.rowAtPoint(point);
			tableau.setRowSelectionInterval(currentRow, currentRow);
		}
		try {
			if(this.modele.getRowCount() != 0){
				String patientName = (String)this.tableau.getValueAt(this.tableau.getSelectedRow(), 0);
				String patientID = (String)this.tableau.getValueAt(this.tableau.getSelectedRow(), 1);
				String patientUID = (String)this.tableau.getValueAt(this.tableau.getSelectedRow(), 2);
				this.modeleStudies.addStudy(patientName, patientID, patientUID);
				this.tableauStudies.setRowSelectionInterval(0,0);
			}
		}catch (Exception e1) {
			e1.printStackTrace();
		}
		frame.pack();
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (event.isControlDown() && SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 1) {
            int row = tableau.rowAtPoint(event.getPoint());
            listSelection.addSelectionInterval(row, row);
        }
	}
}
