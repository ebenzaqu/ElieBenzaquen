package edu.yu.cs.com1320.project.stage1.impl;

import java.io.*;
import java.net.URI;
// import java.net.URISyntaxException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {

	HashTableImpl<URI, DocumentImpl> store = new HashTableImpl<URI, DocumentImpl>();

	//WRITE TEST CODE FOR EXISTING URI WITH SAME HASHCODE

	@Override
	public int putDocument(InputStream input, URI uri, DocumentFormat format) {
		try {
			if(format == null || uri == null || input == null && store.get(uri) != null || input == null && store.get(uri) == null){return errorsForInput(input,uri,format);}
			byte[] byteArray = inputStreamToByteArray(input);
			if (format == DocumentFormat.TXT) {
				String message = new String(byteArray);
				DocumentImpl document = new DocumentImpl(uri, message, message.hashCode());
				if(store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == message.hashCode()){return message.hashCode();}
				if(store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != message.hashCode()){
					int temp = store.get(uri).getDocumentTextHashCode();
					store.put(uri,document);
					return temp;
				}
				store.put(uri, document);
				return 0;
			}
			if (format == DocumentFormat.PDF) {
				PDFTextStripper stripper = new PDFTextStripper();
				String text = stripper.getText(PDDocument.load(byteArray)).trim();
				DocumentImpl doc = new DocumentImpl(uri, text, text.hashCode(), byteArray);
				if(store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() == text.hashCode()){return text.hashCode();}
				if(store.get(uri) != null && store.get(uri).getDocumentAsTxt().hashCode() != text.hashCode()){
					int temp = store.get(uri).getDocumentTextHashCode();
					store.put(uri,doc);
					return temp;
				}
				store.put(uri, doc);
				return 0;
		} } catch (IOException e) {e.printStackTrace();}
		return 0;}

	@Override
	public byte[] getDocumentAsPdf(URI uri) {
		if(uri == null){throw new IllegalArgumentException("URI cannot equal null");}
		if (store.get(uri) != null && store.get(uri) != null) {
			DocumentImpl doc = store.get(uri);
			return doc.getDocumentAsPdf();
		}
		return null;
	}

	@Override
	public String getDocumentAsTxt(URI uri) {
		if(uri == null){throw new IllegalArgumentException("URI cannot equal null");}
		if (store.get(uri) != null) {
			DocumentImpl doc = store.get(uri);
			return doc.getDocumentAsTxt();
		}
		return null;
	}

	@Override
	public boolean deleteDocument(URI uri) {
		if(uri == null){throw new IllegalArgumentException("URI cannot equal null");}
		if (store.get(uri) != null) {
			store.put(uri, null);
			return true;
		}
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

	private int errorsForInput(InputStream input, URI uri, DocumentFormat format){
		if(format == null){
			throw new IllegalArgumentException("Format must be either TXT or PDF");
		}
		if(uri == null){
			throw new IllegalArgumentException("URI must have a value");
		}
		if(input == null && store.get(uri) != null){
			int temp = store.get(uri).getDocumentTextHashCode();
			store.put(uri,null);
			return temp;
		}
		if(input == null && store.get(uri) == null){
			return 0;
		}
		return 0;
	}

	// private boolean uriExistsWithSameHashCode(URI uri, DocumentImpl document){
	// 	if(store.get(uri) != null && store.get(uri).hashCode() == document.hashCode()){
	// 		return true;
	// 	}
	// 	return false;

	// }

	// public static void main(String[] args) {
	// 	System.out.println("hi");
	// }
}