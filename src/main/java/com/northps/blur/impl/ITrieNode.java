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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.northps.blur.Heuristic;
import com.northps.blur.TrieNode;

public class ITrieNode implements TrieNode {
    private final Character value;
    private final HashMap<Character, ITrieNode> children;
    private static int guid = 0;
    private final int id;
    private int weight = 0;
    private final int depth;
    private HashMap<Integer, Integer> frontierWeightMap = null;
    
    private static final boolean DEBUG = false;

    private static synchronized int getGuid() {
        guid++;
        return guid;
    }

    public ITrieNode() {
        this.id = getGuid();
        this.children = new HashMap <Character, ITrieNode> ();
        this.value = null;
        this.depth = 0;
        this.frontierWeightMap = new HashMap <Integer, Integer>();
    }

    private ITrieNode(String word, int depth, HashMap<Integer, Integer> frontierWeights) {
        this.id = getGuid();
        this.depth = depth;
        this.children = new HashMap<Character, ITrieNode>();
        int wordLength = word.length();
        if (wordLength == 0) {
            throw new IllegalArgumentException("Trie nodes must have a non-empty string.");
        }
        this.value = word.charAt(0);
        if (wordLength > 1) {
            int retWeight = attach(word, frontierWeights);
            updateWeight(retWeight, frontierWeights);
        } else {
            setWeight(depth);
            frontierWeights.put(getIntegerId(), Integer.valueOf(depth));
        }

        if(DEBUG) {
            System.out.println("Creating TrieNode2 " + this.id +  " for [" + word.charAt(0) + "]" + word.substring(1) + " at depth " + this.depth + " and weight " + this.weight);
        }

    }
    
    private void updateWeight(int weight, HashMap<Integer, Integer> frontierWeights) {
        if (weight > getWeight()) {
            setWeight(weight);
            frontierWeights.put(getIntegerId(), Integer.valueOf(getWeight()));
        }
    }
    
    public void attach (String word) { 
        if(!this.isRoot()) {
            throw new IllegalStateException("Cannot attach word " + word + " to node " + getIntegerId() + " because it is not the root node.");
        }
        attach(word.toLowerCase(), getFrontierWeightMap());
    }
    
    private int attach(String word, HashMap<Integer, Integer> frontierWeights) {
        if (word.length() > 1) {
            if (this.isRoot()) {
                Character key = Character.valueOf(word.charAt(0));
                ITrieNode  node = this.children.get(key);
                if (node != null) {
                    updateWeight(node.attach(word, frontierWeights), frontierWeights);
                } else {
                    this.children.put(key, new ITrieNode(word, getDepth() + 1, frontierWeights));
                }
            } else {
                Character key = Character.valueOf(word.charAt(1));
                String value = String.valueOf(word.substring(1).toCharArray());
                if (value.length() > 0) {
                    ITrieNode node = this.children.get(key);
                    if (node != null) {
                        updateWeight(node.attach(value, frontierWeights), frontierWeights);
                    } else {
                        ITrieNode newNode = new ITrieNode(value, getDepth() + 1, frontierWeights);
                        updateWeight(newNode.weight, frontierWeights);
                        this.children.put(key, newNode);
                    }
                }
            }
        }
        return getWeight();
    }

