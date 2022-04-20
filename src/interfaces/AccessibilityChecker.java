package interfaces;

import de.fh.stud.p2.Knoten;

public interface AccessibilityChecker {
	boolean isAccecssible(Knoten node, byte newPosX, byte newPosY);
}
