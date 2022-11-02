import java.rmi.RemoteException;

import java.util.*;

import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;

/**
 * this class is responsible for processing the requests received from the frontend 
 * it also syncs with the auction data in other backends if they are present
 */
public class Backend {

    private JChannel groupChannel; // the cluster to connect to
    private RpcDispatcher dispatcher; // allows to invoke remote methods in all cluster members
    private int requestCount; // stores the number of requests processed by a backend 
    private final int DISPATCHER_TIMEOUT = 1000; // amount of time the dispatcher waits before timing out

    
    AuctionItem auctionItem;
    Hashtable<Integer, AuctionItem> items = new Hashtable<Integer, AuctionItem>(); // hashtable to store the items using their ids
    Hashtable<String, List<AuctionItem>> clients = new Hashtable<String, List<AuctionItem>>(); // hashtable to store clientIds and their items that are either for auctioning or bought
    int id = 0;                      // item id

    /**
     * Constructor that connects to the cluster and processes requests recieved from frontend
     */
    public Backend() {
        this.requestCount = 0;

        // Connect to the group (channel)
        this.groupChannel = GroupUtils.connect();
        if (this.groupChannel == null) {
        System.exit(1); // error to be printed by the 'connect' function
        }

        // Make this instance of Backend a dispatcher in the channel (group)
        this.dispatcher = new RpcDispatcher(this.groupChannel, this);
        // sync the data with other backends
        syncData(findRecentAuctions(), findRecentId());
    }

    /**
     * this methods syncs the data between the current backend and others if they exist
     * @param data the current auction data of the backend 
     * @param id the current id value 
     */
    public void syncData(Hashtable<Integer, AuctionItem> data, int id){
        if(this.items.isEmpty() && data != null){
            System.out.println("Backend started! Syncing data with other backends");
            this.items = data;
            this.id = id;
            System.out.println("Current Auction data: "+ items);
        }
    }

    /**
     * this method returns the auction items hashtable from other backends 
     * to use it for new backends
     * @return the hashtable of the auction items
     */
    public Hashtable<Integer, AuctionItem> findRecentAuctions(){
        try {
            RspList<Hashtable<Integer, AuctionItem>> responses = this.dispatcher.callRemoteMethods(null, "getListings",
            new Object[] {}, new Class[] {},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            return responses.getFirst();
        } catch (Exception e) {
            System.err.println("dispatcher exception:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * this method gets the recent id count
     * @return int the current id count
     */
    public int findRecentId() {
        try {
            int initialId = 0;
            RspList<Integer> responses = this.dispatcher.callRemoteMethods(null, "getId",
            new Object[] {}, new Class[] {},
            new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
            // if this is the first backend
            if(responses.getFirst() == null){
                return initialId;
            } else{
                return responses.getFirst();
            }    
        } catch (Exception e) {
            System.err.println("dispatcher exception: ");
            e.printStackTrace();
        }
        return 0;
    }

  
    /**
     * this methods returns the auction item using the id passed
     * @param itemId the id of the auction item to be returned
     * @return the auction item specified by the id provided
     */
    public AuctionItem getSpec(int itemId) throws RemoteException{
        return items.get(itemId);
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
    public synchronized int createItem(int startingPrice, String description, int minimumPrice, String sellerId){
        // increase the value of requestCount 
        requestCount++;
        AuctionItem auctionItem = new AuctionItem(startingPrice, description, minimumPrice, sellerId);
        // increase the id in order to provide ids starting from 1
        id++;
        // print the details 
        System.out.println("item created with id: "+ id +" by seller: "+ sellerId);
        auctionItem.setItemId(id);
        // add the item to the hashtable that stores all the items
        items.put(id, auctionItem);
        System.out.println("Hashtable after creation is: " + items);
        // check if a client id is already taken otherwise its a new client
        if (clients.containsKey(sellerId)) {
            // add the item to the client's corresponding list of items
            clients.get(sellerId).add(auctionItem);
        } else {
            // create a list to store each client's items
            List<AuctionItem> sellerItems = new ArrayList<AuctionItem>();
            sellerItems.add(auctionItem);
            clients.put(sellerId, sellerItems);
        }
        return id;
    }


    /** 
     * this method returns the current hashtable 
     * @return Hashtable<Integer, AuctionItem> the hashtable that stores the ids and the items
     * @throws RemoteException
     */
    public Hashtable<Integer, AuctionItem> getListings(){
        return items;
    }

    /** 
     * this method checks if the client id generated already exists in the hashtable
     * @param id the id of the client generated
     * @return Boolean return true if exists else return false
     * @throws RemoteException
     */
    public Boolean checkClientId(String id){
        for (String clientId : clients.keySet()) {
            if(id.equals(clientId)) {
                System.out.println("found match!");
                return true;
            }
        }
        System.out.println("New Client Login!");
        return false;
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
    public synchronized void bid(int id, int price, String name, String email, String buyerId) throws RemoteException {
        // increase the value of requestCount 
        requestCount++;
        AuctionItem itemToBid = null;
        // check the list for a matching auction item
        for(AuctionItem auction: items.values()){
            // check if the returned id matches the parameter id
            if(auction.getItemId() == id){
                itemToBid = auction;
                break;
            }
        }
        // check if a client id already exists 
        if (clients.containsKey(buyerId)) {
            clients.get(buyerId).add(itemToBid);
        } else {
            // create a list to store each buyer's items they bid for
            List<AuctionItem> buyerItems = new ArrayList<AuctionItem>();
            buyerItems.add(itemToBid);
            clients.put(buyerId, buyerItems);
        }
        items.get(id).setCurrentHighestBid(price);
        items.get(id).setName(name);
        items.get(id).setEmail(email);
    }

    /** 
     * this method closes the auction item using the id provided
     * @param key the id of the auction item to close
     * @return AuctionItem the auction item closed
     * @throws RemoteException
     */
    public AuctionItem closeItem(int id) throws RemoteException {
        // increase the value of requestCount 
        requestCount++;
        return items.remove(id);
    }

    /** 
     * this method checks if the item doesnt exist or closed
     * @param id the id of the auction item to check
     * @return boolean return true if it doesnt exist else false
     * @throws RemoteException
     */
    public boolean checkItemNonExistent(int id){
        for(AuctionItem item: items.values()){
            if(item.getItemId()==id){
                return false;
            }
        }
        // if the item does not exist return true
        return true;
    }

    /**
     * return the current id to be copied to newer backends
     * @return the current id
     */
    public int getId(){
        return id;
    }


    /**
     * return the current requests processed by a backend
     * @return the current request count 
     */
    public int getRequestCount() {
        return requestCount;
    }

  public static void main(String args[]) {
    new Backend();
  }

}
