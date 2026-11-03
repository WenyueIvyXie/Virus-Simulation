import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Simulates infection, vaccination, and resistance on an eight-neighbour grid. */
public class VirusSimulation {
    // Store only coordinates that have been reached by the outbreak or vaccine program.
    private final Map<Coordinate, Neighbourhood> regionMap = new HashMap<>();
    // Process only the current outbreak area and its susceptible frontier each cycle.
    private final Set<Coordinate> activeNeighbourhoods = new HashSet<>();

    // These paired structures allow random vaccination without scanning the whole grid.
    private final ArrayList<Coordinate> susceptibleList = new ArrayList<>();
    private final Map<Coordinate, Integer> susceptibleIndex = new HashMap<>();

    private final Random rand = new Random();
    private final double p1;
    private final double p2;
    private final int v;
    private final int a;
    private final int d;

    public VirusSimulation(double p1, double p2, int v, int a, int d) {
        this.p1 = p1;
        this.p2 = p2;
        this.v = v;
        this.a = a;
        this.d = d;

        Coordinate origin = new Coordinate(0, 0);
        regionMap.put(origin, new Neighbourhood(Status.INFECTED, v));
        activeNeighbourhoods.add(origin);

        // Begin with the eight squares surrounding the outbreak in the susceptible frontier.
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Coordinate neighbour = new Coordinate(dr, dc);
                regionMap.put(neighbour, new Neighbourhood());
                activeNeighbourhoods.add(neighbour);
                addSusceptible(neighbour);
            }
        }
    }

    /** Advances one cycle; all infection decisions use the same pre-cycle snapshot. */
    public void stepCycle(int currentCycle) {
        if (currentCycle < 1) {
            throw new IllegalArgumentException("Cycle numbers start at 1.");
        }

        // Store changes separately so a newly infected square cannot infect another square
        // until the next cycle (synchronous status changes).
        Map<Coordinate, Neighbourhood> changes = new HashMap<>();
        Map<Coordinate, Neighbourhood> vaccines = new HashMap<>();

        // Infected and vaccinated grid become resistant after V elapsed cycles.
        for (Coordinate coordinate : activeNeighbourhoods) {
            Neighbourhood neighbourhood = regionMap.get(coordinate);
            if (neighbourhood.status == Status.INFECTED
                    || neighbourhood.status == Status.VACCINATED) {
                int remaining = neighbourhood.timer - 1;
                if (remaining <= 0) {
                    changes.put(coordinate, new Neighbourhood(Status.RESISTANT, 0));
                } else {
                    changes.put(coordinate, new Neighbourhood(neighbourhood.status, remaining));
                }
            }
        }

        // Test only squares that were susceptible at the start of this cycle for infection
        for (Coordinate coordinate : activeNeighbourhoods) {
            Neighbourhood neighbourhood = regionMap.get(coordinate);
            if (neighbourhood.status != Status.SUSCEPTIBLE) continue;

            int infectedCount = infNeighbourCount(coordinate);
            if (infectedCount == 0) continue;

            double infectionProbability = infectedCount == 1 ? p1 : p2;
            if (rand.nextDouble() < infectionProbability) {
                changes.put(coordinate, new Neighbourhood(Status.INFECTED, v));

                // Add only previously unseen squares; existing changed status changes are kept.
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        Coordinate neighbour = new Coordinate(
                                coordinate.row + dr, coordinate.col + dc);
                        if (!regionMap.containsKey(neighbour) && !changes.containsKey(neighbour)) {
                            changes.put(neighbour, new Neighbourhood());
                        }
                    }
                }
            }
        }

        // Vaccination starts after D full cycles. An infection scheduled this cycle takes priority.
        if (currentCycle > d) {
            for (Coordinate coordinate : pickRandomSusceptibles(a)) {
                Neighbourhood pending = changes.get(coordinate);
                if (pending != null && pending.status == Status.INFECTED) continue;
                vaccines.put(coordinate, new Neighbourhood(Status.VACCINATED, v));
            }
        }

        // Remove no-longer-susceptible entries before applying the status changes.
        for (Map.Entry<Coordinate, Neighbourhood> entry : changes.entrySet()) {
            if (entry.getValue().status != Status.SUSCEPTIBLE) {
                removeSusceptible(entry.getKey());
            }
        }
        for (Coordinate coordinate : vaccines.keySet()) {
            removeSusceptible(coordinate);
        }

        regionMap.putAll(changes);
        regionMap.putAll(vaccines);

        // Add newly exposed frontier squares, but do not re-add squares vaccinated this cycle.
        for (Map.Entry<Coordinate, Neighbourhood> entry : changes.entrySet()) {
            if (entry.getValue().status == Status.SUSCEPTIBLE
                    && !vaccines.containsKey(entry.getKey())) {
                addSusceptible(entry.getKey());
            }
        }

        // Resistant squares no longer affect the simulation; all other changed squares stay active.
        for (Map.Entry<Coordinate, Neighbourhood> entry : changes.entrySet()) {
            if (entry.getValue().status == Status.RESISTANT) {
                activeNeighbourhoods.remove(entry.getKey());
            } else {
                activeNeighbourhoods.add(entry.getKey());
            }
        }
        activeNeighbourhoods.addAll(vaccines.keySet());
    }
