import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;

/**
 * Seller program that implements an application to deal with client requests
 * such as creating auction items, closing them and announcing the winner.
 */
public class Seller{

    public Seller() {
        try {
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("127.0.0.1");
            Item server = (Item) registry.lookup(name);

            Random r = new Random();
            // assign the seller an id which is composed of Seller string and a number
            String sellerId = "Seller"+r.nextInt(1000);
            System.out.println(sellerId);
            // check if the Seller id already exist in the server
            while(server.checkClientId(sellerId)){
                // generate a new id
                sellerId = "Seller"+r.nextInt(1000);
            }
            
            // continue asking the user for input on what they want to perform next
            while (true) {
                // asking the user which operation to perform
                Scanner scanner = new Scanner(System.in);
                System.out.println("Hello there! if you want to list an item for bidding type add,"+
                                "\nif you want to close a listed item please type close, "+
                                "\nor to exit the system type exit.");
                // if the answer is provided in upper case, force lower case
                String answer = scanner.nextLine().toLowerCase();
                switch (answer) {
                    case "add":
                        // ask the user to provide a description of the item
                        Scanner myObj = new Scanner(System.in);
                        System.out.println("Add the details of the item you want to auction :)");
                        System.out.println("Please provide the following:"+
                                                "\nDescription  of your item:");
                        String description = myObj.nextLine();
                        System.out.println("starting price:");
                        // specify the starting price
                        int price = myObj.nextInt();
                        System.out.println("Minimum price:");
                        // specify the minimum price
                        int price2 = myObj.nextInt();
                        // create the item and return the id of it
                        int id = server.createItem(price, description, price2, sellerId);
                        System.out.println("Item has been added with id: "+id);
                        // print the specs for the Seller to see the details
                        AuctionItem result = server.getSpec(id);
                        System.out.println("The Item id is: " + id
                                    + "\nstarting Price is: "+ result.getItemStartingPrice()
                                    + "\nDescription: "+ result.getItemDescription()
                                    + "\nminimum price is: "+ result.getItemMinimumPrice());
                        break;
                    case "close":
                        System.out.println("enter the id of the item you want to close");
                        int closedItemId = scanner.nextInt();
                        // check if the item is already closed 
                        if(server.checkItemNonExistent(closedItemId)){
                            System.out.println("This item has already been closed or does not exist!");
                            break;
                        } else {
                            /* check if the Seller has the authority to close the item 
                                and remove the item from the server using the specified key/id */
                            if(sellerId.equals(server.getListings().get(closedItemId).getClientId())){
                                AuctionItem closedItem = server.closeItem(closedItemId);
                                // print out the winner if the price is higher than minimum price else print no winner 
                                if (closedItem.getCurrentHighestBid()>=closedItem.getItemMinimumPrice()) {
                                    System.out.println("closed item with id: "+ closedItemId +
                                                    "\nwinner name is: "+ closedItem.getName()+
                                                    "\nwith a bid of: "+ closedItem.getCurrentHighestBid());
                                }else{
                                    System.out.println("No winner! reserve has not been reached :(");
                                }
                                break;
                            }else{
                                System.out.println("You do not have the authority to access this item, try another one!");
                            }
                        }
                        break;
                    case"exit":  
                        // stop the program if the user wants to exit
                        System.exit(1);
                    default:
                        // if the user enters something other than the three options (add, close, and exit) ask again
                        System.out.println("please enter either add, close, or exit");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new Seller();
    }
}