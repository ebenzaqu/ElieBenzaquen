package edu.yu.cs.com1320.project.stage2.impl;

// import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
// import java.net.URISyntaxException;
// import java.net.URISyntaxException;
import java.util.function.Function;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
// import edu.yu.cs.com1320.project.stage2.Document;
// import edu.yu.cs.com1320.project.stage2.*;
import edu.yu.cs.com1320.project.stage2.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {

	HashTableImpl<URI, DocumentImpl> store = new HashTableImpl<URI, DocumentImpl>();
	StackImpl<Command> commandStack = new StackImpl<Command>();

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
				//Make Lambda that returns true and adds to command stack
				Function<URI, Boolean> repeatDoc = (URI putUri) -> {
					return true;
				};
				Command deleteCommand = new Command(uri, repeatDoc);
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

	@Override
	public boolean deleteDocument(URI uri) {
		if (uri == null) {
			throw new IllegalArgumentException("URI cannot equal null");
		}
		if (store.get(uri) != null) {
			DocumentImpl doc = store.get(uri);
			Function<URI, Boolean> undoDelete = (URI putUri) -> {
				store.put(uri, doc);
				return true;
			};
			Command deleteCommand = new Command(uri, undoDelete);
			commandStack.push(deleteCommand);
			store.put(uri, null);
			return true;
		}
		//Make Lambda that returns true and adds to command stack
		Function<URI, Boolean> undoDeleteNothing = (URI putUri) -> {
			return true;
		};
		Command deleteCommand = new Command(uri, undoDeleteNothing);
		commandStack.push(deleteCommand);
		return false;
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

	private int errorsForInput(InputStream input, URI uri, DocumentFormat format) {
		if (format == null) {
			throw new IllegalArgumentException("Format must be either TXT or PDF");
		}
		if (uri == null) {
			throw new IllegalArgumentException("URI must have a value");
		}
		if (input == null && store.get(uri) != null) {
			DocumentImpl oldDoc = store.get(uri);
			Function<URI, Boolean> undoDelete = (URI putUri) -> {
				store.put(uri, oldDoc);
				return true;
			};
			Command deleteCommand = new Command(uri, undoDelete);
			commandStack.push(deleteCommand);
			int temp = store.get(uri).getDocumentTextHashCode();
			store.put(uri, null);
			return temp;
		}
		if (input == null && store.get(uri) == null) {
			return 0;
		}
		return 0;
	}

	@Override
	public void undo() throws IllegalStateException {
		if(commandStack.size() == 0){throw new IllegalStateException("Nothing To Undo");}
		Command newCommand = commandStack.pop();
		newCommand.undo();
	}

	@Override
	public void undo(URI uri) throws IllegalStateException {
		boolean foundURI = false;
		StackImpl<Command> tempStack = new StackImpl<Command>();
		//do the throws thing
		for(int j = 0; j<=commandStack.size(); j++){
			Command command = commandStack.pop();
			if(command.getUri() == uri){
				foundURI = true;
				command.undo();
				break;
			}
			tempStack.push(command);
		}
		if(foundURI == true){
			for(int i = 0; i<=tempStack.size(); i++){
				Command newCommand = tempStack.pop();
				commandStack.push(newCommand);
			}
		} 
		else {
			for(int i = 0; i<=tempStack.size(); i++){
				Command newCommand = tempStack.pop();
				commandStack.push(newCommand);
			}
			throw new IllegalStateException("URI does not exist");
		}
	}

	protected Document getDocument(URI uri) {
		if (store.get(uri) == null) {
			return null;
		}
		return store.get(uri);
	}

	private int putDocumentAsTXT(URI uri, String message){
		// String message = new String(byteArray);
		DocumentImpl document = new DocumentImpl(uri, message, message.hashCode());
		if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == message.hashCode()) {
			return message.hashCode();
		}
		if (store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != message.hashCode()) {
			DocumentImpl oldDoc = store.get(uri);
			Function<URI, Boolean> undoReplace = (URI replacedUri) -> {
				store.put(replacedUri, oldDoc);
				return true;
			};

			Command putCommand = new Command(uri, undoReplace);
			commandStack.push(putCommand);
			int temp = store.get(uri).getDocumentTextHashCode();
			store.put(uri, document);
			return temp;
		}
		Function<URI, Boolean> undoStamPutTXT = (URI deleteUri) -> {
			store.put(deleteUri, null);
			return true;
		};
		Command putCommand = new Command(uri, undoStamPutTXT);
		commandStack.push(putCommand);
		store.put(uri, document);
		return 0;
	}

	private int putDocumentAsPDF(URI uri, byte[] byteArray){
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
					return true;
				};
				Command putCommand = new Command(uri, undoReplace);
				commandStack.push(putCommand);
				int temp = store.get(uri).getDocumentTextHashCode();
				store.put(uri, doc);
				return temp;
			}
			Function<URI, Boolean> undoStamPutPDF = (URI deleteUri) -> {
				store.put(uri, null);
				return true;
			};
			Command putCommand = new Command(uri, undoStamPutPDF);
			commandStack.push(putCommand);
			store.put(uri, doc);
			return 0;
		} catch (IOException e) {e.printStackTrace();}
		return 0;
	}

	// private boolean uriExistsWithSameHashCode(URI uri, DocumentImpl document){
	// if(store.get(uri) != null && store.get(uri).hashCode() ==
	// document.hashCode()){
	// return true;
	// }
	// return false;

	// }

	// public static void main(String[] args) throws URISyntaxException {
	// 	String initialString = "This is a message";
	// 	String initialString2 = "This is the second message";

	// 	// String initialString2 = "This is a different message";
	// 	InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
	// 	InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());

	// 	// InputStream targetStream2 = new ByteArrayInputStream(initialString2.getBytes());
	// 	// InputStream nullIS = null;
	// 	URI uri = new URI("Ea");
	// 	URI uri2 = new URI("kkd");
	// 	// URI uri2 = new URI("aba");
    //     DocumentStoreImpl document = new DocumentStoreImpl();
	// 	// document.putDocument(targetStream, uri, DocumentFormat.TXT);
	// 	// document.putDocument(targetStream2, uri2, DocumentFormat.TXT);
	// 	// document.undo(uri);
	// 	// System.out.println(document.getDocument(uri).getDocumentAsTxt());	
	// 	document.undo();
	// 	// document.undo();
	// 	// System.out.println(document.getDocument(uri2).getDocumentAsTxt());

	// 	// System.out.println(document.getDocument(uri2));	
	// }
}