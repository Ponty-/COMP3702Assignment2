package solver;

import problem.Cycle;
import problem.Track;

public class EvaluatedTrack {
	
	private Track track;
	private Cycle cycle;
	private double prize;
	
	public EvaluatedTrack(Track track, Cycle cycle, Double prize) {
		this.track = track;
		this.cycle = cycle;
		this.prize = prize;
	}
	
	public Track getTrack() {
		return track;
	}
	
	public Cycle getCycle() {
		return cycle;
	}
	
	public Double getPrize() {
		return prize;
	}

}
