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

import java.util.List;

import com.northps.blur.Heuristic;
import com.northps.blur.TrieNode;

/**
 * @author mikepatterson
 *
 */
public class FindByShortest implements Heuristic {

	private static final int PRECISION_MULTIPLIER = Integer.MAX_VALUE; // maximum depth = square root of Integer.MAX_VALUE (46340).

	@Override
	public void setTreeHeuristics (TrieNode node) throws Exception {
		if (!node.isRoot()){
			throw new Exception("calcHeuristic must be run on root node");
		}
		
		for (TrieNode child : node.getChildren().values()) {
			calcHeuristic(child);
		}
	}
	
	private void calcHeuristic(TrieNode node) {
		if (node.isLeaf()) {
			node.setHeuristic(PRECISION_MULTIPLIER / node.getDepth());
		} else { 
			for (TrieNode child : node.getChildren().values()) {
				calcHeuristic(child);
			}
			node.setHeuristic(getMinChildHeuristic(node));
		}
	}
	
	private int getMinChildHeuristic(TrieNode node) {
		int min = 0;
		for (TrieNode child : node.getChildren().values()) {
			if (child.getHeuristic() > min) {
				min = child.getHeuristic();
			}
		}
		return min;
	}

	@Override
	public List<String> fuzzySearch(TrieNode node, String query, int count)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
