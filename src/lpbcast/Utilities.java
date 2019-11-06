package lpbcast;

import java.util.ArrayList;

public class Utilities {
	public static <T> T findById(String id, ArrayList<T> list) {
		for (T e : list) {
			if (((Event) e).getId().equals(id)) {
				return e;
			}
		}
		return null;
	
	}

}
