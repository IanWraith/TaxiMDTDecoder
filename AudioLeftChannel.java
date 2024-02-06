

/**
 * @author Andy
 * Implementation of the abstract class for the left channel
 */
public class AudioLeftChannel extends AudioChannel {

	public AudioLeftChannel(TaxiMdtDecoder theApp){
		super(theApp);
		this.whichChannel = TaxiMdtDecoder.STEREO_LEFT;
	}
}
