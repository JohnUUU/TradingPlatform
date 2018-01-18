package brown.rules.paymentrules;

import brown.market.marketstate.ICompleteState;

public interface IPaymentRule {

  public void setPayments(ICompleteState state); 
  
  public void permitShort(ICompleteState state);
  
}
