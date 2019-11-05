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
	
	
	public static class RecoverFromFailure implements IAction {
		private Node node;

		public RecoverFromFailure(Node node) {
			this.node = node;
		}

		public void execute() {
			node.recover();
		}
	}
}
