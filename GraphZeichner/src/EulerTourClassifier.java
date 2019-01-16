import java.util.HashMap;
import java.util.Map;

public class EulerTourClassifier {

    public static boolean hasEulerTour(Graph g) {
        int countOfUnEvenEdges = 0;

        List<Vertex> vertices = g.getVertices();

        vertices.toFirst();
        while (vertices.hasAccess()) {
            Vertex v = vertices.getContent();

            vertices.next();
        }

    }

    private static Map<String, Integer> getDegrees(List<Edge> edges) {
        Map<String, Integer> result = new HashMap<>();
        edges.toFirst();

        while (edges.hasAccess()) {

        }
    }

    private static int length(List l) {
        l.toFirst();

        int counter = 0;

        while(l.hasAccess()) {
            counter++;
            l.next();
        }

        return counter;
    }
}
