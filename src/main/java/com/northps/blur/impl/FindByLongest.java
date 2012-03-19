/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Mike Patterson and In Koo
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.northps.blur.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.northps.blur.Fuzzy;
import com.northps.blur.Heuristic;
import com.northps.blur.TrieNode;
import com.northps.blur.Word;
import com.northps.blur.util.QuickSort;

/**
 * @author mikepatterson
 *
 */
public class FindByLongest implements Heuristic {
	
	private static final int INDEL_DISTANCE = 2; // Maximum allowable distance of considered words (number of misses).
    private static final int MAX_LENGTH_MULTIPLIER = 2; // Determines the maximum characters the returned results will have based on this multiplier multiplied by the search query length.
    private static final int ADJUSTED_DISTANCE_MULTIPLIER = 4; // Penalty given to word length.
    private static final int CRITICAL_FUZZY_LENGTH = 3; // Word must be longer than this for fuzzy search to be enabled.
	
	@Override
	public void setTreeHeuristics (TrieNode node) throws Exception {
		if (!node.isRoot()){
			throw new Exception("calcHeuristic must be run on root node");
		}
		node.setHeuristic(0);
		for (TrieNode child : node.getChildren().values()) {
			calcHeuristic(child);
		}
	}
	
	private void calcHeuristic(TrieNode node) {
		if (node.isLeaf()) {
			node.setHeuristic(node.getDepth());
		} else {
			for (TrieNode child : node.getChildren().values()) {
				calcHeuristic(child);
			}
			node.setHeuristic(getMaxChildHeuristic(node));
		}
	}
	
	private int getMaxChildHeuristic(TrieNode node) {
		int max = 0;
		for (TrieNode child : node.getChildren().values()) {
			if (child.getHeuristic() > max) {
				max = child.getHeuristic();
			}
		}
		return max;
	}

	@Override
	public List<String> fuzzySearch(TrieNode node, String query, int count) throws Exception {
        if (query.length() < CRITICAL_FUZZY_LENGTH) {
        	// TODO: Maybe we do regular search here....
        	return new ArrayList <String> ();
        }
		
		List <String> results = null;

		// get all fuzzy word combinations
		List <Word> words = Fuzzy.blur(INDEL_DISTANCE, query);
		
        List <Word> wordRes = new ArrayList <Word> (); // This is our list of results
        Map <String, Integer> noDups = new HashMap <String, Integer> (); // Temp map to assure no duplicate strings.
                
        long start = System.currentTimeMillis();

        // Set up heuristics for this trie
		setTreeHeuristics(node);
		
		// Loop through fuzzy words, getting possible results for each one.
        for (Word word : words) {
        	results = node.findByHeuristic(word.getValue(), count);
        	
        	// Add results for single word to over all list of results
        	for (String result : results) {
        		if (result.length() <= ((query.length() * MAX_LENGTH_MULTIPLIER) + 3)) {
        			Integer i = noDups.get(result);
        			if (i == null) {
        				wordRes.add(new Word(result, word.getDistance()));
        				noDups.put(result, wordRes.size() - 1);
        			} else {
        				if (wordRes.get(i.intValue()).getDistance() > word.getDistance()) {
        					wordRes.set(i.intValue(), new Word(result, word.getDistance()));
        				}
        			}
        		}
        	}
        	results.clear();
        }
        
        long end = System.currentTimeMillis();
//        System.out.println("Gathering results time: " + (end - start));
        
        // Adjust distance based on word length and multiplier
        for (int i = 0; i < wordRes.size(); i++) {
        	Word next = wordRes.get(i);
        	next.setDistance(next.getValue().length() - (next.getDistance() * ADJUSTED_DISTANCE_MULTIPLIER));
        	wordRes.set(i, next);
        }
        
//        System.out.println("size: " + wordRes.size());
//        System.out.println("size without dups: " + noDups.size());
                
        // sort by adjusted distance, largest distance to smallest distance
        start = System.currentTimeMillis();
        QuickSort <Word> sort = new QuickSort <Word> ();
        sort.sort(wordRes);
        
        end = System.currentTimeMillis();
//        System.out.println("Sorting time: " + (end - start));

//        System.out.println(">>> results:");
        for (Word next : wordRes) {
//        	System.out.println("val: " + next.getValue() + ", dist: " + next.getDistance());
        	results.add(next.getValue());
        	if (results.size() >= count) {
        		break;
        	}
        }
		return results;
	}
	
	
	
}
