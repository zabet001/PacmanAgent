package de.fh.stud.interfaces;

import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface ICallbackFunction extends ISearchFunction {
    void callback(Knoten expCand);
}
