import java.util.*;
import java.util.stream.*;
import java.io.*;

/**
 * Main Application Entry Point
 * Orchestrates the road network repair cost calculation using MST algorithms
 *
 * Design Pattern: Facade Pattern - Provides simplified interface to complex subsystems
 * SOLID Principles: Single Responsibility - Only handles application orchestration
 */
public class RoadNetworkRepairSystem {

    private final GraphLoader graphLoader;
    private final AlgorithmFactory algorithmFactory;
    private final OutputWriter outputWriter;

    /**
     * Constructor with Dependency Injection
     * Follows IoC (Inversion of Control) principle
     *
     * @param graphLoader Strategy for loading graph data
     * @param algorithmFactory Factory for creating MST algorithms
     * @param outputWriter Strategy for writing output
     */
    public RoadNetworkRepairSystem(GraphLoader graphLoader,
                                   AlgorithmFactory algorithmFactory,
                                   OutputWriter outputWriter) {
        this.graphLoader = Objects.requireNonNull(graphLoader, "GraphLoader cannot be null");
        this.algorithmFactory = Objects.requireNonNull(algorithmFactory, "AlgorithmFactory cannot be null");
        this.outputWriter = Objects.requireNonNull(outputWriter, "OutputWriter cannot be null");
    }

    /**
     * Executes the complete road network analysis workflow
     * Functional approach with clear separation of concerns
     *
     * @throws IOException if file operations fail
     */
    public void execute() throws IOException {
        // Question 1: Initial damaged network using Kruskal's Algorithm
        processQuestion1();

        // Question 2: Network with additional repairs using Prim's Algorithm
        processQuestion2();
    }

    /**
     * Process Question 1: Minimum cost for damaged network
     * Uses Kruskal's Algorithm (Edge-based, optimal for sparse graphs)
     */
    private void processQuestion1() throws IOException {
        System.out.println("=== Question 1: Initial Damaged Network Analysis ===");

        // Load graph from file or hardcoded data
        Graph graph = graphLoader.loadGraph("graph1_input.txt");

        // Create Kruskal's algorithm instance
        MinimumSpanningTree kruskalAlgorithm = algorithmFactory.createKruskalAlgorithm();

        // Execute algorithm and get result
        MSTResult result = kruskalAlgorithm.findMinimumSpanningTree(graph);

        // Generate comprehensive output
        AlgorithmReport report = new AlgorithmReport.Builder()
                .setQuestionNumber(1)
                .setMinimumCost(result.getTotalCost())
                .setAlgorithmName("Kruskal's Algorithm")
                .setReasoning("Kruskal's algorithm is optimal for sparse graphs where E << V². " +
                        "It uses Union-Find data structure for efficient cycle detection. " +
                        "Time Complexity: O(E log E), suitable for edge-dominant scenarios.")
                .setEdges(result.getSelectedEdges())
                .build();

        // Write to output file
        outputWriter.writeReport(report, "output1.txt");

        // Console output
        System.out.println(report.formatOutput());
        System.out.println();
    }

    /**
     * Process Question 2: Minimum cost after repairs
     * Uses Prim's Algorithm (Vertex-based, optimal for dense graphs)
     */
    private void processQuestion2() throws IOException {
        System.out.println("=== Question 2: Network After Repairs Analysis ===");

        // Load enhanced graph with repair costs
        Graph graph = graphLoader.loadGraph("graph2_input.txt");

        // Create Prim's algorithm instance
        MinimumSpanningTree primAlgorithm = algorithmFactory.createPrimAlgorithm();

        // Execute algorithm and get result
        MSTResult result = primAlgorithm.findMinimumSpanningTree(graph);

        // Generate comprehensive output
        AlgorithmReport report = new AlgorithmReport.Builder()
                .setQuestionNumber(2)
                .setMinimumCost(result.getTotalCost())
                .setAlgorithmName("Prim's Algorithm")
                .setReasoning("Prim's algorithm is optimal when graph becomes denser after repairs. " +
                        "It uses a priority queue for efficient minimum edge selection. " +
                        "Time Complexity: O(E log V), efficient for dense graphs and adjacency list representation.")
                .setEdges(result.getSelectedEdges())
                .build();

        // Write to output file
        outputWriter.writeReport(report, "output2.txt");

        // Console output
        System.out.println(report.formatOutput());
        System.out.println();
    }

