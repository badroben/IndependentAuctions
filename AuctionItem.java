import java.io.Serializable;

public class AuctionItem implements Serializable{
    private int itemId;                     // item id
    private int itemStartingPrice;          // item starting price
    private String itemDescription;         // item description
    private int itemMinimumPrice;           // item minimum price
    private String name;                    // buyer's name
    private String email;                   // buyer's email
    private int currentHighestBid;          // item's current highest bid
    private String clientId;                // seller id

    public AuctionItem(int startingPrice, String description, int minimumPrice, String ownerId){
        this.itemStartingPrice = startingPrice;
        this.itemDescription = description;
        this.itemMinimumPrice = minimumPrice;
        this.currentHighestBid = startingPrice;
        this.clientId = ownerId;
    }

    // return the item id
    public int getItemId() {
        return itemId;
    }

    // return the item starting price
    public int getItemStartingPrice() {
        return itemStartingPrice;
    }

    // return the item description
    public String getItemDescription() {
        return itemDescription;
    }

    // return the item minimum price
    public int getItemMinimumPrice() {
        return itemMinimumPrice;
    }

    // return the item's buyer email
    public String getEmail() {
        return email;
    }

    // return the item's buyer name
    public String getName() {
        return name;
    }

    // return the item's current highest bid
    public int getCurrentHighestBid() {
        return currentHighestBid;
    }

    // return the owner/seller id
    public String getClientId() {
        return clientId;
    }

    // set the item starting price to the value specified  in case the seller wants to change it 
    public void setItemStartingPrice(int itemStartingPrice) {
        this.itemStartingPrice = itemStartingPrice;
    }

    // set the item description to the value specified in case the seller wants to change it 
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    // set the item minimum price to the value specified  in case the seller wants to change it 
    public void setItemMinimumPrice(int itemMinimumPrice) {
        this.itemMinimumPrice = itemMinimumPrice;
    }

    // set the item id to the value specified
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    // set the buyer email to the value specified
    public void setEmail(String email) {
        this.email = email;
    }

    // set the buyer name to the value specified
    public void setName(String name) {
        this.name = name;
    }

    // set the item current highest price to the value specified
    public void setCurrentHighestBid(int currentPrice) {
        this.currentHighestBid = currentPrice;
    }

}
