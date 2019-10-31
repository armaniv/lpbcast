package lpbcast;

import java.util.HashMap;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class LpbcastBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		
		// --- Lightweight Probabilistic Broadcast (P. Th. Eugster et al)
		
		context.setId("lpbcast");

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		// get the value for the parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		int grid_size = params.getInteger("grid_size");
		int node_count = params.getInteger("node_count");
		
		int max_l = params.getInteger("max_l");
		int max_m = params.getInteger("max_m");
		int fanout = params.getInteger("fanout");
		int initial_neighbors = params.getInteger("initial_neighbors");
		int round_k = params.getInteger("round_k");
		int round_r = params.getInteger("round_r");
		int n_messages = params.getInteger("n_messages");
		int churn_rate = params.getInteger("churn_rate");
		
		// create a grid
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), false, grid_size, grid_size));
		
		
		// Network to show edges between nodes which means 
		// that a message between them has been sent
		NetworkBuilder<Object> networkBuilder = new NetworkBuilder<Object>("messages network", context, true);
		networkBuilder.buildNetwork();
		// this is the actual entity which manages communication
		MessagesNetwork network = new MessagesNetwork();
		context.add(network);
		HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
		
		// populate the grid with at most one Node per cell
		for (int i = 0; i < node_count; i++) {
			Node node = new Node(grid, i, network, max_l, max_m, fanout, initial_neighbors, round_k, round_r);
			nodes.put(i, node);
			context.add(node);
			int x = RandomHelper.nextIntFromTo(0, grid_size - 1);
			int y = RandomHelper.nextIntFromTo(0, grid_size - 1);
			while (!grid.moveTo(node, x, y)) {
				x = RandomHelper.nextIntFromTo(0, grid_size - 1);
				y = RandomHelper.nextIntFromTo(0, grid_size - 1);
			}
		}
		
		network.setNodes(nodes);

		return context;
	}

}
