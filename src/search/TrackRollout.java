package search;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import problem.Action;
import problem.Cycle;
import problem.Distractor;
import problem.GridCell;
import problem.Track;
import problem.RaceSimTools;

//k boss.

public class TrackRollout {
	// Global Fields
	// Discount factor, a decreasing amount to multiply the reward by at each step.
	static private Double DISCOUNT_FACTOR = 1.0;
	// Rate at which the discount factor reduces thereby lowering the reward.
	static final private Double DISCOUNT_RATE = 0.9;
	
	// Reward
	// Empty cell
	static final private Double EMPTY = -0.04;
	// Distractor Reliable cell
	static final private Double DISTRACTOR_RELIABLE = -1.0;
	// Distractor Unreliable cell
	static final private Double DISTRACTOR_UNRELIABLE = -7.5;
	// Obstacle Wild cell
	static final private Double OBSTACLE_WILD = -0.5;
	// Obstacle Domesticated cell
	static final private Double OBSTACLE_DOMESTICATED = -5.0;
	
	// Fields
	// Random Generator for calculating results of an action.
	private Random rGen = new Random();
	// The node the rollout begins from.
	private SearchNode startNode;
	// The cell the cycle is occupying from step to step.
	private GridCell currentCell;
	// Total reward accrued so far.
	private Double totalDiscountedReward;
	// Distractor Matrix for lookup, corresponds with cell row and column, contains distractor probability. 
	// double arrays are automatically filled with 0.0 on creation.
	double[][] distractorMatrix;
	
	
	// Constructor
	public TrackRollout (SearchNode startNode) {
		this.startNode = startNode;
		// Initialise the summation to the starting cell's reward.
		this.totalDiscountedReward = DISCOUNT_FACTOR * reward(startNode.getCell());
		this.distractorMatrix = startNode.getDistractorMatrix();
	}
	
	// Methods
	// This method will begin the simulation run given a starting node and return the simulated value for this path.
	public static double rollout() {
		return 0;
	}
	
	// Returns the probability that the cycle will arrive in the state sPrime in the next step given the current state (currentCell) and action a. 
	private double transition(GridCell sPrime, Action a) {
		return 0.0;
	}
	
	// Moves the cycle given a valid action and returns the cell it arrived in.
	private GridCell step (Action a) {
		// Roll the dice to see which probability occurred. This is the simulation section.
		
		// Stubbed, will contain resultant cell from above.
		GridCell arrivedCell = new GridCell(0,0);
		// Increase the amount rewards are discounted.
		DISCOUNT_FACTOR *= DISCOUNT_RATE;
		// Add the reward to the summation.
		totalDiscountedReward += DISCOUNT_FACTOR * reward(arrivedCell);
		// tell the rollout where we landed.
		return arrivedCell;
	}

	private Double reward(GridCell cell) {
		// Get the track to determine factors.
		Track t = startNode.getTrack();
		// Get the cycle for the same.
		Cycle c = startNode.getCycle();
		// Cell Attributes
		int cellRow = cell.getRow();
		int cellCol = cell.getCol();
		// Determine reward of a given cell, take into account distractors and obstacles.
		// OBSTACLE
		if (t.getCellType(cell) == Track.CellType.OBSTACLE) {
			if (c.isWild()) {
				return OBSTACLE_WILD;
			} else {
				return OBSTACLE_DOMESTICATED;
			}
		}
		// DISTRACTOR
		// Get the probability of a distractor occurring at the cell from the distractor matrix.
		double dProbability = distractorMatrix[cellRow][cellCol];
		// If the probability is non zero
		if (dProbability != 0.0) {
			// and the cycle is reliable
			if (c.isReliable()) {
				return DISTRACTOR_RELIABLE * dProbability; 
			} else {
				// and the cycle is unreliable
				return DISTRACTOR_UNRELIABLE * dProbability;
			}
		}
		// Cell isn't an obstacle and has zero distractor probability, return empty cell reward.
		return EMPTY;
	}
}
