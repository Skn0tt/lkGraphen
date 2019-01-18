public class BreadthTraverser implements GraphTraverser {

    @Override
    public List<String> traverse(Graph g, Vertex start) {
        List<String> result = new List<>();

        start.setMark(true);

        Queue<Vertex> d = new Queue<>();
        d.enqueue(start);

        while (!d.isEmpty()) {
            Vertex w = d.front();
            d.dequeue();

            List<Vertex> neighborsOfW = g.getNeighbours(w);
            for (neighborsOfW.toFirst(); neighborsOfW.hasAccess(); neighborsOfW.next()) {
                Vertex u = neighborsOfW.getContent();

                if (!u.isMarked()) {
                    u.setMark(true);
                    d.enqueue(u);

                    result.append(w.getID() + u.getID() + "1");
                }
            }
        }

        return result;
    }

}
