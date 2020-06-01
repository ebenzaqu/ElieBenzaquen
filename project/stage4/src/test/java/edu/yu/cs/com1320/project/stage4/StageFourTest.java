package edu.yu.cs.com1320.project.stage4;

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
import java.util.ArrayList;
import java.util.List;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage4.DocumentStore.*;
import edu.yu.cs.com1320.project.stage4.impl.*;

public class StageFourTest {

    HashTableImpl<String, String> store = new HashTableImpl<String, String>();
    MinHeapImpl<Integer> heap = new MinHeapImpl<Integer>();
    MinHeapImpl<Document> docDeap = new MinHeapImpl<Document>();

    @Test
    public void getFromHashTable() {
        store.put("Elie", "This should work");
        store.put("HI", "I hope this works");

        assertEquals("This should give back some string", "This should work", store.get("Elie"));
        assertEquals("This should give back some string", "I hope this works", store.get("HI"));
        assertEquals("This should give back some string", "This should work", store.put("Elie", "Let's do this"));

    }

    @Test
    public void testSeparateChaining () {
        HashTableImpl<Integer, String> table = new HashTableImpl<Integer, String>();
        for(int i = 0; i <= 23; i++) {
            table.put(i, "entry " + i);
        }
        assertEquals("entry 12",table.put(12, "entry 12+1"));
        assertEquals("entry 12+1",table.get(12));
        assertEquals("entry 23",table.get(23));
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
		// String initialString2 = "This is a message";
		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
		InputStream targetStream2 = new ByteArrayInputStream(initialString.getBytes());
		URI uri = new URI("hello");
		// URI uri2 = new URI("hello");
        DocumentStoreImpl document = new DocumentStoreImpl();
        assertEquals("This should give back some integer", 0, document.putDocument(targetStream, uri, DocumentFormat.TXT));
        assertEquals("This should give back some integer", initialString.hashCode(), document.putDocument(targetStream2, uri, DocumentFormat.TXT));
    }

