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
/**
 * 
 */
package com.jgaap.eventDrivers;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

import com.jgaap.generics.Document;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventSet;

/**
 * @author darrenvescovi
 *  
 *	http://www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html
 */
public class PartOfSpeechEventDriverTest {

	/**
	 * Test method for {@link com.jgaap.eventDrivers.PartOfSpeechEventDriver#createEventSet(com.jgaap.generics.JGAAP)}.
	 */
	@Test
	public void testCreateEventSetDocumentSet() {
		System.out.println("Test Started");
		Document doc = new Document();
		String text = "Today the fox jumped over the lazy dog, "
			+"while the fox jumped over the lazy dog a cat ran under a truck.  "
			+"The truck missed the cat: the lazy dog wasn't so lazy and caught the cat";
		doc.readStringText(text);
		//System.out.println("Sample text:\n"+text);

		EventSet sampleSet = new PartOfSpeechEventDriver().testCreateEventSet(doc);
		//System.out.println(sampleSet.size());

		EventSet expectedSet = new EventSet();
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("VBD"));
		expectedSet.addEvent(new Event("IN"));
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("JJ"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event(","));

		expectedSet.addEvent(new Event("IN"));
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("VBD"));
		expectedSet.addEvent(new Event("IN"));
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("JJ"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("VBD"));//
		expectedSet.addEvent(new Event("IN"));
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("."));

		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event("VBD"));//
		expectedSet.addEvent(new Event("DT"));
		expectedSet.addEvent(new Event("NN"));
		expectedSet.addEvent(new Event(":"));
		expectedSet.addEvent(new Event("DT")); //the
		expectedSet.addEvent(new Event("JJ")); //lazy
		expectedSet.addEvent(new Event("NN")); //dog
		expectedSet.addEvent(new Event("VBD")); //was
		expectedSet.addEvent(new Event("RB")); //n't
		expectedSet.addEvent(new Event("RB")); //so
		expectedSet.addEvent(new Event("JJ")); //lazy
		expectedSet.addEvent(new Event("CC")); //and
		expectedSet.addEvent(new Event("VBD")); //caught : Changed from VBN
		expectedSet.addEvent(new Event("DT")); //the
		expectedSet.addEvent(new Event("NN")); //cat

		//Print out just the classified parts of speech
		/*System.out.println("Parts of Speech:");
		for(Event e : sampleSet){
			System.out.print(e+" ");
		}
		System.out.println();*/

		//Print out events which do not match
		/*if(expectedSet.size() == sampleSet.size()){
			for(int i = 0; i < expectedSet.size(); i++){
				if(!expectedSet.eventAt(i).equals(sampleSet.eventAt(i))){
					System.out.println("Events at "+i+" do not equal: " + expectedSet.eventAt(i) + " and " + sampleSet.eventAt(i));
				}
			}
		}*/

		//Print out the pair of classified parts of speech and the expected part of speech
		/*System.out.println("Expected and Classified");
		for(int i = 0; i < expectedSet.size(); i++){
			System.out.println("Events at "+i+": " + expectedSet.eventAt(i) + " and " + sampleSet.eventAt(i));
		}*/

		assertTrue(expectedSet.equals(sampleSet));
	}

}
