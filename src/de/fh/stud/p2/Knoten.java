package de.fh.stud.p2;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.p3.Suchen.Suchfunktionen.Suchszenario;
import interfaces.AccessibilityChecker;
import interfaces.GoalPredicate;
import interfaces.HeuristicFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Knoten {

    private static final byte[][] NEIGHBOUR_POS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
    public static boolean IS_STATE_NODE = true;
    private static PacmanTileType[][] WORLD;

    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen
    private static HeuristicFunction HEURISTIC_FUNC;
    private static GoalPredicate GOAL_PRED;
    private static AccessibilityChecker ACCESS_CHECK;

    private final Knoten pred;
    private byte[][] view;
    private byte posX, posY;

    private final short cost;
    private static final short COST_LIMIT = 1000;
    private final int heuristic;
    // TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichern

    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY,boolean isStateNode, AccessibilityChecker accessCheck,
                                      GoalPredicate goalPred, HeuristicFunction heuristicFunc) {
        Knoten.WORLD = world;
        Knoten.IS_STATE_NODE = isStateNode;
        Knoten.ACCESS_CHECK = accessCheck;
        Knoten.GOAL_PRED = goalPred;
        Knoten.HEURISTIC_FUNC = heuristicFunc;
        return new Knoten((byte) posX, (byte) posY);
    }

    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY, Suchszenario searchScenario) {
        return Knoten.generateRoot(world, posX, posY,
                searchScenario.isStateSearch(),
                searchScenario.getAccessCheck(),
                searchScenario.getGoalPred(),
                searchScenario.getHeuristicFunc());
    }

    private Knoten(byte posX, byte posY) {
        this(null, posX, posY);
    }

    private Knoten(Knoten pred, byte posX, byte posY) {
        this.pred = pred;
        this.posX = posX;
        this.posY = posY;

        if (pred == null) {
            // Wurzelknoten
            this.view = createView();
            this.view[posX][posY] = tileToByte(PacmanTileType.EMPTY);
        } else {
            // Kindknoten
            if (!IS_STATE_NODE || pred.view[posX][posY] == tileToByte(PacmanTileType.EMPTY)) {
                this.view = pred.view;
            } else {
                this.view = copyView(pred.view);
                this.view[posX][posY] = tileToByte(PacmanTileType.EMPTY);
            }
        }
        this.cost = pred == null ? 0 : (short) (pred.cost + 1);
        this.heuristic = HEURISTIC_FUNC != null ? HEURISTIC_FUNC.calcHeuristic(this) : -1;
    }

    // region Klassenmethoden
    private static byte[][] copyView(byte[][] orig) {
        byte[][] ret = new byte[orig.length][];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOf(orig[i], orig[i].length);
        }
        return ret;
    }

    private boolean isPassable(byte newPosX, byte newPosY) {
        return ACCESS_CHECK.isAccecssible(this, newPosX, newPosY);
    }

    public static byte tileToByte(PacmanTileType tile) {
        return (byte) tile.ordinal();
    }

    public static PacmanTileType byteToTile(byte b) {
        return b < PacmanTileType.values().length ? PacmanTileType.values()[b] : null;
    }
    // endregion

    public List<Knoten> expand() {
        // Macht es einen Unterschied, wenn NEIGHBOUR_POS pro expand aufruf neu erzeugt wird? Ja
        List<Knoten> children = new LinkedList<>();

        for (byte[] neighbour : NEIGHBOUR_POS) {
            if (cost < COST_LIMIT && isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]))) {
                children.add(new Knoten(this, (byte) (posX + neighbour[0]), (byte) (posY + neighbour[1])));
            }
        }
        return children;
    }

    public boolean isGoalNode() {
        return GOAL_PRED != null && GOAL_PRED.isGoalNode(this);
    }

    public List<PacmanAction> identifyActionSequence() {
        List<PacmanAction> ret = new LinkedList<>();
        Knoten it = this;
        while (it.pred != null) {
            if (it.posX > it.pred.posX)
                ret.add(0, PacmanAction.GO_EAST);
            else if (it.posX < it.pred.posX)
                ret.add(0, PacmanAction.GO_WEST);
            else if (it.posY > it.pred.posY)
                ret.add(0, PacmanAction.GO_SOUTH);
            else if (it.posY < it.pred.posY)
                ret.add(0, PacmanAction.GO_NORTH);
            else
                ret.add(0, PacmanAction.WAIT);
            it = it.pred;
        }
        if (ret.size() == 0)
            ret.add(PacmanAction.WAIT);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Knoten knoten = (Knoten) o;
        return heuristic == knoten.heuristic && posX == knoten.posX && posY == knoten.posY && Arrays.deepEquals(view,
                knoten.view);

    }

    @Override
    public int hashCode() {
        int result = Objects.hash(posX, posY);
        result = 31 * result + Arrays.deepHashCode(view);
        return result;
    }

    // region Setup
    // TODO: ? Wie macht man beides in einem Schritt und trennt dennoch zwischen Aufgaben ?
    private byte[][] createView() {
        view = new byte[WORLD.length][WORLD[0].length];
        for (int i = 0; i < WORLD.length; i++) {
            for (int j = 0; j < WORLD[i].length; j++) {
                view[i][j] = tileToByte(WORLD[i][j]);
            }
        }
        return view;
    }

    public int countDots() {
        int cnt = 0;
        for (byte[] rowVals : view) {
            for (int col = 0; col < view[0].length; col++) {
                if (rowVals[col] == tileToByte(PacmanTileType.DOT) || rowVals[col] == tileToByte(PacmanTileType.GHOST_AND_DOT))
                    cnt++;
            }
        }
        return cnt;
    }
    // endregion

    // region Debug
    public static PacmanTileType[][] reformatToTileType(byte[][] view) {
        PacmanTileType[][] ret = new PacmanTileType[view.length][view[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                ret[i][j] = byteToTile(view[i][j]);
            }
        }
        return ret;
    }
    // endregion

    // region Getter und Setter
    public byte[][] getView() {
        return view;
    }

    public Knoten getPred() {
        return pred;
    }

    public Vector2 getPos() {
        return new Vector2(posX, posY);
    }

    public byte getPosX() {
        return posX;
    }

    public byte getPosY() {
        return posY;
    }

    public void setPos(Vector2 pos) {
        posX = (byte) pos.x;
        posY = (byte) pos.y;
    }

    public int getCost() {
        return cost;
    }

    public int getHeuristic() {
        return heuristic;
    }
    // endregion

}
