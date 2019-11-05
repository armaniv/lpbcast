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
		int unsub_rate = params.getInteger("unsub_rate");
		boolean age_purging = params.getBoolean("age_purging");
		boolean frequency_purging = Boolean.getBoolean(params.getString("frequency_purging"));
		
		// create a grid
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), false, grid_size, grid_size));
		
		// Network to show edges between nodes which means 
		// that a message between them has been sent
		NetworkBuilder<Object> networkBuilder = new NetworkBuilder<Object>("network", context, true);
		networkBuilder.buildNetwork();
		
		// this is the actual entity which manages communication
		Router router = new Router();
		
		ApplicationNode appNode = new ApplicationNode(node_count, n_messages, churn_rate, unsub_rate);
		context.add(appNode);
		
		HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
		for (int i = 0; i < node_count; i++) {
			//Node node = new Node(i, grid, router, max_l, max_m, fanout, initial_neighbors, round_k, round_r, age_purging);
			Node node = new Node(i, grid, router, max_l, max_m, fanout, initial_neighbors, 
					round_k, round_r, age_purging, frequency_purging, 1);
			nodes.put(i, node);
			context.add(node);
			appNode.addNode(node);
			int x = RandomHelper.nextIntFromTo(0, grid_size - 1);
			int y = RandomHelper.nextIntFromTo(0, grid_size - 1);
			while (!grid.moveTo(node, x, y)) {
				x = RandomHelper.nextIntFromTo(0, grid_size - 1);
				y = RandomHelper.nextIntFromTo(0, grid_size - 1);
			}
		}
		
		router.setNodes(nodes);
		return context;
	}

}
