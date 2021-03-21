package brown.auction.value.distribution.library;

import java.util.List;

import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorldSetup;

import brown.auction.value.distribution.IValuationDistribution;
import brown.auction.value.generator.IValuationGenerator;
import brown.auction.value.valuation.ISpecificValuation;
import brown.auction.value.valuation.library.LSVM18Valuation;
import brown.auction.value.valuation.library.LSVM18Valuation;
import brown.platform.item.ICart;
import brown.user.agent.library.LSVM18Util;

public class LSVM18ValuationDistribution extends AbsValuationDistribution implements IValuationDistribution {
	private int seed;
	private long populationID;
	
	/**
	 * For kryo DO NOT USE
	 */
	public LSVM18ValuationDistribution() {
		super(null, null);
		this.seed = 0;
		this.populationID = LSVM18Util.createLSVM18Population(this.seed).get(0).getPopulation();
	}

	public LSVM18ValuationDistribution(ICart items, List<IValuationGenerator> generators) {
		super(items, generators);
		this.seed = (int)(System.currentTimeMillis() % 10000);
		this.populationID = LSVM18Util.createLSVM18Population(this.seed).get(0).getPopulation();
	}

	@Override
	public ISpecificValuation sample(Integer agentID, List<List<Integer>> agentGroups) {
		int index = 0;
		for (List<Integer> group : agentGroups) {
			int j = group.indexOf(agentID);
			if (j != -1) {
				index = j;
				break;
			}
		}
		return new LSVM18Valuation(this.seed, index, this.populationID, agentID);
	}

}
