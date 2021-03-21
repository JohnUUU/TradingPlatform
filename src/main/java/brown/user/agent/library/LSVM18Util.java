package brown.user.agent.library;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidderSetup;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class LSVM18Util {
	public static final LSVMWorld WORLD = new LocalSynergyValueModel().createWorld(System.currentTimeMillis() % 10000);
			
	public static final Map<String, Long> ITEM_TO_LSVM_ID = new ImmutableMap.Builder<String, Long>()
			.put("A", 0l)
			.put("B", 1l)
			.put("C", 2l)
			.put("D", 3l)
			.put("E", 4l)
			.put("F", 5l)
			.put("G", 6l)
			.put("H", 7l)
			.put("I", 8l)
			.put("J", 9l)
			.put("K", 10l)
			.put("L", 11l)
			.put("M", 12l)
			.put("N", 13l)
			.put("O", 14l)
			.put("P", 15l)
			.put("Q", 16l)
			.put("R", 17l)
			.build();
	
	public static final Map<Long, String> LSVM_ID_TO_ITEM = ImmutableMap.copyOf(
			ITEM_TO_LSVM_ID.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
	
	public static List<LSVMBidder> createLSVM18Population(int seed) {
		RNGSupplier rng2 = new JavaUtilRNGSupplier(seed);
		List<LSVMBidderSetup> setups = new ArrayList<>(2);
		setups.add(new LSVMBidderSetup.NationalBidderBuilder().build());
		setups.add(new LSVMBidderSetup.RegionalBidderBuilder().build());
		return WORLD.createPopulation(setups, rng2);
	}
	
	public static List<LSVMBidder> restoreLSVM18Population(long populationID) {
		List<LSVMBidder> res = new ArrayList<>();
		WORLD.restorePopulation(populationID).forEach(bidder -> res.add((LSVMBidder)bidder));
		return res;
	}
	
	public static Map<Long, LSVMLicense> mapIDToLSVM18License(LSVMWorld world) {
		Map<Long, LSVMLicense> result = new HashMap<>();
		for (LSVMLicense license : world.getLicenses()) {
			result.put(license.getId(), license);
		}
		return result;
	}
}
