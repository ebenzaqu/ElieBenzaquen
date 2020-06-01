package edu.yu.cs.com1320.project.stage4.impl;

// import java.io.ByteArrayInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
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
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;

@SuppressWarnings("unchecked")
public class DocumentStoreImpl implements DocumentStore {

    public DocumentStoreImpl(){
        this.minValue = System.nanoTime();
    }

    HashTableImpl<URI, DocumentImpl> store = new HashTableImpl<URI, DocumentImpl>();
    StackImpl<Undoable> commandStack = new StackImpl<Undoable>();
    TrieImpl<DocumentImpl> trieImpl = new TrieImpl<DocumentImpl>();
    MinHeapImpl<Document> heap = new MinHeapImpl<Document>();

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

            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == message.hashCode()) {

                // Make Lambda that returns true and adds to command stack
                Function<URI, Boolean> repeatDoc = (URI putUri) -> {
                    return true;
                };

                GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, repeatDoc);
                commandStack.push(deleteCommand);

                DocumentImpl doc = store.get(uri);
                doc.setLastUseTime(System.nanoTime());

                return message.hashCode();
            }

            putDocumentAsTXT(uri, message);
        }

        if (format == DocumentFormat.PDF) {
            putDocumentAsPDF(uri, byteArray);
        }

        return 0;
    }

    private int putDocumentAsTXT(URI uri, String message) {

        DocumentImpl document = new DocumentImpl(uri, message, message.hashCode());

        // if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == message.hashCode()) {
        //     return message.hashCode();
        // }

        if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != message.hashCode()) {

            DocumentImpl oldDoc = store.get(uri);
            oldDoc.setLastUseTime(System.nanoTime());
            //the lambda is supposed to replace something in the trie so I delete the document from the trie and then put the new one in
            //done
            Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
                this.memory = this.memory + oldDoc.getMemorySize() - document.getMemorySize();
                
                this.deleteFromHeap(uri);
                oldDoc.setLastUseTime(System.nanoTime());
                heap.insert(oldDoc);
                store.put(uri, oldDoc);
                deleteFromTrie(message, document);
                trieImpl.put(message,oldDoc);
                this.checkIfOver();
                return true;
            };
            
            //delete the onld value from the hashtable and trie and put a new one in
            //done
            GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoReplace);
            commandStack.push(putCommand);

            this.memory = this.memory - oldDoc.getMemorySize() + document.getMemorySize();
            this.deleteFromHeap(uri);
            document.setLastUseTime(System.nanoTime());
            heap.insert(document);
            deleteFromTrie(message, oldDoc);
            trieImpl.put(message, document);
            store.put(uri, document);

            this.checkIfOver();
            return oldDoc.getDocumentTextHashCode();
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

        heap.insert(document);
        trieImpl.put(message, document);
        store.put(uri, document);

        this.checkIfOver();

        return 0;
    }

    private int putDocumentAsPDF(URI uri, byte[] byteArray) {

        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(PDDocument.load(byteArray)).trim();
            DocumentImpl doc = new DocumentImpl(uri, text, text.hashCode(), byteArray);

            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == text.hashCode()) {

                DocumentImpl document = store.get(uri);
                document.setLastUseTime(System.nanoTime());

                return text.hashCode();
            }

            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != text.hashCode()) {

                DocumentImpl oldDoc = store.get(uri);
                oldDoc.setLastUseTime(System.nanoTime());

                Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
                    this.memory = this.memory + oldDoc.getMemorySize() - doc.getMemorySize();

                    this.deleteFromHeap(uri);
                    oldDoc.setLastUseTime(System.nanoTime());
                    heap.insert(oldDoc);
                    store.put(uri, oldDoc);
                    deleteFromTrie(text, doc);
                    trieImpl.put(text,oldDoc);

                    this.checkIfOver();
                    return true;
                };
                GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoReplace);
                commandStack.push(putCommand);

                // int temp = store.get(uri).getDocumentTextHashCode();
                this.memory = this.memory - oldDoc.getMemorySize() + doc.getMemorySize();

                this.deleteFromHeap(uri);
                doc.setLastUseTime(System.nanoTime());
                heap.insert(doc);
                deleteFromTrie(text, oldDoc);
                trieImpl.put(text,doc);
                store.put(uri, doc);
                this.checkIfOver();
                return oldDoc.getDocumentTextHashCode();
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

            heap.insert(doc);
            trieImpl.put(text, doc);
            store.put(uri, doc);

            this.checkIfOver();

            return 0;

        } catch (IOException e) {e.printStackTrace();}
        return 0;
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
            DocumentImpl oldDoc = store.get(uri);
            String docText = oldDoc.getDocumentAsTxt();
            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                oldDoc.setLastUseTime(System.nanoTime());
                heap.insert(oldDoc);
                store.put(uri, oldDoc);
                trieImpl.put(docText, oldDoc);
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
            DocumentImpl doc = store.get(uri);
            String docText = doc.getDocumentAsTxt();

            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                doc.setLastUseTime(System.nanoTime());
                heap.insert(doc);
                store.put(uri, doc);
                trieImpl.put(docText, doc);
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
            DocumentImpl doc = store.get(uri);
            doc.setLastUseTime(System.nanoTime());
            heap.reHeapify(doc);
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
            DocumentImpl doc = store.get(uri);
            doc.setLastUseTime(System.nanoTime());
            heap.reHeapify(doc);
            return doc.getDocumentAsTxt();
        }

        return null;
    }

    protected Document getDocument(URI uri) {
        if (store.get(uri) == null) {
            return null;
        }
        DocumentImpl doc = store.get(uri);
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

        List<DocumentImpl> docList = trieImpl.getAllSorted(keyword, new CustomComparator(keyword));
        List<String> docTextList = new ArrayList<>();
        long time = System.nanoTime();

        for(Document doc : docList){
            doc.setLastUseTime(time);
            heap.reHeapify(doc);
            docTextList.add(doc.getDocumentAsTxt());
        }

        return docTextList;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {

        List<DocumentImpl> docList = trieImpl.getAllSorted(keyword, new CustomComparator(keyword));
        List<byte[]> docPdfList = new ArrayList<>();
        long time = System.nanoTime();

        for(Document doc : docList){
            doc.setLastUseTime(time);
            heap.reHeapify(doc);
            docPdfList.add(doc.getDocumentAsPdf());
        }

        return docPdfList;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {

        List<DocumentImpl> docList = trieImpl.getAllWithPrefixSorted(prefix, new CustomComparatorForPrefix(prefix));
        List<String> docTextList = new ArrayList<>();
        long time = System.nanoTime();

        for(Document doc : docList){
            doc.setLastUseTime(time);
            heap.reHeapify(doc);
            docTextList.add(doc.getDocumentAsTxt());
        }

        return docTextList;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {

        List<DocumentImpl> docList = trieImpl.getAllWithPrefixSorted(prefix, new CustomComparatorForPrefix(prefix));
        List<byte[]> docPdfList = new ArrayList<>();
        long time = System.nanoTime();

        for(Document doc : docList){
            doc.setLastUseTime(time);
            heap.reHeapify(doc);
            docPdfList.add(doc.getDocumentAsPdf());
        }

        return docPdfList;
    }

    @Override
    public Set<URI> deleteAll(String key) {

        Set<DocumentImpl> deletedDocSet = trieImpl.deleteAll(key);
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

        for(DocumentImpl doc : deletedDocSet){
            this.counter--;
            this.memory = memory - doc.getMemorySize();

            StringTokenizer wordsInDoc = new StringTokenizer(doc.getDocumentAsTxt());
            URI uri = doc.getKey();
            String docText = doc.getDocumentAsTxt();

            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                doc.setLastUseTime(time);
                heap.insert(doc);
                store.put(uri, doc);
                trieImpl.put(docText, doc);
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
                trieImpl.delete(wordsInDoc.nextToken(), doc);
            }
        }

        commandStack.push(commandSet);

        return deletedURISet;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {

        Set<DocumentImpl> deletedDocSet = trieImpl.deleteAllWithPrefix(prefix);
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

        for(DocumentImpl doc : deletedDocSet){

            this.counter--;
            this.memory = memory - doc.getMemorySize();

            StringTokenizer wordsInDoc = new StringTokenizer(doc.getDocumentAsTxt());
            URI uri = doc.getKey();
            String docText = doc.getDocumentAsTxt();

            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                doc.setLastUseTime(time);
                heap.insert(doc);
                store.put(uri, doc);
                trieImpl.put(docText, doc);
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

            while(wordsInDoc.hasMoreTokens()){
                trieImpl.delete(wordsInDoc.nextToken(), doc);
            }
        }

        commandStack.push(commandSet);

        return deletedURISet;
    }

    private void deleteFromTrie(String message, DocumentImpl doc){

        StringTokenizer wordsToDelete = new StringTokenizer(message);

        while(wordsToDelete.hasMoreTokens()){
            trieImpl.delete(wordsToDelete.nextToken(), doc);
        }

    }

    private void deleteFromHeap(URI uri){
        DocumentImpl doc = store.get(uri);
        doc.setLastUseTime(minValue);
        heap.reHeapify(doc);
        heap.removeMin();
    }

    public class CustomComparator implements Comparator<DocumentImpl>{

        String word;

        public CustomComparator(String text){
            this.word = text;
        }

        @Override
        public int compare(DocumentImpl doc1, DocumentImpl doc2) {
            if(doc1.wordCount(word) < doc2.wordCount(word)){
                return 1;
            } 
            else if(doc1.wordCount(word) > doc2.wordCount(word)){
                return -1;
            }
            return 0;
        }  
    }

    public class CustomComparatorForPrefix implements Comparator<DocumentImpl>{

        String word;

        public CustomComparatorForPrefix(String text){
            this.word = text;
        }

        @Override
        public int compare(DocumentImpl doc1, DocumentImpl doc2) {
            if(comparePrefix(doc1, word) < comparePrefix(doc2, word)){
                return 1;
            } 
            else if(comparePrefix(doc1, word) > comparePrefix(doc2, word)){
                return -1;
            }
            return 0;
        }
    }

    private int comparePrefix(DocumentImpl doc, String word){
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
        DocumentImpl doc = (DocumentImpl) heap.removeMin();
        store.put(doc.getKey(), null);
        this.deleteFromTrie(doc.getDocumentAsTxt(), doc);
        this.deleteFromStack(doc.getKey());

        this.counter--;
        this.memory = this.memory - doc.getMemorySize();
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

	public static void main(String[] args) throws URISyntaxException {
		String initialString = "This is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

        URI uri = new URI("Ea");
        URI uri2 = new URI("E");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
        document.setMaxDocumentCount(1);
        document.setMaxDocumentBytes(10000);
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        // document.undo(uri2);
        System.out.println(document.search("this"));
	}

}