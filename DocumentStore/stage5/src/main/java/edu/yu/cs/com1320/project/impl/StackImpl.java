package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

// @SuppressWarnings("unchecked")
public class StackImpl<T> implements Stack<T>{

    public StackImpl(){}

    private class LinkedStack{
        //This class is the seperate nodes that are created inside the HashTable Array
        //Each node has a Key-Value pair that can be accessed by the get methods
        //The next node in the Linked list can be called by saying "LinkedNode.next"

        T stackLayer;
        LinkedStack next = null;

        private LinkedStack(T stack){
            this.stackLayer = stack;
        }

        private T getLayer() {
            return stackLayer;
        }

    }

    LinkedStack head;

    int counter;

    @Override
    public void push(T element) {
        counter++;
        LinkedStack current = new LinkedStack(element);
        LinkedStack newElement = new LinkedStack(element);
        if(head == null){
            head = current;
        }
        else{
            newElement.next = head;
            head = newElement;
        }
    }

    @Override
    public T pop() {
        if(counter == 0){return null;}
        counter--;
        LinkedStack value = head;
        head = head.next;
        return value.getLayer();
    }

    @Override
    public T peek() {
        if(counter == 0){return null;}
        return head.getLayer();
    }

    @Override
    public int size() {
        return counter;
    }

    // private void printStack(){
    //     if(head != null){
    //         System.out.println(head.getLayer());
    //         // System.out.println(head.next.getLayer());
    //         LinkedStack current = head;
    //         while(current.next != null){
    //             System.out.println(current.next.getLayer());
    //             current = current.next;
    //         }
    //     }
    // }
}