package de.fh.stud;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Suchen.Suchszenarien;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Knoten {

    public static final byte[][] NEIGHBOUR_POS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
    public static boolean IS_STATE_NODE = true;
    private static PacmanTileType[][] STATIC_WORLD;


    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen
    private static IAccessibilityChecker ACCESS_CHECK;
    private static IGoalPredicate GOAL_PRED;
    private static IHeuristicFunction HEURISTIC_FUNC;
    private static ICallbackFunction[] CALLBACK_FUNCS;

    private final Knoten pred;
    private byte[][] view;
    private byte posX, posY;
    private final short cost;

    private static final short COST_LIMIT = 1000;
    private final int heuristic;
    // TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichern
    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY, Suchszenarien searchScenario) {
        return Knoten.generateRoot(world, posX, posY, searchScenario.isStateSearch(), searchScenario.getAccessCheck()
                , searchScenario.getGoalPred(), searchScenario.getHeuristicFunc(), searchScenario.getCallbackFuncs());
    }

    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY, boolean isStateNode,
                                      IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                                      IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFuncs) {
        Knoten.STATIC_WORLD = world;
        Knoten.IS_STATE_NODE = isStateNode;
        Knoten.ACCESS_CHECK = accessCheck;
        Knoten.GOAL_PRED = goalPred != null ? goalPred : node -> false;
        Knoten.HEURISTIC_FUNC = heuristicFunc != null ? heuristicFunc : node -> 0;
        Knoten.CALLBACK_FUNCS = callbackFuncs != null ? callbackFuncs : new ICallbackFunction[]{expCand -> {}};

        return new Knoten((byte) posX, (byte) posY);
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
            this.view = MyUtil.createByteView(STATIC_WORLD);
            this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
        } else {
            // Kindknoten
            if (!IS_STATE_NODE || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.EMPTY)) {
                this.view = pred.view;
            } else {
                this.view = MyUtil.copyView(pred.view);
                this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
            }
        }
        this.cost = pred == null ? 0 : (short) (pred.cost + 1);
        this.heuristic = HEURISTIC_FUNC.calcHeuristic(this);
    }

    // region Klassenmethoden

    public boolean isPassable(byte newPosX, byte newPosY) {
        return ACCESS_CHECK.isAccessible(this, newPosX, newPosY);
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
        return GOAL_PRED.isGoalNode(this);
    }

    public void executeCallbacks() {
        for (ICallbackFunction callbacks : Knoten.CALLBACK_FUNCS) {
            callbacks.callback(this);
        }
    }

    public PacmanAction previousAction() {
        if (pred == null)
            return PacmanAction.WAIT;
        else {
            if (posX > pred.posX)
                return PacmanAction.GO_EAST;
            else if (posX < pred.posX)
                return PacmanAction.GO_WEST;
            else if (posY > pred.posY)
                return PacmanAction.GO_SOUTH;
            else if (posY < pred.posY)
                return PacmanAction.GO_NORTH;
            else
                return PacmanAction.WAIT;
        }
    }

    public List<PacmanAction> identifyActionSequence() {
        List<PacmanAction> ret = new LinkedList<>();
        Knoten it = this;
        while (it.pred != null) {
            ret.add(0, it.previousAction());
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

    public int countDots() {
        int cnt = 0;
        for (byte[] rowVals : view) {
            for (int col = 0; col < view[0].length; col++) {
                if (rowVals[col] == MyUtil.tileToByte(PacmanTileType.DOT) || rowVals[col] == MyUtil.tileToByte(PacmanTileType.GHOST_AND_DOT))
                    cnt++;
            }
        }
        return cnt;
    }
    // endregion
    // region Debug

    // endregion
    // region Getter und Setter

    public byte[][] getView() {
        return view;
    }
    public Knoten getPred() {
        return pred;
    }

    public Vector2 getPosition() {
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

    public static PacmanTileType[][] getStaticWorld() {
        return STATIC_WORLD;
    }

    // endregion
}
