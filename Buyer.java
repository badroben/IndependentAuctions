import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.Scanner;

/**
 * Buyer program that implements an application to deal with client requests
 * such as viewing auction items and bidding on them.
 */
public class Buyer{
    public Buyer(){
        try {
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("127.0.0.1");
            Item server = (Item) registry.lookup(name);

            Random r = new Random();
            // assign the buyer an id which is composed of Buyer string and a number
            String buyerId = "Buyer"+r.nextInt(1000);
            // check if the Buyer id already exist in the server
            while(server.checkClientId(buyerId)){
                // generate a new id
                buyerId = "Buyer"+r.nextInt(1000);
            }
            
            // ask the user to provide his name and email details 
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please provide your name and a valid email address\nName: ");
            String clientName = scanner.nextLine();
            System.out.println("Email");
            String clienEmail = scanner.nextLine();

            // continue asking the user for input on what they want to perform next
            while(true){
                // accessing the current listings and store them in a list
                List<AuctionItem> items = new ArrayList<AuctionItem>(server.getListings().values());
                System.out.println("here are the available items for bidding: ");
                // print the current available auctions to the buyer
                System.out.println("Item id    //    Item description    //   Current highest bid");
                for(int i=0; i< items.size(); i++) {
                    System.out.println(items.get(i).getItemId() + "         //        "+items.get(i).getItemDescription()+"       //         "+ items.get(i).getCurrentHighestBid());
                }
                System.out.println("If you would like to place a bet on a certain item type bid"+
                                    "\notherwise type exit to leave the page! Thanks for stopping by <3");
                // if the answer is provided in upper case, force lower case
                String answer = new Scanner(System.in).nextLine().toLowerCase();
                switch (answer) {
                    case("bid"):
                        // ask the Buyer to specify the item id to bid for
                        System.out.println("Enter the id of the item you want to bid for");
                        int biddingItemId = scanner.nextInt();
                        // check if the item is available in the server or is closed
                        if(server.checkItemNonExistent(biddingItemId)){
                            System.out.println("Item has been closed or does not exist!");
                            break;
                        } else{
                            System.out.println("Enter how much you want to bid for that item");
                            // specify the price you want to bid for
                            int biddingPrice = scanner.nextInt();
                            // check if the price provided is higher than the current highest
                            while(biddingPrice <= server.getListings().get(biddingItemId).getCurrentHighestBid()){
                                System.out.println("Please enter a price that is higher than the current highest :)");
                                biddingPrice = scanner.nextInt();
                            }
                            // add the bidding price to the specfied item
                            server.bid(biddingItemId, biddingPrice, clientName, clienEmail, buyerId);
                            // confirm bid
                            System.out.println("bidding confirmed for item with id: " + biddingItemId + 
                                            "\nwith a price of: "+ server.getListings().get(biddingItemId).getCurrentHighestBid());
                            break;
                        }
                    case("exit"):
                        // stop the program if the user wants to exit
                        System.exit(1);
                    default:
                        // if the user enters something other than the two options (bid or exit) ask again
                        System.out.println("please enter either bid or exit :)");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new Buyer();
    }
}