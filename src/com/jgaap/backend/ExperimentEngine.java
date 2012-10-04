/*
 * JGAAP -- a graphical program for stylometric authorship attribution
 * Copyright (C) 2009,2011 by Patrick Juola
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jgaap.backend;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.jgaap.JGAAPConstants;
import com.jgaap.generics.*;

/**
 * Experiment Engine This class takes a csv file of experiments and then will
 * run them one after the other and generates result files in the tmp directory
 * 
 * @author Mike Ryan
 */
public class ExperimentEngine {
	
	static Logger logger = Logger.getLogger(ExperimentEngine.class);
	
	private static final int workers = 2;
	
	/**
	 * This method generates unique file names and a directory structure to save
	 * the results of an experiment run
	 * 
	 * @param canons
	 *            the canonicizors used
	 * @param event
	 *            the event used
	 * @param analysis
	 *            the analysis method or distance function used
	 * @param experimentName
	 *            the given name of this experiment specified on the top line of
	 *            the experiment csv file
	 * @param number
	 *            the identifier given to this experiment
	 * @return the location of where the file will be written
	 */
	public static String fileNameGen(List<String> canons, String event,
			String[] eventCullers, String analysis, String experimentName,
			String number) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = new java.util.Date();
		Iterator<String> iterator = canons.iterator();
		StringBuilder canonNameBuilder = new StringBuilder();
		canonNameBuilder.append((iterator.hasNext() ? iterator.next() : "none"));
		while (iterator.hasNext()) {
			canonNameBuilder.append(" ").append(iterator.next().trim());
		}
		String canonName = canonNameBuilder.toString();
		boolean first = true;
		StringBuilder cullerNameBuilder = new StringBuilder();
		for (String eventCuller : eventCullers) {
			if (!first) {
				cullerNameBuilder.append(" ");
			}
			cullerNameBuilder.append(eventCuller.trim());
			first = false;
		}
		String cullerName = cullerNameBuilder.toString();
		if ("".equals(cullerName)) {
			cullerName = "none";
		}
		String path = JGAAPConstants.JGAAP_TMPDIR
		+ canonName.replace("/", "\\/") + "/"
		+ event.trim().replace("/", "\\/") + "/"
		+ cullerName.replace("/", "\\/") + "/"
		+ analysis.trim().replace("/", "\\/") + "/";
		File file = new File(path);
		boolean newDirs = file.mkdirs();
		if(!newDirs) {
			; // Nothing (check added to satisfy static analysis / show we are aware of this)
		}
		return (path + experimentName + number + dateFormat.format(date) + ".txt");
	}

	/**
	 * This method will iterate a the rows of a csv file of experiments running
	 * jgaap on each one and then generate a results file for it
	 * 
	 * @param listPath
	 *            the location of the csv file of experiments
	 */

	public static void runExperiment(String listPath) {
		try {
			runExperiment(CSVIO.readCSV(listPath));
		} catch (IOException e) {
			logger.fatal("Problem processing experiment file: "+listPath, e);
		}

	}

	public static void runExperiment(List<List<String>> experimentTable) {
		final String experimentName = experimentTable.remove(0).get(0);
		ExecutorService experimentExecutor = Executors.newFixedThreadPool(workers);
		for (final List<String> experimentRow : experimentTable) {
			if(experimentRow.isEmpty()){
				continue;
			}else if (experimentRow.size() >= 7) {
				Runnable work = new Runnable() {
					@Override
					public void run() {
						String number = experimentRow.get(0);
						List<String> canons = new ArrayList<String>();
						if (!"".equalsIgnoreCase(experimentRow.get(1).trim())) {
							String[] canonicizers = experimentRow.get(1).split("\\|");
							canons = new ArrayList<String>();
							for (String current : canonicizers) {
								canons.add(current.trim());
							}
						}
						String event = experimentRow.get(2);
						String[] eventCullers = experimentRow.get(3).split("\\|");
						String analysis = experimentRow.get(4);
						String distance = experimentRow.get(5).trim();
						String documentsPath = experimentRow.get(6);
						String fileName = fileNameGen(canons, event,
								eventCullers, analysis+(distance.isEmpty()?"":"-"+distance), experimentName, number);
						API experiment = API.getPrivateInstance();
						try {
							List<List<String>> tmp;
							if(documentsPath.startsWith(JGAAPConstants.JGAAP_RESOURCE_PACKAGE)){
								tmp = CSVIO.readCSV(com.jgaap.JGAAP.class.getResourceAsStream(documentsPath));
							} else {
								tmp = CSVIO.readCSV(documentsPath);
							}
							List<Document> documents = Utils.getDocumentsFromCSV(tmp);
							for (Document document : documents) {
								experiment.addDocument(document);
							}
							for (String canonicizer : canons) {
								experiment.addCanonicizer(canonicizer);
							}
							EventDriver eventDriver = experiment.addEventDriver(event);
							for (String eventCuller : eventCullers) {
								if (eventCuller != null
										&& !"".equalsIgnoreCase(eventCuller))
									experiment.addEventCuller(eventCuller
											.trim());
							}
							AnalysisDriver analysisDriver = experiment
									.addAnalysisDriver(analysis);
							if(!distance.isEmpty()){
								experiment.addDistanceFunction(distance, analysisDriver);
							}
							experiment.execute();
							List<Document> unknowns = experiment
									.getUnknownDocuments();
							StringBuffer buffer = new StringBuffer();
							for (Document unknown : unknowns) {
								buffer.append(unknown.getFormattedResult(analysisDriver, eventDriver));
							}
							Utils.saveFile(fileName, buffer.toString());
						} catch (Exception e) {
							logger.error("Could not run experiment "+experimentRow.toString(),e);
						}
					}
				};
				experimentExecutor.execute(work);
			} else {
				logger.error("Experiment "+experimentRow.toString()+" missing "+(7-experimentRow.size())+" column(s)");
			}
		}
		experimentExecutor.shutdown();
		try {
			experimentExecutor.awaitTermination(30, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logger.error("Experiment Executor interrupted", e);
		}
		
	}

}
