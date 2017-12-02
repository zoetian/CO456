import java.util.*;

public class TeamNihilist extends Player {
	private Random rand;

	public TeamNihilist(int maxNumMoves) {
		rand = new Random(System.currentTimeMillis());
	}

	public void prepareForSeries() {
	}

	public void prepareForMatch() {
	}

	public void receiveMatchOutcome(int matchOutcome) {
	}

	public MoveDescription chooseMove() {
		ArrayList<MoveDescription> allPossibleMoves = getAllPossibleMoves();
		return allPossibleMoves.get(rand.nextInt(allPossibleMoves.size()));
	}
}
