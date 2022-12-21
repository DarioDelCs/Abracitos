package edu.upc.epsevg.prop.othello;

import edu.upc.epsevg.prop.othello.players.HumanPlayer;
import edu.upc.epsevg.prop.othello.players.RandomPlayer;
import edu.upc.epsevg.prop.othello.Level;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.players.DesdemonaPlayer;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos_IDS;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos_IDS_Hash;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos_IDS_Hash_pruebas;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos_IDS_anillo;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos_NO_IDS;
import edu.upc.epsevg.prop.othello.players.abracitos.Abracitos_Thread;
import edu.upc.epsevg.prop.othello.players.abracitos.Task;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


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
//                IPlayer player1 = new Abracitos_Thread("pruebas", 1);
//                  IPlayer player1 = new Abracitos_IDS_anillo();
//                IPlayer player2 = new Abracitos_IDS_Hash_pruebas("winner", 1);
                //IPlayer player1 = new HumanPlayer("Human1");
                //IPlayer player1 = new Abracitos(10);
//                IPlayer player1 = new DesdemonaPlayer(1);//GB

                //player 1 no_ids vs player 2 ids peta
                                
//                new Board(player1 , player2, 2, false);


                Set<Integer> moves = new HashSet<Integer>() {{
                    add(1);
                    add(2);
                    add(3);
                    add(4);
                    add(5);
                    add(6);
                    add(7);
                    add(8);
                    add(9);
                    add(10);
                    add(11);
                    add(12);
                }};
                
                
                
                ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
                for (int i = 0; i < 10; i++) 
                {
                    Task task = new Task(i);
                    System.out.println("A new task has been added : " + i);
                    executor.execute(task);
                }
                System.out.println("Maximum threads inside pool " + executor.getMaximumPoolSize());
                executor.shutdown();
                

                moves.parallelStream().forEach((move) -> {
                    System.out.println("move -> " + move);
                });
            }
        });
    }
}