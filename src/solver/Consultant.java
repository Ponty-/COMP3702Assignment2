package solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import problem.Action;
import problem.Cycle;
import problem.Distractor;
import problem.GridCell;
import problem.Player;
import problem.RaceSimTools;
import problem.RaceState;
import problem.Tour;
import problem.Track;
import search.SearchNode;

/**
 * Implement your solver here.
 * 
 * @author Joshua Song
 * 
 */
public class Consultant {

	// The time to calculate a single step online in nanoseconds
	private static long STEP_TIME = 1000000000;
	// Badass name for our cycle
	private static String BADASS_NAME = "Glorious Cycle of Ultimate Destiny";

	/**
	 * Solves a tour. Replace existing code with your code.
	 * 
	 * @param tour
	 */
	public void solveTour(Tour tour) {
		// Work out what races we are participating in and with what cycles
		List<EvaluatedTrack> races = TourPicker.choose(tour);

		// Register for tracks and buy cycles
		if (races != null) {
			for (EvaluatedTrack t : races) {
				// Buy the cycle if we don't have it already
				if (!tour.getPurchasedCycles().contains(t.getCycle())) {
					tour.buyCycle(t.getCycle());
				}
				// Register for the track
				tour.registerTrack(t.getTrack(), 1);
			}
		}

		// Loop - do all the races
		for (Track t : tour.getUnracedTracks()) {
			// Track setup
			ArrayList<Player> players = new ArrayList<Player>();
			// Map<String, GridCell> startingPositions =
			// t.getStartingPositions();
			double[][] distractorMatrix = buildDistractorMatrix(t);

			/*
			 * // Iterate over starting positions and use MCTS to find the best
			 * This is now covered in the picker/EvaluatedTrack SearchNode
			 * bestNode = null;
			 * 
			 * for (GridCell pos : startingPositions.values()) { SearchNode node
			 * = new SearchNode(pos, races.get(t), t, buildDistractorMatrix(t));
			 * 
			 * // Search node.loopSearch(STEP_TIME);
			 * 
			 * // Compare to previous position if (bestNode == null) { bestNode
			 * = node; } else if (node.getValue() > bestNode.getValue()) {
			 * bestNode = node; } }
			 */

			// Find the evaluatedtrack for this race
			EvaluatedTrack thisRace = null;
			for (EvaluatedTrack e : races) {
				if (e.getTrack().equals(t)) {
					thisRace = e;
					break;
				}
			}

			// Get the id of the starting position
			String id = null;
			for (String s : t.getStartingPositions().keySet()) {
				if (t.getStartingPositions().get(s).equals(thisRace.getStart())) {
					id = s;
					break;
				}
			}

			// Add player to track at the best starting position
			players.add(new Player(id, thisRace.getCycle(), thisRace
					.getStart()));

			// Start race
			tour.startRace(t, players);
			System.out.println("Number of races: " + tour.getNumRaces());

			// Race - cue music
			while (tour.getLatestRaceState().getStatus() == RaceState.Status.RACING) {
				// Get the current state of the race
				RaceState currentState = tour.getLatestRaceState();
				Player us = currentState.getPlayers().get(0);
				SearchNode root = new SearchNode(us.getPosition(),
						us.getCycle(), t, distractorMatrix);

				// Decide what to do next
				// Search
				root.loopSearch(STEP_TIME);
				// Select the best action and take a step
				ArrayList<Action> actions = new ArrayList<Action>();
				actions.add(root.bestAction());
				tour.stepTurn(actions);
			}
		}

	}

	/**
	 * Solves a tour. Replace existing code with your code.
	 * 
	 * @param tour
	 */
	public void solveTourExample(Tour tour) {

		// You should get information from the tour using the getters, and
		// make your plan.

		// Example: Buy the first cycle that is Wild
		List<Cycle> purchasableCycles = tour.getPurchasableCycles();
		Cycle cycle = null;
		for (int i = 0; i < purchasableCycles.size(); i++) {
			cycle = purchasableCycles.get(i);
			if (cycle.isWild()) {
				tour.buyCycle(cycle);
				break;
			}
		}

		// Example: register for as many tracks as possible
		List<Track> allTracks = tour.getTracks();
		for (Track t : allTracks) {
			tour.registerTrack(t, 1);
		}

		while (!tour.isFinished()) {

			if (tour.isPreparing()) {

				// Race hasn't started. Choose a track, then prepare your
				// players by choosing their cycles and start positions

				// Example:
				Track track = tour.getUnracedTracks().get(0);
				ArrayList<Player> players = new ArrayList<Player>();
				Map<String, GridCell> startingPositions = track
						.getStartingPositions();
				String id = "";
				GridCell startPosition = null;
				for (Map.Entry<String, GridCell> entry : startingPositions
						.entrySet()) {
					id = entry.getKey();
					startPosition = entry.getValue();
					break;
				}
				players.add(new Player(id, cycle, startPosition));

				// Start race
				tour.startRace(track, players);
			}

			// Decide on your next action here. tour.getLatestRaceState()
			// will probably be helpful.

			// Example: Output current position of player, and current state
			RaceState state = tour.getLatestRaceState();
			System.out.println("Player position: "
					+ state.getPlayers().get(0).getPosition());
			Track track = tour.getCurrentTrack();
			System.out.println(RaceSimTools.stateToString(state, track));

			// Example: Keep moving forward slowly
			ArrayList<Action> actions = new ArrayList<Action>();
			actions.add(Action.FS);
			tour.stepTurn(actions);

		}
	}

	// Creates a matrix of values representing the distractor probabilities for
	// each cell
	// non-zero probability means a distractor can occur in the cell.
	public static double[][] buildDistractorMatrix(Track t) {
		// Initialise the distractor matrix.
		// Get the list of cells that have possible distractors.
		List<Distractor> distractors = t.getDistractors();
		// Store it as an Array.
		ArrayList<Distractor> d = new ArrayList<Distractor>();
		d.addAll(distractors);
		// Build the distractor matrix.
		double[][] distractorMatrix = new double[t.getNumRows()][t.getNumCols()];
		// Load the matrix with the probabilities.
		for (int i = 0; i < d.size(); i++) {
			int dRow = d.get(i).getPosition().getRow();
			int dCol = d.get(i).getPosition().getCol();
			double dProbability = d.get(i).getAppearProbability();
			distractorMatrix[dRow][dCol] = dProbability;
		}
		// Give him the blue pill.
		return distractorMatrix;
	}

}
