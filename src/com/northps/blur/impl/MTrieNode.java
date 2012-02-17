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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.northps.blur.Heuristic;
import com.northps.blur.TrieNode;

public class MTrieNode implements TrieNode {
   private final Character value;
   private final Map<Character, TrieNode> children;
   private static int guid = 0;
   private final int id;
   private int heuristic = 0;
   private int depth = 0;
   private boolean isHeuristicSet = false;

   private static synchronized int getGuid() {
       guid++;
       return guid;
   }
   
   public MTrieNode() {
	   this.id = getGuid();
	   this.children = new HashMap <Character, TrieNode> ();
	   this.value = null;
	   this.depth = 0;
	   //System.out.println("Creating TrieNode " + this.id + " for root");
   }

   private MTrieNode(String word, int depth) {
       this.id = getGuid();
       this.depth = depth;
       this.children = new HashMap<Character, TrieNode>();
       //System.out.println("Creating TrieNode " + this.id +  " for " + word);
       int wordLength = word.length();
       if (wordLength == 0) {
           throw new IllegalArgumentException("Trie nodes must have a non-empty string.");
       }
       this.value = word.charAt(0);
       if (wordLength > 1) {
           attach(word);
       }
   }

   public void attach(String word) {
       if (word.length() > 1) {
    	   if (this.isRoot()) {
    		   Character key = Character.valueOf(word.charAt(0));
    		   MTrieNode  node = (MTrieNode) this.children.get(key);
    		   if (node != null) {
    			   node.attach(word);
    		   } else {
    			   this.children.put(key, new MTrieNode(word, this.depth + 1));
    		   }
    	   }
    	   else {
    		   Character key = Character.valueOf(word.charAt(1));
    		   String value = String.valueOf(word.substring(1).toCharArray());
    		   if (value.length() > 0) {
    			   MTrieNode node = (MTrieNode) this.children.get(key);
    			   if (node != null) {
    				   node.attach(value);
    			   } else {
    				   MTrieNode newNode = new MTrieNode(value, this.depth + 1);
    				   this.children.put(key, newNode);
    			   }
    		   }
    	   }
       }
   }

   public void loadDictionary(File dictionary) throws Exception {
	   try {
		   BufferedReader is = null;
		   try {
			   is = new BufferedReader (new FileReader(dictionary));
			   String line = null;
			   while ((line = is.readLine()) != null) {
				   this.attach(line.toLowerCase());
			   }
		   } finally {
			   is.close();
		   }
	   } catch (FileNotFoundException e) {
		   e.printStackTrace();
		   throw e;
	   } catch (IOException e) {
		   e.printStackTrace();
		   throw e;
	   }
   } 
   
   public List <String> findByHeuristic(String word) {
	   return this.findByHeuristic(word, 0);
   }
   
   public List <String> findByHeuristic(String word, int maxResults) {
	   List <Character> queue = new LinkedList <Character> ();
	   List <String> results = new ArrayList <String> ();
	   if (word.length() == 0) {
		   return results;
	   }
	   
	   if (!this.isHeuristicSet()){
		   // Set default heuristic to Find By Longest if not set.
		   Heuristic def = new FindByLongest();
		   try {
			   def.setTreeHeuristics(this);
		   } catch (Exception e) {
			   e.printStackTrace();
		   }
	   }

	   gatherResultsByHeuristic(word.toLowerCase(), queue, results, maxResults);
	   return results;
   }
   
   // Gather results by largest heuristic as defined by func.
   private void gatherResultsByHeuristic(String word, List <Character> queue, List <String> results, int maxResults) {
	   List <Character> locQ = new LinkedList <Character> (queue);
	   if (word.length() == 1 && !this.isRoot()){
		   if(!this.isLeaf()) {
			   Map <Integer, Integer> heuristicMap = new HashMap <Integer, Integer> ();
			   this.addChildrenToHeuristicMap(heuristicMap);
			   while (this.hasMaxChildHeuristic(heuristicMap)) {
				   this.findByHeuristic(queue, results, heuristicMap);
				   if (maxResults > 0 && results.size() == maxResults) {
					   return;
				   }
			   }
		   }
	   } else {
		   if(this.isRoot()){
			   Character key = Character.valueOf(word.charAt(0));
			   MTrieNode node = (MTrieNode) this.children.get(key);
			   if (node != null) {
				   node.gatherResultsByHeuristic(word, locQ, results, maxResults);
			   }
		   } else {
			   locQ.add(this.value);
			   Character key = Character.valueOf(word.charAt(1));
			   String value = String.valueOf(word.substring(1).toCharArray());
			   MTrieNode node = (MTrieNode) this.children.get(key);
			   if (node != null) {
				   node.gatherResultsByHeuristic(value, locQ, results, maxResults);
			   }
		   }
	   }
   }
   
