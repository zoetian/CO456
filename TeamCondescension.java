public class TeamCondescension extends TeamTitForTat {

	public int IRRATIONALITY_DELTA, SUBOPTIMALITY_DELTA;

	public TeamCondescension(int maxNumMoves) {
		super(maxNumMoves);
		this.IRRATIONALITY_DELTA=3;
		this.SUBOPTIMALITY_DELTA=1;
	}
	
	public void prepareForSeries() {
		trust = 0;
	}

	public int updateTrust(int trust, int myMatchPayoff) {
		if (trust>0) { // I trusted you! Let's see how you did:
			if (myMatchPayoff < 2) {
				// I didn't take your king? I trust you less now.
				return trust - BETRAYAL_DELTA;
			} else if (myMatchPayoff == 3) {
				// I tried to tie, but I won!!! I don't trust that you know what you're doing.
				return trust - IRRATIONALITY_DELTA;
			}
		} else if (opponentHadWinningPosition && myMatchPayoff == 2) {
			// I didn't trust you. I'm very picky about restoring trust!
			// You gave up a win for a tie? I trust you more now.
			return trust + COOPERATION_DELTA;
		} else if (opponentHadWinningPosition && myMatchPayoff != 2) {
			// I don't believe that you needed to let me win.
			return trust - SUBOPTIMALITY_DELTA;
		}
		
		return trust;
	}
}
