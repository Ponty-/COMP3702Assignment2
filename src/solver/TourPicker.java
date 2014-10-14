package solver;

import java.util.ArrayList;
import java.util.HashMap;
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
		/* 2 major cases. can get what we want & can't get what we want
		 case 1: plentiful start money can buy a "perfect" cycle/cycles and register for best 3 races
		 case 2: 
		 a. plentiful money sub optimal cycles & cheap races(which of the bad choices is the best)(likely something like speed vs wild)
		 b. scarce money optimal cycles & expensive races (potential winnings vs quality of cycle)
		 - pick obstacle/distractor heavy only?
		 c. scarce money sub optimal cycles & expensive races (seems like a pain to maximize)
		 - everything is bad wtf is balance
		*/
		
		// Return the decision as as a map of tracks and cycles to use on them
		return null;
	}
	
	// Methods	
	/* ranks tracks based upon expected winnings */
	public static Map<Track, double[]> rankTracks(Tour tour) {
		// Map of tracks and their attributes (weighted prize, obstacle heavy, distractor heavy, opponent heavy)
		Map<Track, double[]> trackOrdering = new HashMap<Track, double[]>();
		List<Track> tracks = tour.getTracks();
		Track currentTrack;
		int noTracks = tracks.size();
		double prize = 0;
		
		//iterate over the tracks and find their attributes and weighted prize
		for(int i = 0; i < noTracks; i++) {
			currentTrack = tracks.get(i);
			double[] attributes = {0,0,0,0};
			prize = currentTrack.getPrize();
			
			//reduce expected prize based on obstacles
			//obstacles/cells
			double obsWeight = percentObs(currentTrack);
			prize = prize*(1 - obsWeight);
			attributes[1] = obsWeight;
			
			//reduce expected prize based on distractors
			//distractors/cells
			double distractorWeight = (currentTrack.getDistractors().size()/(currentTrack.getNumCols()*currentTrack.getNumRows()));
			prize = prize*(1 - distractorWeight);
			attributes[2] = distractorWeight;
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
