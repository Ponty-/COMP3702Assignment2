package search;

import java.util.Map;

import problem.Cycle;
import problem.GridCell;
import problem.Action;

public class SearchNode {
	private GridCell cell; //The cell this node represents
	private Cycle cycle; //The cycle being used for the search
	private Map<Action, SearchNode> children; //Map of actions to child nodes
	private int visits; //Number of times this node is visited
	private double avgReward; //Average discounted reward from rollouts (Q(s,a))
	
	public SearchNode (GridCell cell, Cycle cycle) {
		this.cell = cell;
		this.cycle = cycle;
		avgReward = 0;
		visits = 0;
	}
}
