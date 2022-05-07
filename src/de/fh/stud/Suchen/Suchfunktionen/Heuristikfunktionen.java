package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Heuristikfunktionen {

    public static IHeuristicFunction[] toArray(IHeuristicFunction... functions){
        return functions;
    }

    public static IHeuristicFunction remainingDots() {
        return Knoten::getRemainingDots;
    }

    public static IHeuristicFunction dotNearby() {
        // TODO: Gucken, ob in nachbarfelder Dots sind
        return null;
    }

    // TODO: Heuristik optimieren, sodass nur Distanz gesucht wird, wenn gefundener Dot gefressen wurde
    public static IHeuristicFunction nearestDot() {
        return node ->
                Felddistanzen.distanceToNearestDot(node.getView(), node.getPosX(), node.getPosY());
    }

    public static IHeuristicFunction remainingAndNearestDot() {
        return node -> (float) node.getRemainingDots() + (Heuristikfunktionen.nearestDot().calcHeuristic(node) / Felddistanzen.getMaxDistance());
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }
}
