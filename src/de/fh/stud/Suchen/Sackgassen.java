package de.fh.stud.Suchen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchfunktionen.CallbackFunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Sackgassen {

    // Hinweis: Wenn die Map ohne Zyklen ist, wird eine Sackgasse nicht als solche erkannt
    public static byte[][] deadEndDepth;

    public static void initDeadEndDepth(PacmanTileType[][] world) {
        deadEndDepth = new byte[world.length][world[0].length];

        // Schritt 1: Alle Sackgassenenden ausfindig machen
        /* Tupel: (Sackgassenende,Vorgaenger)*/
        List<AbstractMap.SimpleEntry<Vector2, Vector2>> oneWays = oneWayEndsFirstOrder(world);

        /* Schritt 2: Fuer alle Sackgassen: Anfang suchen, dabei CALLBACK: ALLE besuchten Felder seperat abspeichern
         und temporaer schließen (=WALL) -> mehrstufige sackgassen werden einstufig*/
        List<Vector2> temporaryBlocked = new ArrayList<>();
        List<PacmanTileType> temporaryBlockedType = new ArrayList<>();

        for (int i = 0; i < oneWays.size(); i++) {
            AbstractMap.SimpleEntry<Vector2, Vector2> oneWayStartTuple = locateStartOfOneWay(world,
                    oneWays.get(i).getKey().x, oneWays.get(i).getKey().y, temporaryBlocked, temporaryBlockedType);
            if (oneWayStartTuple != null) {
                oneWays.add(i, oneWayStartTuple);
                oneWays.remove(i + 1);
            } else
                oneWays.remove(i--);
        }

        // Schritt 3: Blockaden wieder oeffnen
        for (int i = 0; i < temporaryBlocked.size(); i++) {

            world[temporaryBlocked.get(i).x][temporaryBlocked.get(i).y] = temporaryBlockedType.get(i);
        }

        /* Schritt 4: Suche starten bei Vorgaenger der Sackgassenenden,
        ACCESSIBILITY_CHECKER: das jeweilige Feld VOR Sackgasse als Wand betrachten
        CALLBACK: Kosten fuer das Feld abspeichern, falls gespeicherteKosten < ermittelteKosten*/

        for (AbstractMap.SimpleEntry<Vector2, Vector2> oneWayEntry : oneWays) {
            // IDEE: Wenn deadEndDepth[sackgassenende.x][sackgassenende.y] != 0 muss nicht
            if (deadEndDepth[oneWayEntry.getKey().x][oneWayEntry.getKey().y] == 0) {
                writeOneWayDepth(world, oneWayEntry.getValue(), oneWayEntry.getKey());
            }
        }


    }

    public static void printOneWayDepthMap(PacmanTileType[][] world) {
        System.out.println();

        for (int i = 0; i < deadEndDepth[0].length; i++) {
            for (int j = 0; j < deadEndDepth.length; j++) {
                System.out.printf("%2s ", world[j][i] == PacmanTileType.WALL ? "[]" : deadEndDepth[j][i]);
            }
            System.out.println();
        }

        System.out.println();
    }

    /**
     @return - Tupel(Sackgassenende, Vorgaenger Sackgassenende) -> value Vorgaenger Sackgassenende = null, da Sackgasse
     erster Ordnung
     */
    private static List<AbstractMap.SimpleEntry<Vector2, Vector2>> oneWayEndsFirstOrder(PacmanTileType[][] world) {
        List<AbstractMap.SimpleEntry<Vector2, Vector2>> ret = new LinkedList<>();
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                if (world[i][j] != PacmanTileType.WALL && MyUtil.noWallsNeighbourCnt(world, i, j) < 2) {
                    ret.add(new AbstractMap.SimpleEntry<>(new Vector2(i, j), null));
                }
            }
        }
        return ret;
    }

    /**
     @param blockedFields - Liste der Felder, die temporaer geschlossen wurden
     @param blockedFieldType - korrespondierende Typen zum zwischenspeichern

     @return - Tupel(Sackgassenende, Sackgassenanfang von Sackgasse naechster Tiefe)
     */
    private static AbstractMap.SimpleEntry<Vector2, Vector2> locateStartOfOneWay(PacmanTileType[][] world, int posX,
                                                                                 int posY,
                                                                                 List<Vector2> blockedFields,
                                                                                 List<PacmanTileType> blockedFieldType) {

        Knoten oneWayStart = new Suche(Suchszenario.locateDeadEndExit(),
                CallbackFunktionen.saveVisited(blockedFields, blockedFieldType, false),
                CallbackFunktionen.setVisitedType(PacmanTileType.WALL))
                .start(world, posX, posY,
                Suche.SearchStrategy.DEPTH_FIRST, false);

        if (oneWayStart == null)
            return null;
        return new AbstractMap.SimpleEntry<>(oneWayStart.getPosition(), oneWayStart.getPred().getPosition());
    }

    /**
     @param oneWayEntry - Eintrittspunkt - Beginn der Suche
     @param oneWayGate - Wird als Wand betrachtet, nicht hier expandieren
     */
    private static void writeOneWayDepth(PacmanTileType[][] world, Vector2 oneWayEntry, Vector2 oneWayGate) {
        Suche writeDepths = new Suche(false, Zugangsfilter.merge(Zugangsfilter.noWall(),
                Zugangsfilter.excludePositions(oneWayGate)), null, null, CallbackFunktionen.saveStepCost(deadEndDepth));
        writeDepths.start(world, oneWayEntry.x, oneWayEntry.y, Suche.SearchStrategy.DEPTH_FIRST,
                false);

    }
}
