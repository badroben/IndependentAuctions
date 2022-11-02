import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

public interface Item extends Remote{
    public AuctionItem getSpec(int itemId) throws RemoteException;
    public int createItem(int startingPrice, String description, int minimumPrice, String clienId) throws RemoteException;
    public Hashtable<Integer, AuctionItem> getListings() throws RemoteException;
    public Boolean checkClientId(String id) throws RemoteException;
    public void bid(int id, int price, String name, String email, String buyerId) throws RemoteException;
    public AuctionItem closeItem(int key) throws RemoteException;
    public boolean checkItemNonExistent(int id) throws RemoteException;
}