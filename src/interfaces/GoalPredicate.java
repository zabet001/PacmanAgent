package interfaces;

import de.fh.stud.p2.Knoten;

public interface GoalPredicate {
	// TODO ? Klasse statt interface: Zur Klasse ein flag IS_STATE_GOAL einfuegen
	boolean isGoalNode(Knoten node);
}
