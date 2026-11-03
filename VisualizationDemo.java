import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/** Reads simulation parameters and prints/animates each cycle. */
public class VisualizationDemo {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        int cycles;

        System.out.println("Enter the number of cycles: ");
        cycles = sc.nextInt();
        if (cycles < 0) {
            throw new IllegalArgumentException("The number of cycles cannot be negative.");
        }

        VirusSimulation virus = new VirusSimulation(Const.P1, Const.P2, Const.V, Const.A, Const.D);
        String regionMap = virus.toString();

        for (int t = 1; t<=cycles; t++){
            virus.stepCycle(t);
            regionMap = virus.toString();
            System.out.println("Cycle: " + t + "\n" + regionMap);
        }
        saveFinalMap(regionMap);
        Visualizer visualizer = new Visualizer(regionMap);
        sc.close();
    }

    /** Saves the final grid as a demo capture in a txt file. */
    public static void saveFinalMap(String regionMap) throws IOException {
        File file = new File("regionMap.txt");
        try (PrintWriter printer = new PrintWriter(new FileWriter(file))) {
            printer.println(regionMap);
        }
    }
}
