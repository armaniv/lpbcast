package lpbcast;

import repast.simphony.engine.schedule.IAction;

public class SchedulableActions {
	
	public static class RetrieveFromSender implements IAction {
		private Element element;
		private Node node;

		public RetrieveFromSender(Element element, Node node) {
			this.element = element;
			this.node = node;
		}

		public void execute() {
			node.requestEventFromSender(element);
		}
	}
	
	
	
	public static class RetrieveFromRandom implements IAction {
		private Element element;
		private Node node;

		public RetrieveFromRandom(Element element, Node node) {
			this.element = element;
			this.node = node;
		}

		public void execute() {
			node.requestEventFromRandom(element);
		}
	}
}
