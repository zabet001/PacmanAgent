package de.fh.stud.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;

import javax.swing.*;
import java.util.*;

public class Suche {

    public static List<Double> RUN_TIMES = new LinkedList<>();

    public static boolean SHOW_RESULTS = true;
    public static boolean PRINT_AVG_RUNTIME = false;

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    public Knoten start(Knoten startNode, SearchStrategy strategy, boolean showResults) {
        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<Knoten, Map<String, Double>> searchResult = switch (strategy) {
            case DEPTH_FIRST, BREADTH_FIRST -> uninformedSearch(startNode, strategy, startTime);
            default -> informedSearch(startNode, strategy, startTime);
        };

        if (PRINT_AVG_RUNTIME) {
            double elapsedTime = searchResult.getValue().get("Rechenzeit in ms.");
            RUN_TIMES.add(elapsedTime);
            MyUtil.println(String.format("Laufzeit fuer Durchlauf Nr. %d: %.2f ms.\n", RUN_TIMES.size(), elapsedTime));
            MyUtil.println(String.format("Durchschnittliche. Laufzeit: %.2f ms.",
                    RUN_TIMES.stream().reduce(0.0, Double::sum) / RUN_TIMES.size()));
            MyUtil.println("...");
        }
        if (showResults)
            printDebugInfos(strategy, searchResult);

        return searchResult.getKey();
    }

    public Knoten start(Knoten startNode, SearchStrategy strategy) {
        return start(startNode,strategy,SHOW_RESULTS);
    }

    // TODO: Eine Loesung finden, um NICHT doppelten Code zu schreiben!
    private AbstractMap.SimpleEntry<Knoten, Map<String, Double>> informedSearch(Knoten startNode,
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
                expCand.executeCallbacks();
                openList.addAll(expCand.expand());
            }
        }

        return new AbstractMap.SimpleEntry<>(goalNode, searchResultInfos(startTime, openList.size(), closedList.size()));
    }
    private AbstractMap.SimpleEntry<Knoten, Map<String, Double>> uninformedSearch(Knoten startNode,
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
                expCand.executeCallbacks();
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

    private void printDebugInfos(SearchStrategy strategy, AbstractMap.SimpleEntry<Knoten, Map<String,
            Double>> result) {
        StringBuilder report = new StringBuilder(String.format("""
                Ziel wurde %sgefunden
                Suchalgorithmus: %s
                Suchart: %s
                """, result.getKey() != null ? "" : "nicht ", strategy,Knoten.IS_STATE_NODE ? "Zustandssuche" : "Wegsuche"));
        for (Map.Entry<String, Double> info_value : result.getValue().entrySet()) {
            report.append(String.format("%s: %,.3f\n", info_value.getKey(), info_value.getValue()));
        }
        MyUtil.println(report.toString());
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
