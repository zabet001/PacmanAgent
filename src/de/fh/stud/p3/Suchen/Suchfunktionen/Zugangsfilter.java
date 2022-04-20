package de.fh.stud.p3.Suchen.Suchfunktionen;

import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.p2.Knoten;
import interfaces.AccessibilityChecker;

public class Zugangsfilter {

    public static AccessibilityChecker safeToWalkOn() {
        return safeToWalkOn(false);
    }

    public static AccessibilityChecker safeToWalkOn(boolean noPowerpill) {
        if (noPowerpill) {
            return (node, newPosX, newPosY) -> {
                PacmanTileType field = Knoten.byteToTile(node.getView()[newPosX][newPosY]);
                return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
            };
        } else
            // TODO: Powerpille einbeziehen, falls Geist
            return null;
    }
}
