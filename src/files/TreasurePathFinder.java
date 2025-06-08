package files;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;
import javafx.scene.paint.Color;


public class TreasurePathFinder {
    public static double Time(List<Integer> path, Map<Integer, List<Node>> graph) {
        double totalWeight = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            int current = path.get(i);
            int next = path.get(i + 1);

            // Найдём ребро между current и next
            boolean found = false;
            for (Node neighbor : graph.getOrDefault(current, List.of())) {
                if (neighbor.vertex == next) {
                    totalWeight += neighbor.weight;
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.err.println("⚠️ Ребро между " + current + " и " + next + " не найдено в графе.");
            }
        }

        return totalWeight;
    }

    public static Double totalTime = null;

    public static List<Integer> findPath(Connection conn, String startStation, String endStation) {
        Map<Integer, List<Node>> graph = GraphLoader.loadGraphFromDatabase(conn);

        Integer start = getStationIdByName(startStation);
        Integer end = getStationIdByName(endStation);

        if (start == null || end == null) return Collections.emptyList();

        Map<Integer, Integer> previous = new HashMap<>();
        dijkstra(graph, start, previous);
        List<Integer> result_path = buildPath(previous, end);
        if (!result_path.isEmpty()){
            totalTime = Time(result_path, graph);
        }
        return result_path;
    }
    public static class MetroStation {
        public int id;
        public String name;
        public double x, y;
        public double angleDeg = -1;
        public Color color;

        public MetroStation(int id, String name, double x, double y, Color color) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public MetroStation(int id, String name, double angleDeg, Color color) {
            this.id = id;
            this.name = name;
            this.angleDeg = angleDeg;
            this.color = color;
        }

        public boolean isCircle() {
            return angleDeg != -1;
        }

        public void computeCoordinatesIfCircle() {
            if (isCircle()) {
                double rad = Math.toRadians(this.angleDeg);
                this.x = 550 + 400 * Math.cos(rad);
                this.y = 550 + 400 * Math.sin(rad);
            }
        }
    }



    public static void dijkstra(Map<Integer, List<Node>> graph, int start, Map<Integer, Integer> previous) {
        Map<Integer, Double> distances = new HashMap<>();
        for (int node : graph.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);

        Set<Integer> unvisited = new HashSet<>(graph.keySet());

        while (!unvisited.isEmpty()) {
            Integer currentNode = null;
            double minDistance = Double.POSITIVE_INFINITY;
            for (int node : unvisited) {
                if (distances.get(node) < minDistance) {
                    minDistance = distances.get(node);
                    currentNode = node;
                }
            }

            if (currentNode == null) break;

            for (Node neighbor : graph.getOrDefault(currentNode, new ArrayList<>())) {
                double newDistance = distances.get(currentNode) + neighbor.weight;
                if (newDistance < distances.getOrDefault(neighbor.vertex, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor.vertex, newDistance);
                    previous.put(neighbor.vertex, currentNode);
                }
            }

            unvisited.remove(currentNode);
        }
    }
    public static List<MetroStation> findPathAndBuildStations(String start, String end) {
        try (Connection conn = PostgresConnector.connect()) {

            List<Integer> path = findPath(conn, start, end);
            return getStationData(conn, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public static List<MetroStation> getStationData(Connection conn, List<Integer> path) {
        List<MetroStation> result = new ArrayList<>();
        for (int id : path) {
            try {
                if (id <= 28) {
                    PreparedStatement stmt = conn.prepareStatement("SELECT name, x, y, color FROM station WHERE id = ?");
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        double x = rs.getDouble("x");
                        double y = rs.getDouble("y");
                        String colorStr = rs.getString("color");
                        Color color = Color.valueOf(colorStr); // заменили web() на valueOf()
                        result.add(new MetroStation(id, name, x, y, color));
                    }
                    stmt.close();
                } else {
                    PreparedStatement stmt = conn.prepareStatement("SELECT name, corner, color FROM station_circle WHERE id = ?");
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        double angle = rs.getDouble("corner");
                        String colorStr = rs.getString("color");
                        Color color = Color.valueOf(colorStr); // заменили web() на valueOf()
                        result.add(new MetroStation(id, name, angle, color));
                    }
                    stmt.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }



    public static Integer getStationIdByName(String name) {
        Connection conn = PostgresConnector.connect();
        String sql = "SELECT id FROM station WHERE name = ? UNION SELECT id FROM station_circle WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Integer> buildPath(Map<Integer, Integer> prev, int end) {
        List<Integer> path = new ArrayList<>();
        for (Integer at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    public static void addEdge(Map<Integer, List<Node>> graph, int u, int v, double w) {
        graph.computeIfAbsent(u, k -> new ArrayList<>()).add(new Node(v, w));

    }

    public static class Node {
        public int vertex;
        public double weight;

        public Node(int vertex, double weight) {
            this.vertex = vertex;
            this.weight = weight;
        }
    }
}
