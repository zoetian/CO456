public class TeamForgiveness extends TeamTitForTat {
	
	private int FORGIVENESS_DELTA;

	public TeamForgiveness(int maxNumMoves) {
		super(maxNumMoves);
		this.BETRAYAL_DELTA=5;
		this.COOPERATION_DELTA=2;
		this.FORGIVENESS_DELTA=1;
	}

	public int updateTrust(int trust, int myMatchPayoff) {
		if (myMatchPayoff < 2) {
			// I didn't take your king? I trust you less now.
			return trust - BETRAYAL_DELTA;
		} else if (opponentHadWinningPosition && myMatchPayoff >= 2) {
			// You gave up a win for a tie/loss? I trust you more now.
			return trust + COOPERATION_DELTA;
		}
		return trust+FORGIVENESS_DELTA;
	}
}
