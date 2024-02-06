

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 * @author Andy
 * A wrapper class for switching between audio inputs (mixers)
 *
 */
class AudioMixer{
	private TaxiMdtDecoder theApp;
	public String description;
	public Mixer mixer;
	public TargetDataLine line;
	public Line.Info lineInfo;
	public AudioFormat format = null;
	public static final int NUM_CHANNELS = 2;
	
	public AudioMixer(TaxiMdtDecoder theApp){
		this.theApp = theApp;
		format = setAudioFormat();		
	}
	
	public AudioMixer(TaxiMdtDecoder theApp, String x, Mixer m, Line.Info l){
		this.theApp = theApp;
		this.description = x;
		this.mixer = m;
		this.lineInfo = l;
		format = setAudioFormat();
	}
	
	/**
	 * Setters and getters
	 */
	public Mixer getMixer() {
		return mixer;
	}

	public void setMixer(Mixer mixer) {
		this.mixer = mixer;
	}
	
	public TargetDataLine getLine() {
		return line;
	}

	public void setLine(TargetDataLine line) {
		this.line = line;
	}
	
	/**
	 * Set the audio format required
	 * @return
	 */
	private AudioFormat setAudioFormat(){
		return new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,	//encoding
				10800.0F, //11025.0F,							//sample rate (10800) - 44100 shows less resolution?
				8,									//sample size in bits
				NUM_CHANNELS,						//number of channels (stereo=2)
				2,									//framesize
				10800.F,							//frame reate
				true);								//big endian
	}
	
	/**
	 * Set the default line for the default mixer
	 */
	public void setDefaultLine(){
	    Mixer mx = AudioSystem.getMixer(null);	//default mixer
	    this.setMixer(mx);
		DataLine.Info info = getDataLineInfo();
		
		try{
			this.line = (TargetDataLine) AudioSystem.getLine(info);
		}catch(LineUnavailableException ex){
			System.out.println("Line Unavailable");
		}
	}
	
	
	/**
	 * Get the DataLine.info object for the TargetDataLine 
	 * @return
	 */
	private DataLine.Info getDataLineInfo(){
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, this.format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error.
			System.out.println("Error in AudioSystem");
		}
		
		return info;
	}
	
	/**
	 * Gets a data line for the specified mixer
	 * @param mix
	 * @return
	 */
	public Line getDataLineForMixer(){
		TargetDataLine line = null;
		try {
			line = (TargetDataLine) this.mixer.getLine(getDataLineInfo());
		} catch (LineUnavailableException e) {
			System.out.println("Error getting mix line:" + e.getMessage());
		}
		
		return line;
	}
	
	/**
	 * Open the current line
	 */
	public void openLine(){
		try {
			this.line.open(format);
		} catch (LineUnavailableException e) {
			System.out.println("Unable to open line:" + e.getMessage());
		}
	}
	
	/**
	 * Change the mixer and restart the TargetDataLine
	 * @param mixerName
	 */
	public void changeMixer(String mixerName){
		//stop current line
	    this.line.stop();
		this.line.close();
	    this.line.flush();
	    
	    //set the new mixer and line
	    Mixer mx = AudioSystem.getMixer(getMixerInfo(mixerName));
	    this.setMixer(mx);
	    this.line = (TargetDataLine) getDataLineForMixer();
	    
	    //restart
	    openLine();
	    System.out.println("LineInfo:[" + mixerName + "] " + theApp.myAudio.line.getLineInfo());
	    this.line.start();
	}
	
	/**
	 * Get the MixerInfo based on the mixer name
	 * @param mixerName
	 * @return
	 */
	private Mixer.Info getMixerInfo(String mixerName){
		Mixer.Info mixers[] = AudioSystem.getMixerInfo();
		
		//iterate the mixers and display TargetLines
		for (int i=0; i< mixers.length; i++){
			Mixer m = AudioSystem.getMixer(mixers[i]);
			if (m.getMixerInfo().getName().equals(mixerName)){
				return m.getMixerInfo();
			}
		}
		
		//if no mixer found, returns null which is the default mixer on the machine
		return null;
	}
	
	
}
