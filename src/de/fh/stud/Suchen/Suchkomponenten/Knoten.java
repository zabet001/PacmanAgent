package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
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
    private static final short COST_LIMIT = 1000;
    private final Knoten pred;
    private final byte[][] view;
    private byte posX, posY;
    private final short cost;
    public byte nextTargetX, nextTargetY;

    private final short remainingDots;
    // TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichern

    public static Knoten generateRoot(byte[][] world, int posX, int posY) {
        return new Knoten(world, (byte) posX, (byte) posY);
    }

    private Knoten(byte[][] initialWorld, byte posX, byte posY) {
        this(null, initialWorld, posX, posY, false);
    }

    private Knoten(Knoten pred, byte[][] initialWorld, byte posX, byte posY, boolean isStateSearch) {
        this.pred = pred;
        this.posX = posX;
        this.posY = posY;

        if (pred == null) {
            // Wurzelknoten
            this.view = initialWorld;
            this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
            this.remainingDots = countDots();
            this.cost = 0;
            this.nextTargetX = posX;
            this.nextTargetY = posY;
        }
        else {
            this.nextTargetX = pred.nextTargetX;
            this.nextTargetY = pred.nextTargetY;
            // Kindknoten
            if (!isStateSearch || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.EMPTY)) {
                this.view = pred.view;
            }
            else {
                this.view = MyUtil.copyView(pred.view);
                this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
            }

            if (isStateSearch && MyUtil.byteToTile(pred.view[posX][posY]) == PacmanTileType.DOT) {
                this.remainingDots = (short) (pred.remainingDots - 1);
            }
            else {
                this.remainingDots = pred.remainingDots;
            }
            this.cost = (short) (pred.cost + 1);
        }
    }

    // region Klassenmethoden
    public static int nodeNeighbourCnt(Knoten node, IAccessibilityChecker[] accessChecks) {
        int neighbourCnt = 0;
        for (byte[] neighbour : NEIGHBOUR_POS) {
            if (node.isPassable((byte) (node.getPosX() + neighbour[0]), (byte) (node.getPosY() + neighbour[1]),
                                accessChecks)) {
                neighbourCnt++;
            }
        }
        return neighbourCnt;
    }
    // endregion

    public boolean isPassable(byte newPosX, byte newPosY, IAccessibilityChecker[] accessChecks) {
        for (IAccessibilityChecker accessCheck : accessChecks) {
            if (!accessCheck.isAccessible(this, newPosX, newPosY)) {
                return false;
            }
        }
        return true;
    }

    public List<Knoten> expand(IAccessibilityChecker[] accessChecks, boolean isStateSearch) {
        // Macht es einen Unterschied, wenn NEIGHBOUR_POS pro expand aufruf neu erzeugt wird? Ja
        List<Knoten> children = new LinkedList<>();

        for (byte[] neighbour : NEIGHBOUR_POS) {
            if (cost < COST_LIMIT && isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]),
                                                accessChecks)) {
                children.add(new Knoten(this, null, (byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]),
                                        isStateSearch));
            }
        }
        // children.add(new Knoten(this, posX, posY));

        return children;
    }

    public boolean isGoalNode(IGoalPredicate goalPredicate) {
        return goalPredicate.isGoalNode(this);
    }

    public void executeCallbacks(ICallbackFunction[] callbackFunctions) {
        for (ICallbackFunction callbacks : callbackFunctions) {
            callbacks.callback(this);
        }
    }

    public PacmanAction previousAction() {
        if (pred == null) {
            return PacmanAction.WAIT;
        }
        else {
            if (posX > pred.posX) {
                return PacmanAction.GO_EAST;
            }
            else if (posX < pred.posX) {
                return PacmanAction.GO_WEST;
            }
            else if (posY > pred.posY) {
                return PacmanAction.GO_SOUTH;
            }
            else if (posY < pred.posY) {
                return PacmanAction.GO_NORTH;
            }
            else {
                return PacmanAction.WAIT;
            }
        }
    }

    public List<PacmanAction> identifyActionSequence() {
        List<PacmanAction> ret = new LinkedList<>();
        Knoten it = this;
        while (it.pred != null) {
            ret.add(0, it.previousAction());
            it = it.pred;
        }
        if (ret.size() == 0) {
            ret.add(PacmanAction.WAIT);
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Knoten knoten = (Knoten) o;
        return remainingDots == knoten.remainingDots && posX == knoten.posX && posY == knoten.posY && Arrays.deepEquals(
                view, knoten.view);

    }

    @Override
    public int hashCode() {
        int result = Objects.hash(posX, posY);
        result = 31 * result + Arrays.deepHashCode(view);
        return result;
    }

    // region Setup
    public short countDots() {
        short cnt = 0;
        for (byte[] rowVals : view) {
            for (int col = 0; col < view[0].length; col++) {
                if (rowVals[col] == MyUtil.tileToByte(PacmanTileType.DOT) || rowVals[col] == MyUtil.tileToByte(
                        PacmanTileType.GHOST_AND_DOT)) {
                    cnt++;
                }
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

    public short getCost() {
        return cost;
    }

    public short getRemainingDots() {
        return remainingDots;
    }

    public float heuristicalValue(IHeuristicFunction[] heuristicFunctions, int functionNr) {
        return heuristicFunctions[functionNr].calcHeuristic(this);
    }

    // endregion
}
