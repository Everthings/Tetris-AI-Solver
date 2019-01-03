package andy.tetris;

public class AI {
	double[] genes = new double[4];
	int rank = -1;
	double fitness = -1;
	
	AI(){
		//randomly intialize genes
		for(int i = 0; i < genes.length; i++)
			genes[i] = Math.random() * 2 - 1;
		
		//double[] genesTemp = {-0.510066, 0.760666, -0.35663, -0.184483};
		//genes = genesTemp;
		
		normalizeGenes();
	}
	
	AI(AI parent1, AI parent2){
		
		double squaredSum = parent1.getFitness() * parent1.getFitness() + parent2.getFitness() * parent2.getFitness();
		double noralizationFactor = Math.sqrt(squaredSum);
		double[] weightedAverage = {parent1.getFitness(), parent2.getFitness()};
		for(int i = 0; i < 2; i++) {
			weightedAverage[i] += weightedAverage[i] / noralizationFactor;
		}
		
		double[] genes1 = parent1.getGenes();
		double[] genes2 = parent2.getGenes();
		
		for(int i = 0; i < genes.length; i++)
			genes[i] = genes1[i] * weightedAverage[0] + genes2[i] * weightedAverage[1];
		
		normalizeGenes();
	}
	
	public void normalizeGenes() {
		double squaredSum = 0;
		for(int i = 0; i < genes.length; i++) {
			squaredSum += genes[i] * genes[i];
		}
		
		double noralizationFactor = Math.sqrt(squaredSum);
		
		for(int i = 0; i < genes.length; i++) {
			genes[i] = genes[i] / noralizationFactor;
		}
	}
	
	public void mutateGenes() {
		for(int i = 0; i < genes.length; i++) {
			if(Math.random() < 0.05)
				genes[i] += Math.random() * 0.4 - 0.2;
		}
		
		normalizeGenes();
	}
	
	public double[] getGenes() {
		return genes;
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public int getRank() {
		return rank;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
}
