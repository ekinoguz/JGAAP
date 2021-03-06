package com.jgaap.classifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.jgaap.generics.AnalyzeException;
import com.jgaap.generics.DistanceCalculationException;
import com.jgaap.generics.Document;
import com.jgaap.generics.EventBagging;
import com.jgaap.generics.EventMap;
import com.jgaap.generics.EventSet;
import com.jgaap.generics.NeighborAnalysisDriver;
import com.jgaap.generics.Pair;

public class BaggingNearestNeighborDriver extends NeighborAnalysisDriver {

	private static Logger logger = Logger
			.getLogger("com.jgaap.classifiers.BaggingNearestNeighborDriver");

	private Map<String,List<EventMap>> authorHistograms;

	public BaggingNearestNeighborDriver() {
		addParams("Samples", "Samples", "5", new String[] { "1", "5", "10",
				"15", "20", "25", "30", "40", "50" }, true);
		addParams("SampleSize", "Sample Size", "500",
				new String[] { "500", "750", "1000", "1500", "2000", "3000",
						"4000", "5000", "10000" }, true);
		addParams("Score", "Score", "False", new String[] {"True", "False"}, false);
	}

	@Override
	public String displayName() {
		return "Bagging Nearest Neighbor";
	}

	@Override
	public String tooltipText() {
		return "Nearest Neighbor using bagging to generated the traing data.";
	}

	@Override
	public boolean showInGUI() {
		return false;
	}

	@Override
	public void train(List<Document> knownDocuments) throws AnalyzeException {
		Map<String, EventBagging> authorBags = new HashMap<String, EventBagging>();
		for(Document knownDocument : knownDocuments)
			for (EventSet eventSet : knownDocument.getEventSets().values()) {
				EventBagging eventBag = authorBags.get(eventSet.getAuthor());
				if (eventBag == null) {
					eventBag = new EventBagging(eventSet);
				} else {
					eventBag.addAll(eventSet);
				}
				authorBags.put(eventSet.getAuthor(), eventBag);
			}
		int samples = getParameter("samples", 5);
		int sampleSize = getParameter("sampleSize", 500);
		authorHistograms = new HashMap<String, List<EventMap>>();
		for (Entry<String, EventBagging> entry : authorBags.entrySet()) {
			List<EventMap> histograms = new ArrayList<EventMap>(samples);
			for (int i = 0; i < samples; i++) {
				EventSet eventSet = new EventSet(sampleSize);
				for (int j = 0; j < sampleSize; j++) {
					eventSet.addEvent(entry.getValue().next());
				}
				EventMap histogram = new EventMap();
				histogram.add(eventSet);
				histograms.add(histogram);
			}
			authorHistograms.put(entry.getKey(), histograms);
		}
	}

	@Override
	public List<Pair<String, Double>> analyze(Document unknownDocument)
			throws AnalyzeException {
		List<Pair<String, Double>> rawResults = new ArrayList<Pair<String, Double>>();
		EventSet unknownEventSet = new EventSet();
		for(EventSet eventSet : unknownDocument.getEventSets().values()){
			unknownEventSet.addEvents(eventSet);
		}
		EventMap unknownHistogram = new EventMap();
		unknownHistogram.add(unknownEventSet);
		for (Entry<String, List<EventMap>> entry : authorHistograms.entrySet()) {
			for (EventMap knownHistogram : entry.getValue()) {
				try {
					rawResults.add(new Pair<String, Double>(entry.getKey(), distance.distance(unknownHistogram, knownHistogram), 2));
				} catch (DistanceCalculationException e) {
					logger.fatal("Distance " + distance.displayName() + " failed", e);
					throw new AnalyzeException("Distance " + distance.displayName() + " failed");
				}
			}
		}
		Collections.sort(rawResults);
		
		if(getParameter("score").equalsIgnoreCase("true")){
			Map<String, Integer> authorScores = new HashMap<String, Integer>();
			int samples = getParameter("samples", 5);
			for(int i = 0; i < samples; i++){
				Integer current = authorScores.get(rawResults.get(i).getFirst());
				if (current == null){
					current = 1;
				} else {
					current ++;
				} 
				authorScores.put(rawResults.get(i).getFirst(), current);
			}
			List<Pair<String, Double>> scoreResults = new ArrayList<Pair<String,Double>>();
			double samplesDouble = samples;
			for(Entry<String, Integer> entry : authorScores.entrySet()){
				scoreResults.add(new Pair<String, Double>(entry.getKey(), entry.getValue()/samplesDouble, 2));
			}
			Collections.sort(scoreResults);
			Collections.reverse(scoreResults);
			return scoreResults;
		} else {
			return rawResults;
		}
	}

}
