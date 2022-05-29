package de.fh.stud.Suchen;

import de.fh.stud.Suchen.Suchfunktionen.Heuristikfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zielfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Suchszenario {
    private final IAccessibilityChecker[] accessChecks;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction[] heuristicFuncs;
    private final ICallbackFunction[] callbackFuncs;

    private final boolean stateProblem;

    public static final class SuchszenarioBuilder {
        private IAccessibilityChecker[] accessChecks;
        private IGoalPredicate goalPred;
        private IHeuristicFunction[] heuristicFuncs;
        private boolean stateProblem = true;
        private ICallbackFunction[] callbackFuncs;

        private SuchszenarioBuilder setAccessChecks(IAccessibilityChecker... accessChecks) {
            this.accessChecks = accessChecks;
            return this;
        }

        private SuchszenarioBuilder setGoalPred(IGoalPredicate goalPred) {
            this.goalPred = goalPred;
            return this;
        }

        private SuchszenarioBuilder setHeuristicFuncs(IHeuristicFunction... heuristicFuncs) {
            this.heuristicFuncs = heuristicFuncs;
            return this;
        }

        private SuchszenarioBuilder setStateProblem(boolean stateProblem) {
            this.stateProblem = stateProblem;
            return this;
        }

        private SuchszenarioBuilder setCallbackFuncs(ICallbackFunction... callbackFuncs) {
            this.callbackFuncs = callbackFuncs;
            return this;
        }

        public Suchszenario build() {
            if (accessChecks == null || accessChecks.length == 0) {
                throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
            }
            return new Suchszenario(this);
        }
    }

    private Suchszenario(SuchszenarioBuilder b) {
        this.stateProblem = b.stateProblem;
        this.accessChecks = b.accessChecks;
        this.goalPred = b.goalPred;
        this.heuristicFuncs = b.heuristicFuncs;
        this.callbackFuncs = b.callbackFuncs;
    }

    public static Suchszenario eatAllDots() {
        return new SuchszenarioBuilder()
                .setAccessChecks(Zugangsfilter.noWall())
                .setGoalPred(Zielfunktionen.allDotsEaten())
                .setHeuristicFuncs(Heuristikfunktionen.remainingDots())
                .build();
    }

    public static Suchszenario findDestination(int goalX, int goalY) {
        return new SuchszenarioBuilder()
                .setStateProblem(false)
                .setAccessChecks(Zugangsfilter.safeToWalkOn())
                .setGoalPred(Zielfunktionen.reachedDestination(goalX, goalY))
                .setHeuristicFuncs(Heuristikfunktionen.manhattanToTarget(goalX, goalY))
                .build();
    }

    public static Suchszenario locateDeadEndExit() {
        return new SuchszenarioBuilder()
                .setStateProblem(false)
                .setAccessChecks(Zugangsfilter.noWall())
                .setGoalPred(node -> Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] == 0)
                .build();
    }

    // region getter
    public IAccessibilityChecker[] getAccessChecks() {
        return accessChecks;
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
        return stateProblem;
    }
    // endregion

}
