package solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import problem.Action;
import problem.Cycle;
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

	/**
	 * Solves a tour. Replace existing code with your code.
	 * 
	 * @param tour
	 */
	public void solveTour(Tour tour) {
		// Work out what races we are participating in and with what cycles
		Map<Track, Cycle> races = TourPicker.choose(tour);

		// Register for tracks and buy cycles
		for (Track t : races.keySet()) {
			// Buy the cycle if we don't have it already
			if (!tour.getPurchasedCycles().contains(races.get(t))) {
				tour.buyCycle(races.get(t));
			}
			// Register for the track
			tour.registerTrack(t, 1);
		}

		// Loop - do all the races
		for (Track t : tour.getUnracedTracks()) {
			//Track setup
			ArrayList<Player> players = new ArrayList<Player>();
			Map<String, GridCell> startingPositions = t.getStartingPositions();
			String id = "Glorious Cycle of Ultimate Destiny";
			
			//Iterate over starting positions and use MCTS to find the best
			GridCell startPosition = null;
			
			for (GridCell pos : startingPositions.values()) {
				SearchNode node = new SearchNode(pos, races.get(t), t);
				//Search
				//Compare to previous position
			}
			players.add(new Player(id, races.get(t), startPosition));

			// Start race
			tour.startRace(t, players);
			
			//Race
		}
		// Use MCTS to determine best starting position
		// Loop over actions until goal, using MCTS to pick the best one

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
}
