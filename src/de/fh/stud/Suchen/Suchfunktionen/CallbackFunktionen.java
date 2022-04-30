package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.interfaces.ICallbackFunction;

import java.util.List;

public class CallbackFunktionen {
    public static ICallbackFunction saveStepCost(byte[][] costMap) {
        return expCand -> costMap[expCand.getPosX()][expCand.getPosY()] = (byte) (expCand.getCost() + 1);
    }

    public static ICallbackFunction saveVisitedPos(boolean[][] visitedMap) {
        return expCand -> visitedMap[expCand.getPosX()][expCand.getPosY()] = true;
    }

    public static ICallbackFunction saveVisitedPos(List<Vector2> visitedList) {
        return expCand -> visitedList.add(expCand.getPosition());
    }

    public static ICallbackFunction saveVisitedType(List<PacmanTileType> visitedTypesList) {
        return expCand -> visitedTypesList.add(Knoten.getStaticWorld()[expCand.getPosX()][expCand.getPosY()]);
    }

    public static ICallbackFunction saveVisitedPos(List<Vector2> visitedList, boolean duplicates) {
        return expCand -> {
            if (duplicates || !visitedList.contains(expCand.getPosition()))
                saveVisitedPos(visitedList).callback(expCand);
        };
    }

    public static ICallbackFunction saveVisited(List<Vector2> visitedList, List<PacmanTileType> visitedTypesList,
                                                   boolean duplicates) {
        return expCand -> {
            if (duplicates || !visitedList.contains(expCand.getPosition())) {
                saveVisitedPos(visitedList).callback(expCand);
                saveVisitedType(visitedTypesList).callback(expCand);
            }
        };
    }

    public static ICallbackFunction setVisitedType(PacmanTileType newType) {
        return expCand -> Knoten.getStaticWorld()[expCand.getPosX()][expCand.getPosY()] = newType;
    }

    public static ICallbackFunction printNodePositions() {
        return expCand -> System.out.printf("Position: %d|%d\n", expCand.getPosX(), expCand.getPosY());
    }
}
