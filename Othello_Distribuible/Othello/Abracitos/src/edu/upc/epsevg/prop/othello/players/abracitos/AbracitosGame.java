package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import java.util.BitSet;

/**
 * Clase extendadora de GameStatus per obtenir el color i la ocupacio del tauler, i per canviar el jugador per calcular l'heuristica de mobility
 * @author Dario, Xavi
 */
public class AbracitosGame extends GameStatus{
    
    /**
     * Constructor de la clase
     * @param gs el tauler del gameStatus
     */
    AbracitosGame(GameStatus gs){
        super(gs);
    }
    
    /**
     * Funcio per modificar el jugador actual del tauler
     * @param ct el nou jugador al que volem setejar
     */
    public void changePlayer(CellType ct){
        this.currentPlayer = ct;
    }

    /**
     * Funcio per retornar el color del tauler per saber si una ocupacio la te el jugador1 o el jugador2
     * @return el color del tauler per saber si una ocupacio la te el jugador1 o el jugador2
     */
    public BitSet getBoard_color() {
        return board_color;
    }

    /**
     * Funcio per retornar l'ocupacio del tauler per saber si una poisicio del tauler esta buida o la te un dels jugadors
     * @return l'ocupacio del tauler per saber si una poisicio del tauler esta buida o la te un dels jugadors
     */
    public BitSet getBoard_occupied() {
        return board_occupied;
    }
    
}
