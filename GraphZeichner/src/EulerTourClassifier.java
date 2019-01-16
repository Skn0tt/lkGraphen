import java.util.HashMap;
import java.util.Map;

public class EulerTourClassifier {

    public static boolean hasEulerTour(Graph g) {

        Map<String, Integer> degrees = getDegrees(g.getEdges());

        int countOfUnEvenEdges = 0;

        List<Vertex> vertices = g.getVertices();

        vertices.toFirst();
        while (vertices.hasAccess()) {
            Vertex v = vertices.getContent();
            int degree = degrees.getOrDefault(v.getID(), 0);
            if (degree % 2 == 1) {
                countOfUnEvenEdges++;
            }

            vertices.next();
        }

        return countOfUnEvenEdges <= 2;
    }

    private static Map<String, Integer> getDegrees(List<Edge> edges) {
        Map<String, Integer> result = new HashMap<>();
        edges.toFirst();

        while (edges.hasAccess()) {
            Edge currentEdge = edges.getContent();

            Vertex[] vertices = currentEdge.getVertices();
            for (Vertex v : vertices) {
                String id = v.getID();
                result.put(
                  id,
                  result.getOrDefault(id, 0) + 1
                );
            }

            edges.next();
        }

        return result;
    }

}
