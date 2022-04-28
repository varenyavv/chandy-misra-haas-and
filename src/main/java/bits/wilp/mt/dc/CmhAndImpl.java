package bits.wilp.mt.dc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A console application to demo the implementation of Chandy-Misra-Haas (CMH) Algorithm for the AND
 * Model
 *
 * @author Varenya Varshney
 * @since 28/04/2022
 */
public class CmhAndImpl {

  static boolean[][] dependentArrays;
  static Stack<Probe> sentProbes = new Stack<>();
  static int[][] wfg;
  static int initiatorProcessIndex;
  static boolean deadlockDetected = false;

  public static void main(String[] args) {
    System.out.println("############################################################");
    System.out.println("BEGIN: Deadlock Detection | Chandy-Misra-Haas AND Model");
    System.out.println("############################################################\n");

    constructWfgFromUserInput();
    printInputWfg();

    dependentArrays = initializeDependentArrays();

    System.out.println("Dependent arrays initialized:");
    print2DArray(dependentArrays);
    System.out.println();

    detectDeadlock();

    System.out.println("\nDependent arrays at the end of the execution:");
    print2DArray(dependentArrays);

    if (deadlockDetected) {
      System.out.println("\n!!!!!!! ALERT - DEADLOCK DETECTED !!!!!!!");
    } else {
      System.out.println("\nNo deadlock detected");
    }

    System.out.println("\n############################################################");
    System.out.println("END: Deadlock Detection | Chandy-Misra-Haas AND Model");
    System.out.println("############################################################");
  }

