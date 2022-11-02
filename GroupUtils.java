import org.jgroups.JChannel;

/**
 * This class establishes a connection by creating a JGroup channel if not created 
 * or returns it if it is already established
 */
public class GroupUtils {

  /**
   * Returns a JGroup Channel in which a connection has already been established.
   * The channel name is taken from the "GROUP" env var, or a default is used if
   * no var present. note: this channel will discard self messages.
   * 
   * @return the connected jgroups channel or null if an error occurred.
   */
    public static JChannel connect() {
        // check if the value of the variable is null then channelName = DEFAULT_GROUP
        String channelName = System.getenv("GROUP") == null ? "DEFAULT_GROUP" : System.getenv("GROUP");
        try {
            JChannel channel = new JChannel(); // use the default configuration
            channel.connect(channelName);
            System.out.printf("    connected to jgroups channel: %s\n", channelName);
            channel.setDiscardOwnMessages(true);
            return channel;
        } catch (Exception e) {
            System.err.printf("    could not connect to jgroups channel: %s\n", channelName);
        }
        return null;
    }
}
