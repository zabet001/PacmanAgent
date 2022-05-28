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

    private final boolean isStateProblem;

    public static final class SuchszenarioBuilder {
        private IAccessibilityChecker[] accessChecks;
        private IGoalPredicate goalPred;
        private IHeuristicFunction[] heuristicFuncs;
        private boolean isStateProblem = true;
        private ICallbackFunction[] callbackFuncs;

        public SuchszenarioBuilder setAccessChecks(IAccessibilityChecker... accessChecks) {
            this.accessChecks = accessChecks;
            return this;
        }

        public SuchszenarioBuilder setGoalPred(IGoalPredicate goalPred) {
            this.goalPred = goalPred;
            return this;
        }

        public SuchszenarioBuilder setHeuristicFuncs(IHeuristicFunction... heuristicFuncs) {
            this.heuristicFuncs = heuristicFuncs;
            return this;
        }

        public SuchszenarioBuilder setIsStateProblem(boolean isStateProblem) {
            this.isStateProblem = isStateProblem;
            return this;
        }

        public SuchszenarioBuilder setCallbackFuncs(ICallbackFunction... callbackFuncs) {
            this.callbackFuncs = callbackFuncs;
            return this;
        }

        public Suchszenario createSuchszenario() {
            if (accessChecks == null || accessChecks.length == 0) {
                throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
            }
            return new Suchszenario(this);
        }
    }

    public Suchszenario(SuchszenarioBuilder b) {
        this.isStateProblem = b.isStateProblem;
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
                .createSuchszenario();
    }

    public static Suchszenario findDestination(int goalX, int goalY) {
        return new SuchszenarioBuilder()
                .setIsStateProblem(false)
                .setAccessChecks(Zugangsfilter.safeToWalkOn())
                .setGoalPred(Zielfunktionen.reachedDestination(goalX, goalY))
                .setHeuristicFuncs(Heuristikfunktionen.manhattanToTarget(goalX, goalY))
                .createSuchszenario();
    }

    public static Suchszenario locateDeadEndExit() {
        return new SuchszenarioBuilder()
                .setIsStateProblem(false)
                .setAccessChecks(Zugangsfilter.noWall())
                .setGoalPred(node -> Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] == 0)
                .createSuchszenario();
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
        return isStateProblem;
    }
    // endregion

}
