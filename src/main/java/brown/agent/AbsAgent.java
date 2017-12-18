package brown.agent;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import brown.channels.agent.library.CDAAgentChannel;
import brown.channels.agent.library.SimpleAgentChannel;
import brown.exceptions.AgentCreationException;
import brown.messages.AbsMessage;
import brown.messages.library.Ack;
import brown.messages.library.BankUpdate;
import brown.messages.library.BidRequest;
import brown.messages.library.GameReport;
import brown.messages.library.NegotiateRequest;
import brown.messages.library.Registration;
import brown.messages.library.ValuationRegistration;
import brown.setup.Logging;
import brown.setup.ISetup;

public abstract class AbsAgent extends AbsClient { 

  /**
   * Implementations should always invoke super()
   * 
   * @param host
   * @param port
   * @param gameSetup
   * @throws AgentCreationException
   */
  public AbsAgent(String host, int port, ISetup gameSetup)
      throws AgentCreationException {
    super(host, port, gameSetup);

    final AbsAgent agent = this;
    CLIENT.addListener(new Listener() {
      public void received(Connection connection, Object message) {
        synchronized (agent) {
          if (message instanceof AbsMessage) {
            AbsMessage theMessage = (AbsMessage) message;
            theMessage.dispatch(agent);
          }
        }
      }
    });

    CLIENT.sendTCP(new Registration(-1));
  }
  

  /**
   * Agents must accept their IDs from the server
   * 
   * @param registration
   *            : includes the agent's new ID
   */
  public void onRegistration(Registration registration) {
    this.ID = registration.getID();
  }
  
  /**
   * Whenever a request is accepted/rejected, this method is sent with the
   * request
   * 
   * @param rejection
   *            : includes the rejected method and might say why
   */
  public void onAck(Ack message) {
    if (message.REJECTED) {
      Logging.log("[x] rej: " + message.failedBR);
    }
  }
  
  /**
   * Whenever you get a report
   * @param marketUpdate
   */
  public abstract void onMarketUpdate(GameReport marketUpdate);

  /**
   * Whenever an agent's bank changes, the server sends a bank update
   * 
   * @param bankUpdate
   *            - contains the old bank state and new bank state note: both
   *            accounts provided are immutable
   */
  public abstract void onBankUpdate(BankUpdate bankUpdate);

  /**
   * Whenever an auction is occurring, the server will request a bid using
   * this method and provide information about the auction as a part of the
   * request
   * 
   * @param bidRequest
   *            - auction metadata
   */
  public abstract void onTradeRequest(BidRequest bidRequest);

  /**
   * Whenever another agent requests a trade either directly with this agent
   * or to all agents, this method is invoked with the details of the trade.
   * 
   * @param tradeRequest
   *            - from fields describe what this agent will receive and to
   *            fields describe what it will give up
   */
  public abstract void onNegotiateRequest(NegotiateRequest tradeRequest);
  
  /**
   * Provides response to sealed bid auction
   * @param SealedBid wrapper
   */
  public abstract void onSimpleSealed(SimpleAgentChannel simpleWrapper);

  /**
   * Provides agent response to OpenOutcry auction
   * @param OpenOutcry wrapper
   */
  public abstract void onSimpleOpenOutcry(SimpleAgentChannel simpleWrapper);


  /**
   * Provides agent response to CDAs
   * @param market : CDA wrapper
   */
  public abstract void onContinuousDoubleAuction(CDAAgentChannel market);
  
}

  
 