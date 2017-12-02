import java.io.*;
import java.util.*;

public class Match {

	private HashMap<Colour, Player> players;
	private Board board;
	private PrintWriter log;
	private int numMovesPlayed;
	private boolean verbose;

	public Match(Player whitePlayer, Player blackPlayer, Board board, PrintWriter log, boolean verbose) {
		players = new HashMap<Colour, Player>();
		players.put(Colour.WHITE, whitePlayer);
		players.put(Colour.BLACK, blackPlayer);
		this.board = board;
		this.log = log;
		numMovesPlayed = 0;
		this.verbose = verbose;

		whitePlayer.update(board, Colour.WHITE, numMovesPlayed);
		blackPlayer.update(board, Colour.BLACK, numMovesPlayed);
	}

	private MatchOutcome computeMatchOutcome() {
		boolean whiteKingCaptured = !board.containsPiece(new Piece(PieceType.KING, Colour.WHITE));
		boolean blackKingCaptured = !board.containsPiece(new Piece(PieceType.KING, Colour.BLACK));
		if (!whiteKingCaptured && blackKingCaptured) {
			if (log != null) {
				log.println("Game outcome: white won.");
			}
			return MatchOutcome.WHITE_WINS;
		} else if (whiteKingCaptured && !blackKingCaptured) {
			if (log != null) {
				log.println("Game outcome: black won.");
			}
			return MatchOutcome.BLACK_WINS;
		} else if (whiteKingCaptured && blackKingCaptured) {
			if (log != null) {
				log.println("Game outcome: tie.");
			}
			return MatchOutcome.TIE;
		} else {
			if (log != null) {
				log.println("Game outcome: draw.");
			}
			return MatchOutcome.DRAW;
		}
	}

	public MatchOutcome run() {
		Watch watch = new Watch();

		for (Player player : players.values()) {
			watch.startCounting();
			
			//player.update(board, player.getColour(), numMovesPlayed);
			//player.getColour() won't work at this point!
			
			player.prepareForMatch();
			watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_PREPARE_FOR_MATCH, "prepareForMatch");
		}

		MatchStatus gameStatus = MatchStatus.NOT_OVER;

		if (log != null) {
			board.print(log);
		}

		if (verbose) {
			System.out.println("Board configuration:");
			board.printSmall();
		}

		while (numMovesPlayed < Parameters.MAX_NUM_MOVES && gameStatus != MatchStatus.OVER) {
			Colour currentPlayerColour = (numMovesPlayed % 2 == 0) ? Colour.WHITE : Colour.BLACK;
			Player currentPlayer = players.get(currentPlayerColour);
			currentPlayer.update(board, currentPlayerColour, numMovesPlayed);

			watch.startCounting();
			MoveDescription moveDescription = currentPlayer.chooseMove();
			watch.enforceTimeLimit(currentPlayer, Parameters.TIME_LIMIT_CHOOSE_MOVE, "chooseMove");

			if (log != null) {
				log.println("Move chosen by " + currentPlayer.getColour().toString() + ": " + moveDescription.toString()
						+ "\n");
			}
			
			gameStatus = board.performMove(new Move(moveDescription), currentPlayer.getColour(), gameStatus);

			if (verbose) {
				System.out.println("Board configuration after "
						+ currentPlayer.getColour().toString()
						+ " (" + currentPlayer.getName() + ") plays "
						+ moveDescription.toString() + ":");
				board.printSmall();
			}

			if (log != null) {
				board.print(log);
			}

			++numMovesPlayed;
		}

		MatchOutcome matchOutcome = computeMatchOutcome();

		for (Player player : players.values()) {
			watch.startCounting();
			player.update(board, player.getColour(), numMovesPlayed);
			player.receiveMatchOutcome(matchOutcome.toInt(player.getColour()));
			watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_RECEIVE_MATCH_OUTCOME, "receiveMatchOutcome");
		}

		if (log != null) {
			double[] payoffs = matchOutcome.getPayoffs();
			log.println("Payoffs (white, black): (" + payoffs[0] + ", " + payoffs[1] + ")");
		}

		return matchOutcome;
	}
}
