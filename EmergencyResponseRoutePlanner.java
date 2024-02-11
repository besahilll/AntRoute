import java.util.Arrays;
import java.util.Random;

public class EmergencyResponseRoutePlanner {
    private int numAnts;
    private int numCities;
    private double[][] distances;
    private double[][] pheromones;
    private double alpha;
    private double beta;
    private double evaporationRate;
    private int[][] antPaths;
    private int startingCity;
    private int incidentLocation;
    private String[] locationNames;

    public EmergencyResponseRoutePlanner(int numAnts, int numCities, double[][] distances, double alpha, double beta, double evaporationRate, int startingCity, int incidentLocation, String[] locationNames) {
        this.numAnts = numAnts;
        this.numCities = numCities;
        this.distances = distances;
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = evaporationRate;
        this.pheromones = new double[numCities][numCities];
        this.antPaths = new int[numAnts][numCities];
        this.startingCity = startingCity;
        this.incidentLocation = incidentLocation;
        this.locationNames = locationNames;
    }

    private void initialize() {
        double initialPheromone = 0.01;

        for (int i = 0; i < numCities; i++) {
            Arrays.fill(pheromones[i], initialPheromone);
        }
    }

    private void constructSolutions() {
        Random random = new Random();

        for (int ant = 0; ant < numAnts; ant++) {
            boolean[] visited = new boolean[numCities];
            Arrays.fill(visited, false);

            int currentCity = startingCity;
            antPaths[ant][0] = currentCity;

            visited[currentCity] = true;
            double totalDistance = 0.0;

            for (int i = 1; i < numCities; i++) {
                int nextCity = selectNextCity(currentCity, visited);
                antPaths[ant][i] = nextCity;
                visited[nextCity] = true;
                totalDistance += distances[currentCity][nextCity];
                currentCity = nextCity;
            }

            System.out.println("Ant " + ant + " path: " + getLocationNames(antPaths[ant]) + ", Distance: " + totalDistance);
        }
    }

    private int selectNextCity(int currentCity, boolean[] visited) {
        double[] probabilities = new double[numCities];
        double totalProbability = 0.0;

        for (int i = 0; i < numCities; i++) {
            if (!visited[i] && i != incidentLocation) {
                double pheromone = Math.pow(pheromones[currentCity][i], alpha);
                double attractiveness = Math.pow(1.0 / distances[currentCity][i], beta);
                probabilities[i] = pheromone * attractiveness;
                totalProbability += probabilities[i];
            }
        }

        double randomValue = Math.random() * totalProbability;
        double sum = 0.0;

        for (int i = 0; i < numCities; i++) {
            if (!visited[i] && i != incidentLocation) {
                sum += probabilities[i];
                if (randomValue <= sum) {
                    return i;
                }
            }
        }

        // If no valid city was selected, return the incident location
        return incidentLocation;
    }

    private void updatePheromones() {
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                if (i != j) {
                    pheromones[i][j] *= (1.0 - evaporationRate);
                }
            }
        }

        for (int ant = 0; ant < numAnts; ant++) {
            double pheromoneDelta = 1.0 / calculateAntPathLength(ant);

            for (int i = 0; i < numCities - 1; i++) {
                int city1 = antPaths[ant][i];
                int city2 = antPaths[ant][i + 1];
                pheromones[city1][city2] += pheromoneDelta;
                pheromones[city2][city1] += pheromoneDelta;
            }

            int firstCity = antPaths[ant][0];
            int lastCity = antPaths[ant][numCities - 1];
            pheromones[lastCity][firstCity] += pheromoneDelta;
            pheromones[firstCity][lastCity] += pheromoneDelta;
        }
    }

    private double calculateAntPathLength(int ant) {
        double length = 0.0;

        for (int i = 0; i < numCities - 1; i++) {
            int city1 = antPaths[ant][i];
            int city2 = antPaths[ant][i + 1];
            length += distances[city1][city2];
        }

        return length;
    }

    private String getLocationNames(int[] path) {
        StringBuilder names = new StringBuilder();

        for (int i = 0; i < path.length; i++) {
            names.append(locationNames[path[i]]);
            if (i < path.length - 1) {
                names.append(" -> ");
            }
        }

        return names.toString();
    }

    public int[] findBestRoute() {
        initialize();
        int numIterations = 3;

        for (int iteration = 0; iteration < numIterations; iteration++) {
            constructSolutions();
            updatePheromones();
            updateAlphaBeta(iteration, numIterations);
        }

        int bestAnt = 0;
        double bestPathLength = Double.MAX_VALUE;

        for (int ant = 0; ant < numAnts; ant++) {
            int currentCity = startingCity;
            int[] path = antPaths[ant];
            double length = distances[currentCity][path[0]];

            for (int i = 0; i < numCities - 1; i++) {
                length += distances[path[i]][path[i + 1]];
            }

            length += distances[path[numCities - 1]][incidentLocation];

            if (length < bestPathLength) {
                bestAnt = ant;
                bestPathLength = length;
            }
        }

        return antPaths[bestAnt];
    }

    public static double calculateTotalDistance(int[] route, double[][] distances) {
        double totalDistance = 0.0;

        for (int i = 0; i < route.length - 1; i++) {
            totalDistance += distances[route[i]][route[i + 1]];
        }
        return totalDistance;
    }

    private void updateAlphaBeta(int currentIteration, int totalIterations) {
        // Example: Linearly decrease alpha and beta over iterations
        alpha = 1.0 - (currentIteration / (double) totalIterations);
        beta = 1.0 - (currentIteration / (double) totalIterations);
    }

    public static void main(String[] args) {
        int numAnts= 10;
        int numCities= 5;

        double[][] distances = {
            {0, 2, 5, 7, 9},
            {2, 0, 6, 1, 4},
            {5, 6, 0, 4, 2},
            {7, 1, 4, 0, 1},
            {9, 4, 2, 1, 0}
        };

        String[] locationNames = {"Premnagar", "Panditwari", "Ballupur", "Railway Station", "ISBT"};

        double alpha = 1.0;
        double beta = 1.0;
        double evaporationRate = 0.5;

        int startingCity = 1;      // Starting city for emergency vehicles
        int incidentLocation = 4;  // Location of the emergency incident

        EmergencyResponseRoutePlanner planner = new EmergencyResponseRoutePlanner(numAnts, numCities, distances, alpha, beta, evaporationRate, startingCity, incidentLocation, locationNames);
        int[] bestRoute = planner.findBestRoute();
        double bestRouteDistance = calculateTotalDistance(bestRoute, distances); // Calculate the distance of the best route

        System.out.println("Best Route from City " + locationNames[startingCity] + " to Incident Location " + locationNames[incidentLocation] + ": " + planner.getLocationNames(bestRoute));
        System.out.println("Best Route Distance: " + bestRouteDistance);
    }
}