    @Test
    public void putDocThenUndo() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.undo();
        assertEquals("This should give back some integer", false, document.deleteDocument(uri2));
    }

    @Test
    public void deleteDocThenUndo() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        URI uri2 = new URI("hello");
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.deleteDocument(uri2);
        document.undo();
        assertEquals("This should give back some integer", true, document.deleteDocument(uri2));
    }

    @Test
    public void replaceDocThenUndo() throws URISyntaxException {
        DocumentStoreImpl document = new DocumentStoreImpl();
        String initialString2 = "Ea";
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        String initialString = "ada";
        InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        URI uri2 = new URI("hello");
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream, uri2, DocumentFormat.TXT);
        document.undo();
        // assertEquals("This should give back some integer", "Ea", document.(uri2).getDocumentAsTxt());
    }

    @Test
    public void undoWithUri() throws URISyntaxException {
        String initialString = "This is a message";
		String initialString2 = "This is the second message";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
		InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());

		URI uri = new URI("Ea");
		URI uri2 = new URI("kkd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
		document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
		// document.undo(uri);
		document.undo(uri);
    }

    @Test
    public void inputTrieTestForSearch() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        List<String> list = new ArrayList<>();
        list.add(initialString);
        list.add(initialString2);
        list.add(initialString3);
        assertEquals("should give back a list of the text of both docs",list, document.search("tHis"));
    }

    @Test
    public void InputTrieTestForSearchWithPrefix() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is isnt of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        List<String> list = new ArrayList<>();
        list.add(initialString3);
        list.add(initialString);
        list.add(initialString2);
        assertEquals("should give back a list of the text of both docs",list, document.searchByPrefix("is"));
    }

    @Test
    public void TrieTestForSearchAfterReplaced() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        List<String> list = new ArrayList<>();
        list.add(initialString2);
        list.add(initialString3);
        assertEquals("should give back a list of the second doc",list, document.search("is"));
    }

    @Test
    public void PreviousTestAndThenUndo() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
        document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri, DocumentFormat.TXT);
        document.undo();
        List<String> list = new ArrayList<>();
        // list.add(document.getDocumentAsPdf(uri2));
        list.add(initialString3);
        list.add(initialString);
        assertEquals("should give back a list of the first doc",list, document.search("is"));
    }

    @Test
    public void DeleteDocumentFromTrie() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteDocument(uri);
        List<String> list = new ArrayList<>();
        list.add(initialString3);
        list.add(initialString2);
        assertEquals("should give back a list of the second doc",list, document.search("is"));
    }

    @Test
    public void PreviousTestAndThenUndoForDelete() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteDocument(uri);
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString2);
        list.add(initialString3);
        list.add(initialString);
        assertEquals("should give back a list of both of the docs",list, document.search("is"));
    }

    @Test
    public void DoubleStamUndo() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.undo();
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        assertEquals("should give back an empty list",list, document.search("is"));
    }

    @Test
    public void UndoWithURIForTwoPuts() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.undo(uri);
        List<String> list = new ArrayList<>();
        // list.add(document.getDocumentAsPdf(uri2));
        list.add(initialString2);
        list.add(initialString3);
        assertEquals("should give back a list of the second doc",list, document.search("is"));
    }

    @Test
    public void UndoWithURIForTheSecondPut() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.undo(uri2);
        List<String> list = new ArrayList<>();
        // list.add(document.getDocumentAsPdf(uri2));
        list.add(initialString);
        list.add(initialString3);
        assertEquals("should give back a list of the first doc",list, document.search("is"));
    }

    @Test
    public void UndoWithURIAndThenStamUndo() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.undo(uri2);
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        assertEquals("should give back an empty list",list, document.search("is"));
    }

    @Test
    public void UndoWithURIThenDeleteThenUndo() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.undo(uri2);
        document.deleteDocument(uri);
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString3);
        list.add(initialString);
        assertEquals("should give back a list with the first document",list, document.search("is"));
    }

    @Test
    public void DeleteAllStamTest() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteAll("this");
        List<String> list = new ArrayList<>();
        assertEquals("should give back a list that is empty",list, document.search("is"));
    }

    @Test
    public void DeleteAllThenUndo() throws URISyntaxException {
        String initialString = "This is is is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteAll("this");
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        list.add(initialString2);
        list.add(initialString3);
        assertEquals("should give back a list that is empty",list, document.search("is"));
    }

    @Test
    public void InsertAndRemoveMinHeap(){
        heap.insert(54);
        heap.insert(20);
        heap.insert(25);
        heap.insert(10);
        heap.insert(17);
        heap.insert(-5);
        heap.insert(30);
        heap.reHeapify(20);
        assertEquals("should return 54",-5, (int) heap.removeMin());
        assertEquals("should return 54",10, (int) heap.removeMin());
        assertEquals("should return 54",17, (int) heap.removeMin());
        assertEquals("should return 54",20, (int) heap.removeMin());
        assertEquals("should return 54",25, (int) heap.removeMin());
        assertEquals("should return 54",30, (int) heap.removeMin());
        assertEquals("should return 54",54, (int) heap.removeMin());
    }

    @Test
    public void InsertDocumentsIntoHeapWithCountLimit() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
        document.setMaxDocumentCount(1);
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        List<String> list = new ArrayList<>();
        list.add(initialString3);
        assertEquals("should give back an empty list",list, document.search("is"));
    }

    @Test
    public void PutWithUndoWithCountLimitInTheHeap() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteDocument(uri);
        document.deleteDocument(uri2);
        document.deleteDocument(uri3);
        document.setMaxDocumentCount(1);
        document.undo();
        document.undo();
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        assertEquals("should give back an empty list",list, document.search("is"));
    }

    @Test
    public void DeleteAllThenUndoWithCountLimit() throws URISyntaxException {
        String initialString = "This is is is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteAll("this");
        document.setMaxDocumentCount(1);
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        assertEquals("should give back a list with one string",list, document.search("is"));
    }

    @Test
    public void InsertDocumentsIntoHeapWithSizeLimit() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
        document.setMaxDocumentBytes(1000);
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        List<String> list = new ArrayList<>();
        list.add(initialString3);
        assertEquals("should give back a list with one string",list, document.search("is"));
    }

    @Test
    public void PutWithUndoWithSizeLimitInTheHeap() throws URISyntaxException {
        String initialString = "This is a message";
        String initialString2 = "This is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteDocument(uri);
        document.deleteDocument(uri2);
        document.deleteDocument(uri3);
        document.setMaxDocumentBytes(1000);
        document.undo();
        document.undo();
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        assertEquals("should give back an empty list",list, document.search("is"));
    }

    @Test
    public void DeleteAllThenUndoWithSizeLimit() throws URISyntaxException {
        String initialString = "This is is is a message";
        String initialString2 = "This is is the second message";
        String initialString3 = "the third message is of this kind";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(initialString3.getBytes());

		URI uri = new URI("Ea");
        URI uri2 = new URI("kkd");
        URI uri3 = new URI("kd");
        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
        document.putDocument(targetStream3, uri3, DocumentFormat.TXT);
        document.deleteAll("this");
        document.setMaxDocumentBytes(1000);
        document.undo();
        List<String> list = new ArrayList<>();
        list.add(initialString);
        assertEquals("should give back a list with one string",list, document.search("is"));
    }

    @Test
    public void ReplaceWithCountLimit() throws URISyntaxException {
        String initialString = "This is is is a message";
        String initialString2 = "This is is the second message";

		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());

		URI uri = new URI("Ea");

        DocumentStoreImpl document = new DocumentStoreImpl();
		document.putDocument(targetStream, uri, DocumentFormat.TXT);
        document.putDocument(targetStream2, uri, DocumentFormat.TXT);
        document.setMaxDocumentCount(1);
        List<String> list = new ArrayList<>();
        list.add(initialString2);
        assertEquals("should give back a list with one string",list, document.search("this"));
    }
    
}