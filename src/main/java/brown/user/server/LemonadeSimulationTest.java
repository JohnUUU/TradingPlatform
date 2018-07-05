package brown.user.server;

import java.util.LinkedList;
import java.util.List;

import brown.auction.preset.AbsMarketPreset;
import brown.auction.preset.LemonadeGroupedRulesAnon;
import brown.auction.value.config.LemonadeConfig;
import brown.mechanism.tradeable.ITradeable;
import brown.mechanism.tradeable.MultiTradeable;
import brown.platform.server.RunServer;
import brown.platform.server.SimulMarkets;
import brown.platform.server.Simulation;
import brown.system.setup.LemonadeSetup;

/**
 * Test of a lemonade game run.
 * @author kerry
 *
 */
public class LemonadeSimulationTest {
  public static void main(String[] args) throws InterruptedException {
    // Make sure students know this before hand so they can change some constant in their agents or something
    int numSlots = 12;
    // This is for the 3 person case – the rules will automatically normalize for larger groups
    int totalTradeables = 24;
    
    // simulation variables
    int delayTime = 5;
    int lag = 150; // speed at which rounds run - at lag=100, 100 trials takes 50s-60s
    int numRuns = 2;
    int numSims = 4;
    
    List<ITradeable> allTradeables = new LinkedList<ITradeable>(); 
    allTradeables.add(new MultiTradeable(1, totalTradeables));
       
    List<AbsMarketPreset> firstmarket_seq = new LinkedList<AbsMarketPreset>();
    firstmarket_seq.add(new LemonadeGroupedRulesAnon(12,10));
    firstmarket_seq.add(new LemonadeGroupedRulesAnon(50,20));    
    SimulMarkets firstMarket = new SimulMarkets(firstmarket_seq);

    List<AbsMarketPreset> secondmarket_seq = new LinkedList<AbsMarketPreset>();
    secondmarket_seq.add(new LemonadeGroupedRulesAnon(12,3));    
    SimulMarkets secondMarket = new SimulMarkets(secondmarket_seq);
    
    List<SimulMarkets> seq = new LinkedList<SimulMarkets>();  
//    seq.add(firstMarket);
    seq.add(secondMarket);
    
    Simulation testSim = new Simulation(seq,new LemonadeConfig(),allTradeables,0.,new LinkedList<ITradeable>());    
        
    RunServer testServer = new RunServer(2121, new LemonadeSetup());
    testServer.runSimulation(testSim, numSims, delayTime, lag, null);
  }
}
