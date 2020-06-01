package edu.yu.cs.com1320.project.stage5.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.function.Function;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;

@SuppressWarnings("unchecked")
public class DocumentStoreImpl implements DocumentStore {

    public DocumentStoreImpl(){
        this.minValue = System.nanoTime();
        try{
            URI sentinel = new URI("");
            store.put(sentinel, null);      
        } catch (URISyntaxException e){}
        this.filePath = new File(System.getProperty("user.dir"));
        store.setPersistenceManager(new DocumentPersistenceManager(filePath));
    }

    public DocumentStoreImpl(File baseDir){
        this.minValue = System.nanoTime();
        try{
            URI sentinel = new URI("");
            store.put(sentinel, null);      
        } catch (URISyntaxException e){}
        this.filePath = baseDir;
        store.setPersistenceManager(new DocumentPersistenceManager(baseDir));
    }

    private File filePath;

    BTreeImpl<URI,Document> store = new BTreeImpl<>();
    StackImpl<Undoable> commandStack = new StackImpl<Undoable>();
    TrieImpl<URI> trieImpl = new TrieImpl<URI>();
    MinHeapImpl<HeapCompare> heap = new MinHeapImpl<HeapCompare>();
    List<URI> movedToDisk = new ArrayList<>();
    HashMap<URI,HeapCompare> heapStorage = new HashMap<>();

    int counter = 0;
    int memory = 0;
    int maxDocs = Integer.MAX_VALUE;
    int maxMemory = Integer.MAX_VALUE;

    long minValue;

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {

        if (format == null || uri == null || input == null && store.get(uri) != null
                || input == null && store.get(uri) == null) {
            return errorsForInput(input, uri, format);
        }

        byte[] byteArray = inputStreamToByteArray(input);

        if (format == DocumentFormat.TXT) {

            String message = new String(byteArray);

            if (store.get(uri) != null && store.get(uri).getDocumentTextHashCode() == message.hashCode()) {

                // Make Lambda that returns true and adds to command stack
                Function<URI, Boolean> repeatDoc = (URI putUri) -> {
                    return true;
                };

                GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, repeatDoc);
                commandStack.push(deleteCommand);

                DocumentImpl doc = (DocumentImpl) store.get(uri);
                doc.setLastUseTime(System.nanoTime());

                return message.hashCode();
            }

            this.putDocumentAsTXT(uri, message);
        }

        if (format == DocumentFormat.PDF) {
            this.putDocumentAsPDF(uri, byteArray);
        }

