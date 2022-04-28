package de.fh.stud.p3.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.stud.p2.Knoten;

import javax.swing.*;
import java.util.*;

public class Suche {

    public static List<Double> RUN_TIMES = new LinkedList<>();

    public static boolean SHOW_RESULTS = false;
    public static boolean PRINT_AVG_RUNTIME = true;

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    public Knoten start(Knoten startNode, SearchStrategy strategy) {
        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<Knoten, Map<String, Double>> searchResult = switch (strategy) {
            case DEPTH_FIRST, BREADTH_FIRST -> uninformedSearch(startNode, strategy, startTime);
            default -> informedSearch(startNode, strategy, startTime);
        };

        if (PRINT_AVG_RUNTIME) {
            double elapsedTime = searchResult.getValue().get("Rechenzeit in ms.");
            RUN_TIMES.add(elapsedTime);
            System.out.printf("Laufzeit fuer Durchlauf Nr. %d: %.2f ms.\n", RUN_TIMES.size(), elapsedTime);
            System.out.printf("Durchschnittliche. Laufzeit: %.2f ms.\n",
                    RUN_TIMES.stream().reduce(0.0, Double::sum) / RUN_TIMES.size());
            System.out.println("...");
        }
        if (SHOW_RESULTS)
            printDebugInfos(strategy, startTime, searchResult);

        return searchResult.getKey();
    }

    // TODO: Eine Loesung finden, um NICHT doppelten Code zu schreiben!
    public AbstractMap.SimpleEntry<Knoten, Map<String, Double>> informedSearch(Knoten startNode,
                                                                               SearchStrategy strategy,
                                                                               long startTime) {

        HashSet<Knoten> closedList = new HashSet<>();
        PriorityQueue<Knoten> openList = new PriorityQueue<>(getInsertionCriteria(strategy));
        openList.add(startNode);
        Knoten expCand;
        Knoten goalNode = null;

        while (!openList.isEmpty()) {
            expCand = openList.remove();
            if (expCand.isGoalNode()) {
                goalNode = expCand;
                break;
            }
            if (!closedList.contains(expCand)) {
                closedList.add(expCand);
                openList.addAll(expCand.expand());
            }
        }

        return new AbstractMap.SimpleEntry<>(goalNode, searchResultInfos(startTime, openList.size(), closedList.size()));
    }
    public AbstractMap.SimpleEntry<Knoten, Map<String, Double>> uninformedSearch(Knoten startNode,
                                                                                 SearchStrategy strategy,
                                                                                 long startTime) {
        HashSet<Knoten> closedList = new HashSet<>();
        List<Knoten> openList = new LinkedList<>();
        openList.add(startNode);
        Knoten expCand;
        Knoten goalNode = null;

        while (!openList.isEmpty()) {
            expCand = openList.remove(0);
            if (expCand.isGoalNode()) {
                goalNode = expCand;
                break;
            }
            if (!closedList.contains(expCand)) {
                closedList.add(expCand);
                expCand.expand().forEach(child -> addToOpenList(strategy, openList, child));
            }
        }
        return new AbstractMap.SimpleEntry<>(goalNode, searchResultInfos(startTime, openList.size(), closedList.size()));
    }

    private Map<String, Double> searchResultInfos(long startingTime, int openListSize, int closedListSize) {
        return new LinkedHashMap<>() {{
            put("Rechenzeit in ms.", Util.timeSince(startingTime));
            put("Groesse der openList", (double) openListSize);
            put("Groesse der closedList", (double) closedListSize);
        }};
    }

    private void printDebugInfos(SearchStrategy strategy, long startTime, AbstractMap.SimpleEntry<Knoten, Map<String,
            Double>> result) {
        StringBuilder report = new StringBuilder(String.format("""
                Ziel wurde %sgefunden
                Suchalgorithmus: %s
                Suchart: %s
                """, result.getKey() != null ? "" : "nicht ", strategy,Knoten.IS_STATE_SEARCH ? "Zustandssuche" : "Wegsuche"));
        for (Map.Entry<String, Double> info_value : result.getValue().entrySet()) {
            report.append(String.format("%s: %,.3f\n", info_value.getKey(), info_value.getValue()));
        }
        System.out.println(report);
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(jf, report.toString());
    }

    private Comparator<Knoten> getInsertionCriteria(SearchStrategy strategy) {
        return switch (strategy) {
            case GREEDY -> Comparator.comparingInt(Knoten::getHeuristic);
            case UCS -> Comparator.comparingInt(Knoten::getCost);
            case A_STAR -> Comparator.comparingInt(a -> a.getCost() + a.getHeuristic());
            default -> null;
        };
    }

    private void addToOpenList(SearchStrategy strategy, List<Knoten> openList, Knoten child) {
        switch (strategy) {
            case DEPTH_FIRST -> openList.add(0, child);
            case BREADTH_FIRST -> openList.add(child);
        }
    }

}
