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

// Copyright (c) 2003, Stefan Petersen
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 
// 3. Neither the name of the author nor the names of any contributors
//    may be used to endorse or promote products derived from this 
//    software without specific prior written permission.
//   
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
// ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
// 
// This file is part of ape, Another Packet Engine

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.util.Date;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.swing.SwingUtilities;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;


public class TaxiMdtDecoder {
	//audio variables
	public AudioMixer myAudio = null;
	public static final int BUFFER_SIZE = 2048;//1024;
	private static byte[] buffer = null;//new byte[BUFFER_SIZE * NUM_CHANNELS];
	public static final int STEREO_LEFT = 0;
	public static final int STEREO_RIGHT = 1;
	public static final int STEREO_BOTH = 2;
	private static int stereoChannel = STEREO_LEFT;	//left=0, right=1;
	
	private static boolean RUNNING = true;
	
	//display variables
	private DisplayModel displayModel;
//	public DisplayView displayView;
	private static TaxiMdtDecoder theApp;
	static DisplayFrame window;
	public JEditorPane editorPane;
	public HTMLDocument doc;
	public HTMLDocument docPause;
	public Element el;

	public String program_version = "Taxi MDT Decoder V2.00 Build 45 (2010)";
	public TargetDataLine Line;
	public AudioFormat format;

	public boolean logging = false;
	public FileWriter file;
	public FileWriter csvfile;

	public int verticalScrollbarValue = 0;
	public int horizontalScrollbarValue = 0;

//	public int raw_length = 0;
//	public boolean clearScreenNow = false;
	public boolean generateChannelStatistics = false;
	public SystemInfo sysInfo = new SystemInfo();
	
	private AudioLeftChannel alc;
	private AudioRightChannel arc;

	static public String display_string[] = new String[100];
	
	public String leftChannelLabel = "[L]";
	public String rightChannelLabel = "[R]";


