import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Tournament {

	private int numPlayers;
	private Random rand;
	private DecimalFormat numberFormat;
	private Watch watch;
	private boolean verbose;
	private PlayerFactory playerFactory = new PlayerFactory();

	public Tournament(boolean verbose) {
		rand = new Random(System.currentTimeMillis());
		numberFormat = new DecimalFormat("#.000");
		watch = new Watch();
		this.verbose = verbose;
		// Registering Players
		registerPlayers();

		numPlayers = 0; // Initializing, this will be calculated during the run.
	}

	private void registerPlayers() {
		// New players can be registered here
		playerFactory.registerPlayer("TeamMonkey", i -> new TeamMonkey(i));
		playerFactory.registerPlayer("TeamNihilist", i -> new TeamNihilist(i));
		playerFactory.registerPlayer("TeamRationalOptimist", i -> TeamRational.createOptimist(i));
		playerFactory.registerPlayer("TeamRationalPessimist", i -> TeamRational.createPessimist(i));
		playerFactory.registerPlayer("TeamRationalQueller", i -> TeamRational.createQueller(i));
		playerFactory.registerPlayer("TeamRationalRealist", i -> TeamRational.createRealist(i));
		playerFactory.registerPlayer("TeamRationalScrapper", i -> TeamRational.createScrapper(i));
		playerFactory.registerPlayer("TeamRationalTruster", i -> TeamRational.createTruster(i));
		playerFactory.registerPlayer("TeamRationalUtilitarian", i -> TeamRational.createUtilitarian(i));

		playerFactory.registerPlayer("TeamW", i -> new TeamW(i));
		// playerFactory.registerPlayer("TeamWatermelon", i -> new TeamWatermelon(i));
		playerFactory.registerPlayer("TeamTitForTat", i -> new TeamTitForTat(i));
		playerFactory.registerPlayer("TeamDoubleAgent", i -> new TeamDoubleAgent(i));
		playerFactory.registerPlayer("TeamHandshaker", i -> new TeamHandshaker(i));
	}

	public Player getPlayer(String id) {

		watch.startMemoryComparison();
		watch.startCounting();
		System.out.println("Calling Constructor of player: " + id);

		Player player = playerFactory.createPlayer(id, Parameters.MAX_NUM_MOVES);
		boolean noLimitViolation = true;
		noLimitViolation &= watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_CONSTRUCTOR, "constructor");
		System.out.println("Constructor of player " + player.getName() + " took "
				+ numberFormat.format(watch.getElapsedTime()) + " seconds.");
		noLimitViolation &= watch.enforceMemoryLimit(player, Parameters.MEMORY_LIMIT_CONSTRUCTOR, "constructor");
		System.out.println("Constructor of player " + player.getName() + " used " + Long.toString(watch.getMemoryUse())
				+ " megabytes.\n");
		if (!noLimitViolation) {
			System.out.println("Turning player " + id + " into a monkey");
			player = playerFactory.createPlayer("TeamMonkey", Parameters.MAX_NUM_MOVES);
		}
		// Set player name to id
		player.setName(id);
		return player;
	}

	public Cell[] randomStartingPositions() {
		int numCells = Parameters.NUM_COLUMNS * Parameters.NUM_ROWS;
		Cell[] allCells = new Cell[numCells];
		int pos = 0;
		for (int column = 1; column <= Parameters.NUM_COLUMNS; ++column) {
			for (int row = 1; row <= Parameters.NUM_ROWS; ++row) {
				allCells[pos++] = new Cell(column, row);
			}
		}
		Cell positions[] = new Cell[Parameters.NUM_PIECES];
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

	public void run() throws IOException {
		PrintWriter log = null;
		try {
			log = new PrintWriter("../logs/logs.tex", "UTF-8");
		} catch (FileNotFoundException e) {
			File file = new File("../logs/logs.tex");
			file.getParentFile().mkdirs();
			log = new PrintWriter("../logs/logs.tex", "UTF-8");
		}

		if (Parameters.PRINT_LOG) {
			log.println("\\documentclass[11pt,letterpaper]{article}");
			log.println("\\usepackage{xskak,chessboard}");
			log.println("\\usepackage[margin=0.2in]{geometry}");
			log.println("\\begin{document}");
		}

		// initialize players
		ArrayList<Player> allPlayers = getPlayers();

		// Setting number of players
		numPlayers = allPlayers.size();

		// pair off players, by id number.
		int[][] pairsOfIds;
		pairsOfIds = new int[(numPlayers * (numPlayers - 1)) / 2][];
		int count = 0;
		for (int idPlayer1 = 0; idPlayer1 < numPlayers; ++idPlayer1) {
			for (int idPlayer2 = idPlayer1 + 1; idPlayer2 < numPlayers; ++idPlayer2) {
				pairsOfIds[count++] = new int[] { idPlayer1, idPlayer2 };
			}
		}

		// Setting Scores
		double[] totalScore = new double[numPlayers];

		Collections.shuffle(Arrays.asList(pairsOfIds));
		for (int[] idsInThisRound : pairsOfIds) {
			System.out.println("************************************************************************************");
			System.out.println("Starting new series:");

			// Generate all of the boards that will be played in the series.
			// This is Parameters.MIN_NUM_DIFFERENT_BOARDS + (#heads when we flip a coin until tails).
			int numBoards = getNumBoards();

			Player[] playersInThisRound = new Player[] { allPlayers.get(idsInThisRound[0]), allPlayers.get(idsInThisRound[1]) };

			Cell[][] startingPositions = new Cell[2*numBoards][];
			int[] whiteTeamNumbers = new int[2*numBoards];
			int[] blackTeamNumbers = new int[2*numBoards];
			//Loop over all mirror matches (so jump by 2 at a time):
			for (int matchNumber = 0; matchNumber < 2*numBoards; matchNumber += 2) {
				startingPositions[matchNumber] = randomStartingPositions();
				startingPositions[matchNumber+1] = startingPositions[matchNumber];
				//I hope this is safe... Should I make a proper copy instead?

				//Each mirror match has a random starting player:
				if (rand.nextBoolean()) {
					whiteTeamNumbers[matchNumber] = 0;
					blackTeamNumbers[matchNumber] = 1;
					whiteTeamNumbers[matchNumber+1] = 1;
					blackTeamNumbers[matchNumber+1] = 0;
				} else {
					whiteTeamNumbers[matchNumber] = 1;
					blackTeamNumbers[matchNumber] = 0;
					whiteTeamNumbers[matchNumber+1] = 0;
					blackTeamNumbers[matchNumber+1] = 1;
				}
			}

			// prepare players for round
			for (Player player : playersInThisRound) {
				watch.startCounting();
				player.prepareForSeries();
				watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_PREPARE_FOR_SERIES, "prepareForSeries()");
			}

			double[] averagePayoffInThisRound = new double[2];

			for (int teamNumber = 0; teamNumber <= 1; ++teamNumber) {
				System.out.println("Team " + teamNumber + ": " + playersInThisRound[teamNumber].getName());
			}

			// list the round numbers, and who plays white at each round:
			if (!verbose) {
				/*System.out.print("Match #:    ");
				for (int matchNumber = 0; matchNumber < 2 * numBoards; matchNumber +=2) {
					System.out.print(String.format("%02d", matchNumber));
					System.out.print(" ");
				}
				System.out.print("\nWhite team: ");*/
				System.out.print("White team: ");
				for (int matchNumber = 0; matchNumber < 2 * numBoards; matchNumber +=2) {
					System.out.print(whiteTeamNumbers[matchNumber]);
					System.out.print(whiteTeamNumbers[matchNumber+1]);
					System.out.print(" ");
				}
				System.out.print("\nOutcome:    ");
			}

			for (int matchNumber = 0; matchNumber < 2 * numBoards; ++matchNumber) {
				// These were randomly chosen so that in mirror matches, each team plays each colour:
				int whiteTeamNumber=whiteTeamNumbers[matchNumber];
				Player whitePlayer=playersInThisRound[whiteTeamNumber];
				int blackTeamNumber=blackTeamNumbers[matchNumber];
				Player blackPlayer=playersInThisRound[blackTeamNumber];

				if (Parameters.PRINT_LOG) {
					log.println("\n\\clearpage\n");
					log.println("Starting match:\n");
					log.println("White player: " + whitePlayer.getName() + "\n");
					log.println("Black player: " + blackPlayer.getName() + "\n");
				}
				if (verbose) {
					System.out.println("************************************************************************************");
					System.out.println();
					System.out.println("Starting match number: "+matchNumber);
					System.out.println("White player: " + whitePlayer.getName());
					System.out.println("Black player: " + blackPlayer.getName());
					System.out.println();
				}
				Board board = new Board(startingPositions[matchNumber]);
				Match match = new Match(whitePlayer, blackPlayer, board, log, verbose);
				MatchOutcome matchOutcome = match.run();
				if (verbose) {
					System.out.println("Outcome: " + matchOutcome.toString());
					System.out.println();
				} else {
					switch (matchOutcome) {
					case WHITE_WINS:
						System.out.print(whiteTeamNumber);
						break;
					case BLACK_WINS:
						System.out.print(blackTeamNumber);
						break;
					case TIE:
						System.out.print('T');
						break;
					case DRAW:
						System.out.print('D');
					}
					if (matchNumber%2==1) {
						System.out.print(' ');
					} //separate mirror matches for readability.
				}
				double[] payoffs = matchOutcome.getPayoffs();
				averagePayoffInThisRound[whiteTeamNumber] += payoffs[0] / (2*numBoards);
				averagePayoffInThisRound[blackTeamNumber] += payoffs[1] / (2*numBoards);
			}
			System.out.println();
			System.out.println("************************************************************************************");
			System.out.println();
			System.out.println("Round ended.");

			for (int teamNumber = 0; teamNumber <= 1; ++teamNumber) {
				System.out.println("Average payoff of player " + playersInThisRound[teamNumber].getName() + ": "
						+ numberFormat.format(averagePayoffInThisRound[teamNumber]));
				totalScore[idsInThisRound[teamNumber]] += averagePayoffInThisRound[teamNumber];
			}
			System.out.println();
			System.out.println("************************************************************************************");
			System.out.println();
		}

		if (Parameters.PRINT_LOG) {
			log.println("\\end{document}");
			log.close();
		}

		printRanking(allPlayers, totalScore);
	}

	private ArrayList<Player> getPlayers() throws FileNotFoundException {
		ArrayList<Player> allPlayers = new ArrayList<Player>();
		Scanner scanner = new Scanner(new File("playermanifest.txt"));

		while (scanner.hasNextLine()) {
			// each pair of lines in the manifest after the first should be of
			// the form:
			// teamName
			// n
			// where n is the number of players of team "teamName" you want in
			// tournament
			String teamClassName = scanner.nextLine();
			int population = Integer.parseInt(scanner.nextLine());

			for (int i = 0; i < population; ++i) {
				allPlayers.add(getPlayer(teamClassName));
			}
		}
		scanner.close();
		// randomize player order
		Collections.shuffle(allPlayers);
		return allPlayers;
	}

	public void printRanking(ArrayList<Player> allPlayers, double[] totalScore) {

		Integer[] rankedPlayers = new Integer[numPlayers];
		for (int i = 0; i < numPlayers; ++i) {
			rankedPlayers[i] = i;
		}
		Arrays.sort(rankedPlayers, new ScoreComparator(totalScore));
		System.out.println("Final ranking:");
		for (int i = 0; i < numPlayers; ++i) {
			System.out.println(i + " (" + numberFormat.format(totalScore[rankedPlayers[i]]) + ") : "
					+ allPlayers.get(rankedPlayers[i]).getName());
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0 || (!args[0].equals("full") && !args[0].equals("individual"))
				|| (args.length == 2 && !args[1].equals("verbose")) || args.length > 2) {
			System.out.println("To launch the program use one of the following options:");
			System.out.println("1. \"java Tournament full\" (round robin, non-verbose mode)");
			System.out.println("2. \"java Tournament full verbose\" (round robin, verbose mode)");
			System.out.println(
					"3. \"java Tournament individual\" (your player against all the others, non-verbose mode)");
			System.out.println(
					"4. \"java Tournament individual verbose\" (your player against all the others, verbose mode)");
			// + " or \"Tournament individual\" (your player against all the
			// others)" );
		} else {
			boolean verbose = false;
			if (args.length == 2 && args[1].equals("verbose")) {
				verbose = true;
			}
			Tournament tournament = new Tournament(verbose);
			tournament.run();
		}
	}
}