  public static void constructWfgFromUserInput() {
    int n;

    try (Scanner in = new Scanner(System.in)) {

      System.out.println("Enter the number of processes involved in deadlock detection:");
      n = Integer.parseInt(in.nextLine());

      // Declare WFG as a 2D array
      wfg = new int[n][n];
      for (int i = 1; i <= n; i++) {
        System.out.printf(
            "Enter all the comma-separated processes [Between 1 and %d] processor P%d is waiting for."
                + "%nEnter 0 if it is not waiting for anyone:%n",
            n, i);
        String waitForIdsInput = in.nextLine();
        if (waitForIdsInput.equals("0")) {
          Arrays.stream(wfg).forEach(ints -> Arrays.stream(ints).forEach(value -> value = 0));
        } else {
          final int currentProcess = i;
          parseWaitForIdsUserInput(waitForIdsInput, n)
              .forEach(integer -> wfg[currentProcess - 1][integer - 1] = 1);
        }
      }

      System.out.printf(
          "Enter the process [Between 1 and %d] which initiates the deadlock detection routine:%n",
          n);
      int initiatorProcess = Integer.parseInt(in.nextLine());
      if (initiatorProcess < 1 || initiatorProcess > n) {
        throw new IllegalArgumentException(
            "Invalid initiator process. It should lie between 1 to " + n);
      }
      initiatorProcessIndex = initiatorProcess - 1;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private static void detectDeadlock() {
    if (wfg[initiatorProcessIndex][initiatorProcessIndex] == 1) {
      deadlockDetected = true; // locally dependent on itself
      System.out.println("Uh-oh! Deadlock detected even before sending any probe message!");
      return;
    }

    List<Probe> probesToSend = getProbesToSendAhead(initiatorProcessIndex);

    sentProbes.addAll(probesToSend);
    System.out.println(probesToSend);

    onProbeReception(wfg, null);
  }

  private static void onProbeReception(int[][] wfg, Probe receivedProbe) {

    while (!sentProbes.empty()) {
      Probe probe = sentProbes.pop();
      onProbeReception(wfg, probe);
    }

    if (deadlockDetected || null == receivedProbe) {
      return;
    }

    List<Integer> iAmWaitingFor = whoAllAmIWaitingFor(receivedProbe.k);
    boolean[] dependentArray = dependentArrays[receivedProbe.k];

    if (!iAmWaitingFor.isEmpty()) {
      if (!dependentArray[receivedProbe.i]) {
        dependentArray[receivedProbe.j] = true;
        dependentArray[receivedProbe.i] = true;

        if (receivedProbe.k == receivedProbe.i) {
          deadlockDetected = true; // deadlock detected
          System.out.println("Uh-oh! Deadlock detected at " + receivedProbe);
        } else {
          List<Probe> probesToSend =
              iAmWaitingFor.stream()
                  .map(waitingFor -> new Probe(receivedProbe.i, receivedProbe.k, waitingFor))
                  .collect(Collectors.toList());
          sentProbes.addAll(probesToSend);
          System.out.println(probesToSend);
          onProbeReception(wfg, null);
        }
      } else {
        dependentArray[receivedProbe.j] = true;
        // possibility of a deadlock since Pk has already received a probe from Pi and it has again
        // received it this time from Pj != Pi, so checking the WFG of Pk against its Pj column
        // If wfg[Pk][Pj] = 1, it would mean that there is a cycle since Pk had already sent a probe
        // to Pj and now the probe has come back from Pj again.
        if (wfg[receivedProbe.k][receivedProbe.j] == 1) {
          dependentArray[receivedProbe.k] = true; // locally dependent on itself
          deadlockDetected = true;
          System.out.println("Uh-oh! Deadlock detected at " + receivedProbe);
        } else {
          System.out.println("Discarded: " + receivedProbe);
        }
      }
    } else {
      System.out.println("Discarded: " + receivedProbe); // Pk is not blocked
    }
  }

  private static List<Probe> getProbesToSendAhead(int senderProcess) {
    List<Integer> iAmWaitingFor = whoAllAmIWaitingFor(initiatorProcessIndex);

    return iAmWaitingFor.stream()
        .map(process -> new Probe(initiatorProcessIndex, senderProcess, process))
        .collect(Collectors.toList());
  }

  private static List<Integer> whoAllAmIWaitingFor(int myProcessIndex) {
    List<Integer> waitingFor = new ArrayList<>();
    for (int i = 0; i < wfg.length; i++) {
      if (wfg[myProcessIndex][i] == 1) {
        waitingFor.add(i);
      }
    }
    return waitingFor;
  }

  private static boolean[][] initializeDependentArrays() {
    return IntStream.range(0, wfg.length)
        .mapToObj(value -> new boolean[wfg.length])
        .toArray(boolean[][]::new);
  }

  private static List<Integer> parseWaitForIdsUserInput(String waitForIdsInput, int n) {
    String[] splittedInput = waitForIdsInput.split(",");
    splittedInput = Arrays.stream(splittedInput).distinct().toArray(String[]::new);
    if (splittedInput.length > n) {
      throw new IllegalArgumentException("Total wait-for processes can't be more than " + n);
    }
    List<Integer> invalidInput =
        Arrays.stream(splittedInput)
            .map(Integer::parseInt)
            .filter(integer -> integer > n || integer < 1)
            .collect(Collectors.toList());
    if (!invalidInput.isEmpty()) {
      throw new IllegalArgumentException("Invalid inputs " + invalidInput);
    }

    return Arrays.stream(splittedInput).map(Integer::parseInt).collect(Collectors.toList());
  }

  private static void print2DArray(boolean[][] depArrays) {
    System.out.println(
        Arrays.deepToString(depArrays).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
  }

  private static void printInputWfg() {
    System.out.println("Input Wait-For Graph is:");
    for (int i = 0; i < wfg.length; i++) {
      for (int j = 0; j < wfg.length; j++) System.out.print(wfg[i][j] + "  ");
      System.out.println();
    }
    System.out.println();
  }
}

class Probe {
  int i; // initiator
  int j; // sender
  int k; // receiver

  public Probe(int i, int j, int k) {
    this.i = i;
    this.j = j;
    this.k = k;
  }

  @Override
  public String toString() {
    return String.format("Probe(i=%d,j=%d,k=%d)", i + 1, j + 1, k + 1);
  }
}
