package de.fh.stud.Suchen;

import de.fh.stud.Suchen.Suchfunktionen.Heuristikfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zielfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Suchszenarien {
    private final IAccessibilityChecker accessCheck;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction heuristicFunc;
    private final ICallbackFunction[] callbackFuncs;

    private final boolean isStateSearch;

    public Suchszenarien(IAccessibilityChecker accessCheck, IGoalPredicate goalPred, IHeuristicFunction heuristicFunc
            , ICallbackFunction... callbackFuncs) {
        this(true, accessCheck, goalPred, heuristicFunc, callbackFuncs);
    }

    public Suchszenarien(boolean isStateSearch, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                         IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFuncs) {
        this.isStateSearch = isStateSearch;
        this.accessCheck = accessCheck;
        this.goalPred = goalPred;
        this.heuristicFunc = heuristicFunc;
        this.callbackFuncs = callbackFuncs;
    }

    public static Suchszenarien eatAllDots(ICallbackFunction... callbackFuncs) {
        return new Suchszenarien(true, Zugangsfilter.safeToWalkOn(true), Zielfunktionen.allDotsEaten(),
                Heuristikfunktionen.remainingDots(), callbackFuncs);
    }

    public static Suchszenarien findDestination(int goalX, int goalY, ICallbackFunction... callbackFuncs) {
        return new Suchszenarien(false, Zugangsfilter.safeToWalkOn(true), Zielfunktionen.reachedDestination(goalX,
                goalY), Heuristikfunktionen.manhattanToTarget(goalX, goalY), callbackFuncs);
    }

    public static Suchszenarien locateDeadEndExit(ICallbackFunction... callbackFuncs) {
        return new Suchszenarien(false, Zugangsfilter.noWall(), Zielfunktionen.minimumNeighbours(3), null,
                callbackFuncs);
    }

    // region getter
    public IAccessibilityChecker getAccessCheck() {
        return accessCheck;
    }

    public IGoalPredicate getGoalPred() {
        return goalPred;
    }

    public IHeuristicFunction getHeuristicFunc() {
        return heuristicFunc;
    }

    public ICallbackFunction[] getCallbackFuncs() {
        return callbackFuncs;
    }

    public boolean isStateSearch() {
        return isStateSearch;
    }
    // endregion
}
