package edu.yu.cs.com1320.project.stage1;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.*;
import org.junit.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore.*;
import edu.yu.cs.com1320.project.stage1.impl.*;

public class HashTableImplTest {

    HashTableImpl<String, String> store = new HashTableImpl<String, String>();

    @Test
    public void getFromHashTable() {
        store.put("Elie", "This should work");
        store.put("HI", "I hope this works");

        assertEquals("This should give back some string", "This should work", store.get("Elie"));
        assertEquals("This should give back some string", "I hope this works", store.get("HI"));
        assertEquals("This should give back some string", "Let's do this", store.put("Elie", "Let's do this"));

    }

    @Test
    public void testRemoveMethod() {
        store.put("Elie", "This should work");
        assertEquals("This should give back some string", "This should work", store.get("Elie"));
        assertEquals("This should give back some string", "This should work", store.put("Elie", null));
    }

    @Test
    public void testReplaceValueInExistingKey() {
        store.put("Elie", "This should work");
        store.put("Elie", "New Value Inserted");
        assertEquals("This should give back some string", "New Value Inserted", store.get("Elie"));
    }

    @Test
    public void hashTableGeneralTest() {
        store.put("c", "5");
        store.put("Ea", "6");
        store.put("FB", "8");
        store.put("io", "9");
        store.put("fh", "1");
        store.put("de", "2");
        store.put("adf", "3");

        assertEquals("This should give back some string", "5", store.get("c"));
        assertEquals("This should give back some string", "6", store.get("Ea"));
        assertEquals("This should give back some string", "8", store.get("FB"));
        assertEquals("This should give back some string", "9", store.get("io"));
        assertEquals("This should give back some string", "1", store.get("fh"));
        assertEquals("This should give back some string", "2", store.get("de"));
        assertEquals("This should give back some string", "3", store.get("adf"));
        assertEquals("This should give back some string", null, store.get("five"));
        assertEquals("This should give back some string", null, store.put("five", null));
    }

    @Test
    public void testDocumentPut() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        assertEquals("This should give back some integer", 0, document.putDocument(targetStream2, uri2, DocumentFormat.TXT));
        assertEquals("This should give back some integer", "Ea", document.getDocumentAsTxt(uri2));
    }

    @Test
    public void removeDocument() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        assertEquals("This should give back some integer", 0, document.putDocument(targetStream2, uri2, DocumentFormat.TXT));
        assertEquals("This should give back some string", "Ea", document.getDocumentAsTxt(uri2));
        assertEquals("This should give back some boolean", true, document.deleteDocument(uri2));
        assertEquals("This should give back some boolean", false, document.deleteDocument(uri2));
        assertEquals("This should give back some string", null, document.getDocumentAsTxt(uri2));
    }

    @Test
    public void documentGetterTest() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        String message = "this is a message";
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        DocumentImpl documentStuff = new DocumentImpl(uri2, message, message.hashCode());
        assertEquals("This should give back some string", "this is a message", documentStuff.getDocumentAsTxt());
        assertEquals("This should give back some string", message.hashCode(), documentStuff.getDocumentTextHashCode());
        assertEquals("This should give back some string", uri2, documentStuff.getKey());
    }

    @Test
    public void nullInputStreamWithExistingURI() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        InputStream nullIS = null;
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        assertEquals("This should give back some integer", initialString2.hashCode(), document.putDocument(nullIS, uri2, DocumentFormat.TXT));
        assertEquals("This should give back some integer", null, document.getDocumentAsTxt(uri2));  
    }

    @Test
    public void nullInputStreamWithoutExistingURI() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        URI uri2 = new URI("hello");
        InputStream nullIS = null;
        assertEquals("This should give back some integer", 0, document.putDocument(nullIS, uri2, DocumentFormat.TXT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUriForPutDocument() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = null;
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUriGetterTest() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        URI uri3 = null;
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.getDocumentAsTxt(uri3);
    }

    @Test
    public void testGetDocumentPutAsPDF() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String message = "This is a message";

        try {
            PDDocument doc = new PDDocument();
			PDPage page = new PDPage();
			doc.addPage(page);
			PDFont font = PDType1Font.HELVETICA_BOLD;
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(message);
            contents.endText();
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            contents.close();
			doc.save(byteArray);
            doc.close();
            URI uri2 = new URI("hello");
            byte[] tempByteArray = byteArray.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(byteArray.toByteArray());
            document.putDocument(inputStream, uri2, DocumentFormat.PDF);
            assertEquals("This should give back some integer", tempByteArray.length, document.getDocumentAsPdf(uri2).length);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void docPutWithSameUriAndText() throws URISyntaxException {
        String initialString = "This is a message";
		String initialString2 = "This is a message";
		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
		InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
		URI uri = new URI("hello");
		// URI uri2 = new URI("hello");
        DocumentStoreImpl document = new DocumentStoreImpl();
        assertEquals("This should give back some integer", 0, document.putDocument(targetStream, uri, DocumentFormat.TXT));
        assertEquals("This should give back some integer", initialString.hashCode(), document.putDocument(targetStream2, uri, DocumentFormat.TXT));

        
    }

}