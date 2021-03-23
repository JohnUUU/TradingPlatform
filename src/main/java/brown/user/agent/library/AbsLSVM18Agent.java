package brown.user.agent.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import brown.auction.marketstate.IMarketPublicState;
import brown.auction.rules.activity.GSVM18_SMRAActivity;
import brown.auction.rules.activity.LSVM18_SMRAActivity;
import brown.auction.value.valuation.IGeneralValuation;
import brown.communication.bid.IBidBundle;
import brown.communication.bid.library.OneSidedBidBundle;
import brown.communication.messages.IInformationMessage;
import brown.communication.messages.ISimulationReportMessage;
import brown.communication.messages.ITradeMessage;
import brown.communication.messages.ITradeRequestMessage;
import brown.communication.messages.IValuationMessage;
import brown.communication.messages.library.TradeMessage;
import brown.platform.accounting.IAccountUpdate;
import brown.platform.item.ICart;
import brown.platform.item.IItem;
import brown.platform.item.library.Cart;
import brown.platform.item.library.Item;
import brown.user.agent.IAgent;

public abstract class AbsLSVM18Agent extends AbsAgent implements IAgent {
	protected static final double EPSILON = GSVM18_SMRAActivity.EPSILON;
	private IGeneralValuation valuation;
	private Map<String, Double> baseValues;
	private int round;
	private Integer auctionID;
	private Set<String> allocation;
	private List<Set<String>> allBids;
	private List<Map<String, Double>> allReserves;
	private Map<Integer, Integer> privateToPublic; // definitely needs fixing next year. server not sanitizing.

	public AbsLSVM18Agent(String name) {
		super(name);
		this.valuation = null;
		this.auctionID = null;
		this.baseValues = null;
		this.round = 0;
		this.name = name;
		this.allocation = new HashSet<>();
		this.allBids = new LinkedList<>();
		this.allReserves = new LinkedList<>();
		this.privateToPublic = new HashMap<>();
	}

	@Override
	public void onInformationMessage(IInformationMessage informationMessage) {
		// noop
	}

	@Override
	public void onTradeRequestMessage(ITradeRequestMessage tradeRequestMessage) {
		synchronized (this) {
			this.auctionID = tradeRequestMessage.getAuctionID();

			// set minBids
			Map<String, Double> reserves = new HashMap<>(tradeRequestMessage.getState().getReserves());
			reserves.remove("demand_query");
			reserves.remove("reset");
			reserves.remove("position");
			
			this.parseAllocation(tradeRequestMessage.getState());
			
			this.allReserves.add(reserves);

			Map<String, Double> minBids = Collections.unmodifiableMap(reserves);
			
			Map<String, Double> bids = this.getBids(minBids);
			
			IBidBundle bundle = this.createBidBundle(bids);
			this.agentBackend
					.sendMessage(new TradeMessage(0, this.agentBackend.getPrivateID(), this.auctionID, bundle));

			this.allBids.add(bids.keySet());

			this.round++;
		}
	}

	protected abstract void onAuctionStart();

	protected abstract Map<String, Double> getBids(Map<String, Double> minBids);
	
	protected abstract void onAuctionEnd(Map<Integer, Set<String>> allocations, Map<Integer, Double> payments,
			List<List<ITradeMessage>> tradeHistory);

	@Override
	public void onValuationMessage(IValuationMessage valuationMessage) {
		synchronized (this) {
			this.valuation = valuationMessage.getValuation();
			
			this.baseValues = new HashMap<>();
			for (String s : LSVM18Util.ITEM_TO_LSVM_ID.keySet()) {
				// get position
				ICart cart;
				cart = new Cart();
				cart.addToCart(new Item(s));
				double v = this.valuation.getValuation(cart);
				if (v > 0) {
					this.baseValues.put(s, v);
				}
			}

			this.allBids.clear();
			this.allReserves.clear();
			this.allocation.clear();
			this.round = 0;

			this.onAuctionStart();
		}
	}
	
	private List<List<ITradeMessage>> sanitize(Map<Integer, Set<String>> allocations, Map<Integer, Double> payments,
			List<List<ITradeMessage>> tradeHistory) {
		Map<Integer, Integer> p = new HashMap<>(this.privateToPublic);
		Map<Integer, Integer> r = new HashMap<>();
		for (List<ITradeMessage> msgs : tradeHistory) {
			for (ITradeMessage msg : msgs) {
				if (!p.containsKey(msg.getAgentID())) {
					p.put(msg.getAgentID(), p.size());
					r.put(r.size(), msg.getAgentID());
				}
			}
		}
		
		if (r.containsKey(this.agentBackend.getPublicID())) {
			Integer pr = r.get(this.agentBackend.getPublicID());
			p.put(pr, p.size());
			p.put(this.agentBackend.getPrivateID(), this.agentBackend.getPublicID());
		}
		
		this.privateToPublic = p;
		
		List<List<ITradeMessage>> hist = new ArrayList<>(tradeHistory.size());
		for (List<ITradeMessage> msgs : tradeHistory) {
			hist.add(new ArrayList<>(msgs.size()));
			for (ITradeMessage msg : msgs) {
				if (p.containsKey(msg.getAgentID())) {
					hist.get(hist.size() - 1).add(new TradeMessage(msg.getMessageID(), p.get(msg.getAgentID()), msg.getCorrespondingMessageID(), msg.getAuctionID(), msg.getBid()));
				}
			}
		} 
		
		for (Integer i : new ArrayList<Integer>(allocations.keySet())) {
			allocations.put(p.get(i), allocations.get(i));
			allocations.remove(i);
		}
		
		for (Integer i : new ArrayList<Integer>(payments.keySet())) {
			payments.put(p.get(i), payments.get(i));
			payments.remove(i);
		}
		
		return hist;
	}

