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

package com.northps.blur;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import com.northps.blur.impl.FindByLongest;
import com.northps.blur.impl.MTrieNode;

/**
 * @author mikepatterson
 *
 */
public class TrieNodeTest {

	// @Test
	// public void testFindByShortest() throws Exception {
	//
	// MTrieNode root = new MTrieNode();
	// Heuristic func = new FindByShortest();
	//
	// System.out.println(">>> Testing " + root.getClass().getName());
	//
	// URL dictionaryURL = this.getClass().getClassLoader()
	// .getResource("dict.txt");
	// try {
	// root.loadDictionary(new File(dictionaryURL.toURI()));
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw e;
	// }
	//
	// String query = null;
	// int count = 20;
	//
	// query = "hap";
	//
	// long before = System.currentTimeMillis();
	//
	// List <String> results = func.fuzzySearch(root, query, count);
	//
	// long now = System.currentTimeMillis();
	//
	// System.out.println("\n>>> time: " + (now - before));
	//
//        System.out.println(">>> results:");
//        for (String next : results) {
	// System.out.println(next);
//        }
//
//
	// // query = "H";
	// // results = root.findByHeuristic(query, func, count);
	// // System.out.println(">>> results:");
	// // for (String next : results) {
	// // System.out.println(next);
	// // }
	// // query = "ha";
	// // results = root.findByHeuristic(query, func, count);
	// // System.out.println(">>> results:");
	// // for (String next : results) {
	// // System.out.println(next);
	// // }
	// //
	// // query = "Ha";
	// // results = root.findByHeuristic(query, func, count);
	// // System.out.println(">>> results:");
	// // for (String next : results) {
	// // System.out.println(next);
	// // }
	// //
	// before = System.currentTimeMillis();
	// func.setTreeHeuristics(root);
	// for (int i = 0; i < 5000; i++) {
	// root.findByHeuristic("a", 20);
	// root.findByHeuristic("H", 20);
	// root.findByHeuristic("ha", 20);
	// root.findByHeuristic("Ha", 20);
	// if (i % 1000 == 0 && i != 0) {
	// System.out.print(".");
	// }
	// }
	//
	// now = System.currentTimeMillis();
	//
	// System.out.println("\n>>> time: " + (now - before));
	// }
	
	@Test
	public void testFindByLongest() throws Exception {

		MTrieNode root = new MTrieNode();
		Heuristic func = new FindByLongest();

		System.out.println(">>> Testing " + root.getClass().getName());

		URL dictionaryURL = this.getClass().getClassLoader()
				.getResource("dict.txt");
		try {
			root.loadDictionary(new File(dictionaryURL.toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		String query = null;
		int count = 20;

		query = "hap";

		long before = System.currentTimeMillis();

		List<String> results = func.fuzzySearch(root, query, count);

		long now = System.currentTimeMillis();

		System.out.println("\n>>> time: " + (now - before));

		System.out.println(">>> results:");
		for (String next : results) {
			System.out.println(next);
		}

		// query = "H";
		// results = root.findByHeuristic(query, func, count);
		// System.out.println(">>> results:");
		// for (String next : results) {
		// System.out.println(next);
		// }
		// query = "ha";
		// results = root.findByHeuristic(query, func, count);
		// System.out.println(">>> results:");
		// for (String next : results) {
		// System.out.println(next);
		// }
		//
		// query = "Ha";
		// results = root.findByHeuristic(query, func, count);
		// System.out.println(">>> results:");
		// for (String next : results) {
		// System.out.println(next);
		// }
		//
		before = System.currentTimeMillis();
		func.setTreeHeuristics(root);
		for (int i = 0; i < 5000; i++) {
			root.findByHeuristic("a", 20);
			root.findByHeuristic("H", 20);
			root.findByHeuristic("ha", 20);
			root.findByHeuristic("Ha", 20);
			if (i % 1000 == 0 && i != 0) {
				System.out.print(".");
			}
		}

		now = System.currentTimeMillis();

		System.out.println("\n>>> time: " + (now - before));
	}

	@Test
	public void testNode() throws Exception {
		
		MTrieNode root = new MTrieNode();
		URL dictionaryURL = this.getClass().getClassLoader()
				.getResource("dict.txt");
		try {
			root.loadDictionary(new File(dictionaryURL.toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

        List <String> results = null;
        String query = null;
        int count = 20;

        query = "a";
        results = root.findByHeuristic(query, count);
        System.out.println(">>> results:");
        for (String next : results) {
            System.out.println(next);
        }

        query = "H";
        results = root.findByHeuristic(query, count);
        System.out.println(">>> results:");
        for (String next : results) {
            System.out.println(next);
        }
        query = "ha";
        results = root.findByHeuristic(query, count);
        System.out.println(">>> results:");
        for (String next : results) {
            System.out.println(next);
        }

        query = "Ha";
        results = root.findByHeuristic(query, count);
        System.out.println(">>> results:");
        for (String next : results) {
            System.out.println(next);
        }

        long before = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
			root.findByHeuristic("a", 20);
			root.findByHeuristic("H", 20);
			root.findByHeuristic("ha", 20);
			root.findByHeuristic("Ha", 20);  
			if (i % 1000 == 0 && i != 0) {
				System.out.print(".");
			}
		}

        long now = System.currentTimeMillis();

		System.out.println("\n>>> time: " + (now - before));
	}
}
