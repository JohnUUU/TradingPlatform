package brown.platform.item.library;

import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;

import brown.user.agent.library.GSVM18Util;
import brown.user.agent.library.LSVM18Util;

public class LSVM18Item extends Item {
	private long id;
	private int seed;
	
	public LSVM18Item() {
		this.id = 0;
		this.seed = 0;
	}
	
	public LSVM18Item(long id, int seed) {
		super(Long.toString(id));
		this.seed = seed;
	}
	
	public LSVMLicense toLicense() {
		return LSVM18Util.mapIDToLSVM18License(LSVM18Util.createLSVM18Population(this.seed).get(0).getWorld()).get(this.id);
	}
}