        return 0;
    }

    private int putDocumentAsTXT(URI uri, String message) {

        DocumentImpl document = new DocumentImpl(uri, message, message.hashCode());

        // if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == message.hashCode()) {
        //     return message.hashCode();
        // }

        if (store.get(uri) != null && store.get(uri).getDocumentTextHashCode() != message.hashCode()) {

            return this.putAsTxtReplace(uri, document, message);

        }

        Function<URI, Boolean> undoStamPutTXT = (URI deleteUri) -> {
            this.memory = this.memory - document.getMemorySize();
            this.deleteFromHeap(uri);
            store.put(uri, null);
            deleteFromTrie(message, document);
            this.counter--;
            return true;
        };

        GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoStamPutTXT);
        commandStack.push(putCommand);

        this.counter++;
        this.memory = memory + document.getMemorySize();

        document.setLastUseTime(System.nanoTime());

        //new heap object passing in uri
        //set lastUseTime of object to doc.getLastUseTime 
        //put object into hashmap of heap objects
        //insert into heap
        store.put(uri, document);
        HeapCompare docHeap = new HeapCompare(uri);
        docHeap.setLastUseTime(document.getLastUseTime());
        heapStorage.put(uri, docHeap);

        heap.insert(docHeap);

        trieImpl.put(message, document.getKey());

        this.checkIfOver();

        return 0;
    }

    private int putAsTxtReplace(URI uri, DocumentImpl document, String message){
        DocumentImpl oldDoc = (DocumentImpl) store.get(uri);
        int hashCode = oldDoc.getDocumentTextHashCode();
        oldDoc.setLastUseTime(System.nanoTime());

        if(movedToDisk.contains(uri)){
            this.counter++;
        }
        
        //the lambda is supposed to replace something in the trie so I delete the document from the trie and then put the new one in
        //done
        Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
            this.memory = this.memory + oldDoc.getMemorySize() - document.getMemorySize();
            
            this.deleteFromHeap(uri);
            oldDoc.setLastUseTime(System.nanoTime());

            store.put(uri, oldDoc);

            HeapCompare docHeap = new HeapCompare(uri);
            docHeap.setLastUseTime(oldDoc.getLastUseTime());
            heapStorage.put(uri, docHeap);

            heap.insert(docHeap);

            deleteFromTrie(message, document);
            trieImpl.put(message,oldDoc.getKey());
            this.checkIfOver();
            return true;
        };
        
        //delete the only value from the hashtable and trie and put a new one in
        //done
        GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoReplace);
        commandStack.push(putCommand);

        this.memory = this.memory - oldDoc.getMemorySize() + document.getMemorySize();
        this.deleteFromHeap(uri);
        document.setLastUseTime(System.nanoTime());

        store.put(uri, document);

        HeapCompare docHeap = new HeapCompare(uri);
        docHeap.setLastUseTime(document.getLastUseTime());
        heapStorage.put(uri, docHeap);           
        heap.insert(docHeap);

        deleteFromTrie(message, oldDoc);
        trieImpl.put(message, document.getKey());

        this.checkIfOver();
        
        return hashCode;
    }

    private int putDocumentAsPDF(URI uri, byte[] byteArray) {

        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(PDDocument.load(byteArray)).trim();
            DocumentImpl doc = new DocumentImpl(uri, text, text.hashCode(), byteArray);

            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == text.hashCode()) {

                DocumentImpl document = (DocumentImpl) store.get(uri);
                document.setLastUseTime(System.nanoTime());

                return text.hashCode();
            }

            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != text.hashCode()) {

                return this.putAsPDFReplace(uri, doc, text);

            }

            Function<URI, Boolean> undoStamPutPDF = (URI deleteUri) -> {
                this.memory = this.memory - doc.getMemorySize();
                this.counter--;

                this.deleteFromHeap(uri);
                deleteFromTrie(text, doc);
                store.put(uri, null);
                return true;
            };

            GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoStamPutPDF);
            commandStack.push(putCommand);

            this.counter++;
            this.memory = memory + doc.getMemorySize();

            doc.setLastUseTime(System.nanoTime());

            store.put(uri, doc);

            HeapCompare docHeap = new HeapCompare(uri);
            docHeap.setLastUseTime(doc.getLastUseTime());
            heapStorage.put(uri,docHeap);

            heap.insert(docHeap);


            trieImpl.put(text, doc.getKey());

            this.checkIfOver();

            return 0;

        } catch (IOException e) {e.printStackTrace();}
        return 0;
    }

    private int putAsPDFReplace(URI uri, DocumentImpl doc, String text){
        DocumentImpl oldDoc = (DocumentImpl) store.get(uri);
        oldDoc.setLastUseTime(System.nanoTime());

        Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
            this.memory = this.memory + oldDoc.getMemorySize() - doc.getMemorySize();

            this.deleteFromHeap(uri);
            oldDoc.setLastUseTime(System.nanoTime());
            store.put(uri, oldDoc);
            HeapCompare docHeap = new HeapCompare(uri);
            docHeap.setLastUseTime(oldDoc.getLastUseTime());
            heapStorage.put(uri, docHeap);

            heap.insert(docHeap);

            deleteFromTrie(text, doc);
            trieImpl.put(text,oldDoc.getKey());

            this.checkIfOver();
            return true;
        };
        GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoReplace);
        commandStack.push(putCommand);

        // int temp = store.get(uri).getDocumentTextHashCode();
        this.memory = this.memory - oldDoc.getMemorySize() + doc.getMemorySize();

        this.deleteFromHeap(uri);
        doc.setLastUseTime(System.nanoTime());

        store.put(uri, doc); 

        HeapCompare docHeap = new HeapCompare(uri);
        docHeap.setLastUseTime(doc.getLastUseTime());
        heapStorage.put(uri, docHeap);

        heap.insert(docHeap);

        deleteFromTrie(text, oldDoc);
        trieImpl.put(text,doc.getKey());
        this.checkIfOver();
        return oldDoc.getDocumentTextHashCode();
    }

    private int errorsForInput(InputStream input, URI uri, DocumentFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Format must be either TXT or PDF");
        }
        if (uri == null) {
            throw new IllegalArgumentException("URI must have a value");
        }
        if (input == null && store.get(uri) != null) {
            //delete from the docStore
            DocumentImpl oldDoc = (DocumentImpl) store.get(uri);
            String docText = oldDoc.getDocumentAsTxt();
            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                oldDoc.setLastUseTime(System.nanoTime());

                store.put(uri, oldDoc);

                HeapCompare docHeap = new HeapCompare(uri);
                docHeap.setLastUseTime(oldDoc.getLastUseTime());
                heapStorage.put(uri, docHeap);

                heap.insert(docHeap);
    
                trieImpl.put(docText, oldDoc.getKey());
                this.counter++;
                this.memory = this.memory + oldDoc.getMemorySize();
                this.checkIfOver();
                return true;
            };
            GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDelete);
            commandStack.push(deleteCommand);
            int temp = store.get(uri).getDocumentTextHashCode();
            //delete from both hashtable and Trie

            this.memory = this.memory - oldDoc.getMemorySize();

            this.deleteFromHeap(uri);
            store.put(uri, null);
            deleteFromTrie(docText, oldDoc);
            this.counter--;
            
            return temp;
        }
        if (input == null && store.get(uri) == null) {
            return 0;
        }
        return 0;
    }

    private byte[] inputStreamToByteArray(InputStream input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int size;
        byte[] array = new byte[2048];
        try {
            while ((size = input.read(array, 0, array.length)) != -1) {
                buffer.write(array, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

    @Override
    public boolean deleteDocument(URI uri) {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot equal null");
        }

        if (store.get(uri) != null) {
            DocumentImpl doc = (DocumentImpl) store.get(uri);
            String docText = doc.getDocumentAsTxt();

            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                doc.setLastUseTime(System.nanoTime());
                store.put(uri, doc);
                HeapCompare docHeap = new HeapCompare(uri);
                docHeap.setLastUseTime(doc.getLastUseTime());
                heapStorage.put(uri, docHeap);

                heap.insert(docHeap);

                trieImpl.put(docText, doc.getKey());
                this.counter++;
                this.memory = this.memory + doc.getMemorySize();
                this.checkIfOver();
                return true;
            };

            GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDelete);
            commandStack.push(deleteCommand);

            //deleting from the Hashtable and the Trie and Heap

            this.counter--;
            this.memory = memory - doc.getMemorySize();

            this.deleteFromHeap(uri);
            deleteFromTrie(docText, doc);
            store.put(uri, null);
            return true;
        }

        // Make Lambda that returns true and adds to command stack
        Function<URI, Boolean> undoDeleteNothing = (URI putUri) -> {
            return true;
        };

        GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDeleteNothing);
        commandStack.push(deleteCommand);

        return false;
    }

    private boolean deleteDocument2(URI uri){
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot equal null");
        }
        if (store.get(uri) != null) {
            store.put(uri, null);
            return true;
        }
        return false;
    }


    @Override
    public byte[] getDocumentAsPdf(URI uri) {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot equal null");
        }

        if (store.get(uri) != null && store.get(uri) != null) {
            DocumentImpl doc = (DocumentImpl) store.get(uri);
            doc.setLastUseTime(System.nanoTime());
            try{
                HeapCompare compare = heapStorage.get(uri);
                compare.setLastUseTime(doc.getLastUseTime());
                heap.reHeapify(compare);
            }catch (NullPointerException e){
                //adjust counts and check if overload
                //put back in btree
                this.counter++;
                this.memory = this.memory + doc.getMemorySize();
                HeapCompare docHeap = new HeapCompare(uri);
                docHeap.setLastUseTime(doc.getLastUseTime());
                heapStorage.put(uri, docHeap);

                heap.insert(docHeap);
                movedToDisk.remove(uri);
                this.checkIfOver();
                store.put(uri,doc);
            }
            return doc.getDocumentAsPdf();
        }

        return null;
    }

    @Override
    public String getDocumentAsTxt(URI uri) {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot equal null");
        }

        if (store.get(uri) != null) {
            DocumentImpl doc = (DocumentImpl) store.get(uri);
            doc.setLastUseTime(System.nanoTime());
            try{
                HeapCompare compare = heapStorage.get(uri);
                compare.setLastUseTime(doc.getLastUseTime());
                heap.reHeapify(compare);
            }catch (NullPointerException e){
                //adjust counts and check if overload
                //put back in btree
                this.counter++;
                this.memory = this.memory + doc.getMemorySize();
                HeapCompare docHeap = new HeapCompare(uri);
                docHeap.setLastUseTime(doc.getLastUseTime());
                heapStorage.put(uri, docHeap);

                heap.insert(docHeap);
                movedToDisk.remove(uri);
                this.checkIfOver();
                store.put(uri,doc);
            }
            return doc.getDocumentAsTxt();
        }

        return null;
    }

    protected Document getDocument(URI uri) {
        if (store.get(uri) == null) {
            return null;
        }
        if(movedToDisk.contains(uri)){
            return null;
        }
        DocumentImpl doc = (DocumentImpl) store.get(uri);
        return doc;
    }

    @Override
    public void undo() throws IllegalStateException {
        if (commandStack == null || commandStack.size() == 0) {
            throw new IllegalStateException("Nothing To Undo");
        }
        Undoable newCommand = commandStack.pop();
        newCommand.undo();
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {

        if(commandStack.size() == 0){throw new IllegalStateException("URI does not exists");}

        boolean foundURI = false;
        StackImpl<Undoable> tempStack = new StackImpl<Undoable>();
        int size = commandStack.size();

        for (int j = 0; j <= size; j++) {
            
            if(commandStack.peek().getClass().getName().equals("edu.yu.cs.com1320.project.GenericCommand")){

                GenericCommand<URI> command = (GenericCommand<URI>) commandStack.pop();

                if (command.getTarget() == uri) {
                    foundURI = true;
                    command.undo();
                    break;
                }

                tempStack.push(command);

            } else{

                CommandSet<URI> command = (CommandSet<URI>) commandStack.pop();

                if(command.containsTarget(uri)){
                    foundURI = true;
                    command.undo(uri);
                    if(command.size() != 0){
                        tempStack.push(command);
                    }
                    break;
                } else {tempStack.push(command);}
            } 
        }

        if(tempStack.size() != 0){this.undoHelper(tempStack);}
        if(foundURI == false){throw new IllegalStateException("URI does not exist");}
    }

    private void undoHelper(StackImpl<Undoable> tempStack){

        int tempSize = tempStack.size();

        for (int i = 0; i < tempSize; i++){
            Undoable newCommand = tempStack.pop();
            commandStack.push(newCommand);
        }

    }

    @Override
    public List<String> search(String keyword) {

        List<URI> docList = trieImpl.getAllSorted(keyword, new CustomComparator(keyword));
        List<String> docTextList = new ArrayList<>();
        long time = System.nanoTime();

        for(URI doc : docList){
            DocumentImpl doc1 = (DocumentImpl) store.get(doc);
            doc1.setLastUseTime(time);
            try{
                HeapCompare compare = heapStorage.get(doc);
                compare.setLastUseTime(doc1.getLastUseTime());
                heap.reHeapify(compare);
            }catch (NullPointerException e){
                //adjust counts and check if overload
                //put back in btree
                this.counter++;
                this.memory = this.memory + doc1.getMemorySize();
                HeapCompare docHeap = new HeapCompare(doc);
                docHeap.setLastUseTime(doc1.getLastUseTime());
                heapStorage.put(doc, docHeap);

                heap.insert(docHeap);
                movedToDisk.remove(doc);
                store.put(doc,doc1);
                this.checkIfOver();
            }
            docTextList.add(doc1.getDocumentAsTxt());
        }

        return docTextList;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {

        List<URI> docList = trieImpl.getAllSorted(keyword, new CustomComparator(keyword));
        List<byte[]> docPdfList = new ArrayList<>();
        long time = System.nanoTime();

        for(URI doc : docList){
            DocumentImpl doc1 = (DocumentImpl) store.get(doc);
            doc1.setLastUseTime(time);
            try{
                HeapCompare compare = heapStorage.get(doc);
                compare.setLastUseTime(doc1.getLastUseTime());
                heap.reHeapify(compare);
                this.checkIfOver();
            }catch (NullPointerException e){
                //adjust counts and check if overload
                //put back in btree
                this.counter++;
                this.memory = this.memory + doc1.getMemorySize();
                HeapCompare docHeap = new HeapCompare(doc);
                docHeap.setLastUseTime(doc1.getLastUseTime());
                heapStorage.put(doc, docHeap);

                heap.insert(docHeap);
                movedToDisk.remove(doc);
                this.checkIfOver();
                store.put(doc,doc1);
            }
            docPdfList.add(doc1.getDocumentAsPdf());
        }

        return docPdfList;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {

        List<URI> docList = trieImpl.getAllWithPrefixSorted(prefix, new CustomComparatorForPrefix(prefix));
        List<String> docTextList = new ArrayList<>();
        long time = System.nanoTime();

        for(URI doc : docList){
            DocumentImpl doc1 = (DocumentImpl) store.get(doc);
            doc1.setLastUseTime(time);
            try{
                HeapCompare compare = heapStorage.get(doc);
                compare.setLastUseTime(doc1.getLastUseTime());
                heap.reHeapify(compare);
                this.checkIfOver();
            }catch (NullPointerException e){
                //adjust counts and check if overload
                //put back in btree
                this.counter++;
                this.memory = this.memory + doc1.getMemorySize();
                HeapCompare docHeap = new HeapCompare(doc);
                docHeap.setLastUseTime(doc1.getLastUseTime());
                heapStorage.put(doc, docHeap);

                heap.insert(docHeap);
                movedToDisk.remove(doc);
                this.checkIfOver();
                store.put(doc,doc1);
            }
            docTextList.add(doc1.getDocumentAsTxt());
        }

        return docTextList;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {

        List<URI> docList = trieImpl.getAllWithPrefixSorted(prefix, new CustomComparatorForPrefix(prefix));
        List<byte[]> docPdfList = new ArrayList<>();
        long time = System.nanoTime();

        for(URI doc : docList){
            DocumentImpl doc1 = (DocumentImpl) store.get(doc);
            doc1.setLastUseTime(time);
            try{
                HeapCompare compare = heapStorage.get(doc);
                compare.setLastUseTime(doc1.getLastUseTime());
                heap.reHeapify(compare);
                this.checkIfOver();
            }catch (NullPointerException e){
                //adjust counts and check if overload
                //put back in btree
                this.counter++;
                this.memory = this.memory + doc1.getMemorySize();
                HeapCompare docHeap = new HeapCompare(doc);
                docHeap.setLastUseTime(doc1.getLastUseTime());
                heapStorage.put(doc, docHeap);

                heap.insert(docHeap);
                movedToDisk.remove(doc);
                this.checkIfOver();
                store.put(doc,doc1);
            }
            docPdfList.add(doc1.getDocumentAsPdf());
        }

        return docPdfList;
    }

    @Override
    public Set<URI> deleteAll(String key) {

        Set<URI> deletedDocSet = trieImpl.deleteAll(key);
        Set<URI> deletedURISet = new HashSet<>();
        long time = System.nanoTime();

        if(deletedDocSet.isEmpty()){
            try {
                URI emptyURI = new URI("string");
                Function<URI, Boolean> undoDelete = (URI putUri) -> {
                    return true;
                };
                GenericCommand<URI> deleteCommand = new GenericCommand<URI>(emptyURI, undoDelete);
                commandStack.push(deleteCommand);
                return deletedURISet;
            } catch (URISyntaxException e) {}
        }

        CommandSet<URI> commandSet = new CommandSet<>();

        for(URI doc1 : deletedDocSet){
            DocumentImpl doc = (DocumentImpl) store.get(doc1);
            if(!movedToDisk.contains(doc1)){
                this.counter--;
                this.memory = memory - doc.getMemorySize();
            }

            StringTokenizer wordsInDoc = new StringTokenizer(doc.getDocumentAsTxt());
            URI uri = doc.getKey();
            String docText = doc.getDocumentAsTxt();

            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                doc.setLastUseTime(time);
                store.put(uri, doc);
                HeapCompare docHeap = new HeapCompare(doc1);
                docHeap.setLastUseTime(doc.getLastUseTime());
                heapStorage.put(doc1, docHeap);

                heap.insert(docHeap);

                trieImpl.put(docText, doc.getKey());
                this.counter++;
                this.memory = this.memory + doc.getMemorySize();
                this.checkIfOver();
                return true;
            };

            this.deleteFromHeap(doc.getKey());
            deleteDocument2(doc.getKey());
            deletedURISet.add(doc.getKey());

            GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDelete);
            commandSet.addCommand(deleteCommand);

            //tokenizer of document text
            //inner loop that goes through every single word in the document and calls a delete on every word
            while(wordsInDoc.hasMoreTokens()){
                trieImpl.delete(wordsInDoc.nextToken(), doc.getKey());
            }
        }

        commandStack.push(commandSet);

        return deletedURISet;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {

        Set<URI> deletedDocSet = trieImpl.deleteAllWithPrefix(prefix);
        Set<URI> deletedURISet = new HashSet<>();
        long time = System.nanoTime();

        if(deletedDocSet.isEmpty()){
            try {
                URI emptyURI = new URI("string");
                Function<URI, Boolean> undoDelete = (URI putUri) -> {
                    return true;
                };
                GenericCommand<URI> deleteCommand = new GenericCommand<URI>(emptyURI, undoDelete);
                commandStack.push(deleteCommand);
                return deletedURISet;
            } catch (URISyntaxException e) {}
        }

        CommandSet<URI> commandSet = new CommandSet<>();

        for(URI doc1 : deletedDocSet){
            DocumentImpl doc = (DocumentImpl) store.get(doc1);
            if(!movedToDisk.contains(doc1)){
                this.counter--;
                this.memory = memory - doc.getMemorySize();
            }
            StringTokenizer wordsInDoc = new StringTokenizer(doc.getDocumentAsTxt());
            URI uri = doc.getKey();
            String docText = doc.getDocumentAsTxt();

            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                this.counter++;
                this.memory = this.memory + doc.getMemorySize();
                this.checkIfOver();
                doc.setLastUseTime(time);

                store.put(uri, doc);
                HeapCompare docHeap = new HeapCompare(doc1);
                docHeap.setLastUseTime(doc.getLastUseTime());
                heapStorage.put(doc1, docHeap);

                heap.insert(docHeap);
                
                trieImpl.put(docText, doc.getKey());            
                return true;
            };
            
            this.deleteFromHeap(doc.getKey());
            deleteDocument2(doc.getKey());
            deletedURISet.add(doc.getKey());

            GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDelete);
            commandSet.addCommand(deleteCommand);

            while(wordsInDoc.hasMoreTokens()){
                trieImpl.delete(wordsInDoc.nextToken(), doc.getKey());
            }
        }

        commandStack.push(commandSet);

        return deletedURISet;
    }

    private void deleteFromTrie(String message, DocumentImpl doc){

        StringTokenizer wordsToDelete = new StringTokenizer(message);

        while(wordsToDelete.hasMoreTokens()){
            trieImpl.delete(wordsToDelete.nextToken(), doc.getKey());
        }

    }

    private void deleteFromHeap(URI uri){
        DocumentImpl doc = (DocumentImpl) store.get(uri);
        //set the document time to min
        //get the heap object from the hashmap
        //set that heap object time to min
        //then remove the heap object from the heap
        doc.setLastUseTime(minValue);
        try{
            HeapCompare docHeap = heapStorage.get(uri);
            docHeap.setLastUseTime(minValue);
            heap.reHeapify(docHeap);
            heap.removeMin();
        }catch (NullPointerException e){

            // heap.insert(doc);
            // heap.reHeapify(doc);
            movedToDisk.remove(uri);
        }
        // heap.removeMin();
    }

    private class CustomComparator implements Comparator<URI>{

        String word;

        public CustomComparator(String text){
            this.word = text;
        }

        @Override
        public int compare(URI doc1, URI doc2) {
            if(store.get(doc1).wordCount(word) < store.get(doc2).wordCount(word)){
                return 1;
            } 
            else if(store.get(doc1).wordCount(word) > store.get(doc2).wordCount(word)){
                return -1;
            }
            return 0;
        }  
    }

    private class CustomComparatorForPrefix implements Comparator<URI>{

        String word;

        public CustomComparatorForPrefix(String text){
            this.word = text;
        }

        @Override
        public int compare(URI doc1, URI doc2) {
            if(comparePrefix(store.get(doc1), word) < comparePrefix(store.get(doc2), word)){
                return 1;
            } 
            else if(comparePrefix(store.get(doc1), word) > comparePrefix(store.get(doc2), word)){
                return -1;
            }
            return 0;
        }
    }

    private int comparePrefix(Document doc, String word){
        int counter = 0;
        StringTokenizer docText = new StringTokenizer(doc.getDocumentAsTxt());
        while(docText.hasMoreTokens()){
            String token = docText.nextToken();
            if(token.startsWith(word)){
                counter++;
            }
        } 
        return counter;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.maxDocs = limit;
        this.checkIfOver();
        //when you set it you have to check you are over
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.maxMemory = limit;
        this.checkIfOver();
    }

    private void checkIfOver(){
        while(this.counter > this.maxDocs || this.memory > this.maxMemory){
            this.deleteFromMemory();
        }
    }

    private void deleteFromMemory(){

       //deleting from the heap,then hashtable, then trie, then stack
       HeapCompare doc = heap.removeMin();
       DocumentImpl document = (DocumentImpl) store.get(doc.getURI());
       movedToDisk.add(doc.getURI());
       try{
           store.moveToDisk(doc.getURI());
       } catch (Exception e){
           e.printStackTrace();
       }

    //    this.deleteFromTrie(doc.getDocumentAsTxt(), doc);

    //    this.deleteFromStack(doc.getURI());

       this.counter--;
       this.memory = this.memory - document.getMemorySize();
    }

    private void deleteFromStack(URI uri){

        StackImpl<Undoable> tempStack = new StackImpl<Undoable>();
        int size = commandStack.size();

        for (int j = 0; j < size; j++) {
            
            if(commandStack.peek().getClass().getName().equals("edu.yu.cs.com1320.project.GenericCommand")){

                GenericCommand<URI> command = (GenericCommand<URI>) commandStack.pop();

                if (command.getTarget() == uri) {
                    // continue;
                } else{tempStack.push(command);}

            } else{

                CommandSet<URI> command = (CommandSet<URI>) commandStack.pop();

                if(command.containsTarget(uri)){
                    Iterator<GenericCommand<URI>> iterator = command.iterator();
                    while(iterator.hasNext()){
                        if(iterator.next().getTarget() == uri){
                            iterator.remove();
                        }
                    }
                    if(command.size() != 0){
                        tempStack.push(command);
                    }
                    continue;
                } else {tempStack.push(command);}
            } 
        }

        if(tempStack.size() != 0){this.undoHelper(tempStack);}
    }

    private class HeapCompare implements Comparable<HeapCompare>{

        private URI uri;
        private long lasUseTime;

        public HeapCompare(URI uri){
            this.uri = uri;
        }

        private URI getURI(){
            return uri;
        }

        private void setLastUseTime(long time){
            this.lasUseTime = time;
        }

        private long getLastUseTime(){
            return this.lasUseTime;
        }

        @Override
        public int compareTo(HeapCompare o) { 
            // return (int) (store.get(this.uri).getLastUseTime() - store.get(o.getURI()).getLastUseTime());
            return store.get(this.uri).compareTo(store.get(o.getURI()));
        }
        
    }

}