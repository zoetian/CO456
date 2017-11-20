import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Tournament {

	private int NUM_PLAYERS = 20; // TODO: double check when testing
	private TournamentMode tournamentMode;
	private Random rand;
	private DecimalFormat numberFormat;
	private Watch watch;
	private boolean verbose;

	public Tournament(TournamentMode tournamentMode_, boolean verbose_) {
		tournamentMode = tournamentMode_;
		rand = new Random(0);
		numberFormat = new DecimalFormat("#.000");
		watch = new Watch();
		verbose = verbose_;
	}

	public Player getPlayer(int id) {
		if (0 <= id && id < NUM_PLAYERS) {
			watch.startMemoryComparison();
			watch.startCounting();
			Player player = null;
			System.out.println("Calling Constructor of player " + Integer.toString(id));
			switch (id) {
			case 0:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 18:
			case 19:
				player = new TeamRealist(Parameters.MAX_NUM_MOVES);
				break;
			case 20:
				player = new TeamSlowRealist(Parameters.MAX_NUM_MOVES);
				break;
			case 1:
				player = new TeamNihilist(Parameters.MAX_NUM_MOVES);
				break;
			case 2:
				player = new TeamMonkey(Parameters.MAX_NUM_MOVES);
				break;
			case 3:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
				player = new TeamWatermelon(Parameters.MAX_NUM_MOVES);
				break;
			//Cases need to be added to test tournament with more players.
			}
			watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_CONSTRUCTOR, "constructor");
			System.out.println("Constructor of player " + player.getName() + " took "
					+ numberFormat.format(watch.getElapsedTime()) + " seconds.");
			watch.enforceMemoryLimit(player, Parameters.MEMORY_LIMIT_CONSTRUCTOR, "constructor");
			System.out.println("Constructor of player " + player.getName() + " used "
					+ Long.toString(watch.getMemoryUse()) + " megabytes.\n");
			return player;
		}
		throw new RuntimeException("Invalid player id.");
	}

	public Cell[] randomStartingPositions() {
		int numCells = Parameters.NUM_COLUMNS * Parameters.NUM_ROWS;
		Cell[] allCells = new Cell[numCells];
		int pos = 0;
		for (int column = 1; column <= Parameters.NUM_COLUMNS; ++column) {
			for (int row = 1; row <= Parameters.NUM_ROWS; ++row) {
				allCells[pos ++] = new Cell(column, row);
			}
		}
		Cell positions[] = new Cell [Parameters.NUM_PIECES];
		for (int i = 0; i < Parameters.NUM_PIECES; ++i) {
			int j = rand.nextInt(numCells);
			positions[i] = allCells[j];
			allCells[j] = allCells[numCells - 1];
			--numCells;
		}
		return positions;
	}

	private int getNumBoards() {
		int numBoards = Parameters.MIN_NUM_DIFFERENT_BOARDS;
		while (rand.nextBoolean()) {
			++numBoards;
		}
		return numBoards;
	}

	public void run() throws FileNotFoundException, UnsupportedEncodingException {

		PrintWriter log = null;

		if (Parameters.PRINT_LOG) {
			log = new PrintWriter("../logs/logs.tex", "UTF-8");
			log.println("\\documentclass[11pt,letterpaper]{article}");
			log.println("\\usepackage{xskak,chessboard}");
			log.println("\\usepackage[margin=0.2in]{geometry}");
			log.println("\\begin{document}");
		}

		// initialize players

		Player[] allPlayers = new Player[NUM_PLAYERS];
		double[] totalScore = new double[NUM_PLAYERS];

		for (int playerId = 0; playerId < NUM_PLAYERS; ++playerId) {
			allPlayers[playerId] = getPlayer(playerId);
		}

		// pair off players
		int[][] pairsOfPlayers;
		if (tournamentMode == TournamentMode.FULL) {
			pairsOfPlayers = new int[(NUM_PLAYERS * (NUM_PLAYERS - 1)) / 2][];
			int cnt = 0;
			for (int idPlayer1 = 0; idPlayer1 < NUM_PLAYERS; ++idPlayer1) {
				for (int idPlayer2 = idPlayer1 + 1; idPlayer2 < NUM_PLAYERS; ++idPlayer2) {
					pairsOfPlayers[cnt++] = new int[] {idPlayer1, idPlayer2};
				}
			}
		} else {
			pairsOfPlayers = new int[NUM_PLAYERS - 1][];
			int idPlayer1 = 0;
			int cnt = 0;
			for (int idPlayer2 = 1; idPlayer2 < NUM_PLAYERS; ++idPlayer2) {
				pairsOfPlayers[cnt++] = new int[] {idPlayer1, idPlayer2};
			}
		}

		for (int[] playersInThisRound : pairsOfPlayers) {
			System.out.println("*********************");
			System.out.println("Starting new round.");

			// generate boards
			int numBoards = getNumBoards();

			Cell[][] startingPositions = new Cell[numBoards][];
			for (int t = 0; t < numBoards; ++t) {
				startingPositions[t] = randomStartingPositions();
			}

			Player[] players =
					new Player[] {allPlayers[playersInThisRound[0]], allPlayers[playersInThisRound[1]]};

			// prepare players for round

			for (Player player : players) {
				watch.startCounting();
				player.prepareForSeries();
				watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_PREPARE_FOR_SERIES, "prepareForSeries()");
			}

			double[] averagePayoffInThisRound = new double[2];


			for (int i = 0; i <= 1; ++i) {
				System.out.println("Team " + (i + 1) + ": " + players[i].getName());
			}

			for (int t = 0; t < 2 * numBoards; ++t) {
				if (Parameters.PRINT_LOG) {
					log.println("\n\\clearpage\n");
					log.println("Starting match:\n");
					log.println("White player: " + players[t % 2].getName() + "\n");
					log.println("Black player: " + players[(t + 1) % 2].getName() + "\n");
				}
				if (verbose) {
					System.out.println("*********************");
					System.out.println("Starting match:\n");
					System.out.println("White player: " + players[t % 2].getName() + "\n");
					System.out.println("Black player: " + players[(t + 1) % 2].getName() + "\n");
				}
				Board board = new Board(startingPositions[t / 2]);
				Match match = new Match(players[t % 2], players[(t + 1) % 2], board, log, verbose);
				MatchOutcome matchOutcome = match.run();
				if (verbose) {
					System.out.println("Outcome: " + matchOutcome.toString());
				} else {
					switch (matchOutcome) {
					case WHITE_WINS:
						System.out.print(1 + t % 2);
						break;
					case BLACK_WINS:
						System.out.print(1 + (t + 1) % 2);
						break;
					case TIE:
						System.out.print('T');
						break;
					case DRAW:
						System.out.print('D');
					}
				}
				double[] payoffs = matchOutcome.getPayoffs();
				for (int i = 0; i <= 1; ++i) {
					averagePayoffInThisRound[i] += payoffs[(t % 2 == 0) ? i : 1 - i] / (2 * numBoards);
				}
			}
			System.out.println();
			System.out.println("Round ended");
			for (int i = 0; i <= 1; ++i) {
				System.out.println("Average payoff of player " + players[i].getName() +
						": " + numberFormat.format(averagePayoffInThisRound[i]));
				totalScore[playersInThisRound[i]] += averagePayoffInThisRound[i];
			}
			System.out.println("*********************");
		}

		if (tournamentMode == TournamentMode.INDIVIDUAL) {
			totalScore[0] /= NUM_PLAYERS - 1;
		}

		if (Parameters.PRINT_LOG) {
			log.println("\\end{document}");
			log.close();
		}
		printRanking(allPlayers, totalScore);
	}

	public void printRanking(Player[] allPlayers, double[] totalScore) {

		Integer[] rankedPlayers = new Integer[NUM_PLAYERS];
		for (int i = 0; i < NUM_PLAYERS; ++i) {
			rankedPlayers[i] = i;
		}
		Arrays.sort(rankedPlayers, new ScoreComparator(totalScore));
		System.out.println("Final ranking:");
		for (int i = 0; i < NUM_PLAYERS; ++i) {
			System.out.println(i + " (" + numberFormat.format(totalScore[rankedPlayers[i]]) + ") : " + allPlayers[rankedPlayers[i]].getName());
		}
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException  {
		if (args.length == 0 || (!args[0].equals("full") && !args[0].equals("individual")) || (args.length == 2 && !args[1].equals("verbose")) || args.length > 2) {
			System.out.println("To launch the program use one of the following options:");
			System.out.println("1. \"java Tournament full\" (round robin, non-verbose mode)");
			System.out.println("2. \"java Tournament full verbose\" (round robin, verbose mode)");
			System.out.println("3. \"java Tournament individual\" (your player against all the others, non-verbose mode)");
			System.out.println("4. \"java Tournament individual verbose\" (your player against all the others, verbose mode)");
			//   + " or \"Tournament individual\" (your player against all the others)" );
		} else {
			boolean verbose = false;
			if (args.length == 2 && args[1].equals("verbose")) {
				verbose = true;
			}
			Tournament tournament = new Tournament(args[0].equals("full") ? TournamentMode.FULL : TournamentMode.INDIVIDUAL, verbose);
			tournament.run();
		}
	}
}
