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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import com.michaelbaranov.microba.calendar.DatePicker;

import ij.IJ;
import ij.plugin.PlugIn;


/**
 *
 * @author Anousone Vongsalat
 */

public class VueAnon extends JFrame implements PlugIn{
	private static final long serialVersionUID = 1L;
	private JTabbedPane tabbedPane;
	private JLabel state = new JLabel();

	// Tables (p1)
	private String date;
	private JTable tableauPatients;
	private JTable tableauStudies;
	private JTable tableauSeries;
	private TableDataPatients modelePatients = new TableDataPatients();
	private TableDataStudies modeleStudies = new TableDataStudies();
	private TableDataSeries modeleSeries = new TableDataSeries(state, this);
	private TableDataAnonPatients modeleAnonPatients = new TableDataAnonPatients();
	private TableDataAnonStudies modeleAnonStudies = new TableDataAnonStudies();
	private TableRowSorter<TableDataPatients> sorterPatients;
	private TableRowSorter<TableDataStudies> sorterStudies;
	private TableRowSorter<TableDataSeries> sorterSeries;

	// Orthanc toolbox (p1)
	private JTable anonPatientTable;
	private JTable anonStudiesTable;
	private JButton displayAnonTool;
	private JButton addToAnon;
	private JButton anonBtn;
	private JButton removeFromAnonList;
	private JButton exportZip = new JButton("Export list to Zip");
	private JButton removeFromZip = new JButton("Remove from list");
	private JLabel zipSize;
	private JComboBox<Object> zipShownContent;
	private ArrayList<String> zipShownContentList = new ArrayList<String>();
	private JPanel oToolRight;
	private JComboBox<Object> listeAET;
	private JPopupMenu popMenuPatients = new JPopupMenu();
	private JPopupMenu popMenuStudies = new JPopupMenu();
	private JPopupMenu popMenuSeries = new JPopupMenu();
	private ArrayList<String> zipContent = new ArrayList<String>();
	private JPanel anonTablesPanel;
	private int anonCount;
	private ArrayList<String> newIDs = new ArrayList<String>();

	// Tab Export (p2)
	private JLabel stateExports = new JLabel("");
	private JTable tableauExportStudies;
	private JTable tableauExportSeries;
	private TableDataExportStudies modeleExportStudies = new TableDataExportStudies();
	private TableDataExportSeries modeleExportSeries = new TableDataExportSeries(this, stateExports);
	private TableRowSorter<TableDataExportStudies> sorterExportStudies;
	private TableRowSorter<TableDataExportSeries> sorterExportSeries;
	private StringBuilder remoteFileName;

	// Tab Setup (p3)
	private JComboBox<Object> anonProfiles;
	private Choice bodyCharChoice;
	private Choice datesChoice;
	private Choice bdChoice;
	private Choice ptChoice;
	private Choice scChoice;
	private Choice descChoice;
	private JRadioButton[] bodyCharList = new JRadioButton[2];
	private JRadioButton[] datesList = new JRadioButton[2];
	private JRadioButton[] bdList = new JRadioButton[2];
	private JRadioButton[] ptList = new JRadioButton[2];
	private JRadioButton[] scList = new JRadioButton[2];
	private JRadioButton[] descList = new JRadioButton[2];
	private JTextField centerCode;
	private JTextField remoteServer;
	private JTextField remotePort;
	private JTextField servUsername;
	private JPasswordField servPassword;
	private JTextField remoteFilePath;
	private JComboBox<String> exportType;
	private JTextField dbAdress;
	private JTextField dbPort;
	private JTextField dbName;
	private JTextField dbUsername;
	private JPasswordField dbPassword;
	

