package de.fh.stud.p2;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.p3.Suchen.Suchfunktionen.Zugangsfilter;

import java.util.LinkedList;
import java.util.List;

public class BaumTest {

    public static void main(String[] args) {
        //Anfangszustand nach Aufgabe
        PacmanTileType[][] view = {{PacmanTileType.WALL, PacmanTileType.WALL, PacmanTileType.WALL,
                PacmanTileType.WALL}, {PacmanTileType.WALL, PacmanTileType.EMPTY, PacmanTileType.DOT,
                PacmanTileType.WALL}, {PacmanTileType.WALL, PacmanTileType.EMPTY, PacmanTileType.WALL,
                PacmanTileType.WALL}, {PacmanTileType.WALL, PacmanTileType.WALL, PacmanTileType.WALL,
                PacmanTileType.WALL}};

        //Startposition des Pacman
        int posX = 1, posY = 1;
        /*
         * TODO Praktikum 2 [3]: Baut hier basierend auf dem gegebenen
         * Anfangszustand (siehe view, posX und posY) den Suchbaum auf.
         */

        List<Knoten> expChildren = new LinkedList<>();
        Knoten expCand;
        expChildren.add(Knoten.generateRoot(view, posX, posY, false,Zugangsfilter.safeToWalkOn(true), null,
                null));

        for (int i = 0; i < 10; i++) {
            expCand = expChildren.remove(0);
            Util.printView(Knoten.reformatToTileType(expCand.getView()));
            expCand.expand().forEach(child -> expChildren.add(0, child));
        }
    }
}
