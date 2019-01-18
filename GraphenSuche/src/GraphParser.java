import java.util.HashMap;
import java.util.Map;

public class GraphParser {

    public static Graph parse(String verticesInput, String edgesInput) {
        Graph result = new Graph();

        String[] vertexNames = verticesInput.split(",");

        Map<String, Vertex> vertices = new HashMap<>();
        for (String vertexName : vertexNames) {
            Vertex v = new Vertex(vertexName);
            vertices.put(vertexName, v);
            result.addVertex(v);
        }

        String[] edgeDescriptions = edgesInput.split(",");
        for (String edgeDescription : edgeDescriptions) {
            char vertexAName = edgeDescription.charAt(0);
            char vertexBName = edgeDescription.charAt(1);
            String weightString = edgeDescription.substring(2);
            double weight = Double.parseDouble(weightString);

            Vertex vertexA = vertices.get("" + vertexAName);
            Vertex vertexB = vertices.get("" + vertexBName);

            Edge e = new Edge(vertexA, vertexB, weight);
            result.addEdge(e);
        }

        return result;
    }

}
