public class TeamDoubleAgent extends Player {

	// This will allow us use Handshaking functionality when we play an Alliance handshaker. 
	private Handshaker shaker;

	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;
	public byte[] bestMoveBytesDoubleAgent, scoreWhiteBytesDoubleAgent, scoreBlackBytesDoubleAgent;

	public TeamDoubleAgent(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);
		TeamRational teamDoubleAgent = new TeamRational(maxNumMoves, 2, 0, 3, 1, 2, 0, 3, 1);
		/*TeamDoubleAgent has the following beliefs as P1: 
		 * P1\P2 |  W  |  L  | 
		 * -------------------
		 *   W   | 2,2 | 0,0 |
		 * -------------------
		 *   L   | 3,3 | 1,1 |
		 * -------------------
		 * Or in other words, he is rooting for his opponent.*/

		//Data needed to play as a Realist:
		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;

		//Data needed to play for the opponent's payoff:
		bestMoveBytesDoubleAgent = teamDoubleAgent.bestMoveBytes;
		scoreWhiteBytesDoubleAgent = teamDoubleAgent.scoreWhiteBytes;
		scoreBlackBytesDoubleAgent = teamDoubleAgent.scoreBlackBytes;

		shaker = Handshaker.createHandshakeOfferer();
	}

	public void prepareForSeries() {
		shaker.handshakePrepareForSeries(); 
	}

	public void prepareForMatch() {
		shaker.handshakePrepareForMatch(toBoardPosition());
	}

	public void receiveMatchOutcome(int matchOutcome) {
		shaker.handshakeReceiveMatchOutcome(matchOutcome, toBoardPosition());
	}

	public MoveDescription chooseMove() {
		BoardPosition currentBoardPosition = toBoardPosition();

		shaker.updateTheirMove(currentBoardPosition);

		MoveDescription myMove;
		if (shaker.shouldSendHandshakeMove()) {
			myMove=Handshaker.getHandshakeMove(currentBoardPosition);
		} else {
			myMove = internalChooseMove();
		}

		shaker.receiveMyMove(myMove);
		return myMove;
	}

	private MoveDescription internalChooseMove() {
		BoardPosition currentBoardPosition = toBoardPosition();
		MoveDescription move;
		
		if (shaker.handshakeHasFailed()) {
			TeamRational.Node nodeRealist = new TeamRational.Node(
				bestMoveBytesRealist[currentBoardPosition.toInt()],
				scoreWhiteBytesRealist[currentBoardPosition.toInt()],
				scoreBlackBytesRealist[currentBoardPosition.toInt()]);
			move=nodeRealist.bestMove;
		} else {
			TeamRational.Node nodeDoubleAgent = new TeamRational.Node(
					bestMoveBytesDoubleAgent[currentBoardPosition.toInt()],
					scoreWhiteBytesDoubleAgent[currentBoardPosition.toInt()],
					scoreBlackBytesDoubleAgent[currentBoardPosition.toInt()]);
				move=nodeDoubleAgent.bestMove;
		}
		return move;
	}
}
