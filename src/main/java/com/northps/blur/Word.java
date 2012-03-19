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

import java.util.*;

public class Word implements Comparable<Word> {
    private String value;
    private int distance;

    public Word(String value, int distance) {
        this.value = value;
        this.distance = distance;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public static void main(String[] args) {
        Word a = new Word("inkoo", 2);
        
        char[] b = a.getValue().toCharArray();
    }
    private List<Word> getSubstitutions(int distance) {
        List<Word> returnList = new ArrayList<Word>();

        return returnList;
    }
    public static void addIfNotEmpty(Set container, String str, int distance, String description){
        if (str.length() > 0) {
            if (!container.contains(str)) {
                container.add( new Word(str, distance));
//                System.out.println("Adding new word : [" + description + "] " + str);
            }
        }
    }
    @Override
    // N.B. hash code only depends on value, not distance.
    public int hashCode() {
        //System.out.println("Checking hashcode " + this.value + " = " + this.value.hashCode());
        return (this.getValue()).hashCode();
    }
   
    @Override
    // N.B. equality only depends on value, not distance
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        if (this.getValue().equals(other.getValue())) {
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        return this.getValue();
    }

    public int compareTo(Word t) {
        return (int) (this.getDistance() - t.getDistance());
    }
}