	public static void main(String args[]) {
		
		final TaxiMdtDecoder theApp = new TaxiMdtDecoder();
		
		theApp.prepare_variables();
		
		theApp.myAudio = new AudioMixer(theApp);
		buffer = new byte[BUFFER_SIZE * AudioMixer.NUM_CHANNELS];
		
		try {
			theApp.myAudio.setDefaultLine();
			theApp.myAudio.openLine();
			System.out.println("buffersize chosen:" + theApp.myAudio.line.getBufferSize());
			theApp.myAudio.line.start();
		} catch (Exception ex) {
		   	// Handle the error.
		    System.out.println("Error starting line:" + ex.getMessage());
		}
		
		float sampleRate = theApp.myAudio.format.getSampleRate();
//		System.out.println("Sample rate:" + sampleRate);
		float T = buffer.length / theApp.myAudio.format.getFrameRate();
//		System.out.println("Length of sampled sound in sec:" + T );
		int n = (int)(T * sampleRate) / 2;
//		System.out.println("equidistant points:" + n);
		float h = (T/n);
//		System.out.println("Length of time interval in second:" + h);
		
		theApp.alc = new AudioLeftChannel(theApp);
		theApp.arc = new AudioRightChannel(theApp);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				theApp.createGUI();
			}
		});
		
		while(RUNNING){
			int myBytes = theApp.grab_audio();
			
//			System.out.println("Rcvd:" + myBytes);
			byte[] x = new byte[BUFFER_SIZE/2 * AudioMixer.NUM_CHANNELS];
			byte[] y = new byte[BUFFER_SIZE/2 * AudioMixer.NUM_CHANNELS];
			int z =0;
			for (int i=0;i<myBytes; i++){
				//even = 0 (left), odd = 1 (right)
				//split the audio into left and right channels 
				if (( i % 2) == 0){
					//left
					y[z] = buffer[i];
				}else{
					//right
					x[z] = buffer[i];
					z++;
				}
			}

			if ((stereoChannel == STEREO_LEFT)||(stereoChannel == STEREO_BOTH)){
				//left channel
				theApp.alc.processAudio(myBytes/2, y);
				if (theApp.generateChannelStatistics == true) {
					long nextTime, currentTime;
					ChannelStatistics channelStatsObject = new ChannelStatistics();
					channelStatsObject.clear();
					Date dateNow = new Date();
					
					currentTime = dateNow.getTime();
					nextTime = channelStatsObject.getCurrentTime();
					if (currentTime >= nextTime)
						theApp.writeChannelStats();
				}
			}
			
			if ((stereoChannel == STEREO_RIGHT)||(stereoChannel == STEREO_BOTH)){
				//the right channel
				theApp.arc.processAudio(myBytes/2, x);
				if (theApp.generateChannelStatistics == true) {
					long nextTime, currentTime;
					ChannelStatistics channelStatsObject = new ChannelStatistics();
					channelStatsObject.clear();
					Date dateNow = new Date();
					
					currentTime = dateNow.getTime();
					nextTime = channelStatsObject.getCurrentTime();
					if (currentTime >= nextTime)
						theApp.writeChannelStats();
				}
			}
		}

	}
	
	// Setup the window //
	public void createGUI() {
		window = new DisplayFrame(program_version, this);
		Toolkit theKit = window.getToolkit();
		Dimension wndsize = theKit.getScreenSize();
		window.setBounds(wndsize.width / 6, wndsize.height / 6,
				2 * wndsize.width / 3, 2 * wndsize.height / 3);
		window.addWindowListener(new WindowHandler());
		displayModel = new DisplayModel();
//		displayView = new DisplayView(this);
		editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.setText("<html><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"></table></html>");
		editorPane.addHyperlinkListener(window);
	        doc = (HTMLDocument) editorPane.getDocument();
		docPause = new HTMLDocument(doc.getStyleSheet());
		docPause.setParser(doc.getParser());
		el = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.TABLE);
		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		displayModel.addObserver(displayView);
//		window.getContentPane().add(displayView, BorderLayout.CENTER);
		window.getContentPane().add(scrollPane, BorderLayout.CENTER);
		window.setVisible(true);
	}

	public void addLine(String line, int channel) {
		String prefix = (channel == 0)? leftChannelLabel : rightChannelLabel;
		int i;

		// If we are logging to disk then record this line
		if (logging == true)
			fileWrite(line, prefix);

		line = line.replace("<","&lt;");
		line = line.replace(">","&gt;");

		try {
			doc.insertAfterStart(el,"<tr>" + prefix + line + "</tr>");
			}
		catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
			}
		
	}

	public void addArray(String dline[], int channel) {
		int a;
		String prefix = (channel == 0)? leftChannelLabel : rightChannelLabel;
		int size=dline.length;
		String s = "";
		String t = "";
		// Display the lines //
		for (a = 0; a < size; a++) {
			if (dline[a] != null) {
				t = dline[a].replace("<","&lt;");
				t = t.replace(">","&gt;");
				t = t.replace("Þ","<");
				t = t.replace("ß",">");
				s = s + "<tr>" + prefix + t + "</tr>";
//				s = s + prefix + t + "<br>";
				}
		}
		try {
			doc.insertAfterStart(el,s);
//			doc.insertAfterStart(el,"<tr>" + s + "</tr>");
			}
		catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
			}

		// Log the lines //
		for (a = 0; a < size; a++) {
			if (dline[a] != null)
				fileWrite(dline[a], prefix);
		}

	}

	public void clearScreen() {
		try {
			doc.setInnerHTML(el,"<br>");
			}
		catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
			}
	}


	public void pause(boolean paused) throws BadLocationException {
		String s;
		if (paused == true) {
//			try {
//				docPause.setOuterHTML(docPause.getDefaultRootElement(),doc.getDefaultRootElement());
				docPause.replace(0,docPause.getLength(),doc.getText(0,doc.getLength()),null);
//			} catch (java.io.IOException ie) {}
			editorPane.setDocument(docPause);
		} else {
			editorPane.setDocument(doc);	
		}
	}

	// Get 1102 bytes of audio from the soundcard
	public int grab_audio() {
		int count = 0;
		int total_count = 0;
		
		//check the line is open before grabbing data
		if (myAudio.line.isOpen() == false){
			return 0;
		}
		try {
			while (total_count <= buffer.length-1) {
				//check the line is still open
				if (myAudio.line.isOpen()==false){
					return 0;
				}
				count = myAudio.line.read(buffer, 0, buffer.length);
				total_count = total_count + count;
			}
		
		}catch (IllegalArgumentException ie){
			System.out.println("IllegalArgError:" + ie.getMessage());		
		}catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		return total_count;
	}
	
	public void setChannel(int chan){
		stereoChannel = chan;
		//quick hack to minimise the vol bars for nice effect
		this.updateVolumeBar(STEREO_LEFT, 0);
		this.updateVolumeBar(STEREO_RIGHT, 0);
	}
	
	/**
	 * Set the channel from a description (read from xml file)
	 * @param chanDescription
	 */
	public void setChannel(String chanDescription){
		if(chanDescription.equals("RIGHT")){
			setChannel(STEREO_RIGHT);
		}else if(chanDescription.equals("BOTH")){
			setChannel(STEREO_BOTH);
		}else{
			setChannel(STEREO_LEFT);	//default to left
		}
	}
	
	/**
	 * @return
	 */
	public int getChannelAsInt(){
		return stereoChannel;
	}
	
	/**
	 * Get the stereo channel as a string (for writing to xml)
	 * @return
	 */
	public String getChannel(){
		switch(stereoChannel){
		case(STEREO_LEFT):
			return "LEFT";
		case(STEREO_RIGHT):
			return "RIGHT";
		case(STEREO_BOTH):
			return "BOTH";
		default:
			return "LEFT";
		}
	}
	
	// Setup various variables
	public void prepare_variables() {
		// Clear the user statistics main index //
		UserStatistics stats_object = new UserStatistics();
		stats_object.clearIndex();
	}
	
	// Write to a string to the logging file
	public boolean fileWrite(String fline, String prefix) {
		// Add a CR to the end of each line
		fline = prefix + " " + fline + "\r\n";
		// If we aren't logging don't try to do anything
		if (logging == false)
			return false;
		try {
			file.write(fline);
			file.flush();
		} catch (Exception e) {
			// Stop logging as we have a problem
			logging = false;
			System.out.println("\nError writing to the logging file");
			return false;
		}
		return true;
	}

	// Write to a string to the CSV file
	public boolean csvfileWrite(String fline) {
		// Add a CR to the end of each line
		fline = fline + "\r\n";
		// If we aren't measuring channel statistics don't try to do anything
		if (generateChannelStatistics == false)
			return false;
		try {
			csvfile.write(fline);
			csvfile.flush();
		} catch (Exception e) {
			// Stop as we have a problem
			generateChannelStatistics = false;
			System.out.println("\nError writing to the CSV file");
			return false;
		}
		return true;
	}

	// Writes the channel statistics to a CSV file //
	public void writeChannelStats() {
		String rline;
		// Create a channel statistics object //
		ChannelStatistics channel_stats_object = new ChannelStatistics();
		rline = channel_stats_object.get_report_line();
		// Write this to a file //
		csvfileWrite(rline);
		// Clear the statistics object //
		channel_stats_object.clear();
	}

	public DisplayFrame getWindow() {
		return window;
	}

	public DisplayModel getModel() {
		return displayModel;
	}

