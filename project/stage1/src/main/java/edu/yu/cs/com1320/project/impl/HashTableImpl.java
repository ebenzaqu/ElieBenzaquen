package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;





@SuppressWarnings("unchecked")
public class HashTableImpl<Key,Value> implements HashTable<Key,Value>{

    private class LinkedNode{
        //This class is the separate nodes that are created inside the HashTable Array
        //Each node has a Key-Value pair that can be accessed by the get methods
        //The next node in the Linked list can be called by saying "LinkedNode.next"

        Key key;
        Value value;
        LinkedNode next = null;

        private LinkedNode(Key k, Value v){
            this.key = k;
            this.value = v;
        }

        private Key getKey() {
            return key;
        }

        private Value getValue(){
            return value;
        }

    }

    LinkedNode head;

    Object[] hashTable = new Object[5];

    
    @Override
    public Value get(Key k) { 
        //To find the index of the HashTable array you want to access
        //
        int index = hashFunction(k);
        LinkedNode  current = (HashTableImpl<Key, Value>.LinkedNode) hashTable[index];
        //if there is nothing in that index
        //
        if(current == null){return null;}
        //If the first node of the array is the Key you want to access
        //
        if(current.getKey() == k){return (Value) current.getValue();}
        //Searching through the array until you find the correct Key
        //
        while(current.next != null){
            if(current.next.getKey() == k){
                return (Value) current.next.getValue();
            }
            current = current.next;
        }
        //If nothing is found, return null
        //
        return null;
    }

    @Override
    public Value put(Key k, Value v) {
        if(k == null){throw new IllegalArgumentException("Key cannot be null");}
        Object newNode = new LinkedNode(k,v);
        LinkedNode pair = (HashTableImpl<Key, Value>.LinkedNode) newNode;
        //Finding correct array index
        int index = hashFunction(k);
        LinkedNode current = (HashTableImpl<Key, Value>.LinkedNode) hashTable[index];
        //If key inputed is not in the HashTable and the key is null, it is not put into the HashTable
        if(contains(k) == false && v == null){return null;}
        //If the Key inputed is already in the HashTable and the value is null, remove it from the HashTable and returns it's key
        if(contains(k) && v == null){
            Value temp = get(k);
            remove(k);
            return temp;
        }
        //If the key is already there but the value is not null, replaces the old value with a new one
        //Also puts it at the back of the list
        if(contains(k) && v != null){
            remove(k);
            put(k,v);
            return v;
        }
        //If there is nothing preasant at the first link in the HashTable index, place it at the head
        if(hashTable[index] == null){
            hashTable[index] = pair;
        }
        //Go through the HashTable until you reach the last node and then place the new pair into the last node
        else{
            while(current.next != null){
                current = current.next;
            }
            current.next = pair;
        }
        return null;
    }

    private void remove(Key k){
        //finding correct index in the HashTable array
        int index = hashFunction(k);
        LinkedNode current = (HashTableImpl<Key, Value>.LinkedNode) hashTable[index];
        head = current;
        //If the array index accessed if null, meaning there was no node there, don't remove anything
        if(current == null){return;}
        //If the head of the index accessed is the only node in the Linked List
        if(current.getKey() == k && current.next == null){
            hashTable[index] = null;
            return;
        }
        //If the head of the index accessed in the first node and there are other after it
        if(current.getKey() == k && current.next != null){
            hashTable[index] = current.next;
            return;
        }
        //Scrolling through the Linked List until you the Key you want deleted is found
        while(current.next != null){
            if(current.next.getKey() == k){
                current.next = current.next.next;
                return;
            }
            current = current.next;
        }
    }

    private int hashFunction(Key hashKey){
        //return the index of the HashTable array that the Key should be put in
        return (hashKey.hashCode() & 0x7fffffff) % 5;
        // return(Math.abs(hashKey.hashCode() % 5));
    }

    private boolean contains(Key k){
        //Finding the correct index of the HashTable array
        int index = hashFunction(k);
        LinkedNode current = (HashTableImpl<Key, Value>.LinkedNode) hashTable[index];
        //Return false if there is nothing in the array index
        if(current == null){return false;}
        //Returns true is the head of the index is the Key
        if(current.getKey() == k){ return true;}
        //Scrolls through the Linked List until it finds the key it is looking for
        while(current.next != null){
            if(current.next.getKey() == k){
                return true;
            }
            current = current.next;
        }
        //If not found, return false
        return false;
    }

    // private void printArray(){
        
    //     for(int i = 0; i<hashTable.length; i++){
    //         LinkedNode node = (HashTableImpl<Key, Value>.LinkedNode) hashTable[i];
    //         if(node != null){
    //             System.out.print(node.getKey() + "-" + node.getValue());
    //             while(node.next != null){
    //                 System.out.print("  --->  " + node.next.getKey() + "-" + node.next.getValue());
    //                 node = node.next;
    //             }
    //         }
    //         System.out.println("");
    //     }
    // }
    
}
