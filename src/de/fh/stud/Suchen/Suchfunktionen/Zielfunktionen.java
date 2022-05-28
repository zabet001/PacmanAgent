package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.IGoalPredicate;

public class Zielfunktionen {

    public static IGoalPredicate dotEaten() {
        return node -> node.getPred() != null && MyUtil.byteToTile(node
                                                                           .getPred()
                                                                           .getView()[node.getPosX()][node.getPosY()])
                == PacmanTileType.DOT;
    }

    public static IGoalPredicate allDotsEaten() {
        return node -> node.getRemainingDots() == 0;
    }

    //region Zielzustandsfunktionen
    public static IGoalPredicate reachedDestination(int goalx, int goaly) {
        return node -> node.getPosX() == goalx && node.getPosY() == goaly;
    }

    public static IGoalPredicate minimumNeighbours(int numberOfNeighbours, IAccessibilityChecker... accessChecks) {
        return node -> Knoten.nodeNeighbourCnt(node, accessChecks) >= numberOfNeighbours;
    }

}
