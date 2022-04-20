package de.fh.stud.p3.Suchen.Suchfunktionen;

import interfaces.AccessibilityChecker;
import interfaces.GoalPredicate;
import interfaces.HeuristicFunction;

public class Suchszenario {
    private AccessibilityChecker accessCheck;
    private GoalPredicate goalPred;
    private HeuristicFunction heuristicFunc;

    public Suchszenario(AccessibilityChecker accessCheck, GoalPredicate goalPred, HeuristicFunction heuristicFunc) {
        this.accessCheck = accessCheck;
        this.goalPred = goalPred;
        this.heuristicFunc = heuristicFunc;
    }

}
