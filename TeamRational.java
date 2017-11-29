

import java.util.*;

public class TeamRational extends Player {
	// A BoardPosition includes the piece positions, the number of moves played, and myColour.
	// We attach to each one a Node: bestMove, scoreWhite, scoreBlack (assuming perfect play.)
	public byte[] bestMoveBytes;
	public byte[] scoreWhiteBytes;
	public byte[] scoreBlackBytes;

	int maxNumMoves; // Called GameLimit in the project specs; game cannot have more than this many moves.
	private Random rand; // For choosing random moves

	public int payoff1WW, payoff1WL, payoff1LW, payoff1LL, payoff2WW, payoff2WL, payoff2LW, payoff2LL;
	/*These give the payoff table, according to this player's beliefs:
	 * 
	 * P1\P2 |         W           |          L          | 
	 * ---------------------------------------------------
	 *   W   | payoff1WW,payoff2WW | payoff1WL,payoff2WL |
	 * ---------------------------------------------------
	 *   L   | payoff1LW,payoff2LW | payoff1LL,payoff2LL |
	 * ---------------------------------------------------*/
	public TeamRational(int maxNumMoves,
			int payoff1WW, int payoff1WL,
			int payoff1LW, int payoff1LL,
			int payoff2WW, int payoff2WL,
			int payoff2LW, int payoff2LL) {

		this.maxNumMoves = maxNumMoves;

		this.payoff1WW=payoff1WW; this.payoff1WL=payoff1WL;
		this.payoff1LW=payoff1LW; this.payoff1LL=payoff1LL;
		this.payoff2WW=payoff2WW; this.payoff2WL=payoff2WL;
		this.payoff2LW=payoff2LW; this.payoff2LL=payoff2LL;

		this.rand = new Random(System.currentTimeMillis());

		this.bestMoveBytes = new byte[BoardPosition.MAX_INT];
		for (int i=0; i<BoardPosition.MAX_INT; i++) {
			this.bestMoveBytes[i]=Node.UNINITIALIZED;
		}
		this.scoreWhiteBytes = new byte[BoardPosition.MAX_INT];
		this.scoreBlackBytes = new byte[BoardPosition.MAX_INT];

		LinkedList<BoardPosition> allInitialBoardPositions = BoardPosition.getAllInitialBoardPositions();
		for (BoardPosition boardPosition : allInitialBoardPositions) {
			computeBestMove(boardPosition);
		}
	}

	public TeamRational(int maxNumMoves) {
		//Default's to the TeamRealist payoff table:
		this(maxNumMoves, 2, 3,	0, 1, 2, 0,	3, 1);
	}

	
	public void prepareForSeries() {
	}

	public void prepareForMatch() {
	}

	public void receiveMatchOutcome(int matchOutcome) {
	}

	public MoveDescription chooseMove() {
		BoardPosition boardPosition = toBoardPosition();
		return new MoveDescription((int) bestMoveBytes[boardPosition.toInt()]);
	}

