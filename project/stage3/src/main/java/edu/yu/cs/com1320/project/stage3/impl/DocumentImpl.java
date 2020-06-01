package edu.yu.cs.com1320.project.stage3.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

// import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage3.Document;

public class DocumentImpl implements Document {

    // HashTableImpl<String,Integer> docTextHashTable2 = new HashTableImpl<String,Integer>(); 
    HashMap<String,Integer> docTextHashTable = new HashMap<String,Integer>();

    URI uriInstance;
    String stringInstance;
    int txtInstance;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] byteArray;

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        if(uri == null || txt == null ||  pdfBytes == null){throw new IllegalArgumentException("Fields cannot be null");}
        this.uriInstance = uri;
        this.stringInstance = txt;
        this.txtInstance = txtHash;
        this.byteArray = pdfBytes;
        HashTableWordSorter();
    }

    public DocumentImpl(URI uri, String txt, int txtHash) {
        if(uri == null || txt == null){throw new IllegalArgumentException("Fields cannot be null");}
        this.uriInstance = uri;
        this.stringInstance = txt;
        this.txtInstance = txtHash;
        HashTableWordSorter();
    }

    @Override
    public byte[] getDocumentAsPdf() {
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
            return byteArray.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }

    @Override
    public String getDocumentAsTxt() {
        return stringInstance;
    }

    @Override
    public int getDocumentTextHashCode() {
        return stringInstance.hashCode();
    }

    @Override
    public URI getKey() {
        return uriInstance;
    }

    @Override
    public int wordCount(String word) {
        String newWord = word.toUpperCase();
        word = word.replaceAll("[^a-zA-Z0-9 ]", "");
        return docTextHashTable.get(newWord);
        // int counter = 0;
        // StringTokenizer wordCount = new StringTokenizer(stringInstance);
        // while(wordCount.hasMoreTokens()){
        //     if(wordCount.nextToken().equals(word)){
        //         counter++;
        //     }
        // }
        // return counter;
    }

    private void HashTableWordSorter(){
        StringTokenizer words = new StringTokenizer(stringInstance);
        int one = 1;
        while(words.hasMoreTokens()){
            String word = words.nextToken().toUpperCase();
            word = word.replaceAll("[^a-zA-Z0-9 ]", "");
            if(docTextHashTable.get(word) == null){
                docTextHashTable.put(word,one);
            }
            else{
                int number = docTextHashTable.get(word);
                docTextHashTable.put(word, number + 1);
            }
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baos == null) ? 0 : baos.hashCode());
        result = prime * result + ((stringInstance == null) ? 0 : stringInstance.hashCode());
        result = prime * result + txtInstance;
        result = prime * result + ((uriInstance == null) ? 0 : uriInstance.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DocumentImpl other = (DocumentImpl) obj;
        if (baos == null) {
            if (other.baos != null)
                return false;
        } else if (!baos.equals(other.baos))
            return false;
        if (stringInstance == null) {
            if (other.stringInstance != null)
                return false;
        } else if (!stringInstance.equals(other.stringInstance))
            return false;
        if (txtInstance != other.txtInstance)
            return false;
        if (uriInstance == null) {
            if (other.uriInstance != null)
                return false;
        } else if (!uriInstance.equals(other.uriInstance))
            return false;
        return true;
    }

}