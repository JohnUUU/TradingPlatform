package brown.user.agent.library;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import brown.communication.messages.ITradeMessage;
import brown.user.agent.IAgent;

public class Tier1LSVM18Agent extends AbsLSVM18Agent implements IAgent {
	public Tier1LSVM18Agent(String name) {
		super(name);
	}

	@Override
	protected void onAuctionStart() {
	}
	
	@Override
	protected void onAuctionEnd(Map<Integer, Set<String>> allocations, Map<Integer, Double> payments,
			List<List<ITradeMessage>> tradeHistory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Map<String, Double> getBids(Map<String, Double> minBids) {
		Map<String, Double> bids = new HashMap<>();
		Map<String, Double> vals = new HashMap<>();
		for (String good : minBids.keySet()) {
			vals.put(good, this.getValuation(good));
		}
		
		for (String good : minBids.keySet()) {
			if (vals.get(good) >= minBids.get(good)) {
				bids.put(good, minBids.get(good));
			}
		}
		
		return bids;
	}

}
