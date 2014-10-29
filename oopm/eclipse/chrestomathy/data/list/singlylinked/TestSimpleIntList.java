package data.list.singlylinked;

public class TestSimpleIntList {

	public static void print(SimpleIntList l) {
		l.reset();
		while (l.hasNext())
			System.out.println(l.next());
		System.out.println();
	}
	
	public static void main(String[] args) {
		SimpleIntList l1 = new SimpleIntList();
		System.out.println(l1.getLength());
		print(l1);
		l1.add(1);
		l1.add(2);
		l1.add(4);
		System.out.println(l1.getLength());
		print(l1);
		SimpleIntList l2 = new SimpleIntList();
		l2.add(8);
		l2.add(16);
		l2.add(32);
		print(l2);
		l1.append(l2);
		print(l1);		
	}

}
