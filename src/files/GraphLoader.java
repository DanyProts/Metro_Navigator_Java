package files;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GraphLoader {

    public static Map<Integer, List<TreasurePathFinder.Node>> loadGraphFromDatabase(Connection conn) {
        Map<Integer, List<TreasurePathFinder.Node>> graph = new HashMap<>();

        String sql = "SELECT from_id, to_id, weight FROM connections";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int from = rs.getInt("from_id");
                int to = rs.getInt("to_id");
                int weight = rs.getInt("weight");

                // Добавляем ребро в обе стороны (неориентированный граф)
                TreasurePathFinder.addEdge(graph, from, to, weight);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return graph;
    }
}
