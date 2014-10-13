package solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import problem.Distractor;
import problem.GridCell;
import problem.Track;
import problem.Cycle;
import problem.Tour;
import problem.Track.CellType;

public class TourPicker {
	public static Map<Track, Cycle> choose(Tour tour) {
		// Pick the best 3 races from the tour and the cycles to use on them
		
		// Load this with the index of the track we're about to test. Stubbed to 99.
		Track track = tour.getTrack(99);
		// Load the builder with the track.
		double[][] distractorMatrix = buildDistractorMatrix(track);
		// Now send this downward as a parameter on SearchNode() 
		// or however you want to make it accessible to my reward function.
		
		// Return the decision as as a map of tracks and cycles to use on them
		return null;
	}
	
	// Methods
	// Creates a matrix of values representing the distractor probabilities for each cell
	// non-zero probability means a distractor can occur in the cell.
	public static double[][] buildDistractorMatrix (Track t) {
		// Initialise the distractor matrix.
		// Get the list of cells that have possible distractors.
		List<Distractor> distractors = t.getDistractors();
		// Store it as an Array.
		ArrayList<Distractor> d = new ArrayList<Distractor>();
		d.addAll(distractors);
		// Build the distractor matrix.
		double[][] distractorMatrix = new double [t.getNumRows()] [t.getNumCols()];
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
	
	/* ranks tracks based upon expected winnings */
	public static List<Track> rankTracks(Tour tour) {
		List<Track> trackOrdering = new ArrayList<Track>();
		List<Track> tracks = tour.getTracks();
		Track currentTrack;
		int noTracks = tracks.size();
		double prize = 0;
		for(int i = 0; i < noTracks; i++) {
			currentTrack = tracks.get(i);
			prize = currentTrack.getPrize();
			//reduce expected prize based on obstacles
			prize = prize*(1 - percentObs(currentTrack));
			//reduce expected prize based on distractors
			prize = prize*(1 - currentTrack.getDistractors().size());
			//TODO:add elements and order them
		}
		
		return trackOrdering;
	}
	
	/* iterates over each cell of the track and finds what the percentage is of obstacles to cells */
	public static double percentObs(Track track) {
		double output = 0.0;
		int obsCount = 0;
		double cellCount = track.getNumRows() * track.getNumCols();
		for(int j = 0; j < track.getNumRows(); j++) {
			for(int k = 0; k < track.getNumCols(); k++) {
				GridCell cell = new GridCell(j,k);
				if(track.getCellType(cell).equals(CellType.OBSTACLE)) {
					obsCount++;
				}
			}
		}
		output = obsCount/cellCount;
		return output;
	}
}
