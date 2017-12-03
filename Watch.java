//This is to check whether a player has violated the time (and memory) limits.
//If they have doubled them, the tournament is aborted; otherwise the tournament might take too long to run.

public class Watch {
	private double startingTime;
	private long startingMemory;
	Runtime runtime;

	public Watch() {
		runtime = Runtime.getRuntime();
	}

	public void startCounting() {
		startingTime = System.nanoTime();
	}

	public double getElapsedTime() {
		return (System.nanoTime() - startingTime) / 1000000000L;
	}

	public boolean enforceTimeLimit(Player player, double timeLimit, String offendingMethod) {
		double usedTime = getElapsedTime();
		if (usedTime <= timeLimit) {
			return true;
		}
		System.out.println();
		if (usedTime > 3 * timeLimit) {
			System.out.println("Aborting tournament due to time-limit violation.");
		} else {
			System.out.println("WARNING: time-limit violation.");
		}
		System.out.println("Offending player: " + player.getName());
		System.out.println("Offending method: " + offendingMethod);
		System.out.println("Time used (s): " + usedTime);
		System.out.println("Maximum time allowed (s): " + timeLimit);

		if (usedTime > 3 * timeLimit) {
			return false;
		}
		return true;
	}

	public void startMemoryComparison() {
		runtime.gc(); // Run the garbage collector
		startingMemory = runtime.totalMemory() - runtime.freeMemory();
	}

	public long getMemoryUse() {
		runtime.gc();
		long endingMemory = runtime.totalMemory() - runtime.freeMemory();
		return bytesToMegabytes(endingMemory - startingMemory);
	}

	private static final long MEGABYTE = 1024L * 1024L;

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

	public boolean enforceMemoryLimit(Player player, double memoryLimit, String offendingMethod) {
		double usedMemory = getMemoryUse();
		if (usedMemory <= memoryLimit) {
			return true;
		}
		System.out.println();
		if (usedMemory > 3 * memoryLimit) {
			System.out.println("Aborting tournament due to memory-limit violation.");
		} else {
			System.out.println("WARNING: memory-limit violation.");
		}
		System.out.println("Offending player: " + player.getName());
		System.out.println("Offending method: " + offendingMethod);
		System.out.println("Memory used (MB): " + usedMemory);
		System.out.println("Maximum memory allowed (MB): " + memoryLimit);

		if (usedMemory > 3 * memoryLimit) {
			return false;
		}
		return true;
	}
}
