public class Parameters {
	public static final int NUM_COLUMNS = 4;
	public static final int NUM_ROWS = 4;
	public static final int MAX_NUM_MOVES = 30;
	public static final int NUM_PIECES = 4;

	// Payoff table at the game's end. Tie is when both players won, draw is
	// when the move limit MAX_NUM_MOVES is reached.
	public static final double[] PAYOFFS_WHITE_WINS = { 3.0, 0.0 };
	public static final double[] PAYOFFS_BLACK_WINS = { 0.0, 3.0 };
	public static final double[] PAYOFFS_TIE = { 2.0, 2.0 };
	public static final double[] PAYOFFS_DRAW = { 1.0, 1.0 };

	// Number of rounds in a tournament, followed by a geometric distribution
	// with probability 1/2
	public static int MIN_NUM_DIFFERENT_BOARDS = 38;

	public static final boolean PRINT_LOG = true;
	// public static final boolean PRINT_LOG = false;

	// Rough time limits for each player's constructor, etc. during a
	// tournament.
	public static final double TIME_LIMIT_CONSTRUCTOR = 60; // seconds
	public static final double MEMORY_LIMIT_CONSTRUCTOR = 100; // megabytes
	public static final double TIME_LIMIT_PREPARE_FOR_SERIES = 1;
	public static final double TIME_LIMIT_PREPARE_FOR_MATCH = 1;
	public static final double TIME_LIMIT_CHOOSE_MOVE = .1;
	public static final double TIME_LIMIT_RECEIVE_MATCH_OUTCOME = .1;
}
