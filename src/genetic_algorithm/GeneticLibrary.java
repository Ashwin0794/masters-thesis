package genetic_algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

public class GeneticLibrary {
	Map<String, List<Integer>> things;
	
	public GeneticLibrary() {
		things = new LinkedHashMap<String, List<Integer>>();
		// Array list contains weight(grams), value(favorable factor) respectively for all things
		things.put("Laptop", new ArrayList<Integer>(Arrays.asList(2200, 500)));
		things.put("NotePad", new ArrayList<Integer>(Arrays.asList(333, 40)));
		things.put("Coffee Mug", new ArrayList<Integer>(Arrays.asList(350, 60)));
		things.put("Headphones", new ArrayList<Integer>(Arrays.asList(160, 150)));
		things.put("Bottle", new ArrayList<Integer>(Arrays.asList(192, 30)));
	}
	
	/** genome -> list of Integer*/
	public List<Integer> generate_genome(int length) {
		List<Integer> genome = new ArrayList<>();
		Random rand = new Random();
		for (int k = 0; k < length; k++) {
			genome.add(rand.nextInt(2));
		}
		return genome;
	}
	
	/** population -> List of genome */
	public List<List<Integer>> generate_population(int size, int genome_length) {
		List<List<Integer>> population = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			population.add(generate_genome(genome_length));
		}
		return population;
	}
	
	/**  returns accumulated value of all genomes(population) */
	public int fitness_function(List<Integer> genome, Map<String, List<Integer>> things, int weight_limit) {
		int totalWeight = 0, totalValue = 0;
		int index = 0;
		for (Map.Entry<String, List<Integer>> thing : things.entrySet()) {
			if (genome.get(index) == 1) {
				totalWeight += thing.getValue().get(0);
				totalValue += thing.getValue().get(1);
			}
			if (totalWeight > weight_limit) return 0;
			index++;
		}
		return totalValue;
	}
	
	public List<List<Integer>> selection_pair(List<List<Integer>> population) {
		List<Integer> populationValuesList = new ArrayList<>();
		for (List<Integer> genome: population) {
			populationValuesList.add(fitness_function(genome, things, Constants.WEIGHT_LIMIT));
		}
		
		List<List<Integer>> selection = new ArrayList<>();
		int totalPopulationValue = populationValuesList.stream().mapToInt(Integer::intValue).sum();
		System.out.println("totalPopulationValue is " + totalPopulationValue);
		Random random = new Random();
		for (int i = 0; i < 2; i++) {
            
			double randomValue = random.nextDouble() * totalPopulationValue;
            System.out.println("randomValue is " + randomValue);
            
            double valueSum = 0;
            for (int j = 0; j < population.size(); j++) {
            	valueSum += populationValuesList.get(j);
                if (valueSum >= randomValue) {
                    selection.add(population.get(j));
                    break;
                }
            }
        }
		return selection;
	}
	
	public List<List<Integer>> single_point_crossover(List<Integer> genomeA, List<Integer> genomeB) {
		List<List<Integer>> genomePair = new ArrayList<>();
		
		/** Cannot divide the genome */
		if (genomeA.size() < 2) {
			genomePair.add(genomeA);
			genomePair.add(genomeB);
		
		} else {
			int genome_size = genomeA.size();
			Random rand = new Random();
			int p = rand.nextInt(genome_size);
			
			List<Integer> newGenomeA = new ArrayList<>();
			newGenomeA.addAll(genomeA.subList(0, p));
			newGenomeA.addAll(genomeB.subList(p, genome_size));
			
			genomePair.add(newGenomeA);
			
			List<Integer> newGenomeB = new ArrayList<>();
			newGenomeB.addAll(genomeB.subList(0, p));
			newGenomeB.addAll(genomeA.subList(p, genome_size));
			
			genomePair.add(newGenomeB);
			
		}
		return genomePair;
	}
	
	public List<Integer> mutation(List<Integer> genome, Optional<Integer> num, Optional<Float> probability) {
		
		Random rand = new Random();
		for (int i = 0; i < num.orElse(1); i++) {
			System.out.println(genome.size());
			int index = rand.nextInt(genome.size());
			if (rand.nextDouble() < (probability.orElse((float)0.5))) {
				genome.set(index, Math.abs(genome.get(index) - 1));
			}
		}
		return genome;
	}
	
	public void print_population(List<List<Integer>> population) {
		System.out.println("===== Printing currentPopulation ===========");
		for (List<Integer> genome: population) {
			System.out.println("genome: " + String.join(", ", genome.toString()));
		}
	}
	
	public Result run_evolution(int fitness_limit, int generation_limit) {
		Result resultObj = new Result();
		List<List<Integer>> currentPopulation = new ArrayList<>();
		
		// population size 10 : 10 randomly generated genomes */
		currentPopulation.addAll(generate_population(10, things.size()));
		
		for (int i = 0; i < generation_limit; i++) {
			
			// sort current population based on fitness score in descending order */
			Collections.sort(currentPopulation, new Comparator<List<Integer>>() {
				@Override
				public int compare(List<Integer> genomeA, List<Integer> genomeB) {
					return Integer.compare(fitness_function(genomeB, things, Constants.WEIGHT_LIMIT), 
											fitness_function(genomeA, things, Constants.WEIGHT_LIMIT));
				}
			});
			
			// TODO: later add print stats function 
			
			System.out.println("generation : " + i);
			print_population(currentPopulation);
			
			resultObj.generation = i;
			resultObj.population = currentPopulation;
			
			// sorting in previous step helps here to check if we got the optimal combination */
			if (fitness_function(currentPopulation.get(0),things, Constants.WEIGHT_LIMIT) >= fitness_limit) break; 
			
			List<List<Integer>> nextGeneration = currentPopulation.subList(0, 2);
			
			System.out.println("currentPopulation size is " + currentPopulation.size());
			int totalLoops = currentPopulation.size() / 2 ;
			System.out.println("totalLoops are " + totalLoops);
			
			for(int j = 0; j < (totalLoops - 1); j++) {
				
				System.out.println("generating nextGen with j value : " + j);
				// select 2 parents from currentPopulation
				List<List<Integer>> parents = selection_pair(currentPopulation);
				System.out.println("parents obtained");
				//System.out.println("====== printing parents ========");
				//print_population(parents);
				
				// obtain two offsprings from parents
				List<List<Integer>> offsprings = single_point_crossover(parents.get(0), parents.get(1));
				System.out.println("offsprings obtained");
				
				//System.out.println("====== printing offsprings ========");
				//print_population(offsprings);
				
				// mutation 1
				offsprings.set(0, mutation(offsprings.get(0), Optional.of(1), Optional.of(0.5f)));
				System.out.println("first mutation done");
				//System.out.println("after 1st mutation : " + String.join(", ", offsprings.get(0).toString()));
				
				// mutation 2
				offsprings.set(1, mutation(offsprings.get(1), Optional.of(1), Optional.of(0.5f)));
				System.out.println("second mutation done");
				//System.out.println("after 2nd mutation : " + String.join(", ", offsprings.get(1).toString()));
				
				// next generation
				nextGeneration.add(offsprings.get(0));
				nextGeneration.add(offsprings.get(1));
			}
			
			System.out.println("nextGeneration obtained");
			List<List<Integer>> newGeneration = new ArrayList<>();
			newGeneration.addAll(nextGeneration);
			currentPopulation.clear();
			currentPopulation.addAll(newGeneration);
		}
		return resultObj;
	}
	
	public static void main(String[] args) {
		GeneticLibrary obj = new GeneticLibrary();
		
		System.out.println("things: with name, weight(grams), value(favorable)");
		for (Map.Entry<String, List<Integer>> thing : obj.things.entrySet()) {
			System.out.print(thing.getKey());
			System.out.println(" : " + String.join(", ", thing.getValue().toString()));
		}
		
		int fitness_limit = 740;
		int generation_limit = 100;
		
		Result resultObj = obj.run_evolution(fitness_limit, generation_limit);
		
		System.out.println("====== Result =======================");
		obj.print_population(resultObj.population);
		System.out.println("generation: " + resultObj.generation);
		
	}
}
