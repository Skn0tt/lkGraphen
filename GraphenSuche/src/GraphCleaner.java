public class GraphCleaner {

  static void clearMarks(Graph g) {
    List<Edge> edges = g.getEdges();
    for (edges.toFirst(); edges.hasAccess(); edges.next()) {
      Edge e = edges.getContent();
      e.setMark(false);
    }

    List<Vertex> vertices = g.getVertices();
    for (vertices.toFirst(); vertices.hasAccess(); vertices.next()) {
      Vertex v = vertices.getContent();
      v.setMark(false);
    }
  }
}