   private void findByHeuristic(List <Character> queue, List <String> results, Map <Integer, Integer> heuristicMap) {
	   List <Character> locQ = new LinkedList <Character> (queue);
	   locQ.add(this.value);
	   if(this.isLeaf()) {
		   StringBuffer buf = new StringBuffer();
		   while (!locQ.isEmpty()) {
			   buf.append(locQ.remove(0));
		   }
		   results.add(buf.toString());
		   heuristicMap.put(Integer.valueOf(this.id), new Integer(0));
	   } else if (!this.hasMaxChildHeuristic(heuristicMap)) {
		   heuristicMap.put(Integer.valueOf(this.id), new Integer(0));
	   } else {
		   Character maxChildHeuristicKey = this.getMaxChildHeuristicKey(heuristicMap);
		   MTrieNode max = (MTrieNode) this.children.get(maxChildHeuristicKey);
		   if (max != null) {
			   	max.addChildrenToHeuristicMap(heuristicMap);
				max.findByHeuristic(locQ, results, heuristicMap);
				if (!this.hasMaxChildHeuristic(heuristicMap)) {
					heuristicMap.put(Integer.valueOf(this.id), new Integer(0));
				} else {
					heuristicMap.put(Integer.valueOf(this.id), this.getMaxChildHeuristic(heuristicMap));
				}
		   }
	   }
   }
   
   // Add node's children's heuristic to the heuristic map if it has not yet been added
   private void addChildrenToHeuristicMap(Map <Integer, Integer> heuristicMap) {
	   for (TrieNode child : this.children.values()) {
		   Integer weight = heuristicMap.get(Integer.valueOf(child.getId()));
		   if (weight == null) {
			   try {
				   heuristicMap.put(Integer.valueOf(child.getId()), Integer.valueOf(child.getHeuristic()));
			   } catch (Exception e) {
				   e.printStackTrace();
			   }
		   }
	   }
   }
   
   // Get the max heuristic of the current children from the heuristic map
   private int getMaxChildHeuristic(Map <Integer, Integer> heuristicMap) {
	   int maxWeight = 0;
	   for (TrieNode child : this.children.values()) {
		   Integer weight = heuristicMap.get(Integer.valueOf(child.getId()));
		   if (weight != null && weight > maxWeight) {
			   maxWeight = weight;
		   }
	   }
	   return maxWeight;
   }
   
   // Get the key for the child with the max heuristic from the weight map
   private Character getMaxChildHeuristicKey(Map <Integer, Integer> heuristicMap) {
	   Integer maxWeight = 0;
	   Character maxWeightKey = null;   
	   for (TrieNode child : this.children.values()) {
		   Integer weight = heuristicMap.get(Integer.valueOf(child.getId()));
		   if (weight != null && weight > maxWeight) {
			   maxWeight = weight;
			   maxWeightKey = child.getValue();
		   }
		   else if (weight != null && maxWeightKey == null) {
			   maxWeightKey = child.getValue();
		   }
	   }
	   return maxWeightKey;
   }
   
   // Check if the current node has any children that have a heuristic greater than 0.
   private boolean hasMaxChildHeuristic(Map <Integer, Integer> heuristicMap) {
	   Integer maxWeight = 0;
	   for (TrieNode child : this.children.values()) {
		   Integer weight = heuristicMap.get(Integer.valueOf(child.getId()));
		   if (weight != null && weight > maxWeight) {
				   maxWeight = weight;
		   }
	   }
	   return maxWeight > 0;
   }

   public Character getValue() {
	   return this.value;
   }

   public int getDepth() {
	   return this.depth;
   }

   public Map<Character, TrieNode> getChildren() {
	   return this.children;
   }

   public int getId() {
	   return this.id;
   }

   public int getHeuristic() {
	   return this.heuristic;
   }
   
   public void setHeuristic(int heuristic) {
	   this.heuristic = heuristic;
	   this.isHeuristicSet = true;
   }

   public String toString() {
	   return this.value.toString();
   }
   
   public boolean isLeaf() {
	   return this.children.size() == 0;
   }
   
   public boolean isRoot() {
	   return this.value == null;
   }

   /**
    * @return the isHeuristicSet
    */
   public boolean isHeuristicSet() {
	   return isHeuristicSet;
   }
}

