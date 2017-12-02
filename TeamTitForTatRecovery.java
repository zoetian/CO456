public class TeamTitForTatRecovery extends Player {

	boolean weHaveWonAGame; // Until we win a game in a series, we play as a Realist. Then we play as Cooperative.

	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;
	public byte[] bestMoveBytesCooperative, scoreWhiteBytesCooperative, scoreBlackBytesCooperative;

	public int BETRAYAL_DELTA = 1;
	public int COOPERATION_DELTA = 1;

	public TeamTitForTatRecovery(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);
		TeamRational teamRationalCooperative = TeamRational.createCooperative(maxNumMoves);
		
		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;

		bestMoveBytesCooperative = teamRationalCooperative.bestMoveBytes;
		scoreWhiteBytesCooperative = teamRationalCooperative.scoreWhiteBytes;
		scoreBlackBytesCooperative = teamRationalCooperative.scoreBlackBytes;
	}

	public void prepareForSeries() {
		weHaveWonAGame=false;
	}

	public void prepareForMatch() {
	}

	public void receiveMatchOutcome(int matchOutcome) {
		int myMatchPayoff = outcomeToPayoff(matchOutcome); //Convert to a more reasonable format...
		if( myMatchPayoff == 3) {
			weHaveWonAGame=true;
		}
	}

	public MoveDescription chooseMove() {

		BoardPosition boardPosition = toBoardPosition();
		
		if (!weHaveWonAGame) {
			TeamRational.Node nodeRealist = new TeamRational.Node(
					bestMoveBytesRealist[boardPosition.toInt()],
					scoreWhiteBytesRealist[boardPosition.toInt()],
					scoreBlackBytesRealist[boardPosition.toInt()]);
			return nodeRealist.bestMove;
		}

		TeamRational.Node nodeCooperative = new TeamRational.Node(
				bestMoveBytesCooperative[boardPosition.toInt()],
				scoreWhiteBytesCooperative[boardPosition.toInt()],
				scoreBlackBytesCooperative[boardPosition.toInt()]);
		return nodeCooperative.bestMove;
	}
}
