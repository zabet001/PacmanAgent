package de.fh.stud.Suchen;

import de.fh.pacman.GhostInfo;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Suchen.Suchfunktionen.CallbackFunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;

import java.util.List;

public class Felddistanzen {

    // Optimierungsidee: Momentan noch alle Distanzen doppelt gespeichert ([9][9][1][1]==[1][1][9][9])
    private static short[][][][] distanceMap;
    private static short MAX_DISTANCE;

    public static void initDistances(PacmanTileType[][] world) {
        distanceMap = new short[world.length][world[0].length][][];
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                if (world[i][j] != PacmanTileType.WALL)
                    distanceMap[i][j] = allDistances(world, i, j);
            }
        }

    }

    private static short[][] allDistances(PacmanTileType[][] world, int fieldX, int fieldY) {
        short[][] distancesForThisPos = new short[world.length][world[0].length];

        Suche writeDistances = new Suche(false, Zugangsfilter.noWall(), null, null,
                CallbackFunktionen.saveStepCost(distancesForThisPos));
        writeDistances.start(world, fieldX, fieldY, Suche.SearchStrategy.BREADTH_FIRST, false);

        return distancesForThisPos;
    }

    public static short maxDistance() {
        short max = 0;
        for (int i = 0; i < distanceMap.length; i++)
            for (int j = 0; j < distanceMap[0].length; j++)
                if (distanceMap[i][j] != null)
                    if (maxDistance(i, j) > max)
                        max = maxDistance(i, j);

        return max;
    }

    public static short maxDistance(int posX, int posY) {
        short max = 0;
        for (int i = 0; i < distanceMap.length; i++) {
            for (int j = 0; j < distanceMap[0].length; j++) {
                if (distanceMap[posX][posY][i][j] > max)
                    max = distanceMap[posX][posY][i][j];

            }
        }
        return max;
    }



    public static short getDistance(int firstPosX, int firstPosY, int secondPosX, int secondPosY) {
        assert distanceMap[firstPosX][firstPosY] != null;

        return distanceMap[firstPosX][firstPosY][secondPosX][secondPosY];
    }

    public static short[][] getDistances(int posX, int posY) {
        return distanceMap[posX][posY];
    }

    public static short[][][][] getDistances() {
        return distanceMap;
    }

    public static void printAllDistances(PacmanTileType[][] world) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < distanceMap[0].length; i++) {
            for (int j = 0; j < distanceMap.length; j++) {
                // ausgabe:
                if (world[j][i] != PacmanTileType.WALL) {
                    ret.append(String.format("!! Distanzmap fuer Position: [%d;%d] !!\n", j, i));
                    ret.append(printAllDistances(world, j, i));
                    ret.append("\n");
                }

            }
        }
        System.out.println(ret);
    }

    private static String printAllDistances(PacmanTileType[][] world, int posX, int posY) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < distanceMap[0].length; i++) {
            for (int j = 0; j < distanceMap.length; j++) {
                ret.append(String.format("[%2s]", world[j][i] == PacmanTileType.WALL ? "--" :
                        distanceMap[posX][posY][j][i]));
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    public static class Geisterdistanz {
        public short[] distanceToAllGhost(int posX, int posY, List<GhostInfo> ghostInfos) {
            short[] ret = new short[ghostInfos.size()];
            int i = 0;
            for (GhostInfo ghost : ghostInfos) {
                ret[i++] = Felddistanzen.distanceMap[ghost.getPos().x][ghost.getPos().y][posX][posY];
            }
            return ret;
        }

        public int sumOfGhostDistances(int posX, int posY, List<GhostInfo> ghostInfos) {
            int distanceSum = 0;
            for (short ghostDistance : distanceToAllGhost(posX, posY, ghostInfos)) {
                distanceSum += ghostDistance;
            }
            return distanceSum;
        }

        public short maximumGhostDistance(int posX, int posY, List<GhostInfo> ghostInfos) {
            short maxDistance = 0;
            for (short ghostDistance : distanceToAllGhost(posX, posY, ghostInfos)) {
                if (ghostDistance > maxDistance)
                    maxDistance = ghostDistance;
            }
            return maxDistance;
        }

        public short minimumGhostDistance(int posX, int posY, List<GhostInfo> ghostInfos) {
            short minDistance = Short.MAX_VALUE;
            for (short ghostDistance : distanceToAllGhost(posX, posY, ghostInfos)) {
                if (ghostDistance < minDistance)
                    minDistance = ghostDistance;
            }
            return minDistance;
        }
    }
}
