package de.fh.stud.p3.Suchen.Suchfunktionen;

import interfaces.AccessibilityChecker;
import interfaces.GoalPredicate;
import interfaces.HeuristicFunction;

public class Suchszenario {
    private AccessibilityChecker accessCheck;
    private GoalPredicate goalPred;
    private HeuristicFunction heuristicFunc;

    private boolean isStateSearch;

    public Suchszenario(AccessibilityChecker accessCheck, GoalPredicate goalPred, HeuristicFunction heuristicFunc) {
        this(true, accessCheck, goalPred, heuristicFunc);
    }

    public Suchszenario(boolean isStateSearch, AccessibilityChecker accessCheck, GoalPredicate goalPred,
                        HeuristicFunction heuristicFunc) {
        this.isStateSearch = isStateSearch;
        this.accessCheck = accessCheck;
        this.goalPred = goalPred;
        this.heuristicFunc = heuristicFunc;
    }

    public static Suchszenario eatAllDots() {
        return new Suchszenario(true,
                Zugangsfilter.safeToWalkOn(true),
                Zielfunktionen.allDotsEaten(),
                Heuristikfunktionen.remainingDots());
    }

    public static Suchszenario findDestination(int goalX, int goalY) {
        return new Suchszenario(false,
                Zugangsfilter.safeToWalkOn(true),
                Zielfunktionen.reachedDestination(goalX,goalY),
                Heuristikfunktionen.manhattanToTarget(goalX,goalY));
    }


    public AccessibilityChecker getAccessCheck() {
        return accessCheck;
    }

    public GoalPredicate getGoalPred() {
        return goalPred;
    }

    public HeuristicFunction getHeuristicFunc() {
        return heuristicFunc;
    }

    public boolean isStateSearch() {
        return isStateSearch;
    }
}
