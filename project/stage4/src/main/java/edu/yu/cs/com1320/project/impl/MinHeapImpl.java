package edu.yu.cs.com1320.project.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

import edu.yu.cs.com1320.project.MinHeap;

@SuppressWarnings("unchecked")
public class MinHeapImpl<E extends Comparable> extends MinHeap<E>{

    public MinHeapImpl(){
        this.elements = (E[]) new Comparable[5];
        this.elementsToArrayIndex = new HashMap<E,Integer>();
    }

    @Override
    protected void doubleArraySize() {
        elements = Arrays.copyOf(elements, elements.length * 2);
    }

    @Override
    public void reHeapify(E element) {
        int foundElement = this.getArrayIndex(element);
        this.upHeap(foundElement);
        this.downHeap(foundElement);
    }

    @Override
    protected int getArrayIndex(E element) {
        return this.elementsToArrayIndex.get(element);
    }

    @Override
    protected  void swap(int i, int j)
    {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elementsToArrayIndex.put(this.elements[j], i);

        this.elements[j] = temp;
        this.elementsToArrayIndex.put(temp, j);
    }

    /**
     *while the key at index k is less than its
     *parent's key, swap its contents with its parent’s
     */
    @Override
    protected  void upHeap(int k)
    {
        while (k > 1 && this.isGreater(k / 2, k))
        {
            this.swap(k, k / 2);
            k = k / 2;
        }

        //added myself to set the map entry to make the element equal the array index
        this.elementsToArrayIndex.put(elements[k], k);
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    @Override
    protected  void downHeap(int k)
    {
        while (2 * k <= this.count)
        {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1))
            {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j))
            {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
        
        //added myself to set the map entry to make the element equal the array index
        this.elementsToArrayIndex.put(elements[k], k);
    }

    @Override
    public void insert(E x)
    {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1)
        {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;

        //added myself to set the map entry to make the element equal the array index
        this.elementsToArrayIndex.put(x, this.count);

        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }

    @Override
    public E removeMin()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];

        //added myself to set the map entry take out the element deleted and 
        this.elementsToArrayIndex.remove(min);

        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        return min;
    }
    
}