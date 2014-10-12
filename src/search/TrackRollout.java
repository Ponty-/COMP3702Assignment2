package search;

import java.util.Random;

import problem.Action;
import problem.GridCell;
import problem.Track;

//k boss.

public class TrackRollout {
	// Fields
	// Random Generator for calculating results of an action.
	Random rGen = new Random();
	// The node the rollout begins from.
	SearchNode startNode;
	// The cell the cycle is occupying from step to step.
	GridCell currentCell;
	// Discount factor, a decreasing amount to multiply the reward by at each step.
	Double discountFactor;
	// Rate at which the discount factor reduces thereby lowering the reward.
	Double discountRate;
	// Total reward accrued so far.
	Double totalDiscountedReward;
	
	// Constructor
	public TrackRollout (SearchNode startNode) {
		this.startNode = startNode;
		this.discountFactor = 1.0;
		this.discountRate = 0.9;
		//Initialise the summation to the starting cell's reward.
		this.totalDiscountedReward = discountFactor * reward(startNode.getCell());
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
		discountFactor *= discountRate;
		// Add the reward to the summation.
		totalDiscountedReward += discountFactor * reward(arrivedCell);
		// tell the rollout where we landed.
		return arrivedCell;
	}

	private Double reward(GridCell cell) {
		// Get the track to determine factors.
		Track t = startNode.getTrack();
		// Determine reward of a given cell, take into account distractors and obstacles.
		// RaceSimTools will be useful here.
		
		// Return the generated reward value.
		return 0.0;
	}
	
	
}
