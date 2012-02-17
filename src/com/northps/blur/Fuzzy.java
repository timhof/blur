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

public class Fuzzy {
    public static final char DELETE = '\u007F';
    public static final char[] ALPHABET = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        ' ', '-','\''
    };

    public static final List<String> ALPHABET_COMBINATIONS_1 = new ArrayList();
    public static final List<String> ALPHABET_COMBINATIONS_2 = new ArrayList();
    public static final List<String> ALPHABET_COMBINATIONS_3 = new ArrayList();

    static {
        for (int i=0; i<ALPHABET.length; i++) {
            ALPHABET_COMBINATIONS_1.add(String.valueOf(ALPHABET[i]));
            for (int j=i; j<ALPHABET.length; j++) {
                ALPHABET_COMBINATIONS_2.add(String.valueOf(ALPHABET[i]) + String.valueOf(ALPHABET[j]));
                for (int k=j; k<ALPHABET.length; k++) {
                    ALPHABET_COMBINATIONS_3.add(String.valueOf(ALPHABET[i]) + String.valueOf(ALPHABET[j]) + String.valueOf(ALPHABET[k]));
                }
            }
        }
    }
    public static List<String> getAlphabetCombination(int order) {
        if (order <0 || order>3) {
            throw new IllegalArgumentException("Supports order 1,2, or 3");
        }
        switch(order) {
            case 1: return ALPHABET_COMBINATIONS_1;
            case 2: return ALPHABET_COMBINATIONS_2;
            case 3: return ALPHABET_COMBINATIONS_3;
        }
        return null;
    }

    public static void main(String[] args) {
        String input = "inkoo";
        int distance = 2;

        long t1 = System.currentTimeMillis();
        List<Word> blurred = blur(distance, input);
        long t2 = System.currentTimeMillis();

        for (Word b: blurred) {
            System.out.println(b.getValue());
        }
        System.out.println("Blur size " + blurred.size() + " after " + (t2-t1) + " ms");
        /*
        char[] altChars = {'a','b'};
        char[] word = {'1', '2', '3'};

        for (int i=0; i<=word.length+1; i++) {
            for (int j=i+1; j<=word.length+1; j++) {
                StringBuffer substituteDeleteWordA = new StringBuffer();
                int deletionSubstitutionIdx = 0;

                for (int k=0; k<=word.length+1; k++) {
                    if (i<word.length && j<word.length && k<word.length) {
                        if (k==i) {

                        }
                        if (k==j) {
                            substituteDeleteWordA.append(altChars[0]);

                        }
                        if (k!=i && k!=j) {
                            substituteDeleteWordA.append(word[deletionSubstitutionIdx]);
                        }
                        deletionSubstitutionIdx++;
                    }
                }
                System.out.println(substituteDeleteWordA);
                }
        }
*/

        System.out.println("Testing variants : " + testGetVariants(2));
    }

    // The following method adds distance volume in the n-dimensional alphabet space around each character
    public static List<Word> blur(int distance, String rawWord) {
        List<Word> returnList = new ArrayList<Word>(100000);
        String word = rawWord.toLowerCase().trim();

        Set<Word> returnSet = new HashSet<Word>(100000);
        for (int i=1; i<= distance; i++)  {
            List<String> alphabet = getAlphabetCombination(i);
            for (String junk: alphabet) {
               Set<Word> result = getVariants(junk.toCharArray(), word);
                returnSet.addAll(result);
//                System.out.println("Adding " + result.size());
//                System.out.println("new size after " + junk + " : " + returnSet.size());
            }
        }
        for (Word w: returnSet) {
            returnList.add(w);
        }
        Collections.sort(returnList);
        return returnList;
    }
    public static Set<Word>  getVariants(char[] altChars, String wordString) {
        // N.B. Order of altChars do not matter
        int distance = altChars.length;
        char[] word = wordString.toCharArray();
        Set<Word> returnSet = new HashSet<Word>(1000);
        returnSet.add(new Word(wordString,0));
        if (distance == 0) {
            return returnSet;
        }
        if (distance != 1 && distance != 2) {
            throw new IllegalArgumentException("This method only supports edit distances less than 3.");
        }

/*
        DISTANCE 1 POSITIONS
        insertions:
        a,1,2,3; 1,a,2,3; 1,2,a,3; 1,2,3,a
        
        substitutions:
        a,2,3; 1,a,3; 1,2,a
        
        deletions:
        2,3; 1,3; 1,2


        DISTANCE 2 POSITIONS
        PAIRS: (0,1);(0,2);(0,3);(0,4);(1,2);(1,3);(1,4);(2,3);(2,4);(3,4);(4,4)

        double insertions: 
        a,b,1,2,3(0,1); a,1,b,2,3(0,2); a,1,2,b,3(0,3); a,1,2,3,b(0,4); 1,a,b,2,3(1,2); 1,a,2,b,3(1,3); 1,a,2,3,b(1,4); 1,2,a,b,3(2,3); 1,2,a,3,b(2,4); 1,2,3,a,b(3,4); 
        b,a,1,2,3(0,1); b,1,a,2,3(0,2); b,1,2,a,3(0,3); b,1,2,3,a(0,4); 1,b,a,2,3(1,2); 1,b,2,a,3(1,3); 1,b,2,3,a(1,4); 1,2,b,a,3(2,3); 1,2,b,3,a(2,4); 1,2,3,b,a(3,4); 

        double substitutions:
        a,b,3(0,1); a,2,b(0,2); 1,a,b(1,2);
        b,a,3(0,1); b,2,a(0,2); 1,b,a(1,2);

        insertion substitution: 
        a,b,2,3(0,1); a,1,b,3(0,2); a,1,2,b(0,3); 1,a,b,3(1,2); 1,a,2,b(1,3); 1,2,a,b(2,3)
        b,a,2,3(0,1); b,1,a,3(0,2); b,1,2,a(0,3); 1,b,a,3(1,2); 1,b,2,a(1,3); 1,2,b,a(2,3)

        substitution insertion: 
        a,b,2,3(0,1); a,2,b,3(0,2); a,2,3,b(0,3); 1,a,b,3(1,2); 1,a,3,b(1,3); 1,2,a,b(2,3)
        b,a,2,3(0,1); b,2,a,3(0,2); b,2,3,a(0,3); 1,b,a,3(1,2); 1,b,3,a(1,3); 1,2,b,a(2,3)

        3 deletion deletion: 
        3(0,1); 2(0,2); 1(1,2);

        deletion insertion a: 
        a,2,3(0,1); 2,a,3(0,2); 2,3,a(0,3); 1,a,3(1,2); 1,3,a(1,3); 1,2,a(2,3)
        deletion insertion b:
        b,2,3(0,1); 2,b,3(0,2); 2,3,b(0,3); 1,b,3(1,2); 1,3,b(1,3); 1,2,b(2,3)
        
        insertion deletion a:
        a,1,3(0,1); a,1,2(0,2); a,1,2,3(0,3); 1,a,2(1,2); 1,a,2,3(1,3); 1,2,a,3(2,3)
        insertion deletion b:
        b,1,3(0,1); b,1,2(0,2); b,1,2,3(0,3); 1,b,2(1,2); 1,b,2,3(1,3); 1,2,b,3(2,3)
        
        
        deletion substitution a: 
        a,3(0,1); 2,a(0,2); 1,a(1,2);
        deletion substitution b: 
        b,3(0,1); 2,b(0,2); 1,b(1,2);

        substitution deletion a: 
        a,3(0,1); a,2(0,2); 1,a(1,2);
        substitution deletion b: 
        b,3(0,1); b,2(0,2); 1,b(1,2);

*/
        for (int i=0; i<=word.length+1; i++) {
            for (int j=i+1; j<=word.length+1; j++) {
                // Single Point Variables
                StringBuffer singleInsertion = new StringBuffer();
                StringBuffer singleDeletion = new StringBuffer();
                StringBuffer singleSubstitution = new StringBuffer();

                boolean isSingleInsertion = false;
                boolean isSingleDeletion = false;

                int singleInsertionIdx = 0;

                
                // Double Point Variables
                StringBuffer insertInsertWordA = new StringBuffer();
                StringBuffer insertInsertWordB = new StringBuffer();
                StringBuffer substituteSubstituteWordA = new StringBuffer();
                StringBuffer substituteSubstituteWordB = new StringBuffer();
                StringBuffer insertSubstituteWordA = new StringBuffer();
                StringBuffer insertSubstituteWordB = new StringBuffer();
                StringBuffer substituteInsertWordA = new StringBuffer();
                StringBuffer substituteInsertWordB = new StringBuffer();

                StringBuffer deletionDeletionWord = new StringBuffer();
                
                StringBuffer deletionInsertionWordA = new StringBuffer();
                StringBuffer deletionInsertionWordB = new StringBuffer();
                StringBuffer insertionDeletionWordA = new StringBuffer();
                StringBuffer insertionDeletionWordB = new StringBuffer();

                StringBuffer deletionSubstitutionWordA = new StringBuffer();
                StringBuffer deletionSubstitutionWordB = new StringBuffer();
                StringBuffer substitutionDeletionWordA = new StringBuffer();
                StringBuffer substitutionDeletionWordB = new StringBuffer();

                int insertInsertIdx=0;
                int insertSubstituteIdx=0;
                int substituteInsertIdx=0;
                int deletionDeletionIdx=0;
                int deletionInsertionIdx=0;
                int insertionDeletionIdx=0;
                int deletionSubstitutionIdx=0;
                int substitutionDeletionIdx=0;

                boolean isSubstituteSubstitute = false;
                boolean isInDel = false;
                boolean isDelDel = false;
                boolean isSubDel = false;

                for (int k=0; k<=word.length+1; k++) {
                    if (distance == 1) {
                        if (j==i+1 && k<=word.length) {
                            isSingleInsertion = true;
                        }
                        if (j==i+1 && i<=word.length && j<=word.length && k<word.length) {
                            isSingleDeletion = true;
                        }
                    } else if (distance == 2) {
                        if (i<word.length && j<word.length && k<word.length) {
                            isSubstituteSubstitute = true;
                        }
                        if (i<=word.length && j<=word.length && k<=word.length) {
                            isInDel = true;
                        }
                        if (i<word.length && j<word.length && k<word.length) {
                            isDelDel = true;
                        }
                        if (i<word.length && j<word.length && k<word.length) {
                            isSubDel = true;
                        }
                    }

                    if (k==i) {
                        if (distance == 1) {
                            if (isSingleInsertion) {
                                singleInsertion.append(altChars[0]);
                            }
                            if (isSingleDeletion) {
                                singleSubstitution.append(altChars[0]);
                            }
                        } else if (distance == 2) {
                            insertInsertWordA.append(altChars[0]);
                            insertInsertWordB.append(altChars[1]);
                            if(isSubstituteSubstitute) {
                                substituteSubstituteWordA.append(altChars[0]);
                                substituteSubstituteWordB.append(altChars[1]);
                            }
                            if (isInDel) {
                                insertSubstituteWordA.append(altChars[0]);
                                insertSubstituteWordB.append(altChars[1]);

                                substituteInsertWordA.append(altChars[0]);
                                substituteInsertWordB.append(altChars[1]);
                                substituteInsertIdx++;
    
                                insertionDeletionWordA.append(altChars[0]);
                                insertionDeletionWordB.append(altChars[1]);
                                deletionInsertionIdx++;
                            }
                            if (isSubDel) {
                                substitutionDeletionWordA.append(altChars[0]);
                                substitutionDeletionWordB.append(altChars[1]);
                            }
                        }
                    }
                    
                    if  (k==j) {
                        if (distance == 1) {
                            if (isSingleInsertion) {
                                singleInsertion.append(word[singleInsertionIdx]);
                                singleInsertionIdx++;
                            }
                            if (isSingleDeletion) {
                                singleDeletion.append(word[k]);
                                singleSubstitution.append(word[k]);
                            }
                        } else if (distance == 2) {
                            insertInsertWordA.append(altChars[1]);
                            insertInsertWordB.append(altChars[0]);
                            if(isSubstituteSubstitute) {
                                substituteSubstituteWordA.append(altChars[1]);
                                substituteSubstituteWordB.append(altChars[0]);
                            }
                            if (isInDel) {
                                insertSubstituteWordA.append(altChars[1]);
                                insertSubstituteWordB.append(altChars[0]);

                                substituteInsertWordA.append(altChars[1]);
                                substituteInsertWordB.append(altChars[0]);
                                insertSubstituteIdx++;
    
                                deletionInsertionWordA.append(altChars[0]);
                                deletionInsertionWordB.append(altChars[1]);
                                insertionDeletionIdx++;
                            }
                            if (isSubDel) {
                                deletionSubstitutionWordA.append(altChars[0]);
                                deletionSubstitutionWordB.append(altChars[1]);
                            }
                        }
                    }

                    if (k!=i && k != j){
                        if (distance == 1) {
                            if (isSingleInsertion) {
                                singleInsertion.append(word[singleInsertionIdx]);
                                singleInsertionIdx++;
                            }
                            if (isSingleDeletion) {
                                singleDeletion.append(word[k]);
                                singleSubstitution.append(word[k]);
                            }
                        } else if (distance == 2) {
                            insertInsertWordA.append(word[insertInsertIdx]);
                            insertInsertWordB.append(word[insertInsertIdx]);
                            insertInsertIdx++;
                            if(isSubstituteSubstitute) {
                                substituteSubstituteWordA.append(word[k]);
                                substituteSubstituteWordB.append(word[k]);
                            }
                        }
                    }
                    if (distance == 1) {
                        isSingleInsertion = false;
                        isSingleDeletion = false;
                    } else if (distance == 2) {
                        if (isInDel && k!=i && k<word.length) {
                            insertSubstituteWordA.append(word[insertSubstituteIdx]);
                            insertSubstituteWordB.append(word[insertSubstituteIdx]);
                            substituteInsertWordA.append(word[substituteInsertIdx]);
                            substituteInsertWordB.append(word[substituteInsertIdx]);
                            insertSubstituteIdx++;
                            substituteInsertIdx++;
    
                            deletionInsertionWordA.append(word[deletionInsertionIdx]);
                            deletionInsertionWordB.append(word[deletionInsertionIdx]);
                            deletionInsertionIdx++;
                        }
    
                        if (isDelDel && k!=i && k!=j && k<word.length) {
                            deletionDeletionWord.append(word[deletionDeletionIdx]);
                        }
    
                        if (isInDel && k!=j && k<word.length) {
                            insertionDeletionWordA.append(word[insertionDeletionIdx]);
                            insertionDeletionWordB.append(word[insertionDeletionIdx]);
                            insertionDeletionIdx++;
                        }
                        if (isSubDel) {
                            if (k!=i && k!=j) {
                                deletionSubstitutionWordA.append(word[deletionSubstitutionIdx]);
                                deletionSubstitutionWordB.append(word[deletionSubstitutionIdx]);
                                substitutionDeletionWordA.append(word[deletionSubstitutionIdx]);
                                substitutionDeletionWordB.append(word[deletionSubstitutionIdx]);
                            }
                            deletionSubstitutionIdx++;
                            substitutionDeletionIdx++;
                        }
                        
                        deletionDeletionIdx++;
                        isSubstituteSubstitute = false;
                        isInDel = false;
                        isDelDel = false;
                        isSubDel = false;
                    }
                }
                if (distance == 1) {
                    Word.addIfNotEmpty(returnSet, singleInsertion.toString(), distance, "i");
                    Word.addIfNotEmpty(returnSet, singleDeletion.toString(), distance, "d");
                    Word.addIfNotEmpty(returnSet, singleSubstitution.toString(), distance, "s");
                } else if (distance == 2) {
                    Word.addIfNotEmpty(returnSet, insertInsertWordA.toString(), distance, "iiA");
                    Word.addIfNotEmpty(returnSet, insertInsertWordB.toString(), distance, "iiB");
                    Word.addIfNotEmpty(returnSet, substituteSubstituteWordA.toString(), distance, "ssA");
                    Word.addIfNotEmpty(returnSet, substituteSubstituteWordB.toString(), distance, "ssB");

                    Word.addIfNotEmpty(returnSet, insertSubstituteWordA.toString(), distance, "isA");
                    Word.addIfNotEmpty(returnSet, insertSubstituteWordB.toString(), distance, "isB");

                    Word.addIfNotEmpty(returnSet, substituteInsertWordA.toString(), distance, "siA");
                    Word.addIfNotEmpty(returnSet, substituteInsertWordB.toString(), distance, "siB");

                    Word.addIfNotEmpty(returnSet, deletionDeletionWord.toString(), distance, "dd");

                    Word.addIfNotEmpty(returnSet, deletionInsertionWordA.toString(), distance, "diA");
                    Word.addIfNotEmpty(returnSet, deletionInsertionWordB.toString(), distance, "diB");
                    Word.addIfNotEmpty(returnSet, insertionDeletionWordA.toString(), distance, "idA");
                    Word.addIfNotEmpty(returnSet, insertionDeletionWordB.toString(), distance, "idB");
                    
                    Word.addIfNotEmpty(returnSet, deletionSubstitutionWordA.toString(), distance, "dsA");
                    Word.addIfNotEmpty(returnSet, deletionSubstitutionWordB.toString(), distance, "dsB");
                    Word.addIfNotEmpty(returnSet, substitutionDeletionWordA.toString(), distance, "sdA");
                    Word.addIfNotEmpty(returnSet, substitutionDeletionWordB.toString(), distance, "sdB");
                }
            }
        }
        return returnSet;
    }

    public static boolean testGetVariants(int distance) {
        char[] noiseCharacters;
        switch (distance) {
            case 0:
                noiseCharacters = new char[0];
                break;
            case 1:
                noiseCharacters = new char[] {'a'};
                break;
            case 2:
                noiseCharacters = new char[] {'a', 'b'};
                break;
            default:                    
                throw new IllegalArgumentException("Can only test distance 0,1,2");
        }
        Set<Word> set = getVariants(noiseCharacters, "123");
        boolean passed = true;
        String[] test = null;

        if (distance == 1) {
            test = new String[] {"a123", "1a23", "12a3", "123a"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Insert Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            
            test = new String[] {"a23", "1a3", "12a"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Substitute Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }

            test = new String[] {"23", "13", "12"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Insert Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
        } else if (distance == 2) {
            test = new String[] {"ab123", "a1b23", "a12b3", "a123b", "1ab23", "1a2b3", "1a23b", "12ab3", "12a3b", "123ab", "ba123", "b1a23", "b12a3", "b123a", "1ba23", "1b2a3", "1b23a", "12ba3", "12b3a", "123ba"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Insert Insert Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }

            test = new String[] {"ab3", "a2b", "1ab", "ba3", "b2a", "1ba"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Substitute Substitute Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            test = new String[] {"ab23", "a1b3", "a12b", "1ab3", "1a2b", "12ab", "a2b3", "a23b", 
                                 "1a3b", "ba23", "b1a3", "b12a", "1ba3", "1b2a", "12ba", 
                                 "b2a3", "b23a", "1b3a"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Insert Substitute Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            test = new String[] {"3", "2", "1"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Delete Delete Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            test = new String[] {"a23", "2a3", "23a", "1a3", "13a", "12a", "b23", "2b3", "23b", "1b3", "13b", "12b"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Delete Insert Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            test = new String[] {"a13", "a12", "a123", "1a2", "1a23", "12a3", "b13", "b12", "b123", "1b2", "1b23", "12b3"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Insert Delete Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            test = new String[] {"a3", "2a", "1a", "b3", "2b", "1b", "a2", "b2"};
            for (int i=0; i<test.length; i++) {
                Word thisWord = new Word(test[i],2);
                if (!set.contains(thisWord)) {
                    System.out.println("Delete Substitute Failed : " + test[i]);
                    passed = false;
                } else {
                    set.remove(thisWord);
                }
            }
            if (set.size() != 1) {
                System.out.println("Set size failed. Residual set is not empty.");
                for (Word error: set) {
                    System.out.println("Residual : " + error);
                }
            }
        }
        return passed;
    }

    public static int getLevenshteinDistance(String s, String t) {
      if (s == null || t == null) {
        throw new IllegalArgumentException("Strings must not be null");
      }
            
      int n = s.length(); // length of s
      int m = t.length(); // length of t
            
      if (n == 0) {
        return m;
      } else if (m == 0) {
        return n;
      }

      int[] p = new int[n+1]; //'previous' cost array, horizontally
      int[] d = new int[n+1]; // cost array, horizontally
      int[] _d; //placeholder to assist in swapping p and d

      // indexes into strings s and t
      int i; // iterates through s
      int j; // iterates through t

      char t_j; // jth character of t

      int cost; // cost

      for (i = 0; i<=n; i++) {
         p[i] = i;
      }
            
      for (j = 1; j<=m; j++) {
         t_j = t.charAt(j-1);
         d[0] = j;
            
         for (i=1; i<=n; i++) {
            cost = s.charAt(i-1)==t_j ? 0 : 1;
            // minimum of cell to the left+1, to the top+1, diagonally left and up +cost                
            d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
         }

         // copy current distance counts to 'previous row' distance counts
         _d = p;
         p = d;
         d = _d;
      } 
            
      // our last action in the above loop was to switch d and p, so p now 
      // actually has the most recent cost counts
      return p[n];
    }

}


