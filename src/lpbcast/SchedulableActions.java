package lpbcast;

import repast.simphony.engine.schedule.IAction;

public class SchedulableActions {

	public static class ReceiveGossip implements IAction {
		private Integer sourceNodeId;
		private Integer destinationNodeId;
		private Message message;
		private Router router;

		public ReceiveGossip(Integer sourceNodeId, Integer destinationNodeId, Message message, Router router) {
			this.sourceNodeId = sourceNodeId;
			this.destinationNodeId = destinationNodeId;
			this.message = message;
			this.router = router;
		}

		public void execute() {
			this.router.send(message, sourceNodeId, destinationNodeId);
		}
	}

	/**
	 * Class to schedule the execution of node's method requestEventFromSender()
	 */
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

	/**
	 * Class to schedule the execution of node's method requestEventFromRandom()
	 */
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

	/**
	 * Class to schedule the execution of node's method subscribe()
	 */
	public static class RecoverAndSubscribe implements IAction {
		private Node node;

		public RecoverAndSubscribe(Node node) {
			this.node = node;
		}

		public void execute() {
			node.subscribe();
		}
	}
	
	public static class DeliverToSelf implements IAction {
		private Node node;
		private String eId;

		public DeliverToSelf(Node node, String eId) {
			this.node = node;
			this.eId = eId;
		}

		public void execute() {
			this.node.deliver(eId, "self");
		}
	}
}
