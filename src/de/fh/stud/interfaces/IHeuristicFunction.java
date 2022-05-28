package de.fh.stud.interfaces;

import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface IHeuristicFunction extends ISearchFunction {
    float calcHeuristic(Knoten node);
}
