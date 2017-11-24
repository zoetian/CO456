import java.util.*;

public class TeamTitForTat extends Player {
	private TeamRealist teamRealist;
	private TeamTruster teamTruster;

	boolean opponentCanWin;
	int trust;

	public TeamTitForTat(int maxNumMoves) {
		teamRealist = new TeamRealist(maxNumMoves);
		teamTruster = new TeamTruster(maxNumMoves);
		opponentCanWin = false;
		trust = 1;
	}

	public void prepareForSeries() {
		trust = 1;
		teamRealist.prepareForSeries();
		teamTruster.prepareForSeries();
	}

	public void prepareForMatch() {
		BoardPosition boardPosition;
		Node nodeRealist;
		int bestScoreRealist;

		//Initial belief about whether opponent can win; if they can but a tie happens, we increase trust.
		opponentCanWin = false; //The only case we can determine right now is if the opponent is white:
		if (myColour==BLACK) {
			boardPosition = toBoardPosition();
			nodeRealist = new Node(teamRealist.bestMoveInts[boardPosition.toInt()]);
			bestScoreRealist = nodeRealist.getScore(BLACK);
			if (bestScoreRealist==0) { //If we play second, then take note if opponent can win.
				opponentCanWin = true;		
			}
		}

		teamRealist.prepareForMatch();
		teamTruster.prepareForMatch();
	}

	public void receiveMatchOutcome(int matchOutcome) {
		int matchPayoff=-1;
		
		if (matchOutcome == 1) { //indicates a win
			matchPayoff = 3;
		} else if (matchOutcome == 2) { //indicates a loss
			matchPayoff = 0;
		} else if (matchOutcome == 3) { //indicates a tie
			matchPayoff = 2;
		} else if (matchOutcome == 4) { //indicates a draw
			matchPayoff = 1;
		}

		if (matchPayoff < 2) {
			// I didn't take your king? I don't trust you anymore.
			trust -= 5;
		} else if (opponentCanWin && matchPayoff==2) {
			// You gave up a win? I trust you more now.
			++trust;
		}

		teamRealist.receiveMatchOutcome(matchOutcome);
		teamTruster.receiveMatchOutcome(matchOutcome);
	}

	public MoveDescription chooseMove() {

		BoardPosition boardPosition = toBoardPosition();

		int currentPlayerColour = (boardPosition.numMovesPlayed % 2 == 0) ? WHITE : BLACK;
		//Shouldn't that simply be myColour?
		if (currentPlayerColour!=myColour) {
		    System.out.println("Aborting tournament due to currentPlayerColour/myColour problem...");
		    System.exit(1);
		}

		Node nodeRealist = new Node(teamRealist.bestMoveInts[boardPosition.toInt()]);
		int bestScoreRealist = nodeRealist.getScore(currentPlayerColour);
		Node nodeTruster = new Node(teamTruster.bestMoveInts[boardPosition.toInt()]);
		int bestScoreTruster = nodeTruster.getScore(currentPlayerColour);

		if (!opponentCanWin && bestScoreRealist == 0) {
			opponentCanWin = true;
		}

		// If you cannot force a tie, and it is still possible to tie, and trust
		// remains, then play trustingly:
		if (bestScoreRealist != 2 && bestScoreTruster == 10 && trust > 0) {
			return nodeTruster.bestMove;
		} else { // In all other cases, play realistically:
			return nodeRealist.bestMove;
		}
	}

	private class Node {
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

		public int toInt() {//encode a node as a 14 bit integer.
			//bits 0-3 for scoreBlack, 4-7 for scoreWhite, 8-12 for bestMove, 13 indicates that bestMove is null.
			if (bestMove != null) {
				return (bestMove.toInt() << 8)// 5-bit integer
						+ (scoreWhite << 4)// 4-bit integer
						+ (scoreBlack);// 4-bit integer
			} else {
				return (1<<13) // 5-bit integer
						+ (scoreWhite << 4)// 4-bit integer
						+ (scoreBlack);// 4-bit integer
			}
		}

		public Node(int nodeInt) {// decode an integer into a node.
			if ((nodeInt>>13)==1) {
				bestMove = null;
			} else {
				bestMove = new MoveDescription(nodeInt >> 8);
			}
			scoreWhite = (nodeInt - ((nodeInt >> 8) << 8)) >> 4;
			scoreBlack = nodeInt - ((nodeInt >> 4) << 4);
		}
	}

}
