package de.fh.stud.interfaces;

import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface IGoalPredicate extends ISearchFunction {
    // TODO ? Klasse statt interface: Zur Klasse ein flag IS_STATE_GOAL einfuegen
    boolean isGoalNode(Knoten node);
}