	//We attach a Node (bestMove,scoreWhite,scoreBlack) to a BoardPosition (piece locations, numMovesPlayed, myColour)
	//which indicates that at perfect play, bestMove is allowed, and the scores will be as indicated.
	private byte[] computeBestMove(BoardPosition boardPosition) {

		int currentPositionInt = boardPosition.toInt();
		
		if (this.bestMoveBytes[currentPositionInt] != Node.UNINITIALIZED) {
			return new byte[] {this.bestMoveBytes[currentPositionInt],
					this.scoreWhiteBytes[currentPositionInt],this.scoreBlackBytes[currentPositionInt]};
		}

		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;

		//Now setup the payoff table:
		int[] payoffsWhiteWins, payoffsBlackWins, payoffsTie, payoffsDraw;

		if (boardPosition.myColour==WHITE) { //If player 1 is white:
			payoffsWhiteWins = new int[] {payoff1WL,payoff2WL};
			payoffsBlackWins = new int[] {payoff1LW,payoff2LW};
			payoffsTie = new int[] {payoff1WW,payoff2WW};
			payoffsDraw = new int[] {payoff1LL,payoff2LL};
		} else { //If player 1 is black:
			payoffsWhiteWins = new int[] {payoff2LW,payoff1LW};
			payoffsBlackWins = new int[] {payoff2WL,payoff1WL};
			payoffsTie = new int[] {payoff2WW,payoff1WW};
			payoffsDraw = new int[] {payoff2LL,payoff1LL};
		}

		Node ret = null; // Node to be returned by this function.

		// ---> First we go through the cases where the game has ended:
		if (boardPosition.whiteKingPosition == 0 && boardPosition.blackKingPosition == 0) {
			// if both kings have been captured, the game has ended
			ret = new Node(null, payoffsTie);
		} else if (boardPosition.whiteKingPosition == 0 && currentPlayerColour == BLACK) {
			// if white king has been captured and it's black player's turn, the
			// game has ended black won
			ret = new Node(null, payoffsBlackWins);
		} else if (boardPosition.blackKingPosition == 0 && currentPlayerColour == WHITE) {
			// if black king has been captured and it's white player's turn, the
			// game has ended white won
			ret = new Node(null, payoffsWhiteWins);
		} else if (currentPlayerColour == WHITE && boardPosition.whiteKingPosition == 0
				&& boardPosition.whiteRookPosition == 0) {
			// if it's white player's turn but he has no pieces left, the game
			// has ended black won
			ret = new Node(null, payoffsBlackWins);
		} else if (currentPlayerColour == BLACK && boardPosition.blackKingPosition == 0
				&& boardPosition.blackRookPosition == 0) {
			// if it's black player's turn but he has no pieces left, the game
			// has ended white won
			ret = new Node(null, payoffsWhiteWins);
		} else if (boardPosition.numMovesPlayed >= maxNumMoves) {
			// if we have reached the maximum number of moves, the game is over
			if (boardPosition.whiteKingPosition != 0 && boardPosition.blackKingPosition != 0) {
				// draw
				ret = new Node(null, payoffsDraw);
			} else if (boardPosition.blackKingPosition == 0) {
				// white won
				ret = new Node(null, payoffsWhiteWins);
			} else {
				// !whiteKingRemains
				// black won
				ret = new Node(null, payoffsBlackWins);
			}
		} else {
			// ---> Otherwise, the game has not ended and we explore all possible moves:
			ArrayList<MoveDescription> allPossibleMoves = boardPosition.getAllPossibleMoves();
			ArrayList<Node> allBestNodes = new ArrayList<Node>();
			int bestScore = 0;
			for (MoveDescription moveDescription : allPossibleMoves) {
				BoardPosition newBoardPosition = boardPosition.doMove(moveDescription);
				Node node = new Node(computeBestMove(newBoardPosition));
				if (allBestNodes.size() == 0) {
					bestScore = node.getScore(currentPlayerColour);
					allBestNodes.add(new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK)));
				} else if (node.getScore(currentPlayerColour) == bestScore) {
					allBestNodes.add(new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK)));
				} else if (node.getScore(currentPlayerColour) > bestScore) {
					bestScore = node.getScore(currentPlayerColour);
					allBestNodes = new ArrayList<Node>();
					allBestNodes.add(new Node(moveDescription, node.getScore(WHITE), node.getScore(BLACK)));
				}
			}
			ret = allBestNodes.get(rand.nextInt(allBestNodes.size()));
		}

		// Add result to array!
		byte [] nodeBytes = ret.toBytes();
		bestMoveBytes[currentPositionInt] = nodeBytes[0];
		scoreWhiteBytes[currentPositionInt] = nodeBytes[1];
		scoreBlackBytes[currentPositionInt] = nodeBytes[2];

		return nodeBytes;
	}

	public static class Node {

		public static final int UNINITIALIZED = -2;
		public static final int NULL = -1;
		
		public MoveDescription bestMove;
		public int scoreWhite, scoreBlack;

		public Node(MoveDescription bestMove_, int scoreWhite_, int scoreBlack_) {
			bestMove = bestMove_;
			scoreWhite = scoreWhite_;
			scoreBlack = scoreBlack_;
		}

		public Node(MoveDescription bestMove_, int[] score_) {
			this(bestMove_, score_[WHITE], score_[BLACK]);
		}

		public int getScore(int colour) {
			return (colour == WHITE) ? scoreWhite : scoreBlack;
		}

		public byte[] toBytes() {//encode a node as three bytes.
			if (bestMove != null) {
				return new byte[] {(byte) bestMove.toInt(), (byte) scoreWhite, (byte) scoreBlack};
			} else {
				return new byte[] {(byte) Node.NULL, (byte) scoreWhite, (byte) scoreBlack};
			}
		}

		public Node(byte[] nodeBytes) {// decode an integer into a node.
			this(nodeBytes[0],nodeBytes[1],nodeBytes[2]);
		}

		public Node(byte bestMoveByte, byte scoreWhiteByte, byte scoreBlackByte) {
			if (bestMoveByte==Node.NULL || bestMoveByte==Node.UNINITIALIZED) {
				bestMove = null;
			} else {
				bestMove = new MoveDescription((int) bestMoveByte);
			}
			scoreWhite = (int) scoreWhiteByte;
			scoreBlack = (int) scoreBlackByte;
		}
	}

	public static TeamRational createOptimist(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, 4, 3, 3, 2, 4, 3, 3, 2);
	}

	public static TeamRational createPessimist(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, 2, 3, 0, 1, -2, -3, 0, -1);
	}

	public static TeamRational createQueller(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, -2, 0, -3, -1, 2, 0, 3, 1);
	}

	public static TeamRational createRealist(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, 2, 3, 0, 1, 2, 0, 3, 1);
	}

	public static TeamRational createScrapper(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, -2, 0, -3, -1, -2, -3, 0, -1);
	}

	public static TeamRational createTruster(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, 10, 9, 6, 5, 10, 6, 9, 5);
	}

	public static TeamRational createUtilitarian(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, 4, 3, 3, 2, 4, 3, 3, 2);
	}

	public static TeamRational createCooperative(Integer maxNumMoves) {
		return new TeamRational(maxNumMoves, 3, 2, 0, 1, 3, 0, 2, 1);
	}
}
