package brown.messages.library;

import brown.agent.AbsAgent;

/**
 * a request for an agent to join the server 
 * is a sent as a registration.
 * @author lcamery
 */
public class RegistrationMessage extends AbsMessage {
	
	/**
	 * Empty constructor for Kryo
	 * DO NOT USE
	 */
	public RegistrationMessage() {
		super(null);
	}

	/**
	 * Agent sends a registration message initially
	 * Server sends back a message with the agent's ID
	 * @param ID : agent's ID
	 */
	public RegistrationMessage(Integer ID) {
		super(ID);
	}

	@Override
	public void dispatch(AbsAgent agent) {
		agent.onRegistration(this);
	}
	
}
