package edu.yu.cs.com1320.project.stage3.impl;

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
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
// import java.net.URISyntaxException;
// import java.net.URISyntaxException;
import java.util.function.Function;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
// import edu.yu.cs.com1320.project.stage2.Document;
// import edu.yu.cs.com1320.project.stage2.*;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

@SuppressWarnings("unchecked")
public class DocumentStoreImpl implements DocumentStore {

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

    HashTableImpl<URI, DocumentImpl> store = new HashTableImpl<URI, DocumentImpl>();
    StackImpl<Undoable> commandStack = new StackImpl<Undoable>();
    TrieImpl<DocumentImpl> trieImpl = new TrieImpl<DocumentImpl>();

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
        if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == message.hashCode()) {
            return message.hashCode();
        }
        if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != message.hashCode()) {
            DocumentImpl oldDoc = store.get(uri);
            //the lambda is supposed to replace something in the trie so I delete the document from the trie and then put the new one in
            //done
            Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
                store.put(replacedUri, oldDoc);
                deleteFromTrie(message, document);
                trieImpl.put(message,oldDoc);
                return true;
            };
            //delete the onld value from the hashtable and trie and put a new one in
            //done
            GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoReplace);
            commandStack.push(putCommand);
            // int temp = store.get(uri).getDocumentTextHashCode();
            deleteFromTrie(message, oldDoc);
            trieImpl.put(message, document);
            store.put(uri, document);
            return oldDoc.getDocumentTextHashCode();
        }
        Function<URI, Boolean> undoStamPutTXT = (URI deleteUri) -> {
            //done////////////////
            store.put(deleteUri, null);
            deleteFromTrie(message, document);
            return true;
        };
        GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoStamPutTXT);
        commandStack.push(putCommand);
        trieImpl.put(message, document);
        store.put(uri, document);
        return 0;
    }

    private int putDocumentAsPDF(URI uri, byte[] byteArray) {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(PDDocument.load(byteArray)).trim();
            DocumentImpl doc = new DocumentImpl(uri, text, text.hashCode(), byteArray);
            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == text.hashCode()) {
                return text.hashCode();
            }
            if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != text.hashCode()) {
                DocumentImpl oldDoc = store.get(uri);
                Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
                    store.put(uri, oldDoc);
                    deleteFromTrie(text, doc);
                    trieImpl.put(text,oldDoc);
                    return true;
                };
                GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoReplace);
                commandStack.push(putCommand);
                // int temp = store.get(uri).getDocumentTextHashCode();
                deleteFromTrie(text, doc);
                trieImpl.put(text,doc);
                store.put(uri, doc);
                return oldDoc.getDocumentTextHashCode();
            }
            Function<URI, Boolean> undoStamPutPDF = (URI deleteUri) -> {
                deleteFromTrie(text, doc);
                store.put(uri, null);
                return true;
            };
            GenericCommand<URI> putCommand = new GenericCommand<URI>(uri, undoStamPutPDF);
            commandStack.push(putCommand);
            trieImpl.put(text, doc);
            store.put(uri, doc);
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
                store.put(uri, oldDoc);
                trieImpl.put(docText, oldDoc);
                return true;
            };
            GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDelete);
            commandStack.push(deleteCommand);
            int temp = store.get(uri).getDocumentTextHashCode();
            //delete from both hashtable and Trie
            store.put(uri, null);
            deleteFromTrie(docText, oldDoc);
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
                store.put(uri, doc);
                trieImpl.put(docText, doc);
                return true;
            };
            GenericCommand<URI> deleteCommand = new GenericCommand<URI>(uri, undoDelete);
            commandStack.push(deleteCommand);
            //deleting from the Hashtable and the Trie
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
            return doc.getDocumentAsTxt();
        }
        return null;
    }

    protected Document getDocument(URI uri) {
        if (store.get(uri) == null) {
            return null;
        }
        return store.get(uri);
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
        for(Document doc : docList){
            docTextList.add(doc.getDocumentAsTxt());
        }
        return docTextList;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        List<DocumentImpl> docList = trieImpl.getAllSorted(keyword, new CustomComparator(keyword));
        List<byte[]> docPdfList = new ArrayList<>();
        for(Document doc : docList){
            docPdfList.add(doc.getDocumentAsPdf());
        }
        return docPdfList;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {
        List<DocumentImpl> docList = trieImpl.getAllWithPrefixSorted(prefix, new CustomComparatorForPrefix(prefix));
        List<String> docTextList = new ArrayList<>();
        for(Document doc : docList){
            docTextList.add(doc.getDocumentAsTxt());
        }
        return docTextList;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {
        List<DocumentImpl> docList = trieImpl.getAllWithPrefixSorted(prefix, new CustomComparatorForPrefix(prefix));
        List<byte[]> docPdfList = new ArrayList<>();
        for(Document doc : docList){
            docPdfList.add(doc.getDocumentAsPdf());
        }
        return docPdfList;
    }

    @Override
    public Set<URI> deleteAll(String key) {
        Set<DocumentImpl> deletedDocSet = trieImpl.deleteAll(key);
        Set<URI> deletedURISet = new HashSet<>();
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
            deleteDocument2(doc.getKey());
            deletedURISet.add(doc.getKey());
            StringTokenizer wordsInDoc = new StringTokenizer(doc.getDocumentAsTxt());
            URI uri = doc.getKey();
            String docText = doc.getDocumentAsTxt();
            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                store.put(uri, doc);
                trieImpl.put(docText, doc);
                return true;
            };
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
            deleteDocument2(doc.getKey());
            deletedURISet.add(doc.getKey());
            StringTokenizer wordsInDoc = new StringTokenizer(doc.getDocumentAsTxt());
            URI uri = doc.getKey();
            String docText = doc.getDocumentAsTxt();
            Function<URI, Boolean> undoDelete = (URI putUri) -> {
                store.put(uri, doc);
                trieImpl.put(docText, doc);
                return true;
            };
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

	// private boolean uriExistsWithSameHashCode(URI uri, DocumentImpl document){
	// if(store.get(uri) != null && store.get(uri).hashCode() ==
	// document.hashCode()){
	// return true;
	// }
	// return false;

	// }

	public static void main(String[] args) throws URISyntaxException {
		String initialString = "thi is message a message";
        String initialString2 = " is the second message";
        String initialString3 = "the third me$%&ssage is of kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());
        InputStream targetStream4 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        URI uri4 = new URI("String");
        DocumentStoreImpl document = new DocumentStoreImpl();
        // document.undo(null);
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.putDocument(targetStream4, uri4, DocumentFormat.TXT);
        System.out.println(document.search(" "));
        System.out.println(document.deleteAll("third"));
        System.out.println(document.search("message"));
        // document.undo();
        System.out.println(document.search("message"));
        // document.undo(uri);
        // System.out.println((document.deleteAllWithPrefix(null)));
        // System.out.println(document.search(null));
	}
}