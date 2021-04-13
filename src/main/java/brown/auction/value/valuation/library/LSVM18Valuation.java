package brown.auction.value.valuation.library;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.util.file.FilePathUtils;
import org.spectrumauctions.sats.opt.model.lsvm.demandquery.LSVM_DemandQueryMIP;
import org.spectrumauctions.sats.opt.model.lsvm.demandquery.LSVM_DemandQueryMipResult;

import brown.auction.value.valuation.ISpecificValuation;
import brown.platform.item.ICart;
import brown.platform.item.IItem;
import brown.platform.item.library.PricedItem;
import brown.user.agent.library.LSVM18Util;

public class LSVM18Valuation implements ISpecificValuation {
	private int seed;
	private int index;
	private long populationID;
	private Set<String> dqResult;
	
	public LSVM18Valuation() {
		this.seed = 0;
		this.index = 0;
		this.populationID = 0;
		this.dqResult = new HashSet<>();
	}
	
	public LSVM18Valuation(int seed, int index, long populationID, int agentID) {
		this.seed = seed;
		this.index = index;
		this.populationID = populationID;
		this.dqResult = new HashSet<>();
	}
	
	private LSVMBidder getBidder() {
		LSVMBidder bidder;
		try {
			bidder = LSVM18Util.restoreLSVM18Population(this.populationID).get(this.index);
		} catch (Exception e) {
			bidder = LSVM18Util.createLSVM18Population(this.seed).get(this.index);
		}
		return bidder;
	}
	
	@Override
	public Double getValuation(ICart cart) {
		if (cart.getItemByName("position") != null) {
			return new Integer(this.index).doubleValue();
		}
		
		LSVMBidder bidder = getBidder();
		
		Map<Long, LSVMLicense> allGoods = LSVM18Util.mapIDToLSVM18License(bidder.getWorld());
		
		if (cart.getItemByName("demand_query") != null) {
			if (cart.getItemByName("reset") != null) {
				this.dqResult.clear();
				Map<LSVMLicense, BigDecimal> prices = new HashMap<>();
				for (LSVMLicense license : allGoods.values()) {
					String name = LSVM18Util.LSVM_ID_TO_ITEM.get(license.getId());
					if (cart.containsItem(name)) {
						prices.put(license, new BigDecimal(((PricedItem)cart.getItemByName(name)).getPrice()));
					}
				}
				LSVM_DemandQueryMipResult result = new LSVM_DemandQueryMIP(bidder, prices).getResult();
				result.getResultingBundle().getLicenses().forEach(license -> this.dqResult.add(LSVM18Util.LSVM_ID_TO_ITEM.get(license.getId())));
				return null;
			}
			for (IItem item : cart.getItems()) {
				if (item.getName().equals("demand_query") || item.getName().equals("reset")) {
					continue;
				}
				
				return this.dqResult.contains(item.getName()) ? 1.0 : -1.0;
			}
		}
		
		Bundle<LSVMLicense> bundle = new Bundle<>();
		for (IItem good : cart.getItems()) {
			bundle.add(allGoods.get(LSVM18Util.ITEM_TO_LSVM_ID.get(good.getName())));
		}
		return bidder.calculateValue(bundle).doubleValue();
	}

}
