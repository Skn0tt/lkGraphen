public class DepthTraverser implements GraphTraverser {
    @Override
    public List<String> traverse(Graph g, Vertex start) {
        GraphCleaner.clearMarks(g);

        List<String> result = new List<>();

        start.setMark(true);

        Stack<Vertex> d = new Stack<>();
        d.push(start);

        while (!d.isEmpty()) {
            Vertex w = d.top();
            d.pop();

            List<Vertex> neighborsOfW = g.getNeighbours(w);
            for (neighborsOfW.toFirst(); neighborsOfW.hasAccess(); neighborsOfW.next()) {
                Vertex u = neighborsOfW.getContent();

                if (!u.isMarked()) {
                    u.setMark(true);
                    d.push(u);

                    result.append(w.getID() + u.getID() + "1");
                }
            }
        }

        return result;
    }
}
