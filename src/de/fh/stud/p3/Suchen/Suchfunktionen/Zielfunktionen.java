package de.fh.stud.p3.Suchen.Suchfunktionen;

import interfaces.GoalPredicate;

public class Zielfunktionen {
    public static GoalPredicate allDotsEaten() {
        return node -> node.getHeuristic() == 0;
    }

    //region Zielzustandsfunktionen
    public static GoalPredicate reachedDestination(int goalx, int goaly) {
        return node -> node.getPosX() == goalx && node.getPosY() == goaly;
    }
}