    private List <String> find(String word, int maxCount) {
        if (!this.isRoot()) {
            throw new IllegalStateException("Can only call find from the root node.");
        }
        if (DEBUG) {
            System.out.println("Search term : " + word);
        }

        long before = System.currentTimeMillis();

        // store return results of longest words
        List<String> returnList = new ArrayList<String>();

        // disposable frontier map
        HashMap<Integer,Integer> frontier = (HashMap<Integer,Integer>) getFrontierWeightMap().clone();

        ITrieNode matchingRootNode = this.findMatchingRootNode(word, frontier);
        if (matchingRootNode == null || matchingRootNode.id == this.id) {
            return returnList;
        }
        String prefix = String.valueOf(word.substring(0, word.length()-1).toCharArray());
        String found = null;

        if (DEBUG) {
            printFrontier(frontier);
        }
        do {
            found = matchingRootNode.findLongestWord(maxCount, frontier);
            if (found != null && found.length() > 0) {
                if (DEBUG) {
                    System.out.println("Adding [" + prefix + found + "]");
                    printFrontier(frontier);
                }
                returnList.add(prefix + found);
            }
        } while (found != null  && found.length() > word.length() && returnList.size() < maxCount);
        return returnList;
    }
    private ITrieNode findMatchingRootNode(String word, HashMap<Integer,Integer> tmpFrontier) {
        ITrieNode returnNode = null;
        if (word != null && word.length() > 0) {
            char searchChar = word.charAt(0);
            boolean foundIt = false;
            for (ITrieNode t: this.children.values()) {
                if (!foundIt) {
                    if (t.value.charValue() == searchChar) {
                        if (word.length() > 1) {
                            returnNode =  t.findMatchingRootNode(word.substring(1), tmpFrontier);
                        } else {
                            returnNode = t;
                        }
                        foundIt = true;
                    }
                }
            }
        }
        return returnNode;
    }
    
    private String findLongestWord(int maxcount, HashMap<Integer,Integer> frontierMap) {
        if (DEBUG) {
            System.out.println("Finding inside " + getIntegerId() + ", " + this.value);
        }
        ITrieNode[] maxChildren = getMaxChild(frontierMap);
        ITrieNode maxChild = null;
        ITrieNode secondChild = null;

        if (maxChildren != null && maxChildren.length > 0) {
            maxChild = maxChildren[0];
            if  (maxChildren.length > 1) {
                secondChild = maxChildren[1];
            }
        }
        StringBuffer ret = new StringBuffer();
        if (this.value != null) {
            ret.append(this.value);
        }
        
        if (maxChild != null) {
            ret.append(maxChild.findLongestWord(maxcount, frontierMap));

            if (maxChild.isLeaf(frontierMap)) {
                if (DEBUG) {
                    System.out.println(maxChild.id + " IS leaf");
                }
                // remove leaf from frontier
                frontierMap.remove(Integer.valueOf(maxChild.id));

                if (secondChild != null) {
                    // set new node weight in second child frontier
                    if (DEBUG) {
                        System.out.println("Changing node " + getIntegerId() + " frontier weight from " + frontierMap.get(getIntegerId()) + " to " + secondChild.getWeight(frontierMap));
                    }

                    updateFrontier(frontierMap, getIntegerId(), secondChild.getWeight(frontierMap));
                } else {
//     updateFrontier(frontierMap, this.id, 0);
                    frontierMap.remove(Integer.valueOf(this.id));
// ????????
                }
            } else {
                if (DEBUG) {
                    System.out.println(maxChild.id + " is not leaf");
                }
                ITrieNode[] newMaxChildren = maxChild.getMaxChild(frontierMap);
                if (newMaxChildren != null) {
                    ITrieNode newMaxChild = newMaxChildren[0];
                    int newMaxChildWeight = newMaxChild.getWeight(frontierMap);
                    if (DEBUG) {
                        System.out.println("New max child " + newMaxChild.id + " weight " + newMaxChildWeight);
                    }
                    updateFrontier(frontierMap, maxChild.id, newMaxChildWeight);
                    if (secondChild != null) {
                        int secondChildWeight = secondChild.getWeight(frontierMap);
                        if (secondChildWeight > newMaxChildWeight) {
                            // move node weight to new child
                            updateFrontier(frontierMap, this.id, secondChildWeight);
//                            frontierMap.put(Integer.valueOf(this.id), Integer.valueOf(secondChildWeight));
                        }
                    } else {
                        // no second child so set to new weight
                        updateFrontier(frontierMap, this.id, newMaxChildWeight);
                    }
                }
            }
        }
        return ret.toString();
    }
    
