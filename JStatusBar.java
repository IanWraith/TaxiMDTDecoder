import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

// A status bar class //
public class JStatusBar extends JPanel {
	public static final long serialVersionUID = 1;
	private TaxiMdtDecoder theApp;
	
	private JPanel jStatusBar1 = new JPanel();
	private JPanel jStatusBarLeftPanel = new JPanel();
	private JPanel jStatusBarRightPanel = new JPanel();
	private JPanel jStatusBar2 = new JPanel();
//	private JPanel jStatusBar3 = new JPanel();
	private JLabel decodeMode = new JLabel();
	private JLabel logMode = new JLabel();
	private JLabel statusBar3 = new JLabel();
	private JProgressBar volumeBarLeft = new JProgressBar(0, 10);
	private JProgressBar volumeBarRight = new JProgressBar(0, 10);

	private JButton setLabelsButton = new JButton("?");
	private JTextField leftChannelText = new JTextField("[L]");
	private JTextField rightChannelText = new JTextField("[R]");
	private JToggleButton pauseButton = new JToggleButton("Pause");
	private JButton clearButton = new JButton("Clear");
	
	// Border style definitions
//	private Border raisedbevel = BorderFactory.createRaisedBevelBorder();
	private Border loweredbevel = BorderFactory.createLoweredBevelBorder();
//	private Border compound = BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);

	public JStatusBar() {
		jStatusBar1.setLayout(new BorderLayout());
		jStatusBarLeftPanel.setLayout(new BorderLayout());
		jStatusBarLeftPanel.setBorder(loweredbevel);
		jStatusBarRightPanel.setLayout(new BorderLayout());
		jStatusBar2.setLayout(new BorderLayout());
		jStatusBar2.setBorder(loweredbevel);
//		jStatusBar3.setLayout(borderLayout3);
//		jStatusBar3.setBorder(loweredbevel);
		logMode.setHorizontalAlignment(SwingConstants.LEFT);
		statusBar3.setHorizontalAlignment(SwingConstants.LEFT);
		jStatusBar1.add(jStatusBarLeftPanel, BorderLayout.CENTER);
		jStatusBar1.add(jStatusBarRightPanel, BorderLayout.EAST);
		jStatusBarLeftPanel.add(decodeMode, BorderLayout.CENTER);
		jStatusBarRightPanel.add(jStatusBar2, BorderLayout.CENTER);
//		jStatusBarRightPanel.add(jStatusBar3, BorderLayout.EAST);
		jStatusBar2.add(logMode, BorderLayout.SOUTH);
//		jStatusBar3.add(statusBar3, BorderLayout.SOUTH);
		jStatusBar1.updateUI();
		jStatusBar2.updateUI();
//		jStatusBar3.updateUI();
		decodeMode.updateUI();
		logMode.updateUI();
		statusBar3.updateUI();
		// Give the volume progress bar a border //
		volumeBarLeft.setBorder(loweredbevel);
		volumeBarRight.setBorder(loweredbevel);
		
		//set the size of the button and channel labels
		setLabelsButton.setPreferredSize(new Dimension(10,10));
		leftChannelText.setPreferredSize(new Dimension(60,18));
		rightChannelText.setPreferredSize(new Dimension(60,18));
		pauseButton.setPreferredSize(new Dimension(80,18));
		clearButton.setPreferredSize(new Dimension(80,18));
		
		//add an action event for the button to show/hide the label boxes
		setLabelsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (leftChannelText.isVisible()){
					//hide the labels
					leftChannelText.setVisible(false);
					rightChannelText.setVisible(false);
					//set the label text in the display
					theApp.updateChannelLabels(leftChannelText.getText(), rightChannelText.getText());
				}else{
					//show the labels
					leftChannelText.setVisible(true);
					rightChannelText.setVisible(true);
				}
				//redraw
				jStatusBar2.updateUI();
			}
		});

		pauseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			try {
				theApp.pause(pauseButton.isSelected());
			}
			catch (javax.swing.text.BadLocationException x) {
			}
			}
		});

		clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			theApp.clearScreen();
			}
		});

		
		// Ensure the elements of the status bar are displayed from the left
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(jStatusBar1, BorderLayout.CENTER);
		this.add(setLabelsButton, BorderLayout.CENTER);
		this.add(leftChannelText, BorderLayout.CENTER);
		this.add(volumeBarLeft, BorderLayout.CENTER);
		this.add(rightChannelText, BorderLayout.CENTER);
		this.add(volumeBarRight, BorderLayout.CENTER);
		this.add(pauseButton, BorderLayout.CENTER);
		this.add(clearButton, BorderLayout.CENTER);
		
		//initially set the channel labels to hidden
		leftChannelText.setVisible(false);
		rightChannelText.setVisible(false);
	}
	
	/**
	 * Reference the main app for setting of the labels
	 * @param x
	 */
	public void setTheApp(TaxiMdtDecoder x){
		this.theApp = x;
	}
	
	/**
	 * @param text
	 */
	public void setDecodeStatus(String text) {
		decodeMode.setText(text);
	}

	/**
	 * @param text
	 */
	public void setLoggingStatus(String text) {
		logMode.setText(text);
	}

	public void setText3(String text) {
		statusBar3.setText(text);
	}

	/**
	 * Set the level displayed by the progress meter
	 * @param channel
	 * @param val
	 */
	public void setVolumeBar(int channel, int val) {
		JProgressBar thisVolumeBar;
		
		if (channel == TaxiMdtDecoder.STEREO_LEFT){
			thisVolumeBar = volumeBarLeft;
		}else{
			thisVolumeBar = volumeBarRight;
		}

		if (val<2){
			//0<->2 so set to YELLOW
			thisVolumeBar.setForeground(Color.yellow);
		}else if((val>2)&&(val<7)){
			//2<->7 so set to GREEN
			thisVolumeBar.setForeground(Color.green);
		}else if ((val>7)&&(val<10)){
			//7<->10 so set to RED
			thisVolumeBar.setForeground(Color.red);
		}else{
			//greater 10 reset vol bar to 10
			val = 10;
		}
		
/*		
		// If yellow the volume is to low //
		if (val < 2)
			thisVolumeBar.setForeground(Color.yellow);
		// Green is OK //
		if ((val > 1) && (val < 7))
			thisVolumeBar.setForeground(Color.green);
		// Red is to loud //
		if (val > 7)
			thisVolumeBar.setForeground(Color.red);
		// Make sure the value can't go above 10 //
		if (val > 10)
			val = 10;
*/
		
		// Set the class value //
		thisVolumeBar.setValue(val);
	}

}