/*	public DisplayView getView() {
		return displayView;
	}*/
	
	public void updateVolumeBar(int channel, double val){
		if (window != null) window.updateVolumeBar(channel, val);
	}
	
	/**
	 * Update the labels for the stereo channels
	 * @param left
	 * @param right
	 */
	public void updateChannelLabels(String left, String right){
		leftChannelLabel = left;
		rightChannelLabel = right;
	}
	
	public static void quit(){
		RUNNING = false;
		System.exit(0);
	}
	
	/**
	 * @param b
	 */
	public void setPreambleHunt(boolean b){
		alc.preambleHunt = b;
		arc.preambleHunt = b;
	}
	public boolean getPreambleHunt(){
		return alc.preambleHunt;
	}
	
	/**
	 * @param b
	 */
	public void setSyncHunt(boolean b){
		alc.syncHunt = b;
		arc.syncHunt = b;
	}
	public boolean getSyncHunt(){
		return alc.syncHunt;
	}
	
	/**
	 * @param b
	 */
	public void setDecodeMode(boolean b){
		alc.decodeMode = b;
		arc.decodeMode = b;
	}
	public boolean getDecodeMode(){
		return alc.decodeMode;
	}
	
	/**
	 * @param b
	 */
	public void setViewBadCRC(boolean b){
		alc.packetDecoder.view_bad_crc = b;
		arc.packetDecoder.view_bad_crc = b;
	}
	public boolean getViewBadCRC(){
		return alc.packetDecoder.view_bad_crc;
	}
	
	/**
	 * @param b
	 */
	public void setViewBinary(boolean b){
		alc.packetDecoder.view_binary = b;
		arc.packetDecoder.view_binary = b;
	}
	public boolean getViewBinary(){
		return alc.packetDecoder.view_binary;
	}
	
	/**
	 * @param b
	 */
	public void setViewSpecialAscii(boolean b){
		alc.packetDecoder.view_special_ascii = b;
		arc.packetDecoder.view_special_ascii = b;
	}
	public boolean getViewSpecialAscii(){
		return alc.packetDecoder.view_special_ascii;
	}
	
	/**
	 * @param b
	 */
	public void setViewMultiLines(boolean b){
		alc.packetDecoder.view_multi_lines = b;
		arc.packetDecoder.view_multi_lines = b;
	}
	public boolean getViewMultiLines(){
		return alc.packetDecoder.view_multi_lines;
	}
	
	/**
	 * @param b
	 */
	public void setViewNoAutocab(boolean b){
		alc.packetDecoder.view_no_autocab = b;
		arc.packetDecoder.view_no_autocab = b;
	}
	public boolean getViewNoAutocab(){
		return alc.packetDecoder.view_no_autocab;
	}
	
	/**
	 * @param b
	 */
	public void setViewNoAuriga(boolean b){
		alc.packetDecoder.view_no_auriga = b;
		arc.packetDecoder.view_no_auriga = b;
	}
	public boolean getViewNoAuriga(){
		return alc.packetDecoder.view_no_auriga;
	}
	
	/**
	 * @param b
	 */
	public void setViewBadCrcWarnings(boolean b){
		alc.packetDecoder.view_bad_crc_warnings = b;
		arc.packetDecoder.view_bad_crc_warnings = b;
	}
	public boolean getViewBadCrcWarnings(){
		return alc.packetDecoder.view_bad_crc_warnings;
	}
	
	/**
	 * @param b
	 */
	public void setViewAutocab_t0_ack(boolean b){
		alc.packetDecoder.view_autocab_t0_ack = b;
		arc.packetDecoder.view_autocab_t0_ack = b;
	}
	public boolean getViewAutocab_t0_ack(){
		return alc.packetDecoder.view_autocab_t0_ack;
	}

	/**
	 * @param b
	 */
	public void setViewAutocab_t0_bcast(boolean b){
		alc.packetDecoder.view_autocab_t0_bcast = b;
		arc.packetDecoder.view_autocab_t0_bcast = b;
	}
	public boolean getViewAutocab_t0_bcast(){
		return alc.packetDecoder.view_autocab_t0_bcast;
	}
	
	/**
	 * @param b
	 */
	public void setViewAutocab_t0_s132(boolean b){
		alc.packetDecoder.view_autocab_t0_s132 = b;
		arc.packetDecoder.view_autocab_t0_s132 = b;
	}
	public boolean getViewAutocab_t0_s132(){
		return alc.packetDecoder.view_autocab_t0_s132;
	}
	
	/**
	 * @param b
	 */
	public void setViewAutocab_t1(boolean b){
		alc.packetDecoder.view_autocab_t1 = b;
		arc.packetDecoder.view_autocab_t1 = b;
	}
	public boolean getViewAutocab_t1(){
		return alc.packetDecoder.view_autocab_t1;
	}
	
	/**
	 * @param b
	 */
	public void setViewAutocab_t4_s1(boolean b){
		alc.packetDecoder.view_autocab_t4_s1 = b;
		arc.packetDecoder.view_autocab_t4_s1 = b;
	}
	public boolean getViewAutocab_t4_s1(){
		return alc.packetDecoder.view_autocab_t4_s1;
	}
	
	/**
	 * @param b
	 */
	public void setViewAutocab_t4_s6(boolean b){
		alc.packetDecoder.view_autocab_t4_s6 = b;
		arc.packetDecoder.view_autocab_t4_s6 = b;
	}
	public boolean getViewAutocab_t4_s6(){
		return alc.packetDecoder.view_autocab_t4_s6;
	}
	
	/**
	 * @param b
	 */
	public void setViewAutocab_t4_s32(boolean b){
		alc.packetDecoder.view_autocab_t4_s32 = b;
		arc.packetDecoder.view_autocab_t4_s32 = b;
	}
	public boolean getViewAutocab_t4_s32(){
		return alc.packetDecoder.view_autocab_t4_s32;
	}
	
	/**
	 * @param b
	 */
	public void setLinks(boolean b){
		alc.packetDecoder.view_links = b;
		arc.packetDecoder.view_links = b;
	}
	public boolean getLinks(){
		return alc.packetDecoder.view_links;
	}
	
	class WindowHandler extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
		}
	}

}
