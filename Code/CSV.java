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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

public class CSV {

	private StringBuilder content;
	private Path path;
	private boolean[] choix = {false};
	private DateFormat df = new SimpleDateFormat("MMddyyyyHHmmss");
	private Preferences jpreferAnon = Preferences.userRoot().node("<unnamed>/anonPlugin");
	
	public CSV(){
		content = new StringBuilder();
		content.append("Old patient name");
		content.append(',');
		content.append("Old patient id");
		content.append(',');
		content.append("New patient name");
		content.append(',');
		content.append("New patient id");
		content.append(',');
		content.append("Old study date");
		content.append(',');
		content.append("Old study description");
		content.append(',');
		content.append("New study description");	
		content.append(',');
		content.append("Nb series");
		content.append(',');
		content.append("Nb instances");
		content.append(',');
		content.append("Size");
		content.append(',');
		content.append("Study instance uid");
		content.append('\n');
	}

	public void addStudy(String oldPatientName, String oldPatientId, String newPatientName, String newPatientId,
			String oldStudyDate, String oldStudyDesc, String newStudyDesc, String nbSeries, String nbInstances, 
			String size, String studyInstanceUid){
		content.append(oldPatientName);
		content.append(',');
		content.append(oldPatientId);
		content.append(',');
		content.append(newPatientName);
		content.append(',');
		content.append(newPatientId);
		content.append(',');
		content.append(oldStudyDate);
		content.append(',');
		content.append(oldStudyDesc);
		content.append(',');
		content.append(newStudyDesc);
		content.append(',');
		content.append(nbSeries);
		content.append(',');
		content.append(nbInstances);
		content.append(',');
		content.append(size);
		content.append(',');
		content.append(studyInstanceUid);
		content.append('\n');
	}
	
	public void genCSV() throws FileNotFoundException{
		this.fileChooser();
		if(choix[0]){
			File f = new File(path + File.separator + df.format(new Date()) + ".csv");
			PrintWriter pw = new PrintWriter(f);
			pw.write(content.toString());
			pw.close();
		}
	}
	
	private void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File(jpreferAnon.get("csvLocation", System.getProperty("user.dir"))));
		chooser.setDialogTitle("Export csv to...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			this.path = chooser.getSelectedFile().toPath();
			jpreferAnon.put("csvLocation", this.path.toString());
			this.choix[0] = true;
		} else {
			this.choix[0] = false;
		}
	}

}