	@Override
	public void onSimulationReportMessage(ISimulationReportMessage simReportMessage) {
		synchronized (this) {
			Map<Integer, Set<String>> alloc = new HashMap<>();
			Map<Integer, Double> payments = new HashMap<>();

			IMarketPublicState state = simReportMessage.getMarketResults().get(this.auctionID);
			for (Map.Entry<Integer, List<ICart>> ent : state.getAllocation().entrySet()) {
				alloc.putIfAbsent(ent.getKey(), new HashSet<>());
				for (ICart cart : ent.getValue()) {
					for (IItem item : cart.getItems()) {
						alloc.get(ent.getKey()).add(item.getName());
					}
				}
			}
			this.parseAllocation(state);

			for (IAccountUpdate upd : state.getPayments()) {
				payments.putIfAbsent(upd.getTo(), 0.0);
				payments.put(upd.getTo(), payments.get(upd.getTo()) + upd.getCost());
			}
			
			List<List<ITradeMessage>> hist = this.sanitize(alloc, payments, state.getTradeHistory());

			this.onAuctionEnd(alloc, payments, hist);
		}
	}

	protected double getValuation(Collection<String> goods) {
		goods.remove("position");
		goods.remove("demand_query");
		goods.remove("reset");
		ICart cart = new Cart();
		goods.forEach(g -> cart.addToCart(new Item(g)));
		return this.valuation.getValuation(cart).doubleValue();
	}

	protected double getValuation(String... goods) {
		return this.getValuation(Arrays.asList(goods));
	}

	protected Set<String> getProximity() {
		return this.baseValues.keySet();
	}
	
	protected boolean isNationalBidder() {
		return this.baseValues.size() == 18;
	}

	protected int getCurrentRound() {
		return this.round;
	}

	protected double clipBid(String good, double bid, Map<String, Double> minBids) {
		return Math.max(bid, minBids.getOrDefault(bid, 0.0));
	}

	protected Set<String> getTentativeAllocation() {
		return Collections.unmodifiableSet(this.allocation);
	}

	private boolean checkRevealedPreference(Set<String> bids, Map<String, Double> reserve) {
		double newBundleNewReserve = 0.0;

		for (String s : bids) {
			newBundleNewReserve += reserve.getOrDefault(s, 0.0);
		}

		for (int r = 0; r < this.allBids.size(); r++) {
			double oldBundleNewReserve = 0.0;
			double newBundleOldReserve = 0.0;
			double oldBundleOldReserve = 0.0;

			for (String s : bids) {
				newBundleOldReserve += this.allReserves.get(r).getOrDefault(s, 0.0);
			}

			for (String s : this.allBids.get(r)) {
				oldBundleNewReserve += reserve.getOrDefault(s, 0.0);
				oldBundleOldReserve += this.allReserves.get(r).getOrDefault(s, 0.0);
			}

			if ((oldBundleNewReserve - oldBundleOldReserve) < (newBundleNewReserve - newBundleOldReserve)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isValidBidBundle(Map<String, Double> myBids, Map<String, Double> minBids, boolean printWarnings) {
		if (myBids == null) {
			return true;
		}

		if (minBids == null) {
			return false;
		}

		boolean status = true;

		for (String s : myBids.keySet()) {
			if (myBids.get(s) == null) {
				if (printWarnings) {
					System.out.println("WARNING: null bid detected for good " + s);
				}
				status = false;
			}
		}

		for (String s : myBids.keySet()) {
			if (myBids.containsKey(s) && minBids.containsKey(s) && myBids.get(s) < minBids.get(s)) {
				if (printWarnings) {
					System.out.println("WARNING: bid for good " + s + " is too low. Your bid is: " + myBids.get(s)
							+ "; must be at least " + minBids.get(s));
				}
				status = false;
			}
		}

		if (!checkRevealedPreference(myBids.keySet(), minBids)) {
			if (printWarnings) {
				System.out.println("WARNING: failed revealed preference test");
			}
			status = false;
		}

		return status;
	}

	private IBidBundle createBidBundle(Map<String, Double> bids) {
		if (bids == null) {
			bids = new HashMap<>();
		}
		Map<ICart, Double> bundle = new HashMap<>();
		for (Map.Entry<String, Double> ent : bids.entrySet()) {
			ICart cart = new Cart();
			cart.addToCart(new Item(ent.getKey()));
			bundle.put(cart, ent.getValue());
		}
		IBidBundle bid = new OneSidedBidBundle(bundle);
		return bid;
	}

	private void parseAllocation(IMarketPublicState state) {
		Map<Integer, List<ICart>> allocations = new HashMap<>(state.getAllocation());
		Set<String> alloc = new HashSet<>();
		for (ICart cart : allocations.getOrDefault(this.agentBackend.getPrivateID(), new ArrayList<>())) {
			for (IItem item : cart.getItems()) {
				alloc.add(item.getName());
			}
		}
		this.allocation = alloc;
	}

}
