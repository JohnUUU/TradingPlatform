package brown.server.library; 

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import brown.market.preset.AbsMarketPreset;
import brown.market.preset.library.LemonadeRules;
import brown.market.preset.library.SimSecondPriceRules;
import brown.setup.library.SimpleSetup;
import brown.tradeable.library.Tradeable;
import brown.value.config.AbsValueConfig;
import brown.value.config.NullConfig;
import brown.value.config.SSSPConfig;

/*
 * Use this class to run the server side of your game.
 * just edit the rules to the game that you'd like to play.
 * 
 * Given a full library on the platform, this is the only file the
 * server-side user should have to edit.
 * hmm... what to do about special registrations?
 */
public class MainServer {
  
  public static void main(String[] args) throws InterruptedException {
    //for now just gonna build this where you input things into this file.
    //But later on i'd like to use command line input.
    List<AbsMarketPreset> allMarkets = new ArrayList<AbsMarketPreset>();
    List<AbsValueConfig> allValInfo = new ArrayList<AbsValueConfig>();
    //add whatever you want to do.
    Set<Tradeable> allTradeables = new HashSet<Tradeable>(); 
    for (int i = 0; i < 3; i++) {
      allTradeables.add(new Tradeable(i));
    }
    //out valuation information and rules information.
    allValInfo.add(new SSSPConfig(allTradeables));
    allMarkets.add(new SimSecondPriceRules()); 
//    allMarkets.add(new LemonadeRules());
//    allValInfo.add(new NullConfig());
    new RunServer(2121, new SimpleSetup()).runGame(allMarkets, allValInfo);
  }
}