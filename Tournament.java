import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Tournament {

  private int NUM_PLAYERS = 3;
  private TournamentMode tournamentMode;
  private Random rand;
  private DecimalFormat numberFormat;
  private Watch watch;

  public Tournament(TournamentMode tournamentMode_) {
    tournamentMode = tournamentMode_;
    rand = new Random(0);
    numberFormat = new DecimalFormat("#.000");
    watch = new Watch();
  }

  public Player getPlayer(int id) {
    if (0 <= id && id < NUM_PLAYERS) {
      watch.startCounting();
      Player player = null;
      switch (id) {
      case 0:
        player = new MyPlayer(Parameters.MAX_NUM_MOVES);
        break;
      case 1: 
        player = new TeamNihilist(Parameters.MAX_NUM_MOVES);
        break;
      case 2: 
        player = new TeamMonkey(Parameters.MAX_NUM_MOVES);
        break;
      }
      watch.enforceTimeLimit(player, Parameters.TIME_LIMIT_CONSTRUCTOR, "constructor");
      System.out.println("Constructor of player " + player.getName() + " took " + numberFormat.format(watch.getElapsedTime()) + " seconds");
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
        Board board = new Board(startingPositions[t / 2]);
        Match match = new Match(players[t % 2], players[(t + 1) % 2], board, log);
        MatchOutcome matchOutcome = match.run();
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
    if (args.length != 1 || (!args[0].equals("full") && !args[0].equals("individual"))) {
      System.out.println("To launch the program use: "
          + "\"Tournament full\" (round robin) or \"Tournament individual\" (your player against all the others)" );
    } else {
      Tournament tournament = new Tournament(args[0].equals("full") ? TournamentMode.FULL : TournamentMode.INDIVIDUAL);
      tournament.run();
    }
  }
}
