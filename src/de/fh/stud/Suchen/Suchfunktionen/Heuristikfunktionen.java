package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Heuristikfunktionen {
    public static IHeuristicFunction remainingDots() {
        /*            float ret = node.getRemainingDots();
            ret += Felddistanzen.distanceToNearestDot(node);*/
        return Knoten::getRemainingDots;
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }
}
