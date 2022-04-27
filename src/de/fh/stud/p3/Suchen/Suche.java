package de.fh.stud.p3.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.stud.p2.Knoten;


import javax.swing.*;
import java.util.*;

public class Suche {

    public static List<Double> RUN_TIMES = new LinkedList<>();


    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    private void printDebugInfos(SearchStrategy strategy, long startTime, AbstractMap.SimpleEntry<Knoten, Map<String, Integer>> result) {
        double elapsedTime = Util.timeSince(startTime);
        RUN_TIMES.add(elapsedTime);

        StringBuilder report = new StringBuilder(String.format("""
                Ziel wurde %sgefunden
                Suchalgorithmus: %s
                Zeit: %.2f ms
                """, result.getKey() != null ? "" : "nicht ", strategy, elapsedTime));
        for (Map.Entry<String, Integer> info_value : result.getValue().entrySet()) {
            report.append(String.format("%s: %,d\n", info_value.getKey(), info_value.getValue()));
        }
        System.out.println(report);
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(jf, report.toString());
    }

    public Knoten start(Knoten startNode, SearchStrategy strategy) {
        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<Knoten, Map<String, Integer>> searchResult;
        switch (strategy) {
            case DEPTH_FIRST:
            case BREADTH_FIRST:
            	searchResult = uninformedSearch(startNode, strategy);
            	break;
            default:
            	searchResult = informedSearch(startNode, strategy);
        };
        // printDebugInfos(strategy, startTime, searchResult);

        double elapsedTime = Util.timeSince(startTime);
        RUN_TIMES.add(elapsedTime);
        System.out.printf("Laufzeit fuer Durchlauf %d: %.2f",RUN_TIMES.size(),elapsedTime);
        System.out.printf("Durchschn. Laufzeit: %.2f",RUN_TIMES.stream().reduce(0.0, Double::sum)/RUN_TIMES.size(),elapsedTime);


        return searchResult.getKey();
    }

    // TODO: Eine Loesung finden, um NICHT doppelten Code zu schreiben!
    public AbstractMap.SimpleEntry<Knoten, Map<String, Integer>> informedSearch(Knoten startNode,
                                                                                SearchStrategy strategy) {

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

        return new AbstractMap.SimpleEntry<>(goalNode, new LinkedHashMap<>() {{
            put("Groesse der openList", openList.size());
            put("Groesse der closedList", closedList.size());
        }});
    }

    public AbstractMap.SimpleEntry<Knoten, Map<String, Integer>> uninformedSearch(Knoten startNode,
                                                                                  SearchStrategy strategy) {
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
        return new AbstractMap.SimpleEntry<>(goalNode, new LinkedHashMap<>() {{
            put("Groesse der openList", openList.size());
            put("Groesse der closedList", closedList.size());
        }});
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
