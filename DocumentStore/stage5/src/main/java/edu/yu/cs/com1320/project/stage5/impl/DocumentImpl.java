package edu.yu.cs.com1320.project.stage5.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import java.io.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


// import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage5.Document;

public class DocumentImpl implements Document {

    // HashTableImpl<String,Integer> docTextHashTable2 = new
    // HashTableImpl<String,Integer>();
    private HashMap<String, Integer> docTextHashTable = new HashMap<String, Integer>();
    private URI uriInstance;
    private String stringInstance;
    private int hashCodeOfText;
    // private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private byte[] byteArray;
    private long timeOfUse;

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        if (uri == null || txt == null || pdfBytes == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        this.uriInstance = uri;
        this.stringInstance = txt;
        this.hashCodeOfText = txtHash;
        this.byteArray = pdfBytes;
        HashTableWordSorter();
    }

    public DocumentImpl(URI uri, String txt, int txtHash) {
        if (uri == null || txt == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        this.uriInstance = uri;
        this.stringInstance = txt;
        this.hashCodeOfText = txtHash;
        HashTableWordSorter();
        makeByteArray();
    }

    public DocumentImpl(URI uri, String txt, int txtHash, boolean bool) {
        if (uri == null || txt == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        this.uriInstance = uri;
        this.stringInstance = txt;
        this.hashCodeOfText = txtHash;
        makeByteArray();
    }

    @Override
    public byte[] getDocumentAsPdf() {
        return byteArray;
    }

    private void makeByteArray() {
        try {
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDFont font = PDType1Font.HELVETICA_BOLD;
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(stringInstance);
            contents.endText();
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            contents.close();
            doc.save(byteArray);
            doc.close();
            this.byteArray = byteArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected int getMemorySize() {
        return byteArray.length + stringInstance.getBytes().length;
    }

    @Override
    public String getDocumentAsTxt() {
        return stringInstance;
    }

    @Override
    public int getDocumentTextHashCode() {
        return hashCodeOfText;
    }

    @Override
    public URI getKey() {
        return uriInstance;
    }

    @Override
    public int compareTo(Document doc) {
        return (int) (this.timeOfUse - doc.getLastUseTime());        
    }

    @Override
    public long getLastUseTime() {
        return timeOfUse;
    }

    @Override
    public void setLastUseTime(long timeInMilliseconds) {
        timeOfUse = timeInMilliseconds;
    }

    @Override
    public int wordCount(String word) {
        String newWord = word.toUpperCase();
        newWord = newWord.replaceAll("[^a-zA-Z0-9\\s]", "");
        if(docTextHashTable.get(newWord) == null){
            return 0;
        }
        return docTextHashTable.get(newWord);
    }

    private void HashTableWordSorter() {
        StringTokenizer words = new StringTokenizer(stringInstance);
        int one = 1;
        while (words.hasMoreTokens()) {
            String word = words.nextToken().toUpperCase();
            word = word.replaceAll("[^a-zA-Z0-9\\s]", "");
            if (docTextHashTable.get(word) == null) {
                docTextHashTable.put(word, one);
            } else {
                int number = docTextHashTable.get(word);
                docTextHashTable.put(word, number + 1);
            }
        }
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.docTextHashTable;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.docTextHashTable = (HashMap<String, Integer>) wordMap;
    }

}