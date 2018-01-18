package brown.setup;

//move me later!!
/**
 * Wraps printed messages, so logging can be enabled or disabled.
 * logging.log(X) prints X to console.
 * @author lcamery
 */
public class Logging {
	
  // Enable or disable logging here.
	public final static boolean LOGGING = true;
	
	/**
	 * Logs a message to the console, if logging is set to true.
	 * @param message
	 */
	public static void log(String message) {
		if (LOGGING) {
			System.out.println(message);
		}
	}

}