    private void updateFrontier(HashMap<Integer,Integer> frontierMap, final Integer id, int newWeight) {
        frontierMap.remove(id);
        if (DEBUG) {
            System.out.println("Setting " + id  + " to new weight " + newWeight);
        }
        frontierMap.put(id, Integer.valueOf(newWeight));
    }
    private int getWeight(final HashMap<Integer,Integer> frontierMap) {
        Integer frontierWeight = frontierMap.get(getIntegerId());
        if (frontierWeight == null) {
            return 0;
        }
        return frontierWeight.intValue();
    }
    private ITrieNode[] getMaxChild(final HashMap<Integer,Integer> frontierMap) {
        // returns two largest weight nodes in order
        HashMap<Character, ITrieNode> availableChildren = getAvailableChildren(frontierMap);
        if (availableChildren == null) {
            return null;
        }
        ITrieNode returnNode = null;
        ITrieNode secondNode = null;

        for (ITrieNode x : this.children.values()) {
            if (
                returnNode == null
                || (
                    frontierMap.containsKey(Integer.valueOf(x.id))
                    && frontierMap.get(Integer.valueOf(x.id)).intValue() > returnNode.getWeight(frontierMap)
                    )
                ) {
                secondNode = returnNode;
                returnNode = x;
            }
        }


        return new ITrieNode[] {returnNode, secondNode};
    }

    private HashMap<Character, ITrieNode> getAvailableChildren(HashMap<Integer,Integer> frontierMap) {
        if (this.children == null || this.children.size() == 0) {
            return null;
        }
        HashMap<Character, ITrieNode> returnMap = new HashMap<Character, ITrieNode>(this.children.size());
        for (ITrieNode t: this.children.values()) {
            if (frontierMap.containsKey(t.id)) {
                returnMap.put(t.value, t);
            }
        }
        if (returnMap.size() == 0) {
            return null;
        }
        return returnMap;
    }

    public Integer getIntegerId() {
        return Integer.valueOf(this.id);
    }
    public int getId() {
        return Integer.valueOf(this.id);
    }

    public int getWeight() {
        return this.weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }
    public Character getValue() {
        return this.value;
    }
    public int getDepth() {
        return this.depth;
    }
    public HashMap<Integer, Integer> getFrontierWeightMap() {
        return this.frontierWeightMap;
    }
    public void setFrontierWeightMap(HashMap<Integer, Integer>frontierWeightMap) {
        this.frontierWeightMap = frontierWeightMap;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
    

    // helper methods
    private boolean isLeaf(HashMap<Integer,Integer> frontierMap) {
        if (this.children == null || this.children.size() == 0) {
            return true;
        }
        for (ITrieNode t: this.children.values()) {
            if (frontierMap.containsKey(Integer.valueOf(t.id))) {
                return false;
            }
        }
        return true;
    }
   
    private void printFrontier(HashMap<Integer,Integer> frontier) {
        if (!this.isRoot()) {
            throw new IllegalStateException("Can only call printFrontier from the root node.");
        }
        for (ITrieNode t: this.children.values()) {
            printNode(t, frontier);
        }
    }
    private void printNode(ITrieNode node, HashMap<Integer,Integer> frontier) {
        StringBuffer msg = new StringBuffer(String.valueOf(node.id));
        msg.append(" ").append(node.value).append(" weight ").append(node.weight);
        msg.append("=>").append(node.getWeight(frontier));
        System.out.println(msg.toString());
        if (this.children == null) {
            return;
        }
        for (ITrieNode t: node.children.values()) {
            printNode(t, frontier);
        }
    }

	public void loadDictionary(File dictionary) throws Exception {
		try {
			BufferedReader is = null;
			try {
				is = new BufferedReader (new FileReader(dictionary));
				String line = null;
				while ((line = is.readLine()) != null) {
					this.attach(line);
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

	public boolean isLeaf() {
		if (this.children == null || this.children.size() == 0) {
            return true;
        }
        return false;
	}

	public boolean isRoot() {
        return this.value == null;
	}

	public Map<Character, TrieNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDefaultHeuristic() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<String> findByHeuristic(String word) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> findByHeuristic(String word, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> findByHeuristic(String word, Heuristic func) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> findByHeuristic(String word, Heuristic func,
			int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getHeuristic() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setHeuristic(int heuristic) {
		// TODO Auto-generated method stub

	}

}
