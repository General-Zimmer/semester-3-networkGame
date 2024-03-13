package spil.version1.gamefiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
		return liste.iterator();
	}

	/*public class ConcurrentArrayListIterator<Player> implements Iterator<Player> {

		private int current = 0;

		ConcurrentArrayList thingy;
		public ConcurrentArrayListIterator(ConcurrentArrayList thingy) {
			this.thingy = thingy;
		}

		@Override
		public  boolean hasNext() {
			synchronized (thingy) {
				return current < size ();
			}
		}

		@Override
		public Player next() {
			if (!hasNext())
				throw new NoSuchElementException();


			synchronized (thingy) {
				return (Player) liste.get(current++);
			}
		}
	
}

	 */
}