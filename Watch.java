
public class Watch {
  private double startingTime;
  
  public Watch() {
  }
  
  public void startCounting() {
    startingTime = System.nanoTime();
  }
  
  public double getElapsedTime() {
    return (System.nanoTime() - startingTime) / 1000000000L;
  }
  
  public void enforceTimeLimit(Player player, double timeLimit, String offendingMethod) {
    double usedTime = getElapsedTime();
    if (usedTime > timeLimit) {
      System.out.println("Aborting tournament due to time-limit violation.");
      System.out.println("Offending player: " + player.getName());
      System.out.println("Offending method: " + offendingMethod);
      System.out.println("Time used (s): " + usedTime);
      System.out.println("Maximum time allowed (s): " + timeLimit);
      System.exit(1);
    }
  }
}