    /**
     * Main entry point
     * Demonstrates factory pattern and dependency injection
     */
    public static void main(String[] args) {
        try {
            // Initialize components using factories
            GraphLoader loader = new HardcodedGraphLoader();
            AlgorithmFactory factory = new ConcreteAlgorithmFactory();
            OutputWriter writer = new FileOutputWriter();

            // Create and execute system
            RoadNetworkRepairSystem system = new RoadNetworkRepairSystem(loader, factory, writer);
            system.execute();

            System.out.println("Analysis completed successfully!");

        } catch (IOException e) {
            System.err.println("Error during execution: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// ============================================================================
// CORE DOMAIN MODELS
// ============================================================================

/**
 * Immutable Value Object representing a graph vertex/city
 * Follows Value Object pattern from DDD (Domain-Driven Design)
 */
class Vertex implements Comparable<Vertex> {
    private final String name;
    private final int hashCode;

    /**
     * Private constructor to enforce factory method usage
     */
    private Vertex(String name) {
        this.name = Objects.requireNonNull(name, "Vertex name cannot be null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Vertex name cannot be empty");
        }
        this.hashCode = this.name.hashCode();
    }

    /**
     * Factory method for creating vertices
     * Provides flexibility for caching or pooling if needed
     */
    public static Vertex of(String name) {
        return new Vertex(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex)) return false;
        Vertex vertex = (Vertex) o;
        return name.equals(vertex.name);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(Vertex other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}

/**
 * Immutable Value Object representing a weighted edge between two cities
 * Ensures data integrity and thread-safety
 */
class Edge implements Comparable<Edge> {
    private final Vertex source;
    private final Vertex destination;
    private final int weight;

    /**
     * Private constructor with validation
     */
    private Edge(Vertex source, Vertex destination, int weight) {
        this.source = Objects.requireNonNull(source, "Source vertex cannot be null");
        this.destination = Objects.requireNonNull(destination, "Destination vertex cannot be null");

        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight cannot be negative: " + weight);
        }
        this.weight = weight;
    }

    /**
     * Factory method with builder-like flexibility
     */
    public static Edge between(Vertex source, Vertex destination, int weight) {
        return new Edge(source, destination, weight);
    }

    /**
     * Convenience factory for string-based creation
     */
    public static Edge between(String source, String destination, int weight) {
        return new Edge(Vertex.of(source), Vertex.of(destination), weight);
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    /**
     * Natural ordering by weight (for Kruskal's algorithm)
     */
    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    /**
     * Check if edge contains a specific vertex
     */
    public boolean contains(Vertex vertex) {
        return source.equals(vertex) || destination.equals(vertex);
    }

    /**
     * Get the other endpoint of the edge
     */
    public Vertex getOther(Vertex vertex) {
        if (source.equals(vertex)) return destination;
        if (destination.equals(vertex)) return source;
        throw new IllegalArgumentException("Vertex " + vertex + " is not part of this edge");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        // Undirected edge: (A,B) equals (B,A)
        return weight == edge.weight &&
                ((source.equals(edge.source) && destination.equals(edge.destination)) ||
                        (source.equals(edge.destination) && destination.equals(edge.source)));
    }

    @Override
    public int hashCode() {
        // Symmetric hash for undirected edges
        return Objects.hash(Math.min(source.hashCode(), destination.hashCode()),
                Math.max(source.hashCode(), destination.hashCode()),
                weight);
    }

    @Override
    public String toString() {
        return String.format("%s -- %s [%d]", source, destination, weight);
    }
}

/**
 * Immutable Graph representation using adjacency list
 * Thread-safe and optimized for read operations
 */
class Graph {
    private final Map<Vertex, List<Edge>> adjacencyList;
    private final Set<Vertex> vertices;
    private final List<Edge> edges;

    /**
     * Private constructor - use Builder pattern
     */
    private Graph(Builder builder) {
        // Create immutable defensive copies
        this.adjacencyList = Collections.unmodifiableMap(
                builder.adjacencyList.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> Collections.unmodifiableList(new ArrayList<>(e.getValue()))
                        ))
        );
        this.vertices = Collections.unmodifiableSet(new HashSet<>(builder.adjacencyList.keySet()));
        this.edges = Collections.unmodifiableList(new ArrayList<>(builder.edges));
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Edge> getAdjacentEdges(Vertex vertex) {
        return adjacencyList.getOrDefault(vertex, Collections.emptyList());
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * Functional stream-based operations
     */
    public Stream<Vertex> vertexStream() {
        return vertices.stream();
    }

    public Stream<Edge> edgeStream() {
        return edges.stream();
    }

    /**
     * Builder Pattern for flexible graph construction
     */
    public static class Builder {
        private final Map<Vertex, List<Edge>> adjacencyList = new HashMap<>();
        private final Set<Edge> edges = new HashSet<>();

        /**
         * Add an undirected edge to the graph
         */
        public Builder addEdge(Edge edge) {
            Objects.requireNonNull(edge, "Edge cannot be null");

            // Add edge to both vertices (undirected)
            adjacencyList.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge);
            adjacencyList.computeIfAbsent(edge.getDestination(), k -> new ArrayList<>()).add(edge);

            edges.add(edge);
            return this;
        }

        /**
         * Convenience method for adding edges
         */
        public Builder addEdge(String source, String destination, int weight) {
            return addEdge(Edge.between(source, destination, weight));
        }

        /**
         * Add multiple edges using functional approach
         */
        public Builder addEdges(Collection<Edge> edges) {
            edges.forEach(this::addEdge);
            return this;
        }

        public Graph build() {
            return new Graph(this);
        }
    }
}

// ============================================================================
// ALGORITHM INTERFACES AND STRATEGY PATTERN
// ============================================================================

/**
 * Strategy Interface for Minimum Spanning Tree algorithms
 * Allows interchangeable algorithms (Kruskal, Prim, etc.)
 */
interface MinimumSpanningTree {
    /**
     * Find the minimum spanning tree of the given graph
     *
     * @param graph Input graph
     * @return Result containing MST edges and total cost
     */
    MSTResult findMinimumSpanningTree(Graph graph);

    /**
     * Get the name of the algorithm
     */
    String getAlgorithmName();
}

/**
 * Result object containing MST computation results
 * Immutable to ensure data consistency
 */
class MSTResult {
    private final List<Edge> selectedEdges;
    private final int totalCost;
    private final long computationTimeMs;

    private MSTResult(List<Edge> selectedEdges, int totalCost, long computationTimeMs) {
        this.selectedEdges = Collections.unmodifiableList(new ArrayList<>(selectedEdges));
        this.totalCost = totalCost;
        this.computationTimeMs = computationTimeMs;
    }

    public static MSTResult of(List<Edge> edges, int cost, long timeMs) {
        return new MSTResult(edges, cost, timeMs);
    }

    public List<Edge> getSelectedEdges() {
        return selectedEdges;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public long getComputationTimeMs() {
        return computationTimeMs;
    }
}

// ============================================================================
// KRUSKAL'S ALGORITHM IMPLEMENTATION
// ============================================================================

/**
 * Kruskal's Algorithm Implementation
 * Uses Union-Find (Disjoint Set Union) for cycle detection
 *
 * Time Complexity: O(E log E) for sorting edges
 * Space Complexity: O(V) for Union-Find structure
 *
 * Best for: Sparse graphs where E << V²
 */
class KruskalMST implements MinimumSpanningTree {

    @Override
    public MSTResult findMinimumSpanningTree(Graph graph) {
        long startTime = System.currentTimeMillis();

        // Initialize Union-Find structure
        UnionFind unionFind = new UnionFind(graph.getVertices());

        // Get all edges and sort by weight (greedy approach)
        List<Edge> sortedEdges = graph.edgeStream()
                .sorted() // Natural ordering by weight
                .collect(Collectors.toList());

        // Select edges that don't create cycles
        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;

        for (Edge edge : sortedEdges) {
            Vertex u = edge.getSource();
            Vertex v = edge.getDestination();

            // Check if adding this edge creates a cycle
            if (!unionFind.connected(u, v)) {
                unionFind.union(u, v);
                mstEdges.add(edge);
                totalCost += edge.getWeight();

                // MST has exactly V-1 edges
                if (mstEdges.size() == graph.getVertexCount() - 1) {
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();

        return MSTResult.of(mstEdges, totalCost, endTime - startTime);
    }

    @Override
    public String getAlgorithmName() {
        return "Kruskal's Algorithm";
    }
}

/**
 * Union-Find (Disjoint Set Union) Data Structure
 * Implements path compression and union by rank optimizations
 *
 * Time Complexity: Nearly O(1) amortized for both operations
 */
class UnionFind {
    private final Map<Vertex, Vertex> parent;
    private final Map<Vertex, Integer> rank;

    /**
     * Initialize with all vertices as separate components
     */
    public UnionFind(Set<Vertex> vertices) {
        this.parent = new HashMap<>();
        this.rank = new HashMap<>();

        // Each vertex is its own parent initially
        vertices.forEach(v -> {
            parent.put(v, v);
            rank.put(v, 0);
        });
    }

    /**
     * Find root with path compression
     * Flattens the tree structure for faster future lookups
     */
    public Vertex find(Vertex vertex) {
        if (!parent.get(vertex).equals(vertex)) {
            // Path compression: make every node point to root
            parent.put(vertex, find(parent.get(vertex)));
        }
        return parent.get(vertex);
    }

    /**
     * Union by rank optimization
     * Keeps tree balanced by attaching smaller tree under larger tree
     */
    public void union(Vertex v1, Vertex v2) {
        Vertex root1 = find(v1);
        Vertex root2 = find(v2);

        if (root1.equals(root2)) {
            return; // Already in same set
        }

        // Attach smaller rank tree under higher rank tree
        int rank1 = rank.get(root1);
        int rank2 = rank.get(root2);

        if (rank1 < rank2) {
            parent.put(root1, root2);
        } else if (rank1 > rank2) {
            parent.put(root2, root1);
        } else {
            parent.put(root2, root1);
            rank.put(root1, rank1 + 1);
        }
    }

    /**
     * Check if two vertices are in the same connected component
     */
    public boolean connected(Vertex v1, Vertex v2) {
        return find(v1).equals(find(v2));
    }
}

// ============================================================================
// PRIM'S ALGORITHM IMPLEMENTATION
// ============================================================================

/**
 * Prim's Algorithm Implementation
 * Uses Priority Queue for efficient minimum edge selection
 *
 * Time Complexity: O(E log V) with binary heap
 * Space Complexity: O(V + E)
 *
 * Best for: Dense graphs and when starting from a specific vertex
 */
class PrimMST implements MinimumSpanningTree {

    @Override
    public MSTResult findMinimumSpanningTree(Graph graph) {
        long startTime = System.currentTimeMillis();

        if (graph.getVertexCount() == 0) {
            return MSTResult.of(Collections.emptyList(), 0, 0);
        }

        // Data structures for Prim's algorithm
        Set<Vertex> visited = new HashSet<>();
        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;

        // Priority queue ordered by edge weight
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(Edge::getWeight));

        // Start from arbitrary vertex (first vertex in set)
        Vertex startVertex = graph.getVertices().iterator().next();
        visited.add(startVertex);

        // Add all edges from start vertex
        pq.addAll(graph.getAdjacentEdges(startVertex));

        // Main loop: grow MST one edge at a time
        while (!pq.isEmpty() && visited.size() < graph.getVertexCount()) {
            Edge minEdge = pq.poll();

            // Determine which vertex is new
            Vertex newVertex = getUnvisitedVertex(minEdge, visited);

            if (newVertex == null) {
                continue; // Both vertices already visited (would create cycle)
            }

            // Add edge to MST
            mstEdges.add(minEdge);
            totalCost += minEdge.getWeight();
            visited.add(newVertex);

            // Add all edges from newly added vertex
            graph.getAdjacentEdges(newVertex).stream()
                    .filter(edge -> !visited.contains(edge.getOther(newVertex)))
                    .forEach(pq::add);
        }

        long endTime = System.currentTimeMillis();

        return MSTResult.of(mstEdges, totalCost, endTime - startTime);
    }

    /**
     * Helper method to find the unvisited vertex in an edge
     */
    private Vertex getUnvisitedVertex(Edge edge, Set<Vertex> visited) {
        boolean sourceVisited = visited.contains(edge.getSource());
        boolean destVisited = visited.contains(edge.getDestination());

        if (sourceVisited && !destVisited) {
            return edge.getDestination();
        } else if (!sourceVisited && destVisited) {
            return edge.getSource();
        }
        return null; // Both visited or both unvisited
    }

    @Override
    public String getAlgorithmName() {
        return "Prim's Algorithm";
    }
}

// ============================================================================
// FACTORY PATTERNS
// ============================================================================

/**
 * Abstract Factory for creating MST algorithm instances
 * Follows Factory Pattern and allows easy addition of new algorithms
 */
interface AlgorithmFactory {
    MinimumSpanningTree createKruskalAlgorithm();
    MinimumSpanningTree createPrimAlgorithm();
}

/**
 * Concrete implementation of Algorithm Factory
 */
class ConcreteAlgorithmFactory implements AlgorithmFactory {

    @Override
    public MinimumSpanningTree createKruskalAlgorithm() {
        return new KruskalMST();
    }

    @Override
    public MinimumSpanningTree createPrimAlgorithm() {
        return new PrimMST();
    }
}

// ============================================================================
// DATA LOADING STRATEGIES
// ============================================================================

/**
 * Strategy interface for loading graph data
 * Allows different loading strategies (file, database, hardcoded, etc.)
 */
interface GraphLoader {
    Graph loadGraph(String identifier) throws IOException;
}

/**
 * Hardcoded graph loader for this specific problem
 * Can be replaced with file-based or database loader
 */
class HardcodedGraphLoader implements GraphLoader {

    @Override
    public Graph loadGraph(String identifier) throws IOException {
        if ("graph1_input.txt".equals(identifier)) {
            return loadGraph1();
        } else if ("graph2_input.txt".equals(identifier)) {
            return loadGraph2();
        }
        throw new IllegalArgumentException("Unknown graph identifier: " + identifier);
    }

    /**
     * Load Question 1 graph (damaged network)
     */
    private Graph loadGraph1() {
        return new Graph.Builder()
                .addEdge("B", "M", 7)
                .addEdge("B", "S", 13)
                .addEdge("C", "Q", 25)
                .addEdge("C", "S", 19)
                .addEdge("C", "L", 22)
                .addEdge("C", "K", 7)
                .addEdge("D", "C", 5)
                .addEdge("G", "R", 12)
                .addEdge("I", "E", 76)
                .addEdge("I", "J", 44)
                .addEdge("I", "T", 11)
                .addEdge("J", "Y", 6)
                .addEdge("K", "H", 32)
                .addEdge("K", "B", 35)
                .addEdge("P", "D", 18)
                .addEdge("P", "K", 14)
                .addEdge("Q", "S", 16)
                .addEdge("S", "M", 9)
                .addEdge("T", "W", 17)
                .addEdge("T", "Y", 21)
                .addEdge("T", "J", 8)
                .addEdge("U", "F", 3)
                .addEdge("U", "X", 30)
                .addEdge("U", "I", 20)
                .addEdge("W", "N", 55)
                .addEdge("X", "G", 4)
                .build();
    }

    /**
     * Load Question 2 graph (damaged network + repaired roads)
     */
    private Graph loadGraph2() {
        return new Graph.Builder()
                // Original damaged network
                .addEdge("B", "M", 7)
                .addEdge("B", "S", 13)
                .addEdge("C", "Q", 25)
                .addEdge("C", "S", 19)
                .addEdge("C", "L", 22)
                .addEdge("C", "K", 7)
                .addEdge("D", "C", 5)
                .addEdge("G", "R", 12)
                .addEdge("I", "E", 76)
                .addEdge("I", "J", 44)
                .addEdge("I", "T", 11)
                .addEdge("J", "Y", 6)
                .addEdge("K", "H", 32)
                .addEdge("K", "B", 35)
                .addEdge("P", "D", 18)
                .addEdge("P", "K", 14)
                .addEdge("Q", "S", 16)
                .addEdge("S", "M", 9)
                .addEdge("T", "W", 17)
                .addEdge("T", "Y", 21)
                .addEdge("T", "J", 8)
                .addEdge("U", "F", 3)
                .addEdge("U", "X", 30)
                .addEdge("U", "I", 20)
                .addEdge("W", "N", 55)
                .addEdge("X", "G", 4)
                // Additional repaired roads from Table 2
                .addEdge("Y", "K", 24)
                .addEdge("J", "P", 10)
                .addEdge("J", "H", 52)
                .addEdge("N", "H", 25)
                .addEdge("J", "E", 31)
                .addEdge("G", "K", 34)
                .build();
    }
}

// ============================================================================
// REPORTING AND OUTPUT
// ============================================================================

/**
 * Immutable report object containing algorithm results
 * Uses Builder pattern for flexible construction
 */
class AlgorithmReport {
    private final int questionNumber;
    private final int minimumCost;
    private final String algorithmName;
    private final String reasoning;
    private final List<Edge> edges;

    private AlgorithmReport(Builder builder) {
        this.questionNumber = builder.questionNumber;
        this.minimumCost = builder.minimumCost;
        this.algorithmName = builder.algorithmName;
        this.reasoning = builder.reasoning;
        this.edges = Collections.unmodifiableList(new ArrayList<>(builder.edges));
    }

    public String formatOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Question %d:%n", questionNumber));
        sb.append(String.format("- Minimum cost of keeping the cities connected is %d%n", minimumCost));
        sb.append(String.format("- Algorithm used is %s because %s%n", algorithmName, reasoning));
        sb.append(String.format("%nSelected Edges (%d):%n", edges.size()));
        edges.forEach(edge -> sb.append(String.format("  %s%n", edge)));
        return sb.toString();
    }

    public int getQuestionNumber() { return questionNumber; }
    public int getMinimumCost() { return minimumCost; }
    public String getAlgorithmName() { return algorithmName; }
    public String getReasoning() { return reasoning; }
    public List<Edge> getEdges() { return edges; }

    /**
     * Builder for AlgorithmReport
     */
    public static class Builder {
        private int questionNumber;
        private int minimumCost;
        private String algorithmName;
        private String reasoning;
        private List<Edge> edges = new ArrayList<>();

        public Builder setQuestionNumber(int questionNumber) {
            this.questionNumber = questionNumber;
            return this;
        }

        public Builder setMinimumCost(int minimumCost) {
            this.minimumCost = minimumCost;
            return this;
        }

        public Builder setAlgorithmName(String algorithmName) {
            this.algorithmName = algorithmName;
            return this;
        }

        public Builder setReasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }

        public Builder setEdges(List<Edge> edges) {
            this.edges = new ArrayList<>(edges);
            return this;
        }

        public AlgorithmReport build() {
            return new AlgorithmReport(this);
        }
    }
}

/**
 * Strategy interface for writing output
 */
interface OutputWriter {
    void writeReport(AlgorithmReport report, String filename) throws IOException;
}

/**
 * File-based output writer implementation
 */
class FileOutputWriter implements OutputWriter {

    @Override
    public void writeReport(AlgorithmReport report, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report.formatOutput());
        }
    }
}