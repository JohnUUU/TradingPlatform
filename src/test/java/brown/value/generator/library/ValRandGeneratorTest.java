package brown.value.generator.library;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import brown.tradeable.library.MultiTradeable;

/**
 * test the valrand Generator
 * I
 * @author andrew
 *
 */
public class ValRandGeneratorTest {
  
  @Test
  public void testVrg() {
    int NUMTRIALS = 100;
    ValRandGenerator vrg = new ValRandGenerator();
    MultiTradeable good = new MultiTradeable(0);
    
    for(int i = 0; i < NUMTRIALS; i++) {
    assertTrue(vrg.makeValuation(good).value >= 0.0
        && vrg.makeValuation(good).value <= 1.0);
    }
  }
}