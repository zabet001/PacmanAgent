package de.fh.stud.Suchen.Suchkomponenten;

public class PathClosedList extends ClosedList {
    int size;
    boolean[][] closedList;

    public PathClosedList(byte[][] world) {
        closedList = new boolean[world.length][world[0].length];
    }

    @Override
    public boolean contains(Knoten node) {
        return closedList[node.getPosX()][node.getPosY()];
    }

    @Override
    public void add(Knoten expCand) {
        closedList[expCand.getPosX()][expCand.getPosY()] = true;
        size++;
    }

    @Override
    public int size() {
        return size;
    }
}
