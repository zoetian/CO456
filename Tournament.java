


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

		playerFactory.registerPlayer("TeamTitForTat", i -> new TeamTitForTat(i));
		playerFactory.registerPlayer("TeamW", i -> new TeamW(i));

		// alliance list
		/**
		playerFactory.registerPlayer("TeamAlphaChess0", i -> new TeamAlphaChess0(i));
		playerFactory.registerPlayer("TeamBestResponse", i -> new TeamBestResponse(i));
		playerFactory.registerPlayer("TeamBigBallerBrand", i -> new TeamBigBallerBrand(i));
		playerFactory.registerPlayer("TeamBlue", i -> new TeamBlue(i));
		playerFactory.registerPlayer("TeamBOOPS", i -> new TeamBOOPS(i));
		playerFactory.registerPlayer("TeamBrentastic", i -> new TeamBrentastic(i));
		playerFactory.registerPlayer("TeamDddddd", i -> new TeamDddddd(i));
		playerFactory.registerPlayer("TeamDROPLET", i -> new TeamDROPLET(i));
		playerFactory.registerPlayer("TeamEMANMEAT", i -> new TeamEMANMEAT(i));
		playerFactory.registerPlayer("TeamFUNKYMONKEYS", i -> new TeamFUNKYMONKEYS(i));
		playerFactory.registerPlayer("TeamGOOSEMATE", i -> new TeamGOOSEMATE(i));
		playerFactory.registerPlayer("TeamGUCKSQUAD", i -> new TeamGUCKSQUAD(i));
		playerFactory.registerPlayer("TeamHAL9001", i -> new TeamHAL9001(i));
		playerFactory.registerPlayer("TeamHumongous", i -> new TeamHumongous(i));
		playerFactory.registerPlayer("TeamInfeasible", i -> new TeamInfeasible(i));
		playerFactory.registerPlayer("TeamJinners", i -> new TeamJinners(i));
		playerFactory.registerPlayer("TeamKismet", i -> new TeamKismet(i));
		playerFactory.registerPlayer("TeamLEEROOOOOOY", i -> new TeamLEEROOOOOOY(i));
		playerFactory.registerPlayer("TeamLEMONCHURRO", i -> new TeamLEMONCHURRO(i));
		playerFactory.registerPlayer("TeamMachineLearn", i -> new TeamMachineLearn(i));
		playerFactory.registerPlayer("TeamMaverick", i -> new TeamMaverick(i));
		playerFactory.registerPlayer("TeamMonkeyKing", i -> new TeamMonkeyKing(i));
		playerFactory.registerPlayer("TeamOreo", i -> new TeamOreo(i));
		playerFactory.registerPlayer("TeamMonkeyKing", i -> new TeamPendo(i));
		playerFactory.registerPlayer("TeamPredator", i -> new TeamPredator(i));
		playerFactory.registerPlayer("TeamPVKHNOHX", i -> new TeamPVKHNOHX(i));
		playerFactory.registerPlayer("TeamRocket", i -> new TeamRocket(i));
		playerFactory.registerPlayer("TeamSnek", i -> new TeamSnek(i));
		playerFactory.registerPlayer("TeamTANGOMANGOS", i -> new TeamTANGOMANGOS(i));
		playerFactory.registerPlayer("TeamTwoBrownGuys", i -> new TeamTwoBrownGuys(i));
		playerFactory.registerPlayer("TeamUtilityMaxim", i -> new TeamUtilityMaxim(i));
		playerFactory.registerPlayer("TeamWatermelon", i -> new TeamWatermelon(i));
		playerFactory.registerPlayer("TeamWoops", i -> new TeamWoops(i));
		playerFactory.registerPlayer("TeamYfnl", i -> new TeamYfnl(i));
		playerFactory.registerPlayer("TeamZedModTwoZed", i -> new TeamZedModTwoZed(i));
		playerFactory.registerPlayer("TeamZinger", i -> new TeamZinger(i));
		**/
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

		// pair off players
		int[][] pairsOfPlayers;
		pairsOfPlayers = new int[(numPlayers * (numPlayers - 1)) / 2][];
		int count = 0;
		for (int idPlayer1 = 0; idPlayer1 < numPlayers; ++idPlayer1) {
			for (int idPlayer2 = idPlayer1 + 1; idPlayer2 < numPlayers; ++idPlayer2) {
				pairsOfPlayers[count++] = new int[] { idPlayer1, idPlayer2 };
			}
		}

		// Setting Scores
		double[] totalScore = new double[numPlayers];

		Collections.shuffle(Arrays.asList(pairsOfPlayers));
		for (int[] playersInThisRound : pairsOfPlayers) {
			System.out.println("*********************");
			System.out.println("Starting new round.");

			// generate boards
			int numBoards = getNumBoards();

			Cell[][] startingPositions = new Cell[numBoards][];
			for (int t = 0; t < numBoards; ++t) {
				startingPositions[t] = randomStartingPositions();
			}

			Player[] players = new Player[] { allPlayers.get(playersInThisRound[0]),
					allPlayers.get(playersInThisRound[1]) };

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
				System.out.println("Average payoff of player " + players[i].getName() + ": "
						+ numberFormat.format(averagePayoffInThisRound[i]));
				totalScore[playersInThisRound[i]] += averagePayoffInThisRound[i];
			}
			System.out.println("*********************");
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
