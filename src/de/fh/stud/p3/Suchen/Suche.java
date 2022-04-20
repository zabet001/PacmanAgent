package de.fh.stud.p3.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.stud.p2.Knoten;
import interfaces.AccessibilityChecker;
import interfaces.GoalPredicate;
import interfaces.HeuristicFunction;

import javax.swing.*;
import java.util.*;

public class Suche {

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    private void printDebugInfos(SearchStrategy strategy, long startTime, AbstractMap.SimpleEntry<Knoten, Map<String, Integer>> result) {
        double elapsedTime = Util.timeSince(startTime);
        String report = String.format("Ziel wurde %sgefunden\n" +
                "Suchalgorithmus: %s\n" +
                "Zeit: %.2f ms\n", result.getKey() != null ? "" : "nicht ",strategy,elapsedTime);
        for (Map.Entry<String, Integer> info_value : result.getValue().entrySet()) {
            report += String.format("%s: %,d\n", info_value.getKey(), info_value.getValue());
        }
        System.out.println(report);
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(jf, report);
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
        printDebugInfos(strategy, startTime, searchResult);
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
                expCand.expand().forEach(child -> openList.add(child));
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
        switch (strategy) {
            case GREEDY:
                return Comparator.comparingInt(Knoten::getHeuristic);
            
            case UCS:
                return Comparator.comparingInt(Knoten::getCost);
            
            case A_STAR:
                return Comparator.comparingInt(a -> a.getCost() + a.getHeuristic());
            
            default:
                return null;
            
        }
    }

    private void addToOpenList(SearchStrategy strategy, List<Knoten> openList, Knoten child) {
        switch (strategy) {
            case DEPTH_FIRST:
                openList.add(0, child);
                break;
            
            case BREADTH_FIRST:
                openList.add(child);
                break;
            
		}
	}

}
