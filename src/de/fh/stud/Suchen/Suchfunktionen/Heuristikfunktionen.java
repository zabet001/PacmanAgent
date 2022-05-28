package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Heuristikfunktionen {

    public static IHeuristicFunction[] toArray(IHeuristicFunction... functions) {
        return functions;
    }

    public static IHeuristicFunction remainingDots() {
        return Knoten::getRemainingDots;
    }

    public static IHeuristicFunction dotNearby() {
        return node -> {
            for (byte[] neighbourOffset : Knoten.NEIGHBOUR_POS) {
                if (MyUtil.byteToTile(
                        node.getView()[node.getPosX() + neighbourOffset[0]][node.getPosY() + neighbourOffset[1]])
                        == PacmanTileType.DOT) {
                    return 0;
                }
            }
            return 1;
        };
    }

    public static IHeuristicFunction nearestDotDist() {
/*
        // Ohne Optimierung:  Jeder einzelne Suchlauf
        return node -> Felddistanzen.distanceToNearestDot(node.getView(), node.getPosX(), node.getPosY());
*/
        // Optimierung: Benoetigt Attribut target in Knoten, um zwischenzuspeichern, ob der gesuchte Dot erreicht wurde
        return node -> {
            if (node.getPosX() == node.nextTargetX && node.getPosY() == node.nextTargetY) {
                Knoten nearestDot = MyUtil.nearestDot(node.getView(), node.getPosX(), node.getPosY());
                if (nearestDot == null) {
                    return 0;
                }
                node.nextTargetX = nearestDot.getPosX();
                node.nextTargetY = nearestDot.getPosY();
                return nearestDot.getCost();
            }
            return Felddistanzen.getDistance(node.getPosX(), node.getPosY(), node.nextTargetX, node.nextTargetY);
        };
    }

    public static IHeuristicFunction remainingAndNearestDot() {
        return node -> (float) node.getRemainingDots() + (Heuristikfunktionen
                .nearestDotDist()
                .calcHeuristic(node) / Felddistanzen.getMaxDistance());
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }
}
