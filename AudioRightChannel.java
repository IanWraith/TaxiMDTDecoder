

/**
 * @author Andy
 * Implementation of the abstract class for the right channel
 */
public class AudioRightChannel extends AudioChannel {
	
	public AudioRightChannel(TaxiMdtDecoder theApp){
		super(theApp);
		this.whichChannel = TaxiMdtDecoder.STEREO_RIGHT;
	}
}
