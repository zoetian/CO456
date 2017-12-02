public class TeamHandshaker extends Player {
	
	// This will allow us use Handshaking functionality when we play TeamDoubleAgent. 
	// ADD FOLLOWING LINE TO SUPPORT HANDSHAKE
	private Handshaker shaker;
	
	public byte[] bestMoveBytesRealist, scoreWhiteBytesRealist, scoreBlackBytesRealist;

	public TeamHandshaker(int maxNumMoves) {
		TeamRational teamRationalRealist = TeamRational.createRealist(maxNumMoves);

		bestMoveBytesRealist = teamRationalRealist.bestMoveBytes;
		scoreWhiteBytesRealist = teamRationalRealist.scoreWhiteBytes;
		scoreBlackBytesRealist = teamRationalRealist.scoreBlackBytes;

		// ADD FOLLOWING LINE TO SUPPORT HANDSHAKE
		shaker = Handshaker.createHandshakeAccepter();
	}

	public void prepareForSeries() {
		// ADD FOLLOWING LINE TO SUPPORT HANDSHAKE
		shaker.handshakePrepareForSeries(); 
	}

	public void prepareForMatch() {
		// ADD FOLLOWING LINE TO SUPPORT HANDSHAKE
		shaker.handshakePrepareForMatch(toBoardPosition());
	}

	public void receiveMatchOutcome(int matchOutcome) {
		// ADD FOLLOWING LINE TO SUPPORT HANDSHAKE
		shaker.handshakeReceiveMatchOutcome(matchOutcome, toBoardPosition());
	}

	// TO SUPPORT HANDSHAKE:
	// Rename your original chooseMove -> internalChooseMove and use the following as chooseMove():
	public MoveDescription chooseMove() {
		BoardPosition currentBoardPosition = toBoardPosition();
		// update shaker with opponent move
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

	// Your original strategy goes here. Called whenever you are not sending a handshake.
	private MoveDescription internalChooseMove() {
		BoardPosition currentBoardPosition = toBoardPosition();
		
		TeamRational.Node nodeRealist = new TeamRational.Node(
				bestMoveBytesRealist[currentBoardPosition.toInt()],
				scoreWhiteBytesRealist[currentBoardPosition.toInt()],
				scoreBlackBytesRealist[currentBoardPosition.toInt()]);

		return nodeRealist.bestMove;
	}
}
