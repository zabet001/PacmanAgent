package de.fh.stud.Suchen;

import de.fh.stud.Suchen.Suchfunktionen.Heuristikfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zielfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Suchszenario {
    private final IAccessibilityChecker accessCheck;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction[] heuristicFuncs;
    private final ICallbackFunction[] callbackFuncs;

    private final boolean isStateProblem;

    public Suchszenario(IAccessibilityChecker accessCheck, IGoalPredicate goalPred,IHeuristicFunction... heuristicFuncs) {
        this(true, accessCheck, goalPred, heuristicFuncs, null);
    }

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                        IHeuristicFunction... heuristicFuncs) {
        this(isStateProblem, accessCheck, goalPred, heuristicFuncs, null);
    }

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                        IHeuristicFunction[] heuristicFuncs, ICallbackFunction[] callbackFuncs) {
        this.isStateProblem = isStateProblem;
        this.accessCheck = accessCheck;
        this.goalPred = goalPred;
        this.heuristicFuncs = heuristicFuncs;

        this.callbackFuncs = callbackFuncs;
    }

    public static Suchszenario eatAllDots() {
        // TODO: Heuristikkombinationen austesten
        return new Suchszenario(Zugangsfilter.noWall(), Zielfunktionen.allDotsEaten(),Heuristikfunktionen.remainingDots(),Heuristikfunktionen.dotNearby());
    }

    public static Suchszenario findDestination(int goalX, int goalY) {
        return new Suchszenario(false, Zugangsfilter.safeToWalkOn(), Zielfunktionen.reachedDestination(goalX,
                goalY), Heuristikfunktionen.manhattanToTarget(goalX, goalY));
    }

    public static Suchszenario locateDeadEndExit(byte[][] markedAsOneWays) {
        return new Suchszenario(false, Zugangsfilter.merge(Zugangsfilter.noWall(),
                (node, newPosX, newPosY) -> markedAsOneWays[newPosX][newPosY] == 0),
                Zielfunktionen.minimumNeighbours(2), (IHeuristicFunction[]) null);
    }

    // region getter
    public IAccessibilityChecker getAccessCheck() {
        return accessCheck;
    }

    public IGoalPredicate getGoalPred() {
        return goalPred;
    }

    public IHeuristicFunction[] getHeuristicFuncs() {
        return heuristicFuncs;
    }

    public ICallbackFunction[] getCallbackFuncs() {
        return callbackFuncs;
    }

    public boolean isStateProblem() {
        return isStateProblem;
    }
    // endregion
}
