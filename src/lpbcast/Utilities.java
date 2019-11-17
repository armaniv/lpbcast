package lpbcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Utilities {
	public static <T> T findById(String id, ArrayList<T> list) {
		for (T e : list) {
			if (((Event) e).getId().equals(id)) {
				return e;
			}
		}
		return null;

	}

	public static class Pair<X, Y> {
		private X x;
		private Y y;

		public Pair() {
		}

		public Pair(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		public X getX() {
			return this.x;
		}

		public Y getY() {
			return this.y;
		}

		public void setX(X value) {
			this.x = value;
		}

		public void setY(Y value) {
			this.y = value;
		}
	}
}