	// Settings preferences
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/anonPlugin");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");

	public VueAnon(){

		this.setTitle("Orthanc anonymization");

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////// PANEL 1 : ANONYMIZATION ////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));

		/////////////////////////////////////////////////////////////////////////////
		////////////////////////// TOP PANEL ////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

		String[] patientParam = {"Patient name", "Patient ID", "Accession number"};
		JComboBox<String> inputType = new JComboBox<String>(patientParam);
		inputType.setSelectedIndex(jpreferPerso.getInt("InputParameter", 0));
		topPanel.add(inputType);

		JTextField userInput = new JTextField();
		userInput.setToolTipText("Set your input accordingly to the field combobox on the left. ('*' stands for any character)");
		userInput.setText("*");
		userInput.setPreferredSize(new Dimension(125,20));
		topPanel.add(userInput);

		topPanel.add(new JLabel("Study description"));
		JTextField studyDesc = new JTextField("*");
		studyDesc.setPreferredSize(new Dimension(125,20));
		topPanel.add(studyDesc);

		DatePicker from, to;
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
		try {
			d = sdf.parse("01-01-1980");
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		from = new DatePicker(d, sdf);
		from.setBorder(new EmptyBorder(0, 5, 0 ,0));
		from.setToolTipText("Date format : MM-dd-yyyy");
		to = new DatePicker(new Date(), sdf);
		to.setBorder(new EmptyBorder(0, 5, 0 ,0));
		to.setToolTipText("Date format : MM-dd-yyyy");
		topPanel.add(new JLabel("From"));
		topPanel.add(from);
		topPanel.add(new JLabel("To"));
		topPanel.add(to);

		JButton search = new JButton("Search");
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					search.setText("Searching");
					search.setEnabled(false);
					modelePatients.clear();
					modeleStudies.clear();
					modeleSeries.clear();

					DateFormat df = new SimpleDateFormat("yyyyMMdd");
					date = df.format(from.getDate().getTime())+"-"+df.format(to.getDate().getTime());

					modelePatients.addPatient(inputType.getSelectedItem().toString(), userInput.getText(), date, 
							studyDesc.getText());
					tableauPatients.setRowSelectionInterval(0,0);
					pack();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				} catch (Exception e1){
					// Ignore
				}
				finally{
					state.setText("");
					search.setEnabled(true);
					search.setText("Search");
					jpreferPerso.putInt("InputParameter", inputType.getSelectedIndex());
				}
			}
		});
		JButton queryRetrieveBtn = new JButton("Queries/Retrieve");
		queryRetrieveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				IJ.runMacro("run(\"Launch queries\");");
			}
		});

		topPanel.add(search);
		topPanel.add(queryRetrieveBtn);
		this.state.setText("");
		mainPanel.add(topPanel);

		/////////////////////////////////////////////////////////////////////////////
		////////////////////////// TABLES ///////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////

		JPanel tablesPanel = new JPanel(new GridBagLayout());
		this.tableauPatients = new JTable(modelePatients);
		this.tableauStudies = new JTable(modeleStudies);
		this.tableauSeries = new JTable(modeleSeries);
		this.sorterPatients = new TableRowSorter<TableDataPatients>(modelePatients);
		this.sorterStudies = new TableRowSorter<TableDataStudies>(modeleStudies);
		this.sorterSeries = new TableRowSorter<TableDataSeries>(modeleSeries);
		this.sorterPatients.setSortsOnUpdates(true);
		this.sorterStudies.setSortsOnUpdates(true);
		this.sorterSeries.setSortsOnUpdates(true);
		////////////////////////// PATIENTS ///////////////////////////////

		this.tableauPatients.getTableHeader().setReorderingAllowed(false);
		this.tableauPatients.getColumnModel().getColumn(0).setMinWidth(170);
		this.tableauPatients.getColumnModel().getColumn(0).setMaxWidth(170);
		this.tableauPatients.getColumnModel().getColumn(0).setResizable(false);
		this.tableauPatients.getColumnModel().getColumn(1).setMinWidth(120);
		this.tableauPatients.getColumnModel().getColumn(1).setMaxWidth(120);
		this.tableauPatients.getColumnModel().getColumn(1).setResizable(false);
		this.tableauPatients.getColumnModel().getColumn(2).setMinWidth(0);
		this.tableauPatients.getColumnModel().getColumn(2).setMaxWidth(0);
		this.tableauPatients.getColumnModel().getColumn(2).setResizable(false);
		this.tableauPatients.getColumnModel().getColumn(3).setMinWidth(0);
		this.tableauPatients.getColumnModel().getColumn(3).setMaxWidth(0);
		this.tableauPatients.getColumnModel().getColumn(3).setResizable(false);
		this.tableauPatients.setPreferredScrollableViewportSize(new Dimension(290,267));

		this.tableauPatients.setDefaultRenderer(Date.class, new DateRenderer());
		this.tableauPatients.addMouseListener(new TablePatientsMouseListener(
				this, this.tableauPatients, this.modelePatients, this.tableauStudies, this.modeleStudies, this.modeleSeries, 
				tableauPatients.getSelectionModel()));
		List<RowSorter.SortKey> sortKeysPatients = new ArrayList<>();
		sortKeysPatients.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeysPatients.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sorterPatients.setSortKeys(sortKeysPatients);
		sorterPatients.sort();
		this.tableauPatients.setRowSorter(sorterPatients);

		JMenuItem menuItemZipPatients = new JMenuItem("Add to Zip/send");
		menuItemZipPatients.addActionListener(new addZipAction(tableauPatients));
		JMenuItem menuItemAnonPatients = new JMenuItem("Add to anonymization tool");
		menuItemAnonPatients.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					modeleStudies.clear();
					String patientName = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 0).toString();
					String patientID = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 1).toString();
					String patientUID = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 2).toString();
					Date patientBirthDate = (Date)tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 3);
					ArrayList<String> listeUIDs = new ArrayList<String>();
					modeleStudies.addStudy(patientName, patientID, patientUID);
					listeUIDs.addAll(modeleStudies.getIds());
					modeleAnonPatients.addPatient(patientName, patientID, patientBirthDate, listeUIDs);
					modeleAnonStudies.clear();
					modeleAnonStudies.addStudies(patientName, patientID, listeUIDs);
					for(int i = 0; i < modeleAnonPatients.getPatientList().size(); i++){
						if(modeleAnonPatients.getPatient(i).getPatientId().equals(patientID) && 
								modeleAnonPatients.getPatient(i).getPatientName().equals(patientName)){
							anonPatientTable.setRowSelectionInterval(i, i);
						}
					}
					oToolRight.setVisible(false);
					anonTablesPanel.setVisible(true);
					addToAnon.setVisible(true);
					displayAnonTool.setText("Hide anonymization tool");
					pack();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		JMenuItem menuItemDeletePatients = new JMenuItem("Delete this patient");
		menuItemDeletePatients.addActionListener(new DeleteActionMainPanel("Patient", this.modeleStudies, this.tableauStudies, 
				this.modeleSeries, this.tableauSeries, this.modelePatients, this.tableauPatients, this.state, this, search));

		popMenuPatients.add(menuItemZipPatients);
		popMenuPatients.add(menuItemAnonPatients);
		popMenuPatients.addSeparator();
		popMenuPatients.add(menuItemDeletePatients);
		this.tableauPatients.setComponentPopupMenu(popMenuPatients);

		////////////////////////// STUDIES ///////////////////////////////

		this.tableauStudies.getTableHeader().setReorderingAllowed(false);
		this.tableauStudies.getColumnModel().getColumn(0).setMinWidth(80);
		this.tableauStudies.getColumnModel().getColumn(0).setMaxWidth(80);
		this.tableauStudies.getColumnModel().getColumn(0).setResizable(false);
		this.tableauStudies.getColumnModel().getColumn(1).setMinWidth(180);
		this.tableauStudies.getColumnModel().getColumn(1).setMaxWidth(180);
		this.tableauStudies.getColumnModel().getColumn(1).setResizable(false);
		this.tableauStudies.getColumnModel().getColumn(2).setMinWidth(150);
		this.tableauStudies.getColumnModel().getColumn(2).setMaxWidth(150);
		this.tableauStudies.getColumnModel().getColumn(2).setResizable(false);
		this.tableauStudies.getColumnModel().getColumn(3).setMinWidth(0);
		this.tableauStudies.getColumnModel().getColumn(3).setMaxWidth(0);
		this.tableauStudies.getColumnModel().getColumn(3).setResizable(false);
		this.tableauStudies.setPreferredScrollableViewportSize(new Dimension(410,267));

		this.tableauStudies.addMouseListener(new TableStudiesMouseListener(this, this.tableauStudies, this.modeleStudies,
				this.tableauSeries, this.modeleSeries, tableauStudies.getSelectionModel()));
		List<RowSorter.SortKey> sortKeysStudies = new ArrayList<>();
		sortKeysStudies.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeysStudies.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
		sorterStudies.setSortKeys(sortKeysStudies);
		sorterStudies.sort();
		this.tableauStudies.setRowSorter(sorterStudies);
		this.tableauStudies.setDefaultRenderer(Date.class, new DateRenderer());

		JMenuItem menuItemZipStudies = new JMenuItem("Add to zip/send");
		menuItemZipStudies.addActionListener(new addZipAction(tableauStudies));
		JMenuItem menuItemAnonStudies = new JMenuItem("Add to anonymization tool");
		menuItemAnonStudies.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {					
					ArrayList<String> listeDummy = new ArrayList<String>();
					String patientName = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 0).toString();
					String patientID = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 1).toString();
					Date patientBirthDate = (Date)tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 3);
					listeDummy.add(modeleStudies.getValueAt(tableauStudies.convertRowIndexToModel(tableauStudies.getSelectedRow()), 3).toString());					
					modeleAnonPatients.addPatient(patientName, patientID, patientBirthDate, listeDummy);
					modeleAnonStudies.clear();
					modeleAnonStudies.addStudies(patientName, patientID, listeDummy);
					for(int i = 0; i < modeleAnonPatients.getPatientList().size(); i++){
						if(modeleAnonPatients.getPatient(i).getPatientId().equals(patientID) && 
								modeleAnonPatients.getPatient(i).getPatientName().equals(patientName)){
							anonPatientTable.setRowSelectionInterval(i, i);
						}
					}
					anonTablesPanel.setVisible(true);
					addToAnon.setVisible(true);
					displayAnonTool.setText("Hide anonymization tool");
					pack();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		JMenuItem menuItemDeleteStudy = new JMenuItem("Delete this study");
		menuItemDeleteStudy.addActionListener(new DeleteActionMainPanel("Study", this.modeleStudies, this.tableauStudies, 
				this.modeleSeries, this.tableauSeries, this.modelePatients, this.tableauPatients, this.state, this, search));

		popMenuStudies.add(menuItemZipStudies);
		popMenuStudies.add(menuItemAnonStudies);
		popMenuStudies.addSeparator();
		popMenuStudies.add(menuItemDeleteStudy);
		this.tableauStudies.setComponentPopupMenu(popMenuStudies);

		////////////////////////// SERIES ///////////////////////////////

		this.tableauSeries.getTableHeader().setReorderingAllowed(false);
		this.tableauSeries.getColumnModel().getColumn(0).setMinWidth(260);
		this.tableauSeries.getColumnModel().getColumn(0).setMaxWidth(260);
		this.tableauSeries.getColumnModel().getColumn(0).setResizable(false);
		this.tableauSeries.getColumnModel().getColumn(1).setMinWidth(100);
		this.tableauSeries.getColumnModel().getColumn(1).setMaxWidth(100);
		this.tableauSeries.getColumnModel().getColumn(1).setResizable(false);
		this.tableauSeries.getColumnModel().getColumn(2).setMinWidth(100);
		this.tableauSeries.getColumnModel().getColumn(2).setMaxWidth(100);
		this.tableauSeries.getColumnModel().getColumn(2).setResizable(false);
		this.tableauSeries.getColumnModel().getColumn(3).setMinWidth(0);
		this.tableauSeries.getColumnModel().getColumn(3).setMaxWidth(0);
		this.tableauSeries.getColumnModel().getColumn(3).setResizable(false);
		this.tableauSeries.getColumnModel().getColumn(4).setMinWidth(0);
		this.tableauSeries.getColumnModel().getColumn(4).setMaxWidth(0);
		this.tableauSeries.getColumnModel().getColumn(4).setResizable(false);
		this.tableauSeries.setPreferredScrollableViewportSize(new Dimension(460,267));

		TableColumn serieDescCol = tableauSeries.getColumnModel().getColumn(0);
		serieDescCol.setCellEditor(new DialogCellEditor());
		
		this.tableauSeries.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				// selects the row at which point the mouse is clicked
				Point point = event.getPoint();
				int currentRow = tableauSeries.rowAtPoint(point);
				tableauSeries.setRowSelectionInterval(currentRow, currentRow);
			}
		});

		List<RowSorter.SortKey> sortKeysSeries = new ArrayList<>();
		sortKeysSeries.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sortKeysSeries.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorterSeries.setSortKeys(sortKeysSeries);
		sorterSeries.sort();
		this.tableauSeries.setRowSorter(sorterSeries);

		JMenuItem menuItemZipSeries = new JMenuItem("Add to zip/send");
		menuItemZipSeries.addActionListener(new addZipAction(tableauSeries));
		JMenuItem menuItemSopClass = new JMenuItem("Check if secondary capture");
		menuItemSopClass.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String instanceUid = modeleSeries.getSerie(tableauSeries.convertRowIndexToModel(tableauSeries.getSelectedRow())).getInstance();
					modeleSeries.checkSopClassUid(instanceUid);
					modeleSeries.setValueAt(modeleSeries.checkSopClassUid(instanceUid), tableauSeries.convertRowIndexToModel(tableauSeries.getSelectedRow()), 3);
					modeleSeries.fireTableCellUpdated(tableauSeries.getSelectedRow(), 3);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem menuItemAllSopClass = new JMenuItem("Detect all secondary captures");
		menuItemAllSopClass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					modeleSeries.detectAllSecondaryCaptures();
					modeleSeries.clear();
					modeleSeries.addSerie(tableauStudies.getValueAt(tableauStudies.convertRowIndexToModel(tableauStudies.getSelectedRow()), 3).toString());
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem menuItemDeleteAllSop = new JMenuItem("Remove all secondary captures");
		menuItemDeleteAllSop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					modeleSeries.removeAllSecondaryCaptures();
					modeleSeries.clear();
					modeleSeries.addSerie(tableauStudies.getValueAt(tableauStudies.convertRowIndexToModel(tableauStudies.getSelectedRow()), 3).toString());
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});
		JMenuItem menuItemDeleteSeries = new JMenuItem("Delete this serie");
		menuItemDeleteSeries.addActionListener(new DeleteActionMainPanel("Serie", this.modeleStudies, this.tableauStudies, 
				this.modeleSeries, this.tableauSeries, this.modelePatients, this.tableauPatients, this.state, this, search));

		popMenuSeries.add(menuItemZipSeries);
		popMenuSeries.addSeparator();
		popMenuSeries.add(menuItemSopClass);
		popMenuSeries.add(menuItemAllSopClass);
		popMenuSeries.add(menuItemDeleteAllSop);
		popMenuSeries.addSeparator();
		popMenuSeries.add(menuItemDeleteSeries);
		this.tableauSeries.setComponentPopupMenu(popMenuSeries);

		this.tableauSeries.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				boolean status = (boolean)table.getModel().getValueAt(tableauSeries.convertRowIndexToModel(row), 3);
				if (status && !isSelected) {
					setBackground(Color.RED);
					setForeground(Color.black);
				}else if(isSelected){
					setBackground(tableauExportStudies.getSelectionBackground());
				}else{
					setBackground(tableauExportStudies.getBackground());
				}
				return this;
			}   
		});

		/////////////////////////////////////////////////////////////////////////////
		///////////////////////// ORTHANC TOOLBOX ///////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////

		JPanel toolbox = new JPanel(new BorderLayout());
		JPanel labelAndAnon = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel ortToolbox = new JLabel("<html><font size=\"5\">Orthanc toolbox</font></html>");
		ortToolbox.setBorder(new EmptyBorder(0, 0, 0, 50));
		labelAndAnon.add(ortToolbox);
		labelAndAnon.add(this.state);
		zipShownContent = new JComboBox<Object>(zipContent.toArray());
		zipShownContent.setPreferredSize(new Dimension(297,27));

		oToolRight = new JPanel();
		oToolRight.setLayout(new BoxLayout(oToolRight, BoxLayout.PAGE_AXIS));

		JPanel storeTool = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton storeBtn = new JButton("Store list");
		try {
			QueryFillStore query = new QueryFillStore();
			listeAET = new JComboBox<Object>(query.getAET());

			listeAET.setPreferredSize(new Dimension(297, 27));
			storeTool.add(listeAET);
			storeBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(!zipContent.isEmpty()){
						SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

							@Override
							protected Void doInBackground() throws Exception {
								try {
									storeBtn.setEnabled(false);
									state.setText("<html>Storing data <font color='red'> <br>(Do not use the toolbox while the current operation is not done)</font></html>");
									query.store(listeAET.getSelectedItem().toString(), zipContent);
									zipShownContent.removeAllItems();
									zipShownContentList.removeAll(zipShownContentList);
									zipContent.removeAll(zipContent);
									oToolRight.setVisible(false);
									displayAnonTool.setVisible(true);
									if(displayAnonTool.getText().equals("Hide anonymization tool")){
										anonTablesPanel.setVisible(true);
									}
									storeBtn.setEnabled(true);
									pack();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								return null;
							}

							@Override
							protected void done(){
								state.setText("<html><font color='green'>The data have successfully been stored.</font></html>");
							}
						};
						worker.execute();
					}
				}
			});
			storeTool.add(storeBtn);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (NullPointerException e){
			JOptionPane.showMessageDialog(null, "You should set an AET before using this app (some functions may not work).",
					"No AET found", JOptionPane.INFORMATION_MESSAGE);
		}
		JPanel comboBoxBtn = new JPanel(new FlowLayout(FlowLayout.LEFT));
		comboBoxBtn.add(zipShownContent);
		removeFromZip.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!zipContent.isEmpty()){
					zipContent.remove(zipShownContent.getSelectedIndex());
					zipShownContentList.remove(zipShownContent.getSelectedIndex());
					zipShownContent.removeAllItems();
					for(String s : zipShownContentList){
						zipShownContent.addItem(s);
					}
					if(zipContent.size() >= 1){
						zipSize.setText(zipContent.size() + " element(s)");
					}else{
						state.setText("");
						oToolRight.setVisible(false);
						displayAnonTool.setVisible(true);
						if(displayAnonTool.getText().equals("Hide anonymization tool")){
							anonTablesPanel.setVisible(true);
							addToAnon.setVisible(true);
						}
						pack();
					}
				}
			}
		});
		comboBoxBtn.add(removeFromZip);
		comboBoxBtn.add(exportZip);
		this.zipSize = new JLabel("");
		comboBoxBtn.add(this.zipSize);
		oToolRight.add(comboBoxBtn);
		oToolRight.add(storeTool);
		toolbox.add(labelAndAnon,BorderLayout.WEST);
		oToolRight.setVisible(false);
		toolbox.add(oToolRight);

		/////////////////////////////////////////////////////////////////////////////
		///////////////////////// ANONYMIZATION DETAILS /////////////////////////////
		/////////////////////////////////////////////////////////////////////////////

		JPanel anonDetailed = new JPanel(new BorderLayout());

		anonTablesPanel = new JPanel(new FlowLayout());
		anonPatientTable = new JTable(modeleAnonPatients);
		anonPatientTable.getTableHeader().setToolTipText("Double click on the new name/ID cells to change their values (otherwise, a name/ID will be generated automatically)");
		anonPatientTable.getColumnModel().getColumn(0).setMinWidth(100);
		anonPatientTable.getColumnModel().getColumn(0).setMaxWidth(100);
		anonPatientTable.getColumnModel().getColumn(1).setMinWidth(70);
		anonPatientTable.getColumnModel().getColumn(1).setMaxWidth(70);
		anonPatientTable.getColumnModel().getColumn(2).setMinWidth(0);
		anonPatientTable.getColumnModel().getColumn(2).setMaxWidth(0);
		anonPatientTable.getColumnModel().getColumn(3).setMinWidth(150);
		anonPatientTable.getColumnModel().getColumn(4).setMinWidth(120);
		anonPatientTable.getColumnModel().getColumn(5).setMinWidth(0);
		anonPatientTable.getColumnModel().getColumn(5).setMaxWidth(0);
		anonPatientTable.setPreferredScrollableViewportSize(new Dimension(440,130));
		anonPatientTable.addMouseListener(new TableAnonPatientsMouseListener(anonPatientTable, modeleAnonPatients, modeleAnonStudies));
		anonPatientTable.putClientProperty("terminateEditOnFocusLost", true);

		anonStudiesTable = new JTable(modeleAnonStudies);
		anonStudiesTable.getTableHeader().setToolTipText("Click on the description cells to change their values");
		anonStudiesTable.getColumnModel().getColumn(0).setMinWidth(200);
		anonStudiesTable.getColumnModel().getColumn(1).setMinWidth(80);
		anonStudiesTable.getColumnModel().getColumn(1).setMaxWidth(80);
		anonStudiesTable.getColumnModel().getColumn(2).setMinWidth(150);
		anonStudiesTable.getColumnModel().getColumn(2).setMaxWidth(150);
		anonStudiesTable.getColumnModel().getColumn(3).setMinWidth(0);
		anonStudiesTable.getColumnModel().getColumn(3).setMaxWidth(0);
		anonStudiesTable.setPreferredScrollableViewportSize(new Dimension(430,130));
		anonStudiesTable.setDefaultRenderer(Date.class, new DateRenderer());

		TableColumn studyDescCol = anonStudiesTable.getColumnModel().getColumn(0);
		studyDescCol.setCellEditor(new DialogCellEditor());

		displayAnonTool = new JButton("Display anonymization tool");
		displayAnonTool.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(anonTablesPanel.isVisible()){
					if(!zipContent.isEmpty()){
						oToolRight.setVisible(true);
					}
					anonTablesPanel.setVisible(false);
					addToAnon.setVisible(false);
					displayAnonTool.setText("Display anonymization tool");
				}else{
					oToolRight.setVisible(false);
					anonTablesPanel.setVisible(true);
					addToAnon.setVisible(true);
					displayAnonTool.setText("Hide anonymization tool");
				}
				pack();
			}
		});

		addToAnon = new JButton("Add to anonymization list");
		addToAnon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tableauPatients.getSelectedRow() != -1){
					try {
						String patientName = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 0).toString();
						String patientID = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 1).toString();
						String patientUID = tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 2).toString();
						Date patientBirthDate = (Date)tableauPatients.getValueAt(tableauPatients.getSelectedRow(), 3);
						ArrayList<String> listeDummy = new ArrayList<String>();
						if((tableauSeries.getSelectedRow() != -1 || tableauStudies.getSelectedRow() != -1) && tableauPatients.getSelectedRows().length == 1){
							listeDummy.add(modeleStudies.getValueAt(tableauStudies.convertRowIndexToModel(tableauStudies.getSelectedRow()), 3).toString());
							modeleAnonPatients.addPatient(patientName, patientID, patientBirthDate, listeDummy);
							modeleAnonStudies.clear();
							modeleAnonStudies.addStudies(patientName, patientID, listeDummy);
							for(int i = 0; i < modeleAnonPatients.getPatientList().size(); i++){
								if(modeleAnonPatients.getPatient(i).getPatientId().equals(patientID) && 
										modeleAnonPatients.getPatient(i).getPatientName().equals(patientName)){
									anonPatientTable.setRowSelectionInterval(i, i);
								}
							}
						}else {
							for(Integer i : tableauPatients.getSelectedRows()){
								modeleStudies.clear();
								patientName = tableauPatients.getValueAt(i, 0).toString();
								patientID = tableauPatients.getValueAt(i, 1).toString();
								patientUID = tableauPatients.getValueAt(i, 2).toString();
								patientBirthDate = (Date)tableauPatients.getValueAt(i, 3);
								ArrayList<String> listeUIDs = new ArrayList<String>();
								modeleStudies.addStudy(patientName, patientID, patientUID);
								listeUIDs.addAll(modeleStudies.getIds());
								modeleAnonPatients.addPatient(patientName, patientID, patientBirthDate, listeUIDs);
								modeleAnonStudies.clear();
								modeleAnonStudies.addStudies(patientName, patientID, listeUIDs);
							}
							for(int i = 0; i < modeleAnonPatients.getPatientList().size(); i++){
								if(modeleAnonPatients.getPatient(i).getPatientId().equals(patientID) && 
										modeleAnonPatients.getPatient(i).getPatientName().equals(patientName)){
									anonPatientTable.setRowSelectionInterval(i, i);
								}
							}
						}
					}catch (IOException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				pack();
			}
		});
		removeFromAnonList = new JButton("Remove");
		removeFromAnonList.setPreferredSize(new Dimension(120,27));
		removeFromAnonList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(anonStudiesTable.getSelectedRow() != -1){
					String patientID = modeleAnonStudies.getValueAt(
							anonStudiesTable.convertRowIndexToModel(anonStudiesTable.getSelectedRow()), 3).toString();
					String uidToRemove = modeleAnonStudies.removeStudy(anonStudiesTable.getSelectedRow());
					modeleAnonPatients.removeStudy(uidToRemove);
					if(anonStudiesTable.getRowCount() == 0){
						for(int i = 0; i< modeleAnonPatients.getPatientList().size(); i++){
							if(modeleAnonPatients.getPatientList().get(i).getPatientId().equals(patientID)){
								modeleAnonPatients.removePatient(i);
							}
						}
					}
				}else if(anonPatientTable.getSelectedRow() != -1){
					modeleAnonPatients.removePatient(anonPatientTable.getSelectedRow());
					modeleAnonStudies.empty();
				}
				pack();
			}
		});
		
		JButton setNamesIdBtn = new JButton("Query DB");
		setNamesIdBtn.setPreferredSize(new Dimension(120,27));
		setNamesIdBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(!modeleAnonPatients.getPatientList().isEmpty()){
						SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
						JDBCConnector jdbc;
						jdbc = new JDBCConnector();
						jdbc.newValuesQuery(new java.sql.Date(((Date)anonPatientTable.getValueAt(
								anonPatientTable.convertRowIndexToModel(anonPatientTable.getSelectedRow()), 6)).getTime()), jprefer.get("centerCode", ""));

						ArrayList<String> newName = jdbc.getNewName();
						ArrayList<String> newId = jdbc.getNewId();
						ArrayList<String> oldFirstName = jdbc.getOldFirstName();
						ArrayList<String> oldLastName = jdbc.getOldLastName();
						if(newName.size() == 1){
							anonPatientTable.setValueAt(newName.get(0), anonPatientTable.convertRowIndexToModel(anonPatientTable.getSelectedRow()), 3);
							anonPatientTable.setValueAt(newId.get(0), anonPatientTable.convertRowIndexToModel(anonPatientTable.getSelectedRow()), 4);
						}else if(newName.size() > 1){
							PopUpFrame choicesFrame = new PopUpFrame(anonPatientTable);
							choicesFrame.setData("84000", df.parse("08-03-2015"), oldFirstName, oldLastName, newName, newId, anonPatientTable);
							choicesFrame.setVisible(true);
						}else{
							state.setText("No name found corresponding to this patient");
						}
						jdbc.disconnect();
					}
				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		anonBtn = new JButton("Anonymize");
		anonBtn.setPreferredSize(new Dimension(120,27));
		anonBtn.addActionListener(new AnonAction());

		JPanel anonBtnPanelTop = new JPanel(new FlowLayout());
		anonBtnPanelTop.add(addToAnon);
		anonBtnPanelTop.add(displayAnonTool);
		anonDetailed.add(anonBtnPanelTop, BorderLayout.NORTH);
		anonTablesPanel.add(new JScrollPane(anonPatientTable));
		anonTablesPanel.add(new JScrollPane(anonStudiesTable));
		JPanel anonBtnPanelRight = new JPanel(new GridBagLayout());
		GridBagConstraints gbBtnPanel = new GridBagConstraints();
		gbBtnPanel.gridx = 0;
		gbBtnPanel.gridy = 0;
		anonBtnPanelRight.add(removeFromAnonList, gbBtnPanel);
		gbBtnPanel.insets = new Insets(10, 0, 0, 0);
		gbBtnPanel.gridy = 1;
		anonBtnPanelRight.add(setNamesIdBtn, gbBtnPanel);
		gbBtnPanel.gridy = 2;
		anonBtnPanelRight.add(anonBtn, gbBtnPanel);
		anonTablesPanel.add(anonBtnPanelRight);
		anonTablesPanel.setVisible(false);
		addToAnon.setVisible(false);
		anonDetailed.add(anonTablesPanel, BorderLayout.WEST);

		exportZip.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
					@Override
					protected Void doInBackground() {
						ConvertZipAction zip = new ConvertZipAction(zipShownContent, zipShownContentList, 
								zipContent, exportZip, state, oToolRight, displayAnonTool, anonTablesPanel, removeFromZip,
								storeBtn);
						try{
							zip.generateZip();
						}catch(IOException e){
							e.printStackTrace();
						}
						return null;
					}
				};
				worker.execute();
				pack();
			}
		});

		/////////////////////////////// ADDING COMPONENTS ////////////////
		JPanel p1 = new JPanel(new FlowLayout());
		GridBagConstraints c = new GridBagConstraints();
		JScrollPane jscp = new JScrollPane(tableauPatients);
		jscp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		tablesPanel.add(jscp,c);

		JScrollPane jscp2 = new JScrollPane(tableauStudies);
		jscp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		c.gridx = 1;
		c.gridy = 0;
		tablesPanel.add(jscp2,c);

		JScrollPane jscp3 = new JScrollPane(tableauSeries);
		jscp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		c.gridx = 2;
		c.gridy = 0;
		tablesPanel.add(jscp3,c);

		mainPanel.add(tablesPanel);
		mainPanel.add(toolbox);
		mainPanel.add(anonDetailed);

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////// END TAB 1 : ANONYMIZATION //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////// PANEL 2 : EXPORT ///////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		JPanel mainPanelExport = new JPanel(new BorderLayout());
		JPanel tableExportPanel = new JPanel(new FlowLayout());
		this.sorterExportStudies = new TableRowSorter<TableDataExportStudies>(modeleExportStudies);
		this.sorterExportSeries = new TableRowSorter<TableDataExportSeries>(modeleExportSeries);		

		this.tableauExportStudies = new JTable(modeleExportStudies);
		this.tableauExportStudies.getTableHeader().setReorderingAllowed(false);
		this.tableauExportStudies.getColumnModel().getColumn(0).setMinWidth(170);
		this.tableauExportStudies.getColumnModel().getColumn(0).setMaxWidth(170);
		this.tableauExportStudies.getColumnModel().getColumn(0).setResizable(false);
		this.tableauExportStudies.getColumnModel().getColumn(1).setMinWidth(120);
		this.tableauExportStudies.getColumnModel().getColumn(1).setMaxWidth(120);
		this.tableauExportStudies.getColumnModel().getColumn(1).setResizable(false);
		this.tableauExportStudies.getColumnModel().getColumn(2).setMinWidth(80);
		this.tableauExportStudies.getColumnModel().getColumn(2).setMaxWidth(80);
		this.tableauExportStudies.getColumnModel().getColumn(2).setResizable(false);
		this.tableauExportStudies.getColumnModel().getColumn(3).setMinWidth(180);
		this.tableauExportStudies.getColumnModel().getColumn(3).setMaxWidth(180);
		this.tableauExportStudies.getColumnModel().getColumn(3).setResizable(false);
		this.tableauExportStudies.getColumnModel().getColumn(4).setMinWidth(150);
		this.tableauExportStudies.getColumnModel().getColumn(4).setMaxWidth(150);
		this.tableauExportStudies.getColumnModel().getColumn(4).setResizable(false);
		this.tableauExportStudies.getColumnModel().getColumn(5).setMinWidth(0);
		this.tableauExportStudies.getColumnModel().getColumn(5).setMaxWidth(0);
		this.tableauExportStudies.getColumnModel().getColumn(5).setResizable(false);
		this.tableauExportStudies.setPreferredScrollableViewportSize(new Dimension(700,267));

		this.tableauExportStudies.setDefaultRenderer(Date.class, new DateRenderer());

		JPopupMenu popMenuExportStudies = new JPopupMenu();
		this.tableauExportStudies.setComponentPopupMenu(popMenuExportStudies);

		JMenuItem menuItemExportStudiesRemove = new JMenuItem("Remove from list");
		menuItemExportStudiesRemove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				modeleExportSeries.clear();
				modeleExportStudies.removeStudy(tableauExportStudies.convertRowIndexToModel(tableauExportStudies.getSelectedRow()));
			}
		});
		popMenuExportStudies.add(menuItemExportStudiesRemove);

		JMenuItem menuItemExportStudiesDelete = new JMenuItem("Delete this study");
		menuItemExportStudiesDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DeleteActionExport del = new DeleteActionExport(tableauExportStudies, modeleExportStudies);
				del.delete();
				modeleExportStudies.removeStudy(tableauExportStudies.convertRowIndexToModel(tableauExportStudies.getSelectedRow()));
				modeleExportSeries.clear();
			}
		});
		popMenuExportStudies.add(menuItemExportStudiesDelete);

		JMenuItem menuItemEmptyList = new JMenuItem("Empty the export list");
		menuItemEmptyList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int dialogResult;
				dialogResult = JOptionPane.showConfirmDialog (null, 
						"Are you sure you want to clear the export list ?",
						"Clearing the export list",
						JOptionPane.YES_NO_OPTION);
				if(dialogResult == JOptionPane.YES_OPTION){
					modeleExportSeries.clear();
					modeleExportStudies.clear();
					modeleExportStudies.clearIdsList();
				}
			}
		});
		popMenuExportStudies.add(menuItemEmptyList);

		sorterExportStudies.setSortKeys(sortKeysStudies);
		sorterExportStudies.sort();
		this.tableauExportStudies.setRowSorter(sorterExportStudies);

		this.tableauExportStudies.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				// selects the row at which point the mouse is clicked
				Point point = event.getPoint();
				int currentRow = tableauExportStudies.rowAtPoint(point);
				tableauExportStudies.setRowSelectionInterval(currentRow, currentRow);
				// We clear the details
				modeleExportSeries.clear();
				try {
					if(modeleExportStudies.getRowCount() != 0){
						String studyID = (String)tableauExportStudies.getValueAt(tableauExportStudies.getSelectedRow(), 5);
						modeleExportSeries.addSerie(studyID);
					}
				}catch (RuntimeException e1){
					//Ignore
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

		});

		this.tableauExportSeries = new JTable(modeleExportSeries);
		this.tableauExportSeries.getTableHeader().setReorderingAllowed(false);
		this.tableauExportSeries.getColumnModel().getColumn(0).setMinWidth(260);
		this.tableauExportSeries.getColumnModel().getColumn(0).setMaxWidth(260);
		this.tableauExportSeries.getColumnModel().getColumn(0).setResizable(false);
		this.tableauExportSeries.getColumnModel().getColumn(1).setMinWidth(100);
		this.tableauExportSeries.getColumnModel().getColumn(1).setMaxWidth(100);
		this.tableauExportSeries.getColumnModel().getColumn(1).setResizable(false);
		this.tableauExportSeries.getColumnModel().getColumn(2).setMinWidth(100);
		this.tableauExportSeries.getColumnModel().getColumn(2).setMaxWidth(100);
		this.tableauExportSeries.getColumnModel().getColumn(2).setResizable(false);
		this.tableauExportSeries.getColumnModel().getColumn(3).setMinWidth(0);
		this.tableauExportSeries.getColumnModel().getColumn(3).setMaxWidth(0);
		this.tableauExportSeries.getColumnModel().getColumn(3).setResizable(false);
		this.tableauExportSeries.getColumnModel().getColumn(4).setMinWidth(0);
		this.tableauExportSeries.getColumnModel().getColumn(4).setMaxWidth(0);
		this.tableauExportSeries.getColumnModel().getColumn(4).setResizable(false);
		this.tableauExportSeries.setPreferredScrollableViewportSize(new Dimension(460,267));

		this.tableauExportSeries.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				// selects the row at which point the mouse is clicked
				Point point = event.getPoint();
				int currentRow = tableauExportSeries.rowAtPoint(point);
				tableauExportSeries.setRowSelectionInterval(currentRow, currentRow);
			}
		});

		this.tableauExportSeries.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				boolean status = (boolean)table.getModel().getValueAt(tableauExportSeries.convertRowIndexToModel(row), 3);
				if (status & !isSelected) {
					setBackground(Color.RED);
					setForeground(Color.black);
				}else if(isSelected){
					setBackground(tableauExportStudies.getSelectionBackground());
				}else{
					setBackground(tableauExportStudies.getBackground());
				}
				return this;
			}   
		});

		JPopupMenu popMenuExportSeries = new JPopupMenu();
		this.tableauExportSeries.setComponentPopupMenu(popMenuExportSeries);

		JMenuItem menuItemExportSeriesDelete = new JMenuItem("Delete this serie");
		menuItemExportSeriesDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String uid = tableauExportStudies.getValueAt(tableauExportStudies.getSelectedRow(), 4).toString();
				DeleteActionExport del = new DeleteActionExport(tableauExportSeries, modeleExportSeries);
				del.delete();
				if(tableauExportSeries.getRowCount() == 1){
					modeleAnonStudies.removeFromList(uid);
					modeleExportStudies.removeStudy(tableauExportStudies.convertRowIndexToModel(tableauExportStudies.getSelectedRow()));
				}
				modeleExportSeries.removeSerie(tableauExportSeries.convertRowIndexToModel(tableauExportSeries.getSelectedRow()));
			}
		});

		JMenuItem menuItemExportSeriesDeleteAllSc = new JMenuItem("Delete all secondary captures");
		menuItemExportSeriesDeleteAllSc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					boolean[] studyExist = {true};
					modeleExportSeries.removeAllSecondaryCaptures();

					if(modeleExportSeries.getSeries().isEmpty()){
						modeleExportStudies.removeStudy(tableauExportStudies.convertRowIndexToModel(tableauExportStudies.getSelectedRow()));
						studyExist[0] = false;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		popMenuExportSeries.add(menuItemExportSeriesDelete);
		popMenuExportSeries.add(menuItemExportSeriesDeleteAllSc);
		sorterExportSeries.setSortKeys(sortKeysSeries);
		sorterExportSeries.sort();
		this.tableauExportSeries.setRowSorter(sorterExportSeries);

		tableExportPanel.add(new JScrollPane(this.tableauExportStudies));
		tableExportPanel.add(new JScrollPane(this.tableauExportSeries));

		stateExports.setBorder(new EmptyBorder(0, 0, 0, 40));

		JPanel exportPanel = new JPanel(new FlowLayout());

		JPanel labelPanelExport = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel exportToLabel = new JLabel("<html><font size=\"5\">Export list to...</font></html>");
		exportToLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
		labelPanelExport.add(exportToLabel);
		labelPanelExport.add(stateExports);

		JButton exportBtn = new JButton("Remote server");
		exportBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
					boolean[] successful = {true};
					String exception = "";
					@Override
					protected Void doInBackground() {
						// Putting the export preferences in the anon plugin registry
						if(remoteServer.getText() != null){
							jprefer.put("remoteServer", remoteServer.getText());
						}
						
						if(remotePort.getText() != null){
							jprefer.put("remotePort", remotePort.getText());
						}
						
						if(servUsername.getText() != null){
							jprefer.put("servUsername", servUsername.getText());
						}
						if(new String(servPassword.getPassword()) != null){
							jprefer.put("servPassword", new String(servPassword.getPassword()));
						}
						if(remoteFilePath.getText() != null){
							jprefer.put("remoteFilePath", remoteFilePath.getText());
						}
						jprefer.put("exportType", exportType.getSelectedItem().toString());

							exportBtn.setText("Exporting...");
							exportBtn.setEnabled(false);
							ConvertZipAction zip;
							try {
								String property = "java.io.tmpdir";
								String tempDir = System.getProperty(property);
								Path path = Paths.get(tempDir);
								zip = new ConvertZipAction(path.toString(), modeleExportStudies.getOrthancIds(), true);
								zip.generateZip();
								String zipPath = zip.getGeneratedZipPath();
								String zipName = zip.getGeneratedZipName();
								remoteFileName = new StringBuilder();
								
								//removing the temporary file default name value
								remoteFileName.append(zipName.substring(0,14));
								remoteFileName.append(zipName.substring(zipName.length() - 4));
								ExportFiles export = new ExportFiles(jprefer.get("exportType", ExportFiles.OPTION_FTP), 
										jprefer.get("remoteFilePath", "/"), remoteFileName.toString(), zipPath, jprefer.get("remoteServer", ""), 
										jprefer.getInt("remotePort", 21), jprefer.get("servUsername", ""), jprefer.get("servPassword", ""));
								export.export();
								if(export.getResult() != null){
									successful[0] = false;
									exception = export.getResult();
								}
							} catch (FileNotFoundException e){
								successful[0] = false;
								stateExports.setText("<html><font color='red'>The data export failed (the zip was not created)</font></html>");
							} catch (IOException e) {
								successful[0] = false;
								exception = e.getMessage();								
							} 
						return null;
					}

					@Override
					public void done(){
						if(successful[0]){
							stateExports.setText("<html><font color='green'>The data has been successfully been exported</font></html>");
						}else{
							if(!stateExports.getText().contains("The zip was not created")){
								stateExports.setText("<html><font color='red'>The data export failed (" + exception + ") </font></html>");	
							}
						}
						exportBtn.setText("Remote server");
						exportBtn.setEnabled(true);
					}
				};
				if(!modeleExportStudies.getOrthancIds().isEmpty()){
					stateExports.setText("Exporting...");
					worker.execute();
				}
			}
		});

		exportBtn.setToolTipText("Fill the remote server parameters in the setup tab before attempting an export.");

		JComboBox<String> reportType = new JComboBox<String>();
		reportType.addItem("CSV");
		
		JButton reportBtn = new JButton("Report");
		reportBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(reportType.getSelectedItem().equals("CSV")){
					jprefer.put("reportType", "CSV");
					CSV csv = new CSV();
					if(!modeleExportStudies.getOrthancIds().isEmpty()){
						for(String uid : modeleExportStudies.getOrthancIds()){
							try {
								DataFetcher cdf = new DataFetcher(uid);
								DataFetcher cdfOriginalStudyData = new DataFetcher(cdf.extractData("AnonymizedFrom"));

								String oldPatientName = cdfOriginalStudyData.extractData("PatientName");
								String oldPatientId = cdfOriginalStudyData.extractData("PatientID");
								String newPatientName = cdf.extractData("PatientName");
								String newPatientId = cdf.extractData("PatientID");
								String oldStudyDate = cdfOriginalStudyData.extractData("StudyDate");
								String oldStudyDesc = cdfOriginalStudyData.extractData("StudyDescription");
								String newStudyDesc = cdf.extractData("StudyDescription");
								String nbSeries = cdf.extractStats("CountSeries");
								String nbInstances = cdf.extractStats("CountInstances");
								String size = cdf.extractStats("DiskSize");
								String studyInstanceUid = cdf.extractData("StudyInstanceUID");

								csv.addStudy(oldPatientName, oldPatientId, newPatientName, newPatientId, oldStudyDate, oldStudyDesc, newStudyDesc, nbSeries, nbInstances, size, studyInstanceUid);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						try {
							csv.genCSV();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}
					}
				}else{
					jprefer.put("reportType", "CTP");
					boolean[] dataSent = {true};
					if(!modeleExportStudies.getOrthancIds().isEmpty()){
						try {
							JDBCConnector jdbc = new JDBCConnector();
							for(Study study : modeleExportStudies.getStudiesList()){
								DataFetcher cdf = new DataFetcher(study.getId());
								if(!jdbc.sendSizeAndNewUID(study.getPatientName(), cdf.extractStats("DiskSize"), study.getNewStudyInstanceUID())){
									dataSent[0] = false;
								}
								if(remoteFileName != null){
									if(!jdbc.sendFileName(study.getPatientName(), remoteFileName.toString())){
										dataSent[0] = false;										
									}
								}
							}
							remoteFileName = null;
							jdbc.disconnect();
							if(!dataSent[0]){
								stateExports.setText("<html><font color = 'red'>The report was not sent to the database</font></html>");
							}else{
								stateExports.setText("<html><font color = 'green'>The report was sent to the database</font></html>");
							}
						} catch (ClassNotFoundException | SQLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

		try {
			QueryFillStore query = new QueryFillStore();

			JButton exportToZip = new JButton("Zip");
			exportToZip.addActionListener(new ActionListener() {
				boolean[] confirm = {true};
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() {
							confirm[0] = true;
							stateExports.setText("Converting to Zip...");
							exportToZip.setText("Converting to Zip...");
							exportToZip.setEnabled(false);
							Path path = null;
							JFileChooser chooser = new JFileChooser();
							chooser.setCurrentDirectory(new java.io.File(jprefer.get("zipLocation", ".")));
							chooser.setDialogTitle("Export zip to...");
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							chooser.setAcceptAllFileFilterUsed(false);
							if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
								path = chooser.getSelectedFile().toPath();
							}else{
								confirm[0] = false;
							}
							if(confirm[0]){
								try{
									ConvertZipAction zip = new ConvertZipAction(path.toString(), modeleExportStudies.getOrthancIds(), false);
									zip.generateZip();
								}catch(IOException e){
									e.printStackTrace();
								}
							}
							return null;
						}

						@Override
						public void done(){
							if(confirm[0]){
								stateExports.setText("<html><font color='green'>The data has been successfully been converted to zip</font></html>");
							}else{
								stateExports.setText("");
							}
							exportToZip.setText("Zip");
							exportToZip.setEnabled(true);
						}
					};
					if(!modeleExportStudies.getOrthancIds().isEmpty()){
						worker.execute();
					}
				}
			});


			JComboBox<Object> listeAETExport = new JComboBox<Object>(query.getAET());
			JButton storeExport = new JButton("Store");
			storeExport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() {
							try {
								storeExport.setEnabled(false);
								storeExport.setText("Storing...");
								query.store(listeAETExport.getSelectedItem().toString(), modeleExportStudies.getOrthancIds());
							} catch (IOException e1) {
								stateExports.setText("<html><font color= 'red'>The request was not received (" + e1.getMessage() + ") </font></html>");
							}
							return null;
						}

						@Override
						protected void done(){
							stateExports.setText("<html><font color= 'green'>The request was successfully received</font></html>");
							storeExport.setText("Store");
							storeExport.setEnabled(true);
						}
					};
					if(!modeleExportStudies.getOrthancIds().isEmpty()){
						stateExports.setText("Storing data...");
						worker.execute();
					}
				}
			});

			JComboBox<Object> listePeers = new JComboBox<Object>(query.getPeers());
			JButton peerExport = new JButton("OrthancPeer");
			peerExport.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() {
							peerExport.setEnabled(false);
							peerExport.setText("Sending...");
							try {
								query.sendPeer(listePeers.getSelectedItem().toString(), modeleExportStudies.getOrthancIds());
							} catch (IOException e1) {
								stateExports.setText("<html><font color= 'red'>The request was not received (" + e1.getMessage() + ") </font></html>");
							}
							return null;
						}

						@Override
						protected void done(){
							stateExports.setText("<html><font color= 'green'>The request was successfully received</font></html>");
							peerExport.setText("OrthancPeer");
							peerExport.setEnabled(true);
						}
					};
					if(!modeleExportStudies.getOrthancIds().isEmpty()){
						stateExports.setText("Sending to a peer");
						worker.execute();
					}
				}
			});

			exportPanel.add(reportType);
			exportPanel.add(reportBtn);
			JLabel dummyLabel0 = new JLabel("");
			dummyLabel0.setBorder(new EmptyBorder(0,0,0,50));
			exportPanel.add(dummyLabel0);
			exportPanel.add(exportToZip);
			JLabel dummyLabel1 = new JLabel("");
			dummyLabel1.setBorder(new EmptyBorder(0,0,0,50));
			exportPanel.add(dummyLabel1);
			exportPanel.add(exportBtn);
			JLabel dummyLabel2 = new JLabel("");
			dummyLabel2.setBorder(new EmptyBorder(0,0,0,50));
			exportPanel.add(dummyLabel2);
			exportPanel.add(listeAETExport);
			exportPanel.add(storeExport);
			JLabel dummyLabel3 = new JLabel("");
			dummyLabel3.setBorder(new EmptyBorder(0,0,0,50));
			exportPanel.add(dummyLabel3);
			exportPanel.add(listePeers);
			exportPanel.add(peerExport);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		JPanel southExport = new JPanel();
		southExport.setLayout(new BoxLayout(southExport, BoxLayout.PAGE_AXIS));
		southExport.add(labelPanelExport);
		southExport.add(exportPanel);

		mainPanelExport.add(southExport, BorderLayout.SOUTH);
		mainPanelExport.add(tableExportPanel, BorderLayout.CENTER);
		JPanel p2 = new JPanel(new FlowLayout());

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////// END PANEL 2 : EXPORT ///////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////


		///////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////// PANEL 3 : SETUP ////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		this.bodyCharChoice = Choice.KEEP;
		this.datesChoice = Choice.KEEP;
		this.bdChoice = Choice.REPLACE;
		this.ptChoice = Choice.CLEAR;
		this.scChoice = Choice.CLEAR;
		this.descChoice = Choice.CLEAR;

		JPanel mainPanelSetup = new JPanel();
		mainPanelSetup.setLayout(new BorderLayout());

		JPanel westSetup = new JPanel(new GridBagLayout());
		JPanel tabSetup = new JPanel(new GridBagLayout());
		JPanel westNorth1Setup = new JPanel(new FlowLayout());
		JPanel westNorth2Setup = new JPanel(new FlowLayout());
		JPanel eastExport = new JPanel(new GridBagLayout());
		JPanel eastDB = new JPanel(new GridBagLayout());

		GridBagConstraints gbSetup = new GridBagConstraints();
		gbSetup.gridx = 0;
		gbSetup.gridy = 0;
		JLabel centerCodeLabel = new JLabel("Center code");
		centerCodeLabel.setBorder(new EmptyBorder(0, 0, 0, 73));
		westNorth1Setup.add(centerCodeLabel);
		this.centerCode = new JTextField();
		this.centerCode.setText(jprefer.get("centerCode", "12345"));
		this.centerCode.setPreferredSize(new Dimension(100,20));
		westNorth1Setup.add(this.centerCode);
		westSetup.add(westNorth1Setup);

		gbSetup.gridx = 0;
		gbSetup.gridy = 1;
		anonProfiles = new JComboBox<Object>(new String[]{"Default", "Full clearing", "Custom"});
		anonProfiles.setPreferredSize(new Dimension(100,27));
		JLabel anonProfilesLabel = new JLabel("Anonymization profile");
		anonProfilesLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
		westNorth2Setup.add(anonProfilesLabel);
		westNorth2Setup.add(anonProfiles);
		westNorth2Setup.setBorder(new EmptyBorder(10, 0, 20, 0));
		westSetup.add(westNorth2Setup, gbSetup);

		JLabel bcLabel = new JLabel("Body characteristics");
		JLabel datesLabel = new JLabel("Full dates");
		JLabel bdLabel = new JLabel("Birth date*");
		bdLabel.setToolTipText("Choosing clear will actually change the birth date to 01/01/1900");
		JLabel ptLabel = new JLabel("Private tags");
		JLabel scLabel = new JLabel("Secondary capture/Structured reports");
		JLabel descLabel = new JLabel("Series/Study descriptions");

		gbSetup.gridy = 0;
		gbSetup.anchor = GridBagConstraints.WEST;
		gbSetup.insets = new Insets(0, 0, 20, 0);
		gbSetup.gridx = 1;
		JLabel keepLabel = new JLabel("Keep");
		tabSetup.add(keepLabel, gbSetup);
		gbSetup.gridx = 2;
		JLabel clearLabel = new JLabel("Clear");
		tabSetup.add(clearLabel, gbSetup);

		// Body characteristics
		gbSetup.insets = new Insets(0, 0, 10, 50);
		gbSetup.gridx = 0;
		gbSetup.gridy = 1;
		tabSetup.add(bcLabel, gbSetup);
		ButtonGroup bgBodyCharac = new ButtonGroup();

		JRadioButton radioBodyCharac1 = new JRadioButton();
		JRadioButton radioBodyCharac2 = new JRadioButton();
		this.bodyCharList[0] = radioBodyCharac1;
		this.bodyCharList[1] = radioBodyCharac2;
		bgBodyCharac.add(radioBodyCharac1);
		gbSetup.gridx = 1;
		tabSetup.add(radioBodyCharac1, gbSetup);
		bgBodyCharac.add(radioBodyCharac2);
		gbSetup.gridx = 2;
		tabSetup.add(radioBodyCharac2, gbSetup);

		// Dates
		gbSetup.gridx = 0;
		gbSetup.gridy = 2;
		tabSetup.add(datesLabel, gbSetup);
		ButtonGroup bgDates = new ButtonGroup();
		JRadioButton radioDates1 = new JRadioButton();
		JRadioButton radioDates2 = new JRadioButton();
		this.datesList[0] = radioDates1;
		this.datesList[1] = radioDates2;
		bgDates.add(radioDates1);
		gbSetup.gridx = 1;
		tabSetup.add(radioDates1, gbSetup);
		bgDates.add(radioDates2);
		gbSetup.gridx = 2;
		tabSetup.add(radioDates2, gbSetup);

		// Birth date
		gbSetup.gridx = 0;
		gbSetup.gridy = 3;
		tabSetup.add(bdLabel, gbSetup);
		ButtonGroup bgBd = new ButtonGroup();
		JRadioButton radioBd1 = new JRadioButton();
		JRadioButton radioBd2 = new JRadioButton();
		this.bdList[0] = radioBd1;
		this.bdList[1] = radioBd2;
		bgBd.add(radioBd1);
		gbSetup.gridx = 1;
		tabSetup.add(radioBd1, gbSetup);
		bgBd.add(radioBd2);
		gbSetup.gridx = 2;
		tabSetup.add(radioBd2, gbSetup);

		// Private tags
		gbSetup.gridx = 0;
		gbSetup.gridy = 4;
		tabSetup.add(ptLabel, gbSetup);
		ButtonGroup bgPt = new ButtonGroup();
		JRadioButton radioPt1 = new JRadioButton();
		JRadioButton radioPt2 = new JRadioButton();
		this.ptList[0] = radioPt1;
		this.ptList[1] = radioPt2;
		bgPt.add(radioPt1);
		gbSetup.gridx = 1;
		tabSetup.add(radioPt1, gbSetup);
		bgPt.add(radioPt2);
		gbSetup.gridx = 2;
		tabSetup.add(radioPt2, gbSetup);

		// Secondary capture
		gbSetup.gridx = 0;
		gbSetup.gridy = 5;
		tabSetup.add(scLabel, gbSetup);
		ButtonGroup bgSc = new ButtonGroup();
		JRadioButton radioSc1 = new JRadioButton();
		JRadioButton radioSc2 = new JRadioButton();
		this.scList[0] = radioSc1;
		this.scList[1] = radioSc2;
		bgSc.add(radioSc1);
		gbSetup.gridx = 1;
		tabSetup.add(radioSc1, gbSetup);
		bgSc.add(radioSc2);
		gbSetup.gridx = 2;
		tabSetup.add(radioSc2, gbSetup);

		// Study/serie description
		gbSetup.gridx = 0;
		gbSetup.gridy = 6;
		tabSetup.add(descLabel, gbSetup);
		ButtonGroup bgDesc = new ButtonGroup();
		JRadioButton radioDesc1 = new JRadioButton();
		JRadioButton radioDesc2 = new JRadioButton();
		this.descList[0] = radioDesc1;
		this.descList[1] = radioDesc2;
		bgDesc.add(radioDesc1);
		gbSetup.gridx = 1;
		tabSetup.add(radioDesc1, gbSetup);
		bgDesc.add(radioDesc2);
		gbSetup.gridx = 2;
		tabSetup.add(radioDesc2, gbSetup);

		JLabel profileLabel = new JLabel();

		anonProfiles.addActionListener(
				new AnonActionProfileListener(anonProfiles, profileLabel, radioBodyCharac1, 
						radioBodyCharac2, radioDates1, radioDates2, radioBd2, 
						radioBd1, radioPt1, radioPt2, radioSc1, radioSc2, radioDesc1, radioDesc2));

		anonProfiles.setSelectedItem(jprefer.get("profileAnon", "Default"));

		// Showing the currently selected profile in the main panel
		gbBtnPanel.gridy = 3;
		anonBtnPanelRight.add(profileLabel, gbBtnPanel);

		JTabbedPane eastSetupPane = new JTabbedPane();
		eastSetupPane.add("Export setup", eastExport);
		eastSetupPane.addTab("Database setup", eastDB);

		gbSetup.insets = new Insets(20, 10, 0, 10);
		gbSetup.gridx = 0;
		gbSetup.gridy = 2;
		westSetup.add(tabSetup, gbSetup);
		
		mainPanelSetup.add(westSetup, BorderLayout.WEST);

		gbSetup.gridx = 0;
		gbSetup.gridy = 0;
		eastExport.add(new JLabel("Adress"), gbSetup);

		gbSetup.gridx = 1;
		gbSetup.gridy = 0;
		this.remoteServer = new JTextField();
		this.remoteServer.setText(jprefer.get("remoteServer", ""));
		this.remoteServer.setPreferredSize(new Dimension(300,20));
		eastExport.add(this.remoteServer, gbSetup);

		gbSetup.gridx = 0;
		gbSetup.gridy = 1;
		eastExport.add(new JLabel("Port"), gbSetup);

		gbSetup.gridx = 1;
		gbSetup.gridy = 1;
		this.remotePort = new JTextField();
		this.remotePort.setText(jprefer.get("remotePort", ""));
		this.remotePort.setPreferredSize(new Dimension(300,20));
		eastExport.add(this.remotePort, gbSetup);
		
		gbSetup.gridx = 0;
		gbSetup.gridy = 2;
		eastExport.add(new JLabel("Username"), gbSetup);

		gbSetup.gridx = 1;
		gbSetup.gridy = 2;
		this.servUsername = new JTextField();
		this.servUsername.setText(jprefer.get("servUsername", ""));
		this.servUsername.setPreferredSize(new Dimension(300,20));
		eastExport.add(this.servUsername, gbSetup);

		gbSetup.gridx = 0;
		gbSetup.gridy = 3;
		eastExport.add(new JLabel("Password"), gbSetup);

		gbSetup.gridx = 1;
		gbSetup.gridy = 3;
		this.servPassword = new JPasswordField();
		this.servPassword.setText(jprefer.get("servPassword", ""));
		this.servPassword.setPreferredSize(new Dimension(300,20));
		eastExport.add(this.servPassword, gbSetup);

		gbSetup.gridx = 0;
		gbSetup.gridy = 4;
		eastExport.add(new JLabel("Remote file path"), gbSetup);

		gbSetup.gridx = 1;
		gbSetup.gridy = 4;
		this.remoteFilePath = new JTextField();
		this.remoteFilePath.setText(jprefer.get("remoteFilePath", "/"));
		this.remoteFilePath.setPreferredSize(new Dimension(300,20));
		eastExport.add(this.remoteFilePath, gbSetup);

		gbSetup.gridx = 0;
		gbSetup.gridy = 5;
		eastExport.add(new JLabel("Export protocol"), gbSetup);

		gbSetup.gridx = 1;
		gbSetup.gridy = 5;
		String[] exportTypeList = {"FTP", "SFTP", "WEBDAV"};
		this.exportType = new JComboBox<String>(exportTypeList);
		this.exportType.setSelectedItem(jprefer.get("exportType", "FTP"));
		this.exportType.setPreferredSize(new Dimension(140,20));
		eastExport.add(this.exportType, gbSetup);

		gbSetup.gridx = 0;
		gbSetup.gridy = 0;
		eastDB.add(new JLabel("Adress"), gbSetup);
		
		gbSetup.gridx = 1;
		gbSetup.gridy = 0;
		this.dbAdress = new JTextField();
		this.dbAdress.setText(jprefer.get("dbAdress", ""));
		this.dbAdress.setPreferredSize(new Dimension(300,20));
		eastDB.add(this.dbAdress, gbSetup);
		
		gbSetup.gridx = 0;
		gbSetup.gridy = 1;
		eastDB.add(new JLabel("Port"), gbSetup);
		
		gbSetup.gridx = 1;
		gbSetup.gridy = 1;
		this.dbPort = new JTextField();
		this.dbPort.setText(jprefer.get("dbPort", ""));
		this.dbPort.setPreferredSize(new Dimension(300,20));
		eastDB.add(this.dbPort, gbSetup);
		
		gbSetup.gridx = 0;
		gbSetup.gridy = 2;
		eastDB.add(new JLabel("Database name"), gbSetup);
		
		gbSetup.gridx = 1;
		gbSetup.gridy = 2;
		this.dbName = new JTextField();
		this.dbName.setText(jprefer.get("dbName", ""));
		this.dbName.setPreferredSize(new Dimension(300,20));
		eastDB.add(this.dbName, gbSetup);
		
		gbSetup.gridx = 0;
		gbSetup.gridy = 3;
		eastDB.add(new JLabel("Username"), gbSetup);
		
		gbSetup.gridx = 1;
		gbSetup.gridy = 3;
		this.dbUsername = new JTextField();
		this.dbUsername.setText(jprefer.get("dbUsername", ""));
		this.dbUsername.setPreferredSize(new Dimension(300,20));
		eastDB.add(this.dbUsername, gbSetup);
		
		gbSetup.gridx = 0;
		gbSetup.gridy = 4;
		eastDB.add(new JLabel("Password"), gbSetup);
		
		gbSetup.gridx = 1;
		gbSetup.gridy = 4;
		this.dbPassword = new JPasswordField();
		this.dbPassword.setText(jprefer.get("dbPassword", ""));
		this.dbPassword.setPreferredSize(new Dimension(300,20));
		eastDB.add(this.dbPassword, gbSetup);
		
		JPanel aboutPanel = new JPanel(new FlowLayout());
		JButton aboutBtn = new JButton("About us");
		aboutBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutBoxFrame ab = new AboutBoxFrame();
				ab.pack();
				ab.setVisible(true);
			}
		});
		
		aboutPanel.add(aboutBtn);
		
		if(dbAdress.getText().length() > 0 && dbPort.getText().length() > 0 && dbName.getText().length() > 0
				&& dbUsername.getText().length() > 0 && new String(dbPassword.getPassword()).length() > 0){
			reportType.addItem("CTP");
			reportType.setSelectedItem(jprefer.get("reportType", "CSV"));
		}
		
		if(dbAdress.getText().length() == 0 || dbPort.getText().length() == 0 || dbName.getText().length() == 0
				|| dbUsername.getText().length() == 0 || new String(dbPassword.getPassword()).length() == 0){
			setNamesIdBtn.setVisible(false);
		}
		
		mainPanelSetup.add(westSetup, BorderLayout.WEST);
		mainPanelSetup.add(eastSetupPane, BorderLayout.EAST);
		mainPanelSetup.add(aboutPanel, BorderLayout.SOUTH);

		JPanel p3 = new JPanel(new FlowLayout());

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////// END TAB 3 : SETUP //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////

		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				//////////// Filling the user preferences ////////////
				if(anonProfiles.getSelectedItem().equals("Custom")){
					for(int i = 0; i < 2; i++){
						if(bodyCharList[i].isSelected()){
							if(jprefer.getInt("bodyCharac", 0) != i){
								jprefer.putInt("bodyCharac", i);
							}
						}	
					}
					for(int i = 0; i < 2; i++){
						if(datesList[i].isSelected()){
							if(jprefer.getInt("Dates", 0) != i){
								jprefer.putInt("Dates", i);
							}
						}
					}
					for(int i = 0; i < 2; i++){
						if(bdList[i].isSelected()){
							if(jprefer.getInt("BD", 0) != i){
								jprefer.putInt("BD", i);
							}
						}
					}
					for(int i = 0; i < 2; i++){
						if(ptList[i].isSelected()){
							if(jprefer.getInt("PT", 0) != i){
								jprefer.putInt("PT", i);
							}
						}
					}
					for(int i = 0; i < 2; i++){
						if(scList[i].isSelected()){
							if(jprefer.getInt("SC", 0) != i){
								jprefer.putInt("SC", i);
							}
						}
					}
					for(int i = 0; i < 2; i++){
						if(descList[i].isSelected()){
							if(jprefer.getInt("DESC", 0) != i){
								jprefer.putInt("DESC", i);
							}
						}
					}
				}
				jprefer.put("profileAnon", anonProfiles.getSelectedItem().toString());
				jprefer.put("centerCode", centerCode.getText());
				
				// Putting the export preferences in the anon plugin registry
				if(remoteServer.getText() != null){
					jprefer.put("remoteServer", remoteServer.getText());
				}
				if(remotePort.getText() != null){
					jprefer.put("remotePort", remotePort.getText());
				}
				if(servUsername.getText() != null){
					jprefer.put("servUsername", servUsername.getText());
				}
				if(new String(servPassword.getPassword()) != null){
					jprefer.put("servPassword", new String(servPassword.getPassword()));
				}
				if(remoteFilePath.getText() != null){
					jprefer.put("remoteFilePath", remoteFilePath.getText());
				}
				jprefer.put("exportType", exportType.getSelectedItem().toString());

				// Putting the database preferences in the anon plugin registry				
				if(dbAdress.getText() != null){
					jprefer.put("dbAdress", dbAdress.getText());
				}
				
				if(dbPort.getText() != null){
					jprefer.put("dbPort", dbPort.getText());
				}
				
				if(dbName.getText() != null){
					jprefer.put("dbName", dbName.getText());
				}
				
				if(dbUsername.getText() != null){
					jprefer.put("dbUsername", dbUsername.getText());
				}
				
				if(new String(dbPassword.getPassword()) != null){
					jprefer.put("dbPassword", new String(dbPassword.getPassword()));
				}
				
				if(dbAdress.getText().length() == 0 || dbPort.getText().length() == 0 || dbName.getText().length() == 0
						|| dbUsername.getText().length() == 0 || new String(dbPassword.getPassword()).length() == 0){
					reportType.removeAllItems();
					reportType.addItem("CSV");
				}else{
					reportType.removeAllItems();
					reportType.addItem("CSV");
					reportType.addItem("CTP");
				}

				if(dbAdress.getText().length() == 0 || dbPort.getText().length() == 0 || dbName.getText().length() == 0
						|| dbUsername.getText().length() == 0 || new String(dbPassword.getPassword()).length() == 0){
					setNamesIdBtn.setVisible(false);
				}else{
					setNamesIdBtn.setVisible(true);
				}
				
				if(remoteServer.getText().length() == 0){
					exportBtn.setEnabled(false);
				}else{
					exportBtn.setEnabled(true);
				}
				if(tabbedPane.getSelectedIndex() == 0){
					if(!modeleAnonStudies.getOldOrthancUIDs().isEmpty()){
						anonTablesPanel.setVisible(true);
						displayAnonTool.setText("Hide anonymization tool");
						addToAnon.setVisible(true);
					}
				}else{
					anonTablesPanel.setVisible(false);
					displayAnonTool.setText("Display anonymization tool");
					addToAnon.setVisible(false);
				}
				pack();
			}
		});

		p1.add(mainPanel);
		tabbedPane.add("Anonymize", p1);

		p2.add(mainPanelExport);
		tabbedPane.add("Export", p2);

		p3.add(mainPanelSetup);
		tabbedPane.add("Setup", p3);

		Image image = new ImageIcon(ClassLoader.getSystemResource("OrthancIcon.png")).getImage();
		this.setIconImage(image);

		this.getContentPane().add(tabbedPane);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.getRootPane().setDefaultButton(search);
		this.addWindowListener(new CloseWindowAdapter(this, this.zipContent, this.modeleAnonStudies.getOldOrthancUIDs(), this.modeleExportStudies.getStudiesList()));
	}


	// Anonymization query
	private class AnonAction extends AbstractAction{
		private static final long serialVersionUID = 1L;
		int dialogResult = JOptionPane.YES_OPTION;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dialogResult = JOptionPane.YES_OPTION;
			anonCount = 0;
			SwingWorker<Void,Void> workerRemoveScAndSr = new SwingWorker<Void,Void>(){
				@Override
				protected Void doInBackground() {
					try {
						modeleAnonStudies.removeScAndSr();
					} catch(FileNotFoundException e){
						// Ignore
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

				@Override
				protected Void doInBackground() {
					//////////// Filling the user preferences ////////////
					if(anonProfiles.getSelectedItem().equals("Custom")){
						for(int i = 0; i < 2; i++){
							if(bodyCharList[i].isSelected()){
								if(jprefer.getInt("bodyCharac", 0) != i){
									jprefer.putInt("bodyCharac", i);
								}
							}	
						}
						for(int i = 0; i < 2; i++){
							if(datesList[i].isSelected()){
								if(jprefer.getInt("Dates", 0) != i){
									jprefer.putInt("Dates", i);
								}
							}
						}
						for(int i = 0; i < 2; i++){
							if(bdList[i].isSelected()){
								if(jprefer.getInt("BD", 0) != i){
									jprefer.putInt("BD", i);
								}
							}
						}
						for(int i = 0; i < 2; i++){
							if(ptList[i].isSelected()){
								if(jprefer.getInt("PT", 0) != i){
									jprefer.putInt("PT", i);
								}
							}
						}
						for(int i = 0; i < 2; i++){
							if(scList[i].isSelected()){
								if(jprefer.getInt("SC", 0) != i){
									jprefer.putInt("SC", i);
								}
							}
						}
						for(int i = 0; i < 2; i++){
							if(descList[i].isSelected()){
								if(jprefer.getInt("DESC", 0) != i){
									jprefer.putInt("DESC", i);
								}
							}
						}
					}

					jprefer.put("profileAnon", anonProfiles.getSelectedItem().toString());

					anonBtn.setEnabled(false);
					addToAnon.setEnabled(false);
					removeFromAnonList.setEnabled(false);

					anonBtn.setText("Anonymizing");
					// SETTING UP THE CHOICES
					for(int i = 0; i < 2; i++){
						if(i == 0){
							if(bodyCharList[i].isSelected())
								bodyCharChoice = Choice.KEEP;
							if(datesList[i].isSelected())
								datesChoice = Choice.KEEP;
							if(bdList[i].isSelected())
								bdChoice = Choice.KEEP;
							if(ptList[i].isSelected())
								ptChoice = Choice.KEEP;
							if(scList[i].isSelected())
								scChoice = Choice.KEEP;
							if(descList[i].isSelected())
								descChoice = Choice.KEEP;
						}else{
							if(bodyCharList[i].isSelected())
								bodyCharChoice = Choice.CLEAR;
							if(datesList[i].isSelected())
								datesChoice = Choice.CLEAR;
							if(bdList[i].isSelected())
								bdChoice = Choice.REPLACE;
							if(ptList[i].isSelected())
								ptChoice = Choice.CLEAR;
							if(scList[i].isSelected())
								scChoice = Choice.CLEAR;
							if(descList[i].isSelected())
								descChoice = Choice.CLEAR;					
						}
					}

					int i = 0;
					int j = 0;
					try {
						if(anonProfiles.getSelectedItem().equals("Full clearing")){
							if(modeleAnonStudies.getModalities().contains("NM") || 
									modeleAnonStudies.getModalities().contains("PT")){
								dialogResult = JOptionPane.showConfirmDialog (null, 
										"Full clearing is not recommended for NM or PT modalities."
												+ "Are you sure you want to anonymize ?",
												"Warning anonymizing PT/NM",
												JOptionPane.WARNING_MESSAGE,
												JOptionPane.YES_NO_OPTION);
							}
						}
						if(modeleAnonStudies.getModalities().contains("US")){
							JOptionPane.showMessageDialog (null, 
									"DICOM files with the US modality may have hard printed informations, "
											+ "you may want to check your files.",
											"Warning anonymizing US",
											JOptionPane.WARNING_MESSAGE);
						}
						
						// Checking if several anonymized patients have the same ID or not
						boolean[] similarIDs = {false};
						for(int n = 0; n < anonPatientTable.getRowCount(); n++){
							String newID = modeleAnonPatients.getPatient(anonPatientTable.convertRowIndexToModel(n)).getNewID();
							if(newID != "" && !newIDs.contains(newID)){
								newIDs.add(newID);
							}else if(newIDs.contains(newID)){
								similarIDs[0] = true;
							}
						}
						if(similarIDs[0]){
							dialogResult = JOptionPane.showConfirmDialog (null, 
									"You have defined 2 or more identical IDs for anonymized patients, which is not recommended."
											+ " Are you sure you want to anonymize ?",
											"Warning similar IDs",
											JOptionPane.WARNING_MESSAGE,
											JOptionPane.YES_NO_OPTION);
						}
						
						if(dialogResult == JOptionPane.YES_OPTION){

							String substituteName = "A-" + jprefer.get("centerCode", "12345");

							SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
							String substituteID = "A-" + df.format(new Date());

							for(String patientID : modeleAnonStudies.getPatientIDs()){
								String newName = modeleAnonPatients.getPatient(anonPatientTable.convertRowIndexToModel(j)).getNewName();
								String newID = modeleAnonPatients.getPatient(anonPatientTable.convertRowIndexToModel(j)).getNewID();
								String newUID = "";
								if((newName == null || newName.equals("")) || (newID == null || newID.equals(""))){
									anonCount++;
								}
								if(newName == null || newName.equals("")){
									newName = substituteName + "^" + anonCount;
									modeleAnonPatients.setValueAt(newName, anonPatientTable.convertRowIndexToModel(j), 3);
								}

								if(newID == null || newID.equals("")){
									newID = substituteID + "^" + anonCount;
									modeleAnonPatients.setValueAt(newID, anonPatientTable.convertRowIndexToModel(j), 4);
								}

								for(String uid : modeleAnonStudies.getOldOrthancUIDsWithID(patientID)){
									String newDesc = modeleAnonStudies.getNewDesc(uid);
									QueryAnon quAnon;
									quAnon = new QueryAnon(bodyCharChoice, datesChoice, bdChoice, ptChoice, scChoice, descChoice, newName, newID, newDesc);
									quAnon.setQuery();
									state.setText("<html>Anonymization state - " + (i+1) + "/" + modeleAnonStudies.getStudies().size() + 
											" <font color='red'> <br>(Do not use the toolbox while the current operation is not done)</font></html>");
									quAnon.sendQuery("studies", uid);
									modeleAnonStudies.addNewUid(quAnon.getNewUID());
									i++;
									newUID = quAnon.getNewPatientUID();
								}
								modeleExportStudies.addStudy(newName, newID, newUID);
								j++;
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void done(){
					if(scList[1].isSelected()){
						workerRemoveScAndSr.execute();
					}
					anonBtn.setEnabled(true);
					addToAnon.setEnabled(true);
					removeFromAnonList.setEnabled(true);
					anonBtn.setText("Anonymize");
					if(dialogResult == JOptionPane.YES_OPTION){
						state.setText("<html><font color='green'>The data has successfully been anonymized.</font></html>");
						tabbedPane.setSelectedIndex(1);
						modeleAnonPatients.clear();
						modeleAnonStudies.empty();
					}
					if(tableauExportStudies.getRowCount() > 0){
						tableauExportStudies.setRowSelectionInterval(tableauExportStudies.getRowCount() - 1, tableauExportStudies.getRowCount() - 1);
					}
					modeleExportSeries.clear();
					try {
						if(modeleExportStudies.getRowCount() > 0){
							String studyID = (String)tableauExportStudies.getValueAt(tableauExportStudies.getSelectedRow(), 5);
							modeleExportSeries.addSerie(studyID);
							tableauExportSeries.setRowSelectionInterval(0,0);
						}
					} catch (Exception e1) {
						// IGNORE
					}
				}
			};
			if(!modeleAnonStudies.getOldOrthancUIDs().isEmpty()){
				if(dialogResult == JOptionPane.YES_OPTION){
					worker.execute();
				}
			}
		}
	}

	/*
	 * This class defines the action on pop menu, that is, displaying the patient's history.
	 */
	public class addZipAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private JTable tableau;

		public addZipAction(JTable tableau){
			this.tableau = tableau;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			if(zipContent != null){
				anonTablesPanel.setVisible(false);
				displayAnonTool.setVisible(false);
				addToAnon.setVisible(false);
				oToolRight.setVisible(true);
				if(tableau.equals(tableauPatients)){
					String name = "Patient - " + tableauPatients.getValueAt(tableau.getSelectedRow(), 0).toString();
					String id = tableauPatients.getValueAt(tableau.getSelectedRow(), 2).toString();
					if(!zipContent.contains(id)){
						zipShownContent.addItem(name);
						zipShownContentList.add(name);
						zipContent.add(id);
					}else{
						state.setText("<html><font color = 'red'> This element is already in the zip list</font></html>");
					}
				}else if(tableau.equals(tableauStudies)){
					String date = "Study - " + df.format(((Date)tableauStudies.getValueAt(tableau.getSelectedRow(), 0))) + "  " + tableauStudies.getValueAt(tableau.getSelectedRow(), 1);
					String id = tableauStudies.getValueAt(tableau.getSelectedRow(), 3).toString();
					if(!zipContent.contains(id)){
						zipShownContent.addItem(date);
						zipShownContentList.add(date);
						zipContent.add(id);
					}else{
						state.setText("<html><font color = 'red'> This element is already in the zip list</font></html>");
					}
				}else{
					String desc = "Serie - [" + tableauSeries.getValueAt(tableau.getSelectedRow(), 1) + "] "+ tableauSeries.getValueAt(tableau.getSelectedRow(), 2)+ " instances - " + tableauSeries.getValueAt(tableau.getSelectedRow(), 0);
					String id = tableauSeries.getValueAt(tableau.getSelectedRow(), 4).toString();
					if(!zipContent.contains(id)){
						zipShownContent.addItem(desc);
						zipShownContentList.add(desc);
						zipContent.add(id);
					}else{
						state.setText("<html><font color = 'red'> This element is already in the zip list</font></html>");
					}
				}
			}
			if(zipSize != null){
				zipSize.setText(zipContent.size() + " element(s)");
			}
			pack();
		}		
	}
	
	// LAUNCHERS

	public static void main(String... args){
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
		VueAnon vue = new VueAnon();
		vue.setSize(1200,640);
		vue.setVisible(true);
		vue.pack();
	}

	@Override
	public void run(String string) {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
		VueAnon vue = new VueAnon();
		vue.setSize(1200, 640);
		vue.setVisible(true);
		vue.pack();
	}
}