//-------------------------------------------------------------------------
    private void addSusceptible(Coordinate coordinate) {
        if (susceptibleIndex.containsKey(coordinate)) return;
        susceptibleIndex.put(coordinate, susceptibleList.size());
        susceptibleList.add(coordinate);
    }

    // Swap with the last element so removal stays O(1) and keeps the index map correct.
    private void removeSusceptible(Coordinate coordinate) {
        Integer index = susceptibleIndex.remove(coordinate);
        if (index == null) return;

        int lastIndex = susceptibleList.size() - 1;
        if (index != lastIndex) {
            Coordinate lastCoordinate = susceptibleList.get(lastIndex);
            susceptibleList.set(index, lastCoordinate);
            susceptibleIndex.put(lastCoordinate, index);
        }
        susceptibleList.remove(lastIndex);
    }

    /** Selects up to A unique susceptible squares without scanning the whole grid. */
    private ArrayList<Coordinate> pickRandomSusceptibles(int countRequested) {
        int count = Math.min(countRequested, susceptibleList.size());
        ArrayList<Coordinate> picks = new ArrayList<>(count);
        if (count == 0) return picks;

        Set<Integer> chosenIndices = new HashSet<>();
        while (chosenIndices.size() < count) {
            chosenIndices.add(rand.nextInt(susceptibleList.size()));
        }
        for (int index : chosenIndices) {
            picks.add(susceptibleList.get(index));
        }
        return picks;
    }

    /** Counts infected squares in the eight surrounding positions. */
    public int infNeighbourCount(Coordinate coordinate) {
        int infectedCount = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Neighbourhood neighbour = regionMap.get(
                        new Coordinate(coordinate.row + dr, coordinate.col + dc));
                if (neighbour != null && neighbour.status == Status.INFECTED) {
                    infectedCount++;
                }
            }
        }
        return infectedCount;
    }

    /** Prints the known grid using i=infected, v=vaccinated, r=resistant, .=susceptible. */
    @Override
    public String toString() {
        if (regionMap.isEmpty()) return "(empty)\n";
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (Coordinate coordinate : regionMap.keySet()) {
            minRow = Math.min(minRow, coordinate.row);
            maxRow = Math.max(maxRow, coordinate.row);
            minCol = Math.min(minCol, coordinate.col);
            maxCol = Math.max(maxCol, coordinate.col);
        }

        String output = " ";
        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                Neighbourhood neighbourhood = regionMap.get(new Coordinate(row, col));
                if (neighbourhood == null) {
                    output += Const.EMPTY;
                } else if (neighbourhood.status == Status.SUSCEPTIBLE) {
                    output += Const.SUSCEPTIBLE;
                } else if (neighbourhood.status == Status.INFECTED) {
                    output += Const.INFECTED;
                } else if (neighbourhood.status == Status.VACCINATED) {
                    output += Const.VACCINATED;
                } else {
                    output += Const.RESISTANT;
                }
            }
            output += '\n';
        }
        return output;
    }
}
