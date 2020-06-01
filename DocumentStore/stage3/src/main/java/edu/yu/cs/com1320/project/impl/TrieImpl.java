package edu.yu.cs.com1320.project.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
// import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Comparator;

import edu.yu.cs.com1320.project.Trie;

@SuppressWarnings("unchecked")
public class TrieImpl<Value> implements Trie<Value>,Comparator<Value> {

    public TrieImpl(){}

    private static final int alphabetSize = 256;
    private Node<Value> root;

    private static class Node<Value>
    {
        protected Value val;
        protected Node[] links = new Node[TrieImpl.alphabetSize];
    }

    @Override
    public void put(String key, Value val) {
        if(key == null){
            return;
        }
        //in the second put method, create a list at the beginning and add the values before setting it 
        if (val == null)
        {
            return;
        }
        else
        {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d){
        if (x == null){
            x = new Node<Value>();
        }
        key = changeString(key);
        //Splits the String up into tokens
        StringTokenizer wordInDoc = new StringTokenizer(key);
        //each token is put through the while loop
        while(wordInDoc.hasMoreTokens()){
            String key2 = wordInDoc.nextToken();
            //once it gets to the last character of each word it gets sent into he if statement
            if (d == key2.length()){
                //if this is the first value being inserted
                if(x.val == null){
                    List<Value> valuesList = new ArrayList<>();
                    valuesList.add(val);
                    x.val = (Value) valuesList;
                //if there was a list presently made
                } 
                else{
                    List<Value> list = (List<Value>) x.val;
                    //add here a duplicate checker
                    if(list.contains(val)){}
                    else{
                    list.add(val);
                    }
                    x.val = (Value) list;
                }
                //set the counter to 0 for the next word
                d = 0;
                continue;
            }
            char c = key2.charAt(d);
            x.links[c] = this.put(x.links[c], key2, val, d + 1);
        }
        return x;
    }

    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        List<Value> emptyList = new ArrayList<>();
        if(key == null){
            return emptyList;
        }
        key = changeString(key);
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null)
        {
            return emptyList;
        }
        List<Value> newList = (List<Value>) x.val;
        if(newList == null){
            return emptyList;
        } 
        newList.sort(comparator);
        return newList;
    }

    private List<Value> getAllSorted2(String key){
        List<Value> emptyList = new ArrayList<>();
        if(key == null){
            return emptyList;
        }
        key = changeString(key);
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null)
        {
            return emptyList;
        }
        List<Value> newList = (List<Value>) x.val;
        if(newList == null){
            return emptyList;
        } 
        return newList;
    }

    private Node<Value> get(Node<Value> x, String key, int d) {
        key = changeString(key);
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);

        return this.get(x.links[c], key, d + 1);
    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        List<Value> results = new ArrayList<Value>();
        if(prefix == null){
            return results;
        }
        prefix = changeString(prefix);
        //find node which represents the prefix
        Node<Value> x = this.get(this.root, prefix, 0);
        //collect keys under it
        if(x != null) {
            this.collectForGet(x, new StringBuilder(prefix), results);
        }
        results.sort(comparator);
        return results;
    }

    private void collectForGet(Node<Value> x, StringBuilder prefix, List<Value> results) {
        if (x.val != null) {
            //add a string made up of the chars from
            //root to this node to the result set
            List<Value> nodeList = (List<Value>) x.val;
            for (Object val : nodeList) {
                if(results.contains(val)){}
                else{
                results.add((Value) val);
                }
            }
        }
        for (char c = 0; c < TrieImpl.alphabetSize; c++) {
            if(x.links[c]!=null){
                //add child's char to the string
                prefix.append(c);
                this.collectForGet(x.links[c], prefix, results);
                //remove the child's char to prepare for next iteration
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }         
    }

    @Override
    public Set<Value> deleteAllWithPrefix(String prefix){
        if(prefix == null){
            return new HashSet<Value>();
        }
        prefix = changeString(prefix);
        Set<Value> results = new HashSet<Value>();
        //find node which represents the prefix
        Node<Value> x = this.get(this.root, prefix, 0);
        //collect keys under it
        if(x != null) {
            this.collectForDelete(x, new StringBuilder(prefix), results);
            x = null;
        }
        return results;
    }

    private void collectForDelete(Node<Value> x, StringBuilder prefix, Set<Value> results) {
        if (x.val != null) {
            //add a string made up of the chars from
            //root to this node to the result set
            List<Value> listOfNodes = (List<Value>) x.val;
            Set<Value> setOfNodes = new HashSet<>(listOfNodes);
            for (Object val : setOfNodes) {
                results.add((Value) val);
            }
            x.val = null;
        }
        for (char c = 0; c < TrieImpl.alphabetSize; c++){
            if(x.links[c]!=null){
                //add child's char to the string
                prefix.append(c);
                this.collectForDelete(x.links[c], prefix, results);
                //remove the child's char to prepare for next iteration
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }         
    }

    @Override
    public Set<Value> deleteAll(String key) {
        if(key == null){
            return new HashSet<Value>();
        }
        key = changeString(key);
        List<Value> emptyList = new ArrayList<>();
        if(getAllSorted2(key).isEmpty()){
            return new HashSet<Value>();
        }
        Node<Value> node = deleteAll(this.root, key, 0);
        if(nodeToDelete.val == null){
            if(deleteAllWithPrefix(key).isEmpty()){
                node = null;
            }
        }
        else{
            List<Value> listToSet = (List<Value>) nodeToDelete.val;
            Set<Value> set = new HashSet<Value>(listToSet);
            nodeToDelete = null;
            if(deleteAllWithPrefix(key).isEmpty()){
                node = null;
            }
            return set;
        }
        return null;
    }

    private Node<Value> nodeToDelete;

    private Node<Value> deleteAll(Node<Value> x, String key, int d){
        if (x == null){
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()){
            Node<Value> node = new Node<Value>();
            node.val = x.val;
            nodeToDelete = node;
            x.val = null;
        }
        //continue down the trie to the target node
        else{
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (x.val != null){
            //if that node is the last one in the chain and there is nothing else beneath it
            if(getAllSorted2(key).isEmpty()){
                x = null;
            }
            return x;
        }
        //remove subtrie rooted at x if it is completely empty	
        for (int c = 0; c <TrieImpl.alphabetSize; c++){
            if (x.links[c] != null){
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    @Override
    public Value delete(String key, Value val) {
        if(key == null || val == null){
            return null;
        }
        if(getAllSorted2(key).isEmpty()){
            return null;
        }
        key = changeString(key);
        Node<Value> node = delete(this.root, val, key, 0);
        Value delVal = deletedVal;
        deletedVal = null;
        return delVal;
    }

    private Value deletedVal;

    private Node<Value> delete(Node<Value> x, Value val, String key, int d) {
        if (x == null){
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()){
            if(x.val == null){}
            else{
                List<Value> valToDelete = (List<Value>) x.val;
                if(valToDelete.contains(val)){
                    valToDelete.remove(val);
                    deletedVal = val;
                }
            }
        }
        //continue down the trie to the target node
        else{
            char c = key.charAt(d);
            x.links[c] = this.delete(x.links[c], val, key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (x.val != null){
            return x;
        }
        //remove subtrie rooted at x if it is completely empty	
        for (int c = 0; c <TrieImpl.alphabetSize; c++){
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    private String changeString(String str){
        str = str.toUpperCase();
        str = str.replaceAll("[^a-zA-Z0-9\\s]", "");
        return str;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return 0;
    }

    public static void main(String[] args) {
        TrieImpl<Integer> trie = new TrieImpl<>();

        Comparator<Integer> comparator = (Integer int1, Integer int2) -> int1.compareTo(int2);

        String test = "This is a mes$%^sage thisising thisisisisi";

        trie.put(test,54);
        trie.put(test,543);
        trie.put("This is the second message thisis",5);
        trie.put("worlding",4);
        trie.put("worlding",45);
        // System.out.println(trie.getAllWithPrefixSorted("this"));
        System.out.println(trie.deleteAll("message"));
        System.out.println(trie.getAllSorted2("is"));
        System.out.println(trie.getAllSorted2("message"));
        // System.out.println(trie.getAllSorted("message", comparator));
        // System.out.println(trie.delete("is",5));
        // System.out.println(trie.getAllWithPrefixSorted("boo", comparator));
        // System.out.println(trie.deleteAllWithPrefix("prefix"));
        // System.out.println(trie.delete("this", 54));
        // System.out.println(trie.getAllSorted("this", comparator));
        // System.out.println(trie.deleteAllWithPrefix("this"));
        // System.out.println(trie.getAllWithPrefixSorted("this", comparator));        
    }   
}