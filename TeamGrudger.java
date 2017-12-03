public class TeamGrudger extends TeamTitForTat {
	
	public TeamGrudger(int maxNumMoves) {
		super(maxNumMoves);
	}

	public int updateTrust(int trust, int myMatchPayoff) {
		if (myMatchPayoff < 2) {
			// I didn't take your king? I'll never trust you!!!
			return 0;
		}
		return trust;
	}
}
