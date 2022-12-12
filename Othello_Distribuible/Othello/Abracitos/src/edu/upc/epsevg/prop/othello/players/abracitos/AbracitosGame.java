/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import java.util.BitSet;

/**
 *
 * @author Dario
 */
public class AbracitosGame extends GameStatus{
    
    AbracitosGame(GameStatus gs){
        super(gs);
    }
    
    public void changePlayer(CellType ct){
        this.currentPlayer = ct;
    }

    public BitSet getBoard_color() {
        return board_color;
    }

    public BitSet getBoard_occupied() {
        return board_occupied;
    }
    
}
