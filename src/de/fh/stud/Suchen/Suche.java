package de.fh.stud.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.ClosedList;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.Suchen.Suchkomponenten.OpenList;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

import javax.swing.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Suche {
    public static final int MAX_SOLUTION_LIMIT = Integer.MAX_VALUE;
    public static boolean searchRunning = false;
    public static List<Double> runTimes = new LinkedList<>();

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    private final boolean displayResults;

    private final boolean printResults;

    private final boolean stateSearch;
    private final IAccessibilityChecker[] accessChecks;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction[] heuristicFuncs;

    private final ICallbackFunction[] callbackFuncs;

    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen

    public Suche(Builder b) {
        if (Suche.searchRunning) {
            System.err.println("WARNUNG: Eine Suche laeuft bereits!");
        }
        else {
            Suche.searchRunning = true;
        }
        this.displayResults = b.displayResults;
        this.printResults = b.printResults;

        this.stateSearch = b.stateSearch;
        this.accessChecks = b.accessChecks;
        this.goalPred = b.goalPred;
        this.heuristicFuncs = b.heuristicFuncs;
        this.callbackFuncs = b.callbackFuncs;
    }

    // TODO: Wie kriegt man diese Redundanz raus? (orimitive datentypen koennen nicht in generics verwendet werden)
    public Knoten start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy) {
        List<Knoten> ret = start(world, posX, posY, strategy, 1);
        return ret.size() > 0 ? ret.get(0) : null;
    }

    public Knoten start(byte[][] world, int posX, int posY, SearchStrategy strategy) {
        List<Knoten> ret = start(world, posX, posY, strategy, 1);
        return ret.size() > 0 ? ret.get(0) : null;
    }

    public List<Knoten> start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy,
                              int solutionLimit) {
        return start(MyUtil.createByteView(world), posX, posY, strategy, solutionLimit);
    }

    public List<Knoten> start(byte[][] world, int posX, int posY, SearchStrategy strategy, int solutionLimit) {
        Knoten rootNode = Knoten.generateRoot(world, posX, posY);

        long startTime = System.nanoTime();
        SimpleEntry<List<Knoten>, Map<String, Double>> searchResult = beginSearch(rootNode,
                                                                                  OpenList.buildOpenList(strategy,
                                                                                                         getHeuristicFuncs()),
                                                                                  ClosedList.buildClosedList(
                                                                                          isStateSearch(), world),
                                                                                  solutionLimit, startTime);
        if (printResults) {
            System.out.println(debugInfos(strategy, searchResult));
        }
        if (displayResults) {
            JFrame jf = new JFrame();
            jf.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(jf, debugInfos(strategy, searchResult));

        }

        Suche.searchRunning = false;
        return searchResult.getKey();
    }

    private SimpleEntry<List<Knoten>, Map<String, Double>> beginSearch(Knoten startNode, OpenList openList,
                                                                       ClosedList closedList, int solutionLimit,
                                                                       long startTime) {
        openList.add(startNode);
        Knoten expCand;
        List<Knoten> goalNodes = new LinkedList<>();

        while (!openList.isEmpty()) {
            expCand = openList.remove();
            if (expCand.isGoalNode(goalPred)) {
                goalNodes.add(expCand);
                if (goalNodes.size() >= solutionLimit) {
                    break;
                }
            }
            if (!closedList.contains(expCand)) {
                closedList.add(expCand);
                expCand.executeCallbacks(callbackFuncs);
                expCand
                        .expand(accessChecks, stateSearch)
                        .forEach(openList::add);
            }
        }

        return new SimpleEntry<>(goalNodes,
                                 searchResultInfos(startTime, goalNodes.size(), openList.size(), closedList.size()));
    }

    private Map<String, Double> searchResultInfos(long startingTime, int goalListSize, int openListSize,
                                                  int closedListSize) {
        return new LinkedHashMap<>() {{
            put("Rechenzeit in ms.", Util.timeSince(startingTime));
            put("Anzahl gefundener Loesungen", (double) goalListSize);
            put("Groesse der openList", (double) openListSize);
            put("Groesse der closedList", (double) closedListSize);
        }};
    }

    private String debugInfos(SearchStrategy strategy, SimpleEntry<List<Knoten>, Map<String, Double>> result) {
        StringBuilder report = new StringBuilder(String.format("""
                                                                       Ziel wurde %sgefunden
                                                                       Suchalgorithmus: %s
                                                                       Suchart: %s
                                                                       """, result
                                                                       .getKey()
                                                                       .size() != 0 ? "" : "nicht ", strategy,
                                                               isStateSearch() ? "Zustandssuche" : "Wegsuche"));
        for (Map.Entry<String, Double> info_value : result
                .getValue()
                .entrySet()) {
            // Anhaengende nullen nach dem Komma entfernen
            String val = String.format("%,.3f", info_value.getValue());
            int i = val.length() - 1;
            while (val.charAt(i) == '0')
                i--;
            val = val.substring(0, (val.charAt(i) == ',' ? i : i + 1));

            report.append(String.format("%s: %s\n", info_value.getKey(), val));
        }
        return report.toString();
    }

    public boolean isStateSearch() {
        return stateSearch;
    }

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

    public static final class Builder {
        private boolean displayResults = false;
        private boolean printResults = false;

        private boolean stateSearch = true;

        private IAccessibilityChecker[] accessChecks;
        private IGoalPredicate goalPred;
        private IHeuristicFunction[] heuristicFuncs;
        private ICallbackFunction[] callbackFuncs;

        public Builder() {}

        public Builder(Suchszenario scenario) {
            this.stateSearch = scenario.isStateProblem();
            this.accessChecks = scenario.getAccessChecks();
            this.goalPred = scenario.getGoalPred();
            this.heuristicFuncs = scenario.getHeuristicFuncs();
            this.callbackFuncs = scenario.getCallbackFuncs();
        }

        public Suche build() {
            if (accessChecks == null || accessChecks.length == 0) {
                throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
            }
            if (goalPred == null) {
                goalPred = node -> false;
            }
            if (heuristicFuncs == null || heuristicFuncs.length == 0) {
                heuristicFuncs = new IHeuristicFunction[]{node -> 0};
            }
            if (callbackFuncs == null || callbackFuncs.length == 0) {
                callbackFuncs = new ICallbackFunction[]{expCand -> {}};
            }
            return new Suche(this);
        }

        public Builder displayResults(boolean displayResultsResults) {
            this.displayResults = displayResultsResults;
            return this;
        }

        public Builder printResults(boolean printResults) {
            this.printResults = printResults;
            return this;
        }

        public Builder stateSearch(boolean stateSearch) {
            this.stateSearch = stateSearch;
            return this;
        }

        public Builder accessChecks(IAccessibilityChecker... accessChecks) {
            this.accessChecks = accessChecks;
            return this;
        }

        public Builder additionalAccessChecks(IAccessibilityChecker... accessChecks) {
            this.accessChecks = MyUtil.mergeArrays(this.accessChecks, accessChecks);
            return this;
        }

        public Builder goalPred(IGoalPredicate goalPred) {
            this.goalPred = goalPred;
            return this;
        }

        public Builder callbackFuncs(ICallbackFunction... callbackFuncs) {
            this.callbackFuncs = callbackFuncs;
            return this;
        }

        public Builder additionalCallbackFuncs(ICallbackFunction... callbackFuncs) {
            this.callbackFuncs = MyUtil.mergeArrays(this.callbackFuncs, callbackFuncs);
            return this;
        }

        public Builder heuristicFuncs(IHeuristicFunction... heuristicFuncs) {
            this.heuristicFuncs = heuristicFuncs;
            return this;
        }

        public Builder additionalHeuristicFuncs(IHeuristicFunction... heuristicFuncs) {
            this.heuristicFuncs = MyUtil.mergeArrays(this.heuristicFuncs, heuristicFuncs);
            return this;
        }

    }
}
