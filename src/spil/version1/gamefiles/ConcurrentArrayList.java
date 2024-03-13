package spil.version1.gamefiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConcurrentArrayList implements Iterable<Player>, java.io.Serializable{
	private final List<Player> liste = new ArrayList<Player>();
	
	public synchronized void add(Player p) {
		liste.add(p);	
	}

	public synchronized void remove(Player p) {
		liste.remove(p);	
	}
	
	public synchronized void clear() {
		liste.clear();
	}
	
	public synchronized int size() {
		return liste.size();
	}

	public synchronized ArrayList<Player> asArrayList(){
		return new ArrayList<>(liste);
	}

	@Override
	public Iterator<Player> iterator() {
		// TODO Auto-generated method stub
		return new ConcurrentArrayListIterator<>();
	}public class ConcurrentArrayListIterator<Player> implements Iterator<Player> {

		private int current = 0;
		@Override
		public  boolean hasNext() {
			synchronized (liste) {
				return current < size();
			}
		}

		@Override
		public Player next() {
			synchronized (liste) {
				return (Player) liste.get(current++);
			}
		}
	
}
}