
// Assignment 10
// Friedman, Benjamin
// friedmanbe
// Varghese, Ashwin
// ashwinvarghese
// Eric Chung
// wasnotprovided

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents a node of the graph that represents the maze 
class Node {

  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;

  // represents the location of the node
  Posn location;

  // the four adjacent nodes to this one
  Node left;
  Node top;
  Node right;
  Node bottom;

  // represents whether this node has been explored
  boolean isExplored;

  // represents whether this is the the node the player is on
  boolean isPlayerNode;

  // represents whether this node is a solution node
  boolean isSolution;

  Node(int x, int y) {
    location = new Posn(x, y);
    this.x = x;
    this.y = y;
  }

  // makes the given node the bottom neighbor
  public void fixBottom(Node node) {
    this.bottom = node;
  }

  // makes the given node the top neighbor
  public void fixTop(Node node) {
    this.top = node;

  }

  // makes the given node the left neighbor
  public void fixLeft(Node node) {
    this.left = node;
  }

  // makes the given node the right neighbor
  public void fixRight(Node node) {
    this.right = node;
  }

  // creates an image for this node
  public WorldImage nodeImage() {
    if (this.isPlayerNode) {
      WorldImage w1 = new CircleImage(MazeWorld.CELL_SIZE / 3, "solid", Color.RED);
      WorldImage w2 = new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, "solid",
          new Color(145, 184, 244));
      WorldImage w3 = new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, "solid",
          new Color(62, 118, 205));
      if (this.isSolution) {
        return new OverlayImage(w1, w3);
      }
      else {
        return new OverlayImage(w1, w2);
      }
    }
    if (this.isSolution) {
      return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, "solid",
          new Color(62, 118, 205));
    }
    if (this.isExplored) {
      return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, "solid",
          new Color(145, 184, 244));
    }
    else {
      return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, "solid",
          new Color(192, 192, 192));
    }

  }
}

// represents an edge in the graph that represents the maze
class Edge {

  // represents the starting and end points of this edge
  Node from;
  Node to;

  // represents the weight of this edge
  int weight;

  Edge(Node from, Node to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

}

// compares the weights of edges
class WeightComparator implements Comparator<Edge> {

  // determines which edge comes first based on their weight
  public int compare(Edge e1, Edge e2) {
    // TODO Auto-generated method stub
    return e1.weight - e2.weight;
  }

}

// represents the maze
class MazeWorld extends World {

  MazeWorld() {
    this.createNodes();
    this.fixNeighbors();
    this.createEdges();
    this.createReps();
    this.createMSpanning();
    this.playerNode = this.board.get(0).get(0);
    this.playerNode.isExplored = true;
    this.playerNode.isPlayerNode = true;
  }

  // x and y lengths of the maze
  static final int WIDTH = 100;
  static final int HEIGHT = 75;

  // distance between nodes and size of cell
  static final int CELL_SIZE = 10;

  // represents whether a search has been selected
  boolean bfs;
  boolean dfs;

  // represents the Node the player is on
  Node playerNode;

  // represents the solution path to the maze
  ArrayList<Node> solPath = new ArrayList<Node>();

  // represent the list of already explored nodes
  ArrayList<Node> visited = new ArrayList<Node>();

  // represents the grid of nodes that comprise the maze
  ArrayList<ArrayList<Node>> board;

  // represents all the edges in this maze
  ArrayList<Edge> edgeList;

  // represents the edges in the constructed minimal spanning tree
  ArrayList<Edge> mSpanningTree;

  // represents a map of nodes to their representatives
  HashMap<Posn, Posn> map;

  // allows the user to traverse maze and let computer solve it
  public void onKeyEvent(String ke) {
    if (ke.equals("b")) {
      this.bfs = true;
      this.search(ke);
    }
    if (ke.equals("d")) {
      this.dfs = true;
      this.search(ke);
    }
    if (ke.equals("n")) {
      this.createNodes();
      this.fixNeighbors();
      this.createEdges();
      this.createReps();
      this.createMSpanning();
      this.playerNode = this.board.get(0).get(0);
      this.playerNode.isExplored = true;
      this.playerNode.isPlayerNode = true;
      this.bfs = false;
      this.dfs = false;
    }
    if (ke.equals("right") && this.isLegalMove(ke)) {
      this.playerNode.isPlayerNode = false;
      this.playerNode = this.playerNode.right;
      this.playerNode.isPlayerNode = true;
      this.playerNode.isExplored = true;
    }
    if (ke.equals("left") && this.isLegalMove(ke)) {
      this.playerNode.isPlayerNode = false;
      this.playerNode = this.playerNode.left;
      this.playerNode.isPlayerNode = true;
      this.playerNode.isExplored = true;
    }
    if (ke.equals("up") && this.isLegalMove(ke)) {
      this.playerNode.isPlayerNode = false;
      this.playerNode = this.playerNode.top;
      this.playerNode.isPlayerNode = true;
      this.playerNode.isExplored = true;
    }
    if (ke.equals("down") && this.isLegalMove(ke)) {
      this.playerNode.isPlayerNode = false;
      this.playerNode = this.playerNode.bottom;
      this.playerNode.isPlayerNode = true;
      this.playerNode.isExplored = true;
    }

  }

  // handles if the player gets to the end of the maze
  public WorldEnd worldEnds() {
    if (this.playerNode.location.equals(this.board.get(HEIGHT - 1).get(WIDTH - 1).location)) {
      return new WorldEnd(true, this.makeWinScene());
    }
    else if (this.solPath.size() == 0 && (this.bfs || this.dfs)) {
      return new WorldEnd(true, this.makeWinScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // displays ending text for when you win
  WorldScene makeWinScene() {
    WorldScene w = this.makeScene();
    w.placeImageXY(new TextImage("SOLVED!", 100, Color.GREEN), 500, 375);
    return w;
  }

  // determines whether it's possible to move in the key's direction
  boolean isLegalMove(String ke) {
    boolean isLegal = false;
    for (Edge e : this.mSpanningTree) {
      if (ke.equals("right")) {
        if (this.playerNode.right.equals(e.from) && this.playerNode.equals(e.to)
            || (this.playerNode.right.equals(e.to) && this.playerNode.equals(e.from))) {
          isLegal = true;
          break;
        }
      }
      else if (ke.equals("left")) {
        if (this.playerNode.left.equals(e.from) && this.playerNode.equals(e.to)
            || (this.playerNode.left.equals(e.to) && this.playerNode.equals(e.from))) {
          isLegal = true;
          break;
        }
      }
      else if (ke.equals("up")) {
        if (this.playerNode.top.equals(e.from) && this.playerNode.equals(e.to)
            || (this.playerNode.top.equals(e.to) && this.playerNode.equals(e.from))) {
          isLegal = true;
          break;
        }
      }
      else if (ke.equals("down")) {
        if (this.playerNode.bottom.equals(e.from) && this.playerNode.equals(e.to)
            || (this.playerNode.bottom.equals(e.to) && this.playerNode.equals(e.from))) {
          isLegal = true;
          break;
        }
      }
    }
    return isLegal;
  }

  // constructs the minimal spanning tree from the edge list
  void createMSpanning() {
    mSpanningTree = new ArrayList<Edge>();
    this.sortEdges();
    this.createReps();
    for (int i = 0; i < edgeList.size(); i++) {
      Edge currEdge = this.edgeList.get(i);
      Posn keyX = currEdge.from.location;
      Posn keyY = currEdge.to.location;

      if (this.find(keyX).equals(this.find(keyY))) {
        // do nothing
      }
      else {
        mSpanningTree.add(currEdge);
        this.union(keyX, keyY);
      }
    }
  }

  // sets the representative of the first given key to that of the second
  void union(Posn keyX, Posn keyY) {
    this.map.put(this.find(keyX), this.find(keyY));
  }

  // finds the representative of the given key
  public Posn find(Posn keyX) {
    // TODO Auto-generated method stub
    if (map.get(keyX).equals(keyX)) {
      return map.get(keyX);
    }
    else {
      return find(map.get(keyX));
    }
  }

  // initializes every node's representative to itself
  void createReps() {
    map = new HashMap<Posn, Posn>();
    for (int i = 0; i < MazeWorld.HEIGHT; i++) {
      for (int j = 0; j < MazeWorld.WIDTH; j++) {
        map.put(this.board.get(i).get(j).location, this.board.get(i).get(j).location);
      }
    }
  }

  // creates the edges of the maze (has to happen after createNodes)
  void createEdges() {
    this.edgeList = new ArrayList<Edge>();
    Random rand = new Random();
    for (int i = 0; i < MazeWorld.HEIGHT; i++) {
      for (int j = 0; j < MazeWorld.WIDTH; j++) {
        Node currNode = this.board.get(i).get(j);
        this.edgeList.add(new Edge(currNode, currNode.right, rand.nextInt(10000) + 1));
        this.edgeList.add(new Edge(currNode, currNode.bottom, rand.nextInt(10000) + 1));
      }
    }
  }

  // sorts edges by weights in increasing order
  void sortEdges() {
    this.edgeList.sort(new WeightComparator());
  }

  // creates the maze nodes and adds all the nodes to the board
  void createNodes() {
    this.board = new ArrayList<ArrayList<Node>>();
    for (int i = 0; i < MazeWorld.HEIGHT; i++) {
      ArrayList<Node> rowI = new ArrayList<Node>();
      board.add(rowI);
      for (int j = 0; j < MazeWorld.WIDTH; j++) {
        board.get(i).add(new Node(j, i));
      }
    }
  }

  // fix the neighbors of the nodes on the board
  void fixNeighbors() {
    for (int i = 0; i < MazeWorld.HEIGHT; i++) { // each row
      for (int j = 0; j < MazeWorld.WIDTH; j++) { // each element in that row

        Node currNode = board.get(i).get(j);

        // handles each neighbor update individually
        if (j < (MazeWorld.WIDTH - 1)) {
          currNode.fixRight(board.get(i).get(j + 1));
        }

        else {
          currNode.fixRight(currNode);
        }

        if (j > 0) {
          currNode.fixLeft(board.get(i).get(j - 1));
        }

        else {
          currNode.fixLeft(currNode);
        }

        if (i > 0) {
          currNode.fixTop(board.get(i - 1).get(j));
        }

        else {
          currNode.fixTop(currNode);

        }

        if (i < (MazeWorld.HEIGHT - 1)) {
          currNode.fixBottom(board.get(i + 1).get(j));
        }

        else {
          currNode.fixBottom(currNode);
        }
      }
    }
  }

  // renders the initial state of the game
  public WorldScene makeScene() {
    WorldScene w = this.getEmptyScene();
    for (int i = 0; i < MazeWorld.HEIGHT; i++) {
      for (int j = 0; j < MazeWorld.WIDTH; j++) {
        Node currNode = this.board.get(i).get(j);
        w.placeImageXY(currNode.nodeImage(),
            MazeWorld.CELL_SIZE * currNode.x + MazeWorld.CELL_SIZE / 2,
            MazeWorld.CELL_SIZE * currNode.y + MazeWorld.CELL_SIZE / 2);
      }
    }
    this.edgeList.removeAll(this.mSpanningTree); // all the non spanning edges
    for (int i = 0; i < this.edgeList.size(); i++) {
      Edge currEdge = this.edgeList.get(i);
      if (new Posn(0, 1).equals(distancePosn(currEdge.from, currEdge.to))) {
        w.placeImageXY(this.horizontalLine(),
            MazeWorld.CELL_SIZE * currEdge.to.x + MazeWorld.CELL_SIZE / 2,
            MazeWorld.CELL_SIZE * currEdge.to.y);
      }
      if (new Posn(1, 0).equals(distancePosn(currEdge.from, currEdge.to))) {
        w.placeImageXY(this.verticalLine(), MazeWorld.CELL_SIZE * currEdge.to.x,
            MazeWorld.CELL_SIZE * currEdge.to.y + MazeWorld.CELL_SIZE / 2);
      }
    }

    return w;
  }

  // updates the world state for every tick
  public void onTick() {
    boolean consumedVisited = false;
    if (this.visited.size() > 0 && this.bfs && !this.dfs) {
      Node currVisited = this.visited.remove(0);
      currVisited.isExplored = true;
    }
    else if (this.visited.size() > 0 && this.dfs && !this.bfs) {
      Node currVisited = this.visited.remove(0);
      currVisited.isExplored = true;
    }
    else {
      consumedVisited = true;
    }
    if (this.solPath.size() > 0 && (consumedVisited) && this.bfs && !this.dfs) {
      Node currSolNode = this.solPath.remove(0);
      currSolNode.isSolution = true;
    }
    else if (this.solPath.size() > 0 && (consumedVisited) && this.dfs && !this.bfs) {
      Node currSolNode = this.solPath.remove(0);
      currSolNode.isSolution = true;
    }

  }

  // performs a depth-first search or breadth first search depending on given
  // string
  public void search(String type) {
    this.solPath = new ArrayList<Node>();
    this.visited = new ArrayList<Node>();
    HashMap<Node, Edge> cameFromEdge = new HashMap<Node, Edge>();
    Deque<Node> worklist = new ArrayDeque<Node>();
    Node next;
    worklist.add(this.board.get(0).get(0));
    while (worklist.size() > 0) {
      if (type.equals("b")) {
        next = worklist.removeFirst();
      }
      else {
        next = worklist.removeLast();
      }
      if (visited.contains(next)) {
        continue;
      }
      else if (next.equals(this.board.get(HEIGHT - 1).get(WIDTH - 1))) {
        reconstruct(cameFromEdge, next);
        worklist.clear();
      }
      else {
        for (Edge e : this.mSpanningTree) {
          if (((next.top.equals(e.from)) && (next.equals(e.to)))
              || ((next.top.equals(e.to)) && (next.equals(e.from)))) {
            worklist.add(next.top);
            if (!cameFromEdge.containsKey(next.top)) {
              cameFromEdge.put(next.top, new Edge(e.to, e.from, e.weight));
            }
          }
          if (((next.bottom.equals(e.from)) && (next.equals(e.to)))
              || ((next.bottom.equals(e.to)) && (next.equals(e.from)))) {
            worklist.add(next.bottom);
            if (!cameFromEdge.containsKey(next.bottom)) {
              cameFromEdge.put(next.bottom, e);
            }
          }
          if (((next.left.equals(e.from)) && (next.equals(e.to)))
              || ((next.left.equals(e.to)) && (next.equals(e.from)))) {
            worklist.add(next.left);
            if (!cameFromEdge.containsKey(next.left)) {
              cameFromEdge.put(next.left, new Edge(e.to, e.from, e.weight));
            }
          }
          if (((next.right.equals(e.from)) && (next.equals(e.to)))
              || ((next.right.equals(e.to)) && (next.equals(e.from)))) {
            worklist.add(next.right);
            if (!cameFromEdge.containsKey(next.right)) {
              cameFromEdge.put(next.right, e);
            }
          }
        }
        visited.add(next);
      }
    }
  }

  // creates the solution path by backtracking through the HashMap
  void reconstruct(HashMap<Node, Edge> cameFromEdge, Node next) {
    if (next.equals(this.board.get(0).get(0))) {
      this.solPath.add(next);
    }

    else {
      this.solPath.add(next);
      reconstruct(cameFromEdge, cameFromEdge.get(next).from);

    }
  }

  // creates a black horizontal line
  WorldImage horizontalLine() {
    return new RectangleImage(MazeWorld.CELL_SIZE, 1, "solid", Color.BLACK);
  }

  // creates a black horizontal line
  WorldImage verticalLine() {
    return new RectangleImage(1, MazeWorld.CELL_SIZE, "solid", Color.BLACK);
  }

  // creates a posn that contains the difference in the x and y of the nodes
  Posn distancePosn(Node from, Node to) {
    int diffx = to.x - from.x;
    int diffy = to.y - from.y;
    return new Posn(diffx, diffy);
  }
}

// represents maze example
class MazeExamples {

  // example of a MazeWorld
  MazeWorld tinyWorld = new MazeWorld();

  // examples of nodes
  Node a;
  Node b;
  Node c;
  Node d;
  Node e;
  Node f;
  Node g;
  Node h;
  Node i;

  // examples of edges
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;

  // examples of lists of edges
  ArrayList<Edge> smallListEdges;
  ArrayList<Edge> smallListEdgesSorted;

  // examples of comparators
  WeightComparator weightComp;

  // example of HashMap
  HashMap<Posn, Posn> map1;

  // initializes the examples in their original state
  void init() {

    a = new Node(0, 0);
    b = new Node(1, 0);
    d = new Node(0, 1);

    e1 = new Edge(a, b, 10);
    e2 = new Edge(a, d, 15);
    e3 = new Edge(b, c, 8);
    e4 = new Edge(b, e, 17);

    smallListEdges = new ArrayList<Edge>();
    smallListEdges.add(e1);
    smallListEdges.add(e2);
    smallListEdges.add(e3);
    smallListEdges.add(e4);

    smallListEdgesSorted = new ArrayList<Edge>();
    smallListEdgesSorted.add(e3);
    smallListEdgesSorted.add(e1);
    smallListEdgesSorted.add(e2);
    smallListEdgesSorted.add(e4);

    weightComp = new WeightComparator();
    map1 = new HashMap<Posn, Posn>();
  }

  void testSearch(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    this.tinyWorld.search("b");
    t.checkExpect(this.tinyWorld.solPath.size() <= this.tinyWorld.visited.size(), true);
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    this.tinyWorld.search("d");
    t.checkExpect(this.tinyWorld.solPath.size() <= this.tinyWorld.visited.size(), true);

  }

  // tests the onTick method for the MazeWorld
  void testOnTick(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    this.tinyWorld.onTick();
    t.checkExpect(this.tinyWorld.board.get(0).get(0).isExplored, false);
    t.checkExpect(this.tinyWorld.board.get(74).get(99).isExplored, false);

  }

  // tests the makeWinScene method for the MazeWorld class
  void testMakeWinScene(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    WorldScene w = this.tinyWorld.makeScene();
    w.placeImageXY(new TextImage("SOLVED!", 100, Color.GREEN), 500, 375);
    t.checkExpect(this.tinyWorld.makeWinScene(), w);
  }

  // tests isLegalMove method for the MazeWorld class
  void testIsLegalMove(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    this.tinyWorld.playerNode = this.tinyWorld.board.get(0).get(0);
    this.tinyWorld.playerNode.isExplored = true;
    this.tinyWorld.playerNode.isPlayerNode = true;
    // will always be random
  }

  // tests the WorldEnds method for the MazeWorld class
  void testWorldEnds(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    this.tinyWorld.playerNode.location = this.tinyWorld.board.get(MazeWorld.HEIGHT - 1)
        .get(MazeWorld.WIDTH - 1).location;
    t.checkExpect(this.tinyWorld.worldEnds(), new WorldEnd(true, this.tinyWorld.makeWinScene()));
    this.tinyWorld.playerNode.location = this.tinyWorld.board.get(4).get(5).location;
    this.tinyWorld.solPath.clear();
    this.tinyWorld.bfs = true;
    this.tinyWorld.dfs = false;
    t.checkExpect(this.tinyWorld.worldEnds(), new WorldEnd(true, this.tinyWorld.makeWinScene()));
  }

  // tests the compare method for the WeightComparator class
  void testCompare(Tester t) {
    this.init();
    t.checkExpect(this.weightComp.compare(e1, e2), -5);
    t.checkExpect(this.weightComp.compare(e2, e1), 5);
    t.checkExpect(this.weightComp.compare(e4, e4), 0);
  }

  // tests the sortEdges method for the MazeWorld class
  void testSortEdges(Tester t) {
    this.init();
    this.tinyWorld.edgeList = this.smallListEdges;
    this.tinyWorld.edgeList.sort(weightComp);
    t.checkExpect(this.tinyWorld.edgeList, this.smallListEdgesSorted);
  }

  // tests the createNodes method for the MazeWorld class
  void testCreateNodes(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    t.checkExpect(this.tinyWorld.board.get(0).get(7), new Node(7, 0));
    t.checkExpect(this.tinyWorld.board.get(9).get(45), new Node(45, 9));
    t.checkExpect(this.tinyWorld.board.get(74).get(99), new Node(99, 74));
  }

  // tests the fix direction methods for the node class
  void testFixAll(Tester t) {
    this.init();
    this.a.fixLeft(this.a);
    this.a.fixBottom(this.d);
    this.a.fixRight(this.b);
    this.a.fixTop(this.a);
    this.d.fixTop(this.a);
    t.checkExpect(this.a.left, this.a);
    t.checkExpect(this.a.bottom, this.d);
    t.checkExpect(this.a.right, this.b);
    t.checkExpect(this.a.top, this.a);
  }

  // tests the fixNeighbors method for the MazeWorld class
  void testFixNeighbors(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    t.checkExpect(this.tinyWorld.board.get(0).get(0).left, this.tinyWorld.board.get(0).get(0));
    t.checkExpect(this.tinyWorld.board.get(0).get(0).right, this.tinyWorld.board.get(0).get(1));
    t.checkExpect(this.tinyWorld.board.get(0).get(0).top, this.tinyWorld.board.get(0).get(0));
    t.checkExpect(this.tinyWorld.board.get(0).get(0).bottom, this.tinyWorld.board.get(1).get(0));
    t.checkExpect(this.tinyWorld.board.get(45).get(45).left, this.tinyWorld.board.get(45).get(44));
    t.checkExpect(this.tinyWorld.board.get(45).get(45).right, this.tinyWorld.board.get(45).get(46));
    t.checkExpect(this.tinyWorld.board.get(45).get(45).top, this.tinyWorld.board.get(44).get(45));
    t.checkExpect(this.tinyWorld.board.get(45).get(45).bottom,
        this.tinyWorld.board.get(46).get(45));
    t.checkExpect(this.tinyWorld.board.get(73).get(99).left, this.tinyWorld.board.get(73).get(98));
    t.checkExpect(this.tinyWorld.board.get(73).get(99).right, this.tinyWorld.board.get(73).get(99));
    t.checkExpect(this.tinyWorld.board.get(73).get(99).bottom,
        this.tinyWorld.board.get(74).get(99));
    t.checkExpect(this.tinyWorld.board.get(73).get(99).top, this.tinyWorld.board.get(72).get(99));
  }

  // tests the createEdges method for the MazeWorld class
  void testCreateEdges(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    t.checkExpect(this.tinyWorld.edgeList.get(0).to, this.tinyWorld.board.get(0).get(1));
    t.checkExpect(this.tinyWorld.edgeList.get(0).from, this.tinyWorld.board.get(0).get(0));
    t.checkExpect(this.tinyWorld.edgeList.get(0).to, this.tinyWorld.board.get(0).get(1));
    t.checkExpect(this.tinyWorld.edgeList.get(198).to, this.tinyWorld.board.get(0).get(99));
  }

  // tests the createMSpanning method for the MazeWorld class
  void testCreateMSpanning(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createMSpanning();
    t.checkExpect(this.tinyWorld.mSpanningTree.size(), 7499);
    t.checkExpect(this.tinyWorld.edgeList.containsAll(this.tinyWorld.mSpanningTree), true);
  }

  // tests the createReps method for the MazeWorld class
  void testCreateReps(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    t.checkExpect(this.tinyWorld.map.get(this.tinyWorld.board.get(0).get(0).location),
        this.tinyWorld.board.get(0).get(0).location);
    t.checkExpect(this.tinyWorld.map.get(this.tinyWorld.board.get(34).get(0).location),
        this.tinyWorld.board.get(34).get(0).location);
    t.checkExpect(this.tinyWorld.map.get(this.tinyWorld.board.get(74).get(74).location),
        this.tinyWorld.board.get(74).get(74).location);
  }

  // tests the find method for the MazeWorld class
  void testFind(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    t.checkExpect(this.tinyWorld.find(this.tinyWorld.board.get(0).get(0).location),
        this.tinyWorld.board.get(0).get(0).location);
  }

  // tests the union method for the MazeWorld class
  void testUnion(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.union(this.tinyWorld.board.get(0).get(0).location,
        this.tinyWorld.board.get(5).get(7).location);
    t.checkExpect(this.tinyWorld.map.get(this.tinyWorld.board.get(0).get(0).location),
        this.tinyWorld.board.get(5).get(7).location);
  }

  // tests the nodeImage method for the Node class
  void testNodeImage(Tester t) {
    this.init();
    t.checkExpect(this.a.nodeImage(), new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE,
        "solid", new Color(192, 192, 192)));
  }

  // tests the verticalLine method for the MazeWorld Class
  void testVerticalLine(Tester t) {
    t.checkExpect(this.tinyWorld.verticalLine(),
        new RectangleImage(1, MazeWorld.CELL_SIZE, "solid", Color.BLACK));
  }

  // tests the horizontalLine method for the MazeWorld class
  void testHorizontalLine(Tester t) {
    t.checkExpect(this.tinyWorld.horizontalLine(),
        new RectangleImage(MazeWorld.CELL_SIZE, 1, "solid", Color.BLACK));
  }

  // tests the distancePosn method for the MazeWorld class
  void testDistancePosn(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createEdges();
    this.tinyWorld.createReps();
    this.tinyWorld.createMSpanning();
    t.checkExpect(this.tinyWorld.distancePosn(this.a, this.b), new Posn(1, 0));
    t.checkExpect(this.tinyWorld.distancePosn(this.b, this.a), new Posn(-1, 0));
    t.checkExpect(this.tinyWorld.distancePosn(this.a, this.d), new Posn(0, 1));
    t.checkExpect(this.tinyWorld.distancePosn(this.d, this.a), new Posn(0, -1));
  }

  // TO-OD this has to have more tests
  void testOnKeyEvent(Tester t) {
    this.init();
    this.tinyWorld.createNodes();
    this.tinyWorld.fixNeighbors();
    this.tinyWorld.createReps();
    this.tinyWorld.onKeyEvent("b");
    t.checkExpect(this.tinyWorld.bfs, true);
    this.tinyWorld.onKeyEvent("d");
    t.checkExpect(this.tinyWorld.dfs, true);
    // rest is random based on illegal moves

  }

  // tests the game
  // Uncomment to run game
  void testGame(Tester t) {
    MazeWorld game = new MazeWorld();
    game.bigBang(1000, 750, .05);
  }
}
