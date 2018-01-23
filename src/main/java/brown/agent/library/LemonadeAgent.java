package brown.agent.library;

import brown.agent.AbsLemonadeAgent;
import brown.bid.bidbundle.library.GameBidBundle;
import brown.channels.agent.library.LemonadeChannel;
import brown.exceptions.AgentCreationException;
import brown.messages.library.BankUpdateMessage;
import brown.messages.library.GameReportMessage;
import brown.messages.library.LemonadeReportMessage;
import brown.messages.library.PrivateInformationMessage;
import brown.setup.library.LemonadeSetup;
import brown.setup.Logging;

/**
 * Agent for the lemonade game.
 * @author andrew
 */
public class LemonadeAgent extends AbsLemonadeAgent {
  
  private int posn;
  private int NUM_SLOTS = 12;
  private int[] positions = new int[NUM_SLOTS];
  private int count = 0; 
  
  public LemonadeAgent(String host, int port, int position)
      throws AgentCreationException {
    super(host, port, new LemonadeSetup());
    this.posn = position; 
    for(int i = 0; i < NUM_SLOTS; i++) {
      positions[i] = 0; 
    }
  } 
  
  public void onLemonade(LemonadeChannel channel) {
    // Enter a position between 0 and NUM_SLOTS-1 inclusive.
    channel.bid(this, new GameBidBundle(this.posn));
  }
  
  @Override
  public void onBankUpdate(BankUpdateMessage bankUpdate) {
    Logging.log("[Bank update]Agent with position " + this.posn + ": " + (bankUpdate.moniesAdded)); 
  }
  
  @Override
  public void onMarketUpdate(GameReportMessage marketUpdate) {
    // TODO Auto-generated method 
    if (marketUpdate instanceof LemonadeReportMessage) { 
      LemonadeReportMessage lemonadeUpdate = (LemonadeReportMessage) marketUpdate;
      for (int i = 0; i < NUM_SLOTS; i++) {
        this.positions[i] = this.positions[i] + lemonadeUpdate.getCount(i);
      }
      System.out.println(lemonadeUpdate.toString());
      //printIsland();
    }
    else {
      System.out.println("ERROR: Lemonade Report Not Received");
    }
  }
  
  
  public static void main(String[] args) throws AgentCreationException {
    new LemonadeAgent("localhost", 2121, 2);
//    new LemonadeAgent("localhost", 2121, 4);
//    new LemonadeAgent("localhost", 2121, 9);
    
    while(true){}
  }

  // No private info in lemonade
  @Override
  public void onPrivateInformation(PrivateInformationMessage privateInfo) {    
  }
  
}