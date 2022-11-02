import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

/**
 * This class is responsible for dealing with both Seller and Buyer requests by sending these 
 * requests to the backends to process them and return whats needed, 
 * it also makes sure the replicas/backends have the same data stored in them and
 * it checks if a general consensus is present when processing requests.
 */
public class Frontend extends UnicastRemoteObject implements Item, MembershipListener{
    private JChannel groupChannel; // the cluster to connect to
    private RpcDispatcher dispatcher; // allows to invoke remote methods in all cluster members

    private final int DISPATCHER_TIMEOUT = 1000; // amount of time the dispatcher waits before timing out

    /**
     * Constructor that connects to the cluster and allows invoking remote methods
     * @throws RemoteException
     */
    public Frontend() throws RemoteException{
        super();
        // Connect to the group (channel)
        this.groupChannel = GroupUtils.connect();
        if (this.groupChannel == null) {
            System.exit(1); // error to be printed by the 'connect' function
        }
        try {
            String name = "myserver";
            // prepare the object for remote access
            Registry registry = LocateRegistry.createRegistry(1099);
            // bind the remote object to the server with a specific name
            registry.rebind(name, this);
            System.out.println("Server ready and running!");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
        // Make this instance of Frontend a dispatcher in the channel (group)
        this.dispatcher = new RpcDispatcher(groupChannel, this);
        this.dispatcher.setMembershipListener(this);
    }


    /**
     * this methods returns the auction item using the id passed
     * @param itemId the id of the auction item to be returned
     * @return the auction item specified by the id provided
     */
    public AuctionItem getSpec(int itemId) throws RemoteException{
        try {
            // Call the "getSpec" function on all the group members, passing 
            // param of object class integer
            RspList<AuctionItem> responses = this.dispatcher.callRemoteMethods(null, "getSpec",
            new Object[] { itemId }, new Class[] { int.class},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
                return responses.getFirst();
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return null;        
    }


    
    /** 
     * This method create the auction item with the parameters provided 
     * then adds it to the hashTable with the key being the id
     * @param startingPrice starting price of the auction item
     * @param description description of the auction item
     * @param minimumPrice minimum price required to reserve the auction item
     * @param clientId id of the client that wants to add the auction item
     * @return int return the id of the item created
     * @throws RemoteException
     */
    public int createItem(int startingPrice, String description, int minimumPrice, String clientId) throws RemoteException {
        try {
            RspList<Integer> responses = this.dispatcher.callRemoteMethods(null, "createItem",
            new Object[] { startingPrice, description, minimumPrice, clientId }, new Class[] { int.class, String.class, int.class, String.class},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            // check if the values obtained from the backends are all the same i.e consensus achieved
            if(verifyresults(responses.getResults())){
                return responses.getFirst();
            }
            // if no consensus achieved then process the request from the member with the highest 
            // number of requests / oldest member
            else{
                for (Address member : responses.keySet()) {
                    if(member.equals(getMostReliablAddress())){
                        System.out.println("found the correct member/member with the highest requests processed: " + member);
                        return responses.getValue(getMostReliablAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return 0;
    }

    
    /** 
     * this method returns the current hashtable 
     * @return Hashtable<Integer, AuctionItem> the hashtable that stores the ids and the items
     * @throws RemoteException
     */
    public Hashtable<Integer, AuctionItem> getListings() throws RemoteException {
        try {
            RspList<Hashtable<Integer, AuctionItem>> responses = this.dispatcher.callRemoteMethods(null, "getListings",
            new Object[] {}, new Class[] {},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            // check if the values obtained from the backends are all the same i.e consensus achieved
            if(verifyresults(responses.getResults())){
                return responses.getFirst();
            }
            // if no consensus achieved then process the request from the member with the highest 
            // number of requests / oldest member
            else{
                for (Address member : responses.keySet()) {
                    if(member.equals(getMostReliablAddress())){
                        System.out.println("found the correct member/member with the highest requests processed: " + member);
                        return responses.getValue(getMostReliablAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return null;
    }

    
    /** 
     * this method checks if the client id generated already exists in the hashtable
     * @param id the id of the client generated
     * @return Boolean return true if exists else return false
     * @throws RemoteException
     */
    public Boolean checkClientId(String id) throws RemoteException {
        try {
            RspList<Boolean> responses = this.dispatcher.callRemoteMethods(null, "checkClientId",
            new Object[] { id }, new Class[] { String.class},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            // check if the values obtained from the backends are all the same i.e consensus achieved
            if(verifyresults(responses.getResults())){
                return responses.getFirst();
            }
            // if no consensus achieved then process the request from the member with the highest 
            // number of requests / oldest member
            else{
                for (Address member : responses.keySet()) {
                    if(member.equals(getMostReliablAddress())){
                        System.out.println("found the correct member/member with the highest requests processed: " + member);
                        return responses.getValue(getMostReliablAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return null;
    }

    
    /** 
     * this method allows the user to bid for a specific auction item 
     * @param id the id of the auction item to bid for
     * @param price the price to bid on the item
     * @param name the name of the Buyer
     * @param email the email of the Buyer
     * @param buyerId the id of the Buyer
     * @throws RemoteException
     */
    public void bid(int id, int price, String name, String email, String buyerId) throws RemoteException {
            try {
                this.dispatcher.callRemoteMethods(null, "bid",
                new Object[] {id, price, name, email, buyerId}, new Class[] { int.class, int.class, String.class, String.class, String.class},
                new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            } catch (Exception e) {
                System.err.println("dispatcher exception:");
                e.printStackTrace();
            }
    }

    
    /** 
     * this method closes the auction item using the id provided
     * @param key the id of the auction item to close
     * @return AuctionItem the auction item closed
     * @throws RemoteException
     */
    public AuctionItem closeItem(int key) throws RemoteException {
        try {
            RspList<AuctionItem> responses = this.dispatcher.callRemoteMethods(null, "closeItem",
            new Object[] { key }, new Class[] { int.class},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            return responses.getFirst();
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return null;
    }

    
    /** 
     * this method checks if the item doesnt exist or closed
     * @param id the id of the auction item to check
     * @return boolean return true if it doesnt exist else false
     * @throws RemoteException
     */
    public boolean checkItemNonExistent(int id) throws RemoteException {
        try {
            RspList<Boolean> responses = this.dispatcher.callRemoteMethods(null, "checkItemNonExistent",
            new Object[] { id }, new Class[] { int.class },
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            // check if the values obtained from the backends are all the same i.e consensus achieved
            if(verifyresults(responses.getResults())){
                return responses.getFirst();
            }
            // if no consensus achieved then process the request from the member with the highest 
            // number of requests / oldest member
            else{
                for (Address member : responses.keySet()) {
                    if(member.equals(getMostReliablAddress())){
                        System.out.println("found the correct member/member with the highest requests processed: " + member);
                        return responses.getValue(getMostReliablAddress());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * this method is called as an indication that the member should stop sending messages
     */
    @Override
    public void block() {
        System.out.println("    jgroups view block indicator\n");
    }

    
    /** 
     * this method is called when a member is suspected of crashing
     * @param suspectedMember the address of the suspected member 
     */
    @Override
    public void suspect(Address suspectedMember) {
        System.out.println("    jgroups view suspected member crash: " + suspectedMember.toString() + "\n");
    }

    /**
     * this method is called after the FLUSH protocol has unblocked previously blocked senders, 
     * and messages can be sent again.
     */
    @Override
    public void unblock() {
        System.out.println("    jgroups view unblock indicator\n");
    }

    
    /** 
     * Called when a change in membership has occurred.
     * @param v the view after the the change in members of the cluster
     */
    @Override
    public void viewAccepted(View v) {
        System.out.println("    jgroups view changed!\n    new view: " + v.toString() + "\n");
    }

    
    /** 
     * this methods checks if all the results returned from the backends
     * are similair to help achieve consensus 
     * @param results the list of the results
     * @return Boolean return true if consensus is achieved otherwise return false
     */
    public Boolean verifyresults(List results){
        Object valuetoVerify = results.get(0);
        for(int i = 0; i<results.size(); i++){
            // terminate when reaching the end of the list 
            // and return true
            if(i == results.size()-1){
                return true;
            }
            // check if two objects match
            else if(valuetoVerify == results.get(i+1)){
                valuetoVerify = results.get(i+1);
            } 
            else if(valuetoVerify instanceof Hashtable){
                valuetoVerify = results.get(i+1);
            }
            else{
                System.out.println("no consensus, values are not similar!");
                return false;
            }
        }
        return true;
    }

    
    /** 
     * return the most reliable address / backend based on the amount of requests processed
     * @return Address the address of the member 
     */
    public Address getMostReliablAddress() {
        try {
            Address reliablAddress = null;
            int currentHighestCount = 0;
            RspList<Integer> responses = this.dispatcher.callRemoteMethods(null, "getRequestCount",
            new Object[] {}, new Class[] {},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            for (Address member : responses.keySet()) {
                if(responses.getValue(member) > currentHighestCount){
                    currentHighestCount = responses.getValue(member);
                    reliablAddress = member;
                }
            }
            return reliablAddress;
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            new Frontend();
        } catch (Exception e) {
            System.err.println("    remote exception:");
            e.printStackTrace();
            System.exit(1);
        } 
    }
}
