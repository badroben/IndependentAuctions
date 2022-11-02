# Independent Auctions
A distributed auctioning system developed using [Java RMI](https://en.wikipedia.org/wiki/Java_remote_method_invocation), it allows users(Sellers and Buyers) to interact with each other through the main server(Frontend). 
This project ensures [dependability](https://en.wikipedia.org/wiki/Dependability) by enhancing the [availability](https://en.wikipedia.org/wiki/Availability) of the system, it does this using Active [Replication](https://en.wikipedia.org/wiki/Replication_(computing)) technique and it syncs between the replicas and ensures that the same data is stored on all the replicas by doing a [general consesnus](https://en.wikipedia.org/wiki/Consensus_(computer_science)) check. It also ensures that the system is [Fault Tolerant](https://en.wikipedia.org/wiki/Fault_tolerance), as long as one replica is alive and running, the system will continue to operate correctly and as expected.

# AuctionItem 
This class defines properties of an item such as itemID, price, descriptions ...etc. 

# Backend 
This class is responsible for processing the requests receueved from the frontend, it also syncs the auction data(database) if other backends/replicas exist. 

# Frontend 
This is the server class that processes all the requests from Clients(Sellers and Buyers) and forwards them to the Backends, and sends the results back to the clients. It also makes sure the replicas/backends have the same data stored in them, it does this by checking if there is a general consensus between replicas in terms of the data stored.  

# Item
An interface that defines the important methods that need to be specified by classes that implement it.

# GroupUtils
This class establishes a connection by creating a JGroup channel if not created or returns it if it is already established
# Seller
Seller class that allows a user to do different requests, it can create an item, close an item, and announce the winner with the highest bid. 
# Buyer
Buyer class that deals with Buyers' requests such as viewing current auction items and bid on them.

# Diagram
This diagram illustrates the design of the whole project and how are entities connected to each other. ![image](https://user-images.githubusercontent.com/60741379/199395135-dbf69c48-2294-4a3d-a118-a3437dcf4fa1.png)

