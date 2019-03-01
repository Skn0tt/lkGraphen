public class Test {

    public static void main(String... args) {
        Graph input = GraphParser.parse("A,B,C,D,E,F,G,H,I", "AG1,FG1,FI1,EI1,GE1,DE1,CI1,BI1,BH1,AH1");

        System.out.println("Breadth: ");
        printList(getTraversal(input, "A", false));

        System.out.println("Depth: ");
        printList(getTraversal(input, "A", true));
    }

    private static List<String> getTraversal(Graph g, String beginNode, boolean useDepth) {
        GraphTraverser graphTraverser = useDepth
                                        ? new DepthTraverser()
                                        : new BreadthTraverser();

        List<String> traversal = graphTraverser.traverse(g, g.getVertex(beginNode));

        return traversal;
    }

    private static void printList(List<String> list) {
        for (list.toFirst(); list.hasAccess(); list.next()) {
            System.out.println(list.getContent());
        }
    }

}
