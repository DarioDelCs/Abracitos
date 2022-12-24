package edu.upc.epsevg.prop.othello;

import edu.upc.epsevg.prop.othello.players.DesdemonaPlayer;
import edu.upc.epsevg.prop.othello.players.RandomPlayer;
import edu.upc.epsevg.prop.othello.players.abracitos.PlayerID;
import edu.upc.epsevg.prop.othello.players.abracitos.PlayerMinimax;


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
                //IPlayer player2 = new Abracitos_IDS_Hash("A_Hash", 1);
//                IPlayer player1 = new PlayerID(2);
//                  IPlayer player1 = new Abracitos_IDS_anillo();
//                IPlayer player1 = new Abracitos_IDS_Hash_pruebas("winner", 2);
//                IPlayer player1 = new HumanPlayer("Human1");
                IPlayer player1 = new PlayerMinimax(8);
                IPlayer player2 = new DesdemonaPlayer(1);//GB
//                IPlayer player2 = new RandomPlayer("Random");

                //player 1 no_ids vs player 2 ids peta
                                
                new Board(player1 , player2, 2, false);
            }
        });
    }
}