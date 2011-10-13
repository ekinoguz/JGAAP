package com.jgaap.generics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FeatureVectorFactory provides methods for getting known and unknown feature matrices
 * for any analysis method that requires such information.
 * 
 * This class replaces KernelMethodMatrix
 * @author John Noecker Jr.
 *
 */
public class FeatureVectorFactory {
	public static Pair<Double[][], Double[][]> getFeatures(List<EventSet> known, List<EventSet> unknown) {
		Set<Event> vocab = new HashSet<Event>();
		
		// Add all events from known documents to the vocab list.
		for(EventSet eventSet : known) {
			for(Event event : eventSet) {
				vocab.add(event);
			}
		}
		
		// Add all events from unknown documents to the vocab list.
		for(EventSet eventSet: unknown) {
			for(Event event : eventSet) {
				vocab.add(event);
			}
		}
		
		// Construct the known vector
		Double[][] knownSet = new Double[known.size()][vocab.size()];
		for(int i = 0; i < known.size(); i++) {
			EventHistogram histogram = new EventHistogram(known.get(i));
			int j = 0;
			for(Event event : vocab) {
				knownSet[i][j] = (double)histogram.getAbsoluteFrequency(event);
				j++;
			}
		}
		
		// Construct the unknown vector
		Double[][] unknownSet = new Double[unknown.size()][vocab.size()];
		for(int i = 0; i < unknown.size(); i++) {
			EventHistogram histogram = new EventHistogram(unknown.get(i));
			int j = 0;
			for(Event event : vocab) {
				unknownSet[i][j] = (double)histogram.getAbsoluteFrequency(event);
				j++;
			}
		}
		
		return new Pair<Double[][], Double[][]>(knownSet, unknownSet);
	}
}
