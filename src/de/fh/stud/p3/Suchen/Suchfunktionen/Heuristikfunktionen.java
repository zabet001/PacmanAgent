package de.fh.stud.p3.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.p2.Knoten;
import interfaces.HeuristicFunction;

public class Heuristikfunktionen {
    //region Heuristikfunktionen
    // TODO: In Suche lagern (mit innerer Klasse?)
    public static HeuristicFunction remainingDots() {
        return node -> {
            int ret;
            if (node.getPred() == null)
                return node.countDots();
            else if (node.getPred().getView()[node.getPosX()][node.getPosY()] == Knoten.tileToByte(PacmanTileType.DOT)
                    || node.getPred().getView()[node.getPosX()][node.getPosY()] == Knoten.tileToByte(PacmanTileType.GHOST_AND_DOT))
                return node.getPred().getHeuristic() - 1;
            else
                return node.getPred().getHeuristic();
        };
    }

    public static HeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }
}
