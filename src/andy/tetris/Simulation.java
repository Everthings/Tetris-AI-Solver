package andy.tetris;

import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JFrame;

public class Simulation {
	JFrame frame = new JFrame();
	int width = 700;
	int height = 1400;
	
	ArrayList<AI> population = new ArrayList<AI>();
	final int POPULATION_SIZE = 1000;
	
	Game viewedGame;
	
	int generation = 1;
	
	Simulation(){
		initWindow();
		initPopulation();
		
		runNewGeneration();
	}
	
	void initPopulation() {
		for(int i = 0; i < POPULATION_SIZE; i++) {
			population.add(new AI());
		}
	}

	void initWindow() {
		frame.setBackground(Color.BLACK); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		frame.add(new Game(new AI(), width, height));	
		
		frame.validate();		
		frame.setVisible(true);
		frame.repaint();
		
		frame.setTitle("Tetris");
		Insets insets = frame.getInsets();
		frame.setSize(width * 2 + insets.left + insets.right, height + insets.top + insets.bottom); 
		frame.setLocationRelativeTo(null); 
	}
	
	void runNewGeneration() {
		new Thread(new Runnable(){

			@Override
			public void run() {
				System.out.println("Generation: " + generation++);
				simulateParrallel();
				createNewPopulation();
				runNewGeneration();
			}
			
		}).start();
	}
	
	void createNewPopulation() {
		sortPopulation();
		cullWorst(0.3);
		createChildren(0.1);
		mutatePopulation();
		sortPopulation();
		
		for(int i = 0; i < 10; i++)
			System.out.println("Top Fitness (" + i + "): " + population.get(i).getFitness() + " Genes: " + Arrays.toString(population.get(i).getGenes()));
	}
	
	void createChildren(double tournamentPercentage) {
		int numChildren = POPULATION_SIZE - population.size();
		
		ArrayList<AI> tournament = new ArrayList<AI>();
		int num = (int)(POPULATION_SIZE * tournamentPercentage);
		for(int i = 0; i < num; i++)
			tournament.add(population.remove((int)(Math.random() * population.size())));
		
		for(int i = 0; i < numChildren; i++) {
			boolean selected = false;
			int counter = 0;
			AI parent1 = null;
			
			while(!selected) {
				if(Math.random() < 0.15) {
					parent1 = tournament.get(counter);
					selected = true;
				}else if(counter == tournament.size() - 1){
					parent1 = tournament.get(counter);
					selected = true;
				}else
					counter++;
			}
			
			selected = false;
			counter = 0;
			AI parent2 = null;
			while(!selected) {
				if(Math.random() < 0.15 && tournament.get(counter) != parent1) {
					parent2 = tournament.get(counter);
					selected = true;
				}else if(counter == tournament.size() - 1){
					parent2 = tournament.get(counter);
					selected = true;
				}else
					counter++;
			}
			
			AI child = new AI(parent1, parent2);
			population.add(child);
		}

		population.addAll(tournament);
	}
	
	void mutatePopulation() {
		for(AI ai: population)
			ai.mutateGenes();
	}
	
	void sortPopulation() {
		population.sort(new Comparator<AI>(){
			@Override
			public int compare(AI ai1, AI ai2) {
				if(ai1.getFitness() > ai2.getFitness())
					return -1;
				else if(ai1.getFitness() < ai2.getFitness())
					return 1;
				
				return 0;
			}
		});
	}
	
	void cullWorst(double cullPercent) {
		int cullNum = (int) (POPULATION_SIZE * cullPercent);
		
		for(int i = 0; i < cullNum; i++) {
			population.remove(POPULATION_SIZE - 1 - i);
		}
	}
	
	void simulateParrallel() {
		final int batchSize = 10;
		
		int playerIndex = 0;

		final Game[] games = new Game[batchSize];
		Thread[] threads = new Thread[batchSize];
		
		for(int a = 0; a <= POPULATION_SIZE/batchSize; a++){
			
			final int batch_num = a;
			
			for(int i = 0; i < batchSize && playerIndex < POPULATION_SIZE; i++){
				
				final int index = i;
				
				games[i] = new Game(population.get(playerIndex++), width, height);
				updateView(games[0]);
				threads[i] = new Thread(new Runnable(){
					@Override
					public void run() {
						while(!games[index].hasEnded() && games[index].getPiecesGenerated() < 2000) {
							games[index].runIteration();
						}
						
						population.get(batch_num * batchSize + index).setFitness(games[index].getScore());
					}
					
				});
				threads[i].start();
			}
			
			for(Thread t: threads){
				if(t != null){
					try {
						t.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		viewedGame = null;
	}
	
	void updateView(Game game) {
		
		if(viewedGame != null)
			frame.remove(viewedGame);
		
		viewedGame = game;
		frame.add(game);
		frame.repaint();
	}
}
