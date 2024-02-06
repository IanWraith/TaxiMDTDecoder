// Please note :
// The demodulation section of this program was taken from Trunkito 
// which in turn took the code from Stefan Petersen's Another Packet Engine
// the legal notice below explains the terms and conditions.
// In addition I have also borrowed code from Trunksniffer and John's
// Starline program both of which were based around Trunkito.
// All of these programs were written in C but have been converted to
// Java by Ian Wraith.
//
// Ian Wraith 7th April 2007

import java.awt.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class DisplayFrame extends JFrame implements ActionListener, HyperlinkListener {
	public static final long serialVersionUID = 1;
	private JMenuBar menuBar = new JMenuBar();
	private JMenuItem preamble_hunt_item, sync_hunt_item, logging_item,decode_mode_item;
	private JMenuItem view_binary_item, view_special_ascii_item,clear_screen_item, bad_crc_item;
	private JMenuItem view_links;
	private JMenuItem save_settings_item, no_autocab_item, no_auriga_item,exit_item, bad_crc_warnings_item, view_multi_lines_item;
	private JMenuItem channel_stats_item, autocab_t0_ack_filter_item,autocab_t0_bcast_filter_item,autocab_t0_s132_filter_item;
	private JMenuItem autocab_t4_s1_filter_item,autocab_t4_s6_filter_item,autocab_t4_s32_filter_item;
	private JMenuItem autocab_t1_filter_item;
	
	//audio
	private JMenu audioDevicesMenu;
	private JRadioButtonMenuItem bothChannels, leftChannel, rightChannel;
	
	private JMenuItem help_item;
	private TaxiMdtDecoder theApp;
//	private JScrollBar vscrollbar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 500);
//	private JScrollBar hscrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1000);
	private JStatusBar status_bar = new JStatusBar();
	
	private static ArrayList<AudioMixer> devices;

	// Constructor
	public DisplayFrame(String title, TaxiMdtDecoder theApp) {
		setTitle(title);
		this.theApp = theApp;
		status_bar.setTheApp(this.theApp);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		// Menu setup
		setJMenuBar(menuBar);
		// Main
		JMenu mainMenu = new JMenu("Main");
		mainMenu.add(decode_mode_item = new JRadioButtonMenuItem("Decode Mode", true));
		mainMenu.add(preamble_hunt_item = new JRadioButtonMenuItem( "Preamble Hunt", false));
		mainMenu.add(sync_hunt_item = new JRadioButtonMenuItem("Sync Hunt", false));
		mainMenu.addSeparator();
		mainMenu.add(channel_stats_item = new JRadioButtonMenuItem( "Generate Channel Statistics", false));
		mainMenu.add(logging_item = new JRadioButtonMenuItem("Log to Disk", false));
		mainMenu.add(save_settings_item = new JMenuItem("Save Settings"));
		mainMenu.addSeparator();
		mainMenu.add(exit_item = new JMenuItem("Exit"));
		
		decode_mode_item.addActionListener(this);
		preamble_hunt_item.addActionListener(this);
		sync_hunt_item.addActionListener(this);
		logging_item.addActionListener(this);
		channel_stats_item.addActionListener(this);
		save_settings_item.addActionListener(this);
		exit_item.addActionListener(this);
		menuBar.add(mainMenu);
		
		// View
		JMenu viewMenu = new JMenu("View");
		viewMenu.add(clear_screen_item = new JMenuItem("Clear Screen"));
		clear_screen_item.addActionListener(this);
		viewMenu.addSeparator();
		// Auriga Message Filters //
		JMenu auriga_filter_submenu = new JMenu("Auriga Message Filters");
		auriga_filter_submenu.add(no_auriga_item = new JRadioButtonMenuItem("Ignore All Auriga Messages"));
		no_auriga_item.addActionListener(this);
		viewMenu.add(auriga_filter_submenu);
		// Autocab Message Filters //
		JMenu autocab_filter_submenu = new JMenu("Autocab Message Filters");
		viewMenu.add(autocab_filter_submenu);
		autocab_filter_submenu.add(no_autocab_item = new JRadioButtonMenuItem("Ignore All Autocab Messages"));
		no_autocab_item.addActionListener(this);
		autocab_filter_submenu.addSeparator();
		
		// Type 0 sub menu
		JMenu autocab_t0_filter_submenu = new JMenu("Type 0 Filters");
		autocab_filter_submenu.add(autocab_t0_filter_submenu);	
		// Autocab Type 0 ACK Filter //
		autocab_t0_filter_submenu
				.add(autocab_t0_ack_filter_item = new JRadioButtonMenuItem("View Type 0 ACK Messages", false));
		autocab_t0_ack_filter_item.addActionListener(this);
		// Autocab Type 0 BCAST Filter //
		autocab_t0_filter_submenu
				.add(autocab_t0_bcast_filter_item = new JRadioButtonMenuItem("View Type 0 BCAST Messages", false));
		autocab_t0_bcast_filter_item.addActionListener(this);
		// Autocab Type 132 Filter
		autocab_t0_filter_submenu
			.add(autocab_t0_s132_filter_item = new JRadioButtonMenuItem("View Type 0 Subtype 132 Messages", false));
		autocab_t0_s132_filter_item.addActionListener(this);
	
		// Type 1
		autocab_filter_submenu
		.add(autocab_t1_filter_item = new JRadioButtonMenuItem("View Type 1 Messages", false));
		autocab_t1_filter_item.addActionListener(this);
		
		// Type 4 sub menu 
		JMenu autocab_t4_filter_submenu = new JMenu("Type 4 Filters");
		autocab_filter_submenu.add(autocab_t4_filter_submenu);		
		// Sub Type 1
		autocab_t4_filter_submenu
			.add(autocab_t4_s1_filter_item = new JRadioButtonMenuItem("View Type 4 Subtype 1 Messages", false));
		autocab_t4_s1_filter_item.addActionListener(this);	
		// Sub Type 6
		autocab_t4_filter_submenu
			.add(autocab_t4_s6_filter_item = new JRadioButtonMenuItem("View Type 4 Subtype 6 Messages", false));
		autocab_t4_s6_filter_item.addActionListener(this);	
		// Sub Type 32
		autocab_t4_filter_submenu
			.add(autocab_t4_s32_filter_item = new JRadioButtonMenuItem("View Type 4 Subtype 32 Messages", false));
		autocab_t4_s32_filter_item.addActionListener(this);			
		
		//viewMenu.addSeparator();
		viewMenu.add(bad_crc_warnings_item = new JRadioButtonMenuItem("View Bad CRC Warnings", false));
		bad_crc_warnings_item.addActionListener(this);
		viewMenu.add(view_binary_item = new JRadioButtonMenuItem("View Binary",false));
		view_binary_item.addActionListener(this);
		viewMenu.add(bad_crc_item = new JRadioButtonMenuItem("View Data with a bad CRC", false));
		bad_crc_item.addActionListener(this);
		viewMenu.add(view_links = new JRadioButtonMenuItem("View Links in Messages", false));
		view_links.addActionListener(this);
		viewMenu.add(view_multi_lines_item = new JRadioButtonMenuItem("View Messages across Multiple Lines", false));
		view_multi_lines_item.addActionListener(this);
		viewMenu.add(view_special_ascii_item = new JRadioButtonMenuItem("View Special ASCII Characters", false));
		view_special_ascii_item.addActionListener(this);
		menuBar.add(viewMenu);

		
		//audio menu
		JMenu audioMenu = new JMenu("Audio");
		
		audioDevicesMenu = buildAudioDevices();//new JMenu("Audio Devices");
		audioMenu.add(audioDevicesMenu);
		audioDevicesMenu.updateUI();

		//add the stereo options
		ButtonGroup channelGrp = new ButtonGroup();
		JMenuItem ms = new JMenu("Stereo");
		//both
		this.bothChannels = new JRadioButtonMenuItem("Both");
		bothChannels.setActionCommand("Both");
		bothChannels.addActionListener(this);
		channelGrp.add(bothChannels);
		ms.add(bothChannels);
		
		//left
		this.leftChannel = new JRadioButtonMenuItem("Left");
		leftChannel.setActionCommand("Left");
		leftChannel.addActionListener(this);
		channelGrp.add(leftChannel);
		ms.add(leftChannel);
		
		//right
		this.rightChannel = new JRadioButtonMenuItem("Right");
		rightChannel.setActionCommand("Right");
		rightChannel.addActionListener(this);
		channelGrp.add(rightChannel);
		ms.add(rightChannel);

		leftChannel.setSelected(true);	//preselect both
		
		//add to main menu
		audioMenu.add(ms);
		
		menuBar.add(audioMenu);
		
		// Help
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(help_item = new JMenuItem("Help"));
		help_item.addActionListener(this);
		menuBar.add(helpMenu);
		
/*		// Add the vertical scrollbar
		add(vscrollbar, BorderLayout.EAST);
		// Add a listener for this
		vscrollbar.addAdjustmentListener(new MyAdjustmentListener());
		// Add the horizontal scrollbar
		add(hscrollbar, BorderLayout.SOUTH);
		// Add a listener for this
		hscrollbar.addAdjustmentListener(new MyAdjustmentListener());*/
		// Setup the status bar
		getContentPane().add(status_bar, java.awt.BorderLayout.SOUTH);
		// Read in the default settings file //
		try {
			readDefaultSettings();
		} catch (Exception e) {
			// Can't find the default settings file //
			System.out.println("\nError : Unable to read the file default_settings.xml");
			// Clear all menu item settings as they can be left in an unstable
			// state in this condition //
			theApp.setViewSpecialAscii(false);
			theApp.setViewBadCRC(false);
//			theApp.clearScreenNow = false;
			theApp.setViewNoAutocab(false);
			theApp.setViewNoAuriga(false);
			theApp.setViewBadCrcWarnings(false);
			theApp.setViewBinary(false);
			theApp.generateChannelStatistics = false;
			theApp.setViewAutocab_t0_ack(true);
			theApp.setViewAutocab_t0_bcast(true);

		}
		// Read in the system info file //
		try {
			readSystemInfo();
		} catch (Exception e) {
			// Can't find the default settings file //
			System.out.println("\nError : Unable to read the file system_info.xml");
			System.out.println(e.toString());
			
		}
		// Update the menu items //
		menuItemUpdate();
	}
	
	/**
	 * Build menu options for the current audio devices
	 * @return
	 */
	private JMenu buildAudioDevices(){
		JMenu ret = new JMenu("Audio Devices");
		ButtonGroup group = new ButtonGroup();
		
		ArrayList<AudioMixer> deviceList = getCompatibleDevices();
		
		for (int i=0; i<deviceList.size(); i++){
			Line.Info l[] = AudioSystem.getTargetLineInfo(deviceList.get(i).lineInfo);
			JRadioButtonMenuItem dev = new JRadioButtonMenuItem(deviceList.get(i).description);
			dev.setActionCommand("mixer");//deviceList.get(i).description);
			dev.addActionListener(this);
			
			if (i==0){
				dev.setSelected(true);
			}
			
			group.add(dev);
			ret.add(dev);
//			System.out.println(i + 
//					"\n\tMixer: " + deviceList.get(i).description + 
//					"\n\tLineClass: " + l[0].getLineClass() +
//					"\n\tDesc: " + l[0].toString());
		}
		
		return ret;
	}
	
	/**
	 * Get a list of devices that are compatible
	 * @return
	 */
	private ArrayList<AudioMixer> getCompatibleDevices(){
		devices = new ArrayList<AudioMixer>();
		
		System.out.println("Getting Mixers...");
		
		//list the available mixers
		Mixer.Info mixers[] = AudioSystem.getMixerInfo();
		
		//iterate the mixers and display TargetLines
		for (int i=0; i< mixers.length; i++){
			Mixer m = AudioSystem.getMixer(mixers[i]);
//			System.out.println("Mixer:" + m.toString());
			Line.Info l[] = m.getTargetLineInfo();
			if(l.length>0){
				for (int x=0; x< l.length; x++){
					if (l[0].getLineClass().getName().equals("javax.sound.sampled.TargetDataLine")){
						AudioMixer mc = new AudioMixer(this.theApp, mixers[i].getName(), m, l[x]);
						devices.add(mc);	//add the mixer id to the returned list						
					}
				}
			}
		}
		
		return devices;
	}

	/**
	 * Change the mixer to the one selected
	 * @param mixerName
	 */
	private void changeMixer(String mixerName){
		theApp.myAudio.changeMixer(mixerName);

	}
	
	// Handle all menu events
	public void actionPerformed(ActionEvent event) {
		String event_name = event.getActionCommand();

		if (event_name.equalsIgnoreCase("left")){
			theApp.setChannel(TaxiMdtDecoder.STEREO_LEFT);
		}
		if(event_name.equalsIgnoreCase("right")){
			theApp.setChannel(TaxiMdtDecoder.STEREO_RIGHT);
		}
		if(event_name.equalsIgnoreCase("both")){
			theApp.setChannel(TaxiMdtDecoder.STEREO_BOTH);
		}
		if(event_name.equalsIgnoreCase("mixer")){
			changeMixer(((JRadioButtonMenuItem)event.getSource()).getText());
		}
	
		// Preamble Hunt
		if (event_name == "Preamble Hunt") {
			theApp.setPreambleHunt(true);
			theApp.setSyncHunt(false);
//			theApp.preamble_hunt_status = 0;
//			theApp.preamble_rxed = 0;
			theApp.setDecodeMode(false);
			menuItemUpdate();
			return;
		}
		// Sync Hunt
		if (event_name == "Sync Hunt") {
			theApp.setPreambleHunt(false);
			theApp.setSyncHunt(true);
			theApp.setDecodeMode(false);
			menuItemUpdate();
			return;
		}
		// Log to disk /
		if (event_name == "Log to Disk") {			
			// If we aren't logging then do so
			if (theApp.logging == false) {
				// Create a user statistics object //
				UserStatistics stats_object = new UserStatistics();
				// Get the current time & date //
				Date now = new Date();
				DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
				DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);
				String file_name;
				// Bring up a dialog box that allows the user to select the name
				// of the saved file
				JFileChooser fc = new JFileChooser();
				// The dialog box title //
				fc.setDialogTitle("Select the log file name");
				// Start in current directory
				fc.setCurrentDirectory(new File("."));
				// Don't all types of file to be selected //
				fc.setAcceptAllFileFilterUsed(false);
				// Only show .txt files //
				fc.setFileFilter(new TextfileFilter());
				// Show save dialog; this method does not return until the
				// dialog is closed
				int returnval = fc.showSaveDialog(this);
				// If the user has selected cancel then quit
				if (returnval == JFileChooser.CANCEL_OPTION) {
					menuItemUpdate();
					return;
				}
				// Get the file name an path of the selected file
				file_name = fc.getSelectedFile().getPath();
				// Does the file name end in .txt ? //
				// If not then automatically add a .txt ending //
				int last_index = file_name.lastIndexOf(".txt");
				if (last_index != (file_name.length() - 4))
					file_name = file_name + ".txt";
				// Create a file with this name //
				File tfile = new File(file_name);
				// If the file exists ask the user if they want to overwrite it
				// //
				if (tfile.exists()) {
					int response = JOptionPane.showConfirmDialog(null,
							"Overwrite existing file?", "Confirm Overwrite",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.CANCEL_OPTION) {
						menuItemUpdate();
						return;
					}
				}
				// Open the file
				try {
					theApp.file = new FileWriter(tfile);
				} catch (Exception e) {
					System.out.println("\nError opening the logging file");
				}
				theApp.logging = true;
				// Show the version of the program //
				theApp.fileWrite(theApp.program_version, "");
				// Show the date and time that logging started //
				String line = "Started logging on ";
				line = line + df.format(now);
				line = line + " at ";
				line = line + tf.format(now);
				theApp.fileWrite(line, "");
				// Clear the user statistics main index //
				stats_object.clearIndex();
			} else {
				// Stop logging
				stopLogging();
				theApp.logging = false;
			}
			menuItemUpdate();
			return;
		}
		// Generate Channel Statistics //
		if (event_name == "Generate Channel Statistics") {
			// Create a channel statistics object //
			ChannelStatistics channel_stats_object = new ChannelStatistics();
			if (theApp.generateChannelStatistics == false) {
				openCsvFile();
				// Clear the channel statistics object //
				channel_stats_object.clear();
			} else {
				theApp.generateChannelStatistics = false;
			}
			menuItemUpdate();
			return;
		}
		// Decode Mode
		if (event_name == "Decode Mode") {
			theApp.setPreambleHunt(false);
			theApp.setSyncHunt(false);
			theApp.setDecodeMode(true);
			menuItemUpdate();
			return;
		}
		// View Binary //
		if (event_name == "View Binary") {
			if (theApp.getViewBinary() == false)
				theApp.setViewBinary(true);
			else
				theApp.setViewBinary(false);
			menuItemUpdate();
			return;
		}
		// View special ASCII characters //
		if (event_name == "View Special ASCII Characters") {
			if (theApp.getViewSpecialAscii() == false)
				theApp.setViewSpecialAscii(true);
			else
				theApp.setViewSpecialAscii(false);
			menuItemUpdate();
			return;
		}
		// Clear the screen //
		if (event_name == "Clear Screen") {
//			theApp.clearScreenNow = true;
			theApp.clearScreen();
			repaint();
			return;
		}
		// Save the settings //
		if (event_name == "Save Settings") {
			saveSettings();
			return;
		}
		// View data that fails the CRC //
		if (event_name == "View Data with a bad CRC") {
			if (theApp.getViewBadCRC() == false)
				theApp.setViewBadCRC(true);
			else
				theApp.setViewBadCRC(false);
			menuItemUpdate();
			return;
		}
		// View no Autocab items //
		if (event_name == "Ignore All Autocab Messages") {
			if (theApp.getViewNoAutocab() == false)
				theApp.setViewNoAutocab(true);
			else
				theApp.setViewNoAutocab(false);
			menuItemUpdate();
			// Warn the user if they have decided to ignore both Auriga and
			// Autocab traffic
			if ((theApp.getViewNoAutocab() == true)
					&& (theApp.getViewNoAuriga() == true))
				JOptionPane
						.showMessageDialog(
								null,
								"Warning: You have decided to ignore both Autocab and Auriga messages so won't see any traffic !",
								"Taxi MDT Decoder", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// View no Auriga items //
		if (event_name == "Ignore All Auriga Messages") {
			if (theApp.getViewNoAuriga() == false)
				theApp.setViewNoAuriga(true);
			else
				theApp.setViewNoAuriga(false);
			menuItemUpdate();
			// Warn the user if they have decided to ignore both Auriga and
			// Autocab traffic
			if ((theApp.getViewNoAutocab() == true)
					&& (theApp.getViewNoAuriga() == true))
				JOptionPane
						.showMessageDialog(
								null,
								"Warning: You have decided to ignore both Autocab and Auriga messages so won't see any traffic !",
								"Taxi MDT Decoder", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// View bad CRC warnings //
		if (event_name == "View Bad CRC Warnings") {
			if (theApp.getViewBadCrcWarnings() == false)
				theApp.setViewBadCrcWarnings(true);
			else
				theApp.setViewBadCrcWarnings(false);
			menuItemUpdate();
			return;
		}
		// View Autocab Type 0 ACK messages //
		if (event_name == "View Type 0 ACK Messages") {
			if (theApp.getViewAutocab_t0_ack() == false)
				theApp.setViewAutocab_t0_ack(true);
			else
				theApp.setViewAutocab_t0_ack(false);
			menuItemUpdate();
			return;
		}
		// View Autocab Type 0 BCAST messages //
		if (event_name == "View Type 0 BCAST Messages") {
			if (theApp.getViewAutocab_t0_bcast() == false)
				theApp.setViewAutocab_t0_bcast(true);
			else
				theApp.setViewAutocab_t0_bcast(false);
			menuItemUpdate();
			return;
		}
		// View Autocab Type 0 Subtype 132 messages //
		if (event_name == "View Type 0 Subtype 132 Messages") {
			if (theApp.getViewAutocab_t0_s132() == false)
				theApp.setViewAutocab_t0_s132(true);
			else
				theApp.setViewAutocab_t0_s132(false);
			menuItemUpdate();
			return;
		}		
		
		// View Autocab Type 1 messages //
		if (event_name == "View Type 1 Messages") {
			if (theApp.getViewAutocab_t1() == false)
				theApp.setViewAutocab_t1(true);
			else
				theApp.setViewAutocab_t1(false);
			menuItemUpdate();
			return;
		}		
		
		// View Autocab Type 4 Sub Type 1 messages //
		if (event_name == "View Type 4 Subtype 1 Messages") {
			if (theApp.getViewAutocab_t4_s1() == false)
				theApp.setViewAutocab_t4_s1(true);
			else
				theApp.setViewAutocab_t4_s1(false);
			menuItemUpdate();
			return;
		}
		// View Autocab Type 4 Sub Type 6 messages //
		if (event_name == "View Type 4 Subtype 6 Messages") {
			if (theApp.getViewAutocab_t4_s6() == false)
				theApp.setViewAutocab_t4_s6(true);
			else
				theApp.setViewAutocab_t4_s6(false);
			menuItemUpdate();
			return;
		}
		// View Autocab Type 4 Sub Type 32 messages //
		if (event_name == "View Type 4 Subtype 32 Messages") {
			if (theApp.getViewAutocab_t4_s32() == false)
				theApp.setViewAutocab_t4_s32(true);
			else
				theApp.setViewAutocab_t4_s32(false);
			menuItemUpdate();
			return;
		}
		
	
		// View Messages on Multiple Lines //
		if (event_name == "View Messages across Multiple Lines") {
			if (theApp.getViewMultiLines() == false)
				theApp.setViewMultiLines(true);
			else
				theApp.setViewMultiLines(false);
			menuItemUpdate();
			return;
		}


		// View Links in Messages //
		if (event_name == "View Links in Messages") {
			if (theApp.getLinks() == false)
				theApp.setLinks(true);
			else
				theApp.setLinks(false);
			menuItemUpdate();
			return;
		}
		
		
		// Exit //
		if (event_name == "Exit")
			shutdown();
		
		if (event_name == "Help") {
			BareBonesBrowserLaunch.openURL("https://sourceforge.net/apps/mediawiki/taxidecoder");
		}
		
	}

	// Update all the menu items
	private void menuItemUpdate() {
		decode_mode_item.setSelected(theApp.getDecodeMode());
		preamble_hunt_item.setSelected(theApp.getPreambleHunt());
		sync_hunt_item.setSelected(theApp.getSyncHunt());
		logging_item.setSelected(theApp.logging);
		channel_stats_item.setSelected(theApp.generateChannelStatistics);
		view_binary_item.setSelected(theApp.getViewBinary());
		view_multi_lines_item.setSelected(theApp.getViewMultiLines());
		view_links.setSelected(theApp.getLinks());
		view_special_ascii_item.setSelected(theApp.getViewSpecialAscii());
		bad_crc_item.setSelected(theApp.getViewBadCRC());
		no_autocab_item.setSelected(theApp.getViewNoAutocab());
		no_auriga_item.setSelected(theApp.getViewNoAuriga());
		bad_crc_warnings_item.setSelected(theApp.getViewBadCrcWarnings());
		autocab_t0_ack_filter_item.setSelected(theApp.getViewAutocab_t0_ack());
		autocab_t0_bcast_filter_item.setSelected(theApp.getViewAutocab_t0_bcast());
		autocab_t0_s132_filter_item.setSelected(theApp.getViewAutocab_t0_s132());
		autocab_t1_filter_item.setSelected(theApp.getViewAutocab_t1());
		autocab_t4_s1_filter_item.setSelected(theApp.getViewAutocab_t4_s1());
		autocab_t4_s6_filter_item.setSelected(theApp.getViewAutocab_t4_s6());
		autocab_t4_s32_filter_item.setSelected(theApp.getViewAutocab_t4_s32());
		
		//mixer
		MenuElement[] devs = audioDevicesMenu.getSubElements();
//		int list = devs[0].getSubElements().length;
//		System.out.println("Audio device menu count:" + list);
		if (devs.length >0){
			for (MenuElement m : devs[0].getSubElements()){
				if (((JRadioButtonMenuItem)m).getText().equals(theApp.myAudio.getMixer().getMixerInfo().getName())){
					((JRadioButtonMenuItem)m).setSelected(true);
					break;
				}
			}
		}
		
		//audio channel
		switch(theApp.getChannelAsInt()){
		case (0):
			leftChannel.setSelected(true);break;
		case (1):
			rightChannel.setSelected(true);break;
		case (2):
			bothChannels.setSelected(true);break;
		}
		
		// Update the status bar //
		if (theApp.getDecodeMode() == true)
			status_bar.setDecodeStatus("Decode Mode");
		if (theApp.getPreambleHunt() == true)
			status_bar.setDecodeStatus("Preamble Hunt Mode");
		if (theApp.getSyncHunt() == true)
			status_bar.setDecodeStatus("Sync Hunt Mode");
		if (theApp.logging == true)
			status_bar.setLoggingStatus("Logging");
		else
			status_bar.setLoggingStatus("Not Logging");
	}

	// Save the users settings as an XML file //
	private void saveSettings() {
		FileWriter xmlfile;
		String line;
		// Open the default file settings //
		try {
			xmlfile = new FileWriter("default_settings.xml");
			// Start the XML file //
			line = "<?xml version='1.0' encoding='utf-8' standalone='yes'?><settings>";
			xmlfile.write(line);
			// Decode mode //
			line = "<decode_mode val='";
			if (theApp.getDecodeMode() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// Preamble mode //
			line = "<preamble_mode val='";
			if (theApp.getPreambleHunt() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// Sync hunt mode //
			line = "<sync_hunt_mode val='";
			if (theApp.getSyncHunt() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Binary //
			line = "<view_binary val='";
			if (theApp.getViewBinary() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Special ASCII characters //
			line = "<view_special_ascii val='";
			if (theApp.getViewSpecialAscii() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View packets that have failed the CRC //
			line = "<view_bad_crc val='";
			if (theApp.getViewBadCRC() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View no Autocab messages //
			line = "<view_no_autocab val='";
			if (theApp.getViewNoAutocab() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View no Auriga messages //
			line = "<view_no_auriga val='";
			if (theApp.getViewNoAuriga() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View bad CRC warnings //
			line = "<view_bad_crc_warnings val='";
			if (theApp.getViewBadCrcWarnings() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Autocab Type 0 ACK messages //
			line = "<view_autocab_t0_ack val='";
			if (theApp.getViewAutocab_t0_ack() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Autocab Type 0 BCAST messages //
			line = "<view_autocab_t0_bcast val='";
			if (theApp.getViewAutocab_t0_bcast() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Autocab Type 0 Subtpe 132 messages //
			line = "<view_autocab_t0_s132 val='";
			if (theApp.getViewAutocab_t0_s132() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);			
			
			// View Autocab Type 1 messages //
			line = "<view_autocab_t1 val='";
			if (theApp.getViewAutocab_t1() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			
			// View Autocab Type 4 Sub Type 1 messages //
			line = "<view_autocab_t4_s1 val='";
			if (theApp.getViewAutocab_t4_s1() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Autocab Type 4 Sub Type 6 messages //
			line = "<view_autocab_t4_s6 val='";
			if (theApp.getViewAutocab_t4_s6() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			// View Autocab Type 4 Sub Type 32 messages //
			line = "<view_autocab_t4_s32 val='";
			if (theApp.getViewAutocab_t4_s32() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			

			// View messages across multiple lines //
			line = "<view_multi_lines val='";
			if (theApp.getViewMultiLines() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			
			// View Links in Messages //
			line = "<view_links val='";
			if (theApp.getLinks() == true)
				line = line + "TRUE";
			else
				line = line + "FALSE";
			line = line + "'/>";
			xmlfile.write(line);
			
			//audio device
			line = "<audio_device val='" + theApp.myAudio.getMixer().getMixerInfo().getName() + "'/>";
			xmlfile.write(line);
			
			//channel
			line = "<audio_channel val='" + theApp.getChannel() + "'/>";
			xmlfile.write(line);

			// All done so close the root item //
			line = "</settings>";
			xmlfile.write(line);
			// Flush and close the file //
			xmlfile.flush();
			xmlfile.close();
		} catch (Exception e) {
			System.out.println("\nError : Unable to create the file default_settings.xml");
			System.out.println(e.toString());
		}
		return;
	}

	// Read in the default_settings.xml file //
	private void readDefaultSettings() throws SAXException, IOException,
			ParserConfigurationException {
		// Create a parser factory and use it to create a parser
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		// This is the name of the file you're parsing
		String filename = "default_settings.xml";
		// Instantiate a DefaultHandler subclass to handle events
		saxHandler handler = new saxHandler();
		// Start the parser. It reads the file and calls methods of the handler.
		parser.parse(new File(filename), handler);
	}

	// Read in the system_info.xml file //
	private void readSystemInfo() throws SAXException, IOException,
			ParserConfigurationException {
		// Create a parser factory and use it to create a parser
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		// This is the name of the file you're parsing
		String filename = "system_info.xml";
		// Instantiate a DefaultHandler subclass to handle events
		saxHandler handler = new saxHandler();
		// Start the parser. It reads the file and calls methods of the handler.
		parser.parse(new File(filename), handler);
	}

	// Set the volume indicating progress bar //
	public void updateVolumeBar(int channel, double val) {
		int pval = (int) val;
		status_bar.setVolumeBar(channel, pval);
	}

	// Close down the program //
	private void shutdown() {
		// If logging the complete the log file //
		if (theApp.logging == true)
			stopLogging();
		// If logging channel statistics then close the file //
		if (theApp.generateChannelStatistics == true)
			closeStatsFile();
		// Close the audio down //
//		theApp.Line.close();
		theApp.myAudio.line.close();
		
		// Stop the program //
		System.exit(0);
	}

	// Stop logging and write the details to the log file //
	private void stopLogging() {
		// Create a user statistics object //
		UserStatistics stats_object = new UserStatistics();
		// Get the current time & date //
		Date now = new Date();
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);
		try {
			int i, mindex;
			// Show the date and time that logging started //
			String line = "Ended logging on ";
			line = line + df.format(now);
			line = line + " at ";
			line = line + tf.format(now);
			theApp.fileWrite(line, "");
			// Display the user statistics //
			line = stats_object.totalUsers();
			theApp.fileWrite(line, "");
			// Sort the user detail into numerical order //
			stats_object.sortByMobileIdent();
			// Get the main index from the object //
			mindex = stats_object.GetIndex();
			// Show the details of each user //
			for (i = 0; i < mindex; i++) {
				line = stats_object.lineDetails(i);
				theApp.fileWrite(line, "");
			}
			theApp.file.close();
		} catch (Exception e) {
			System.out.println("\nError closing the logging file");
		}
	}

	// Allows the user to select and open a CSV file for monitoring channel
	// statistics //
	private void openCsvFile() {
		String file_name;
		// Bring up a dialog box that allows the user to select the name of the
		// saved file
		JFileChooser fc = new JFileChooser();
		// The dialog box title //
		fc.setDialogTitle("Select the channel statistics CSV file name");
		// Start in current directory
		fc.setCurrentDirectory(new File("."));
		// Don't all types of file to be selected //
		fc.setAcceptAllFileFilterUsed(false);
		// Only show .csv files //
		fc.setFileFilter(new CsvfileFilter());
		// Show save dialog; this method does not return until the dialog is
		// closed
		int returnval = fc.showSaveDialog(this);
		// If the user has selected cancel then quit
		if (returnval == JFileChooser.CANCEL_OPTION) {
			menuItemUpdate();
			return;
		}
		// Get the file name an path of the selected file
		file_name = fc.getSelectedFile().getPath();
		// Does the file name end in .csv ? //
		// If not then automatically add a .csv ending //
		int last_index = file_name.lastIndexOf(".csv");
		if (last_index != (file_name.length() - 4))
			file_name = file_name + ".csv";
		// Create a file with this name //
		File tfile = new File(file_name);
		// If the file exists ask the user if they want to overwrite it //
		if (tfile.exists()) {
			int response = JOptionPane.showConfirmDialog(null,
					"Overwrite existing file?", "Confirm Overwrite",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.CANCEL_OPTION) {
				menuItemUpdate();
				return;
			}
		}
		// Open the file
		try {
			theApp.csvfile = new FileWriter(tfile);
		} catch (Exception e) {
			System.out.println("\nError opening the channel statistics file");
		}
		theApp.generateChannelStatistics = true;
		// Write an opening line to the CSV file //
		String line = "Date,Time,Total Packets,Total Good Auriga Packets,Total Bad Auriga Packets,Percentage Bad Auriga Packets,Total Good Autocab Packets,Total Bad Autocab Packets,Percentage Bad Autocab Packets,Total Bytes Transmitted";
		theApp.csvfileWrite(line);
	}

	// Close the channel statistics CSV file //
	private void closeStatsFile() {
		try {
			theApp.csvfile.close();
		} catch (Exception e) {
			System.out.println("\nError closing the statistics file");
		}
	}

	// This class handles the SAX events
	public class saxHandler extends DefaultHandler {
		String value;

		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (qName == "company")
				theApp.sysInfo.increment();
			if (qName == "name")
				theApp.sysInfo.addSystemName(value);
			if (qName == "id")
				theApp.sysInfo.addSystemId(value);
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// Extract the element value as a string //
			String tval = new String(ch);
			value = tval.substring(start, (start + length));
		}

		// Handle an XML start element //
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// Check an element has a value //
			if (attributes.getLength() > 0) {
				// Get the elements value //
				String aval = attributes.getValue(0);
				// Decode mode //
				if (qName.equals("decode_mode")) {
					if (aval.equals("TRUE"))
						theApp.setDecodeMode(true);
					else
						theApp.setDecodeMode(false);
				}
				// Preamble hunt //
				if (qName.equals("preamble_mode")) {
					if (aval.equals("TRUE"))
						theApp.setPreambleHunt(true);
					else
						theApp.setPreambleHunt(false);
				}
				// Sync hunt //
				if (qName.equals("sync_hunt_mode")) {
					if (aval == "TRUE")
						theApp.setSyncHunt(true);
					else
						theApp.setSyncHunt(false);
				}
				// Binary mode //
				if (qName.equals("view_binary")) {
					if (aval.equals("TRUE"))
						theApp.setViewBinary(true);
					else
						theApp.setViewBinary(false);
				}
				// View special ASCII characters //
				if (qName.equals("view_special_ascii")) {
					if (aval.equals("TRUE"))
						theApp.setViewSpecialAscii(true);
					else
						theApp.setViewSpecialAscii(false);
				}
				// View packets that have failed their CRC //
				if (qName.equals("view_bad_crc")) {
					if (aval.equals("TRUE"))
						theApp.setViewBadCRC(true);
					else
						theApp.setViewBadCRC(false);
				}
				// View Links in Messages //
				if (qName.equals("view_links")) {
					if (aval.equals("TRUE"))
						theApp.setLinks(true);
					else
						theApp.setLinks(false);
				}				
				// View no Autocab messages //
				if (qName.equals("view_no_autocab")) {
					if (aval.equals("TRUE"))
						theApp.setViewNoAutocab(true);
					else
						theApp.setViewNoAutocab(false);
				}
				// View no Auriga messages //
				if (qName.equals("view_no_auriga")) {
					if (aval.equals("TRUE"))
						theApp.setViewNoAuriga(true);
					else
						theApp.setViewNoAuriga(false);
				}
				// View bad CRC warnings //
				if (qName.equals("view_bad_crc_warnings")) {
					if (aval.equals("TRUE"))
						theApp.setViewBadCrcWarnings(true);
					else
						theApp.setViewBadCrcWarnings(false);
				}
				// View Autocab Type 0 ACK messages //
				if (qName.equals("view_autocab_t0_ack")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t0_ack(true);
					else
						theApp.setViewAutocab_t0_ack(false);
				}
				// View Autocab Type 0 BCAST messages //
				if (qName.equals("view_autocab_t0_bcast")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t0_bcast(true);
					else
						theApp.setViewAutocab_t0_bcast(false);
				}		
				// View Autocab Type 0 Subtype 132 messages //
				if (qName.equals("view_autocab_t0_s132")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t0_s132(true);
					else
						theApp.setViewAutocab_t0_s132(false);
				}					
				// View Autocab Type 1 messages //
				if (qName.equals("view_autocab_t1")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t1(true);
					else
						theApp.setViewAutocab_t1(false);
				}								
				// View Autocab Type 4 Sub Type 1 messages //
				if (qName.equals("view_autocab_t4_s1")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t4_s1(true);
					else
						theApp.setViewAutocab_t4_s1(false);
				}			
				// View Autocab Type 4 Sub Type 6 messages //
				if (qName.equals("view_autocab_t4_s6")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t4_s6(true);
					else
						theApp.setViewAutocab_t4_s6(false);
				}						
				// View Autocab Type 4 Sub Type 32 messages //
				if (qName.equals("view_autocab_t4_s32")) {
					if (aval.equals("TRUE"))
						theApp.setViewAutocab_t4_s32(true);
					else
						theApp.setViewAutocab_t4_s32(false);
				}					
				
				// View messages on multiple lines //
				if (qName.equals("view_multi_lines")) {
					if (aval.equals("TRUE"))
						theApp.setViewMultiLines(true);
					else
						theApp.setViewMultiLines(false);
				}
				
				//audio device
				if (qName.equals("audio_device")){
					changeMixer(aval);
				}
					
				if (qName.equals("audio_channel")){
					theApp.setChannel(aval);
				}
			}
		}
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			BareBonesBrowserLaunch.openURL(event.getURL().toString());
		}
	}



	// Handle messages from the scrollbars
/*	class MyAdjustmentListener implements AdjustmentListener {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			// Vertical scrollbar
			if (e.getSource() == vscrollbar) {
				theApp.verticalScrollbarValue = e.getValue();
				repaint();
			}
			// Horizontal scrollbar
			if (e.getSource() == hscrollbar) {
				theApp.horizontalScrollbarValue = e.getValue();
				repaint();
			}
		}
	}*/

}
