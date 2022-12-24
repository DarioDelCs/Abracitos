package edu.upc.epsevg.prop.othello;

import edu.upc.epsevg.prop.othello.players.DesdemonaPlayer;
import edu.upc.epsevg.prop.othello.players.RandomPlayer;
import edu.upc.epsevg.prop.othello.players.abracitos.PlayerMiniMax;


import javax.swing.SwingUtilities;

/**
 * Lines Of Action: el joc de taula.
 * @author bernat
 */
public class Game {
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IPlayer player2 = new PlayerMiniMax(8);
                IPlayer player1 = new DesdemonaPlayer(1);//GB
//                IPlayer player2 = new RandomPlayer("Random");

                //player 1 no_ids vs player 2 ids peta
                                
                new Board(player1 , player2, 2, false);
            }
        });
    }
}