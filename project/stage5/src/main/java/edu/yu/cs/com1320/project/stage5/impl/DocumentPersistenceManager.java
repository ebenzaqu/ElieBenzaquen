package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * created by the document store and given to the BTree via a call to
 * BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document>{

    File dir;

    public DocumentPersistenceManager(File baseDir) {
        if(baseDir == null){
            File file = new File(System.getProperty("user.dir"));
            this.dir = file;
        } else{
            this.dir = baseDir;
        }
    }

    HashMap<URI,JsonElement> jsonStorage = new HashMap<>();

    // BTreeImpl<URI,JsonElement> jsonStorage = new BTreeImpl<>();

    @Override
    public void serialize(URI key, Document val) throws IOException {

        JsonElement fileToWrite = serializer.serialize(val, val.getClass(), null);

        String path = key.toString();
        path = path.substring(path.indexOf("//") + 1);
        File newFile = new File(dir,path + ".json");
        // File newFile = new File(dir.getAbsolutePath() + path + ".json");
        // this.generify(newFile);
        newFile.getParentFile().mkdirs();

        FileWriter writer = new FileWriter(newFile);
        writer.write(fileToWrite.toString());
        writer.close();
    }

    @Override
    public Document deserialize(URI key) throws IOException {

        String filePath = key.toString();
        filePath = filePath.substring(filePath.indexOf("//") + 1);
        File newFile = new File(dir,filePath + ".json");

        if(!newFile.exists()){
            return null;
        }

        Scanner sc = new Scanner(newFile);

        String content = sc.useDelimiter("\\Z").next();

        JsonElement jelem = JsonParser.parseString(content).getAsJsonObject();
        sc.close();
        Document doc = deserializer.deserialize(jelem, Document.class, null);
        newFile.delete();
        this.deleteFolders(newFile);
        jsonStorage.put(key, null);
        return doc;
    }

    private void deleteFolders(File file){
        if(file.toString().length() < 5){
            return;
        }
        String path = file.toString();
        for(int i = path.length(); i>=0; i--){
            if(Character.toString(path.charAt(i-1)).equals("/") || Character.toString(path.charAt(i-1)).equals("\\")){ 
                path = path.substring(0, path.length()-1);
                break;
            }   
            path = path.substring(0, path.length()-1);
        }
        File filePath = new File(path);
        filePath.delete();
        deleteFolders(filePath);
    } 

    JsonSerializer<Document> serializer = new JsonSerializer<Document>() {

        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            String docText = src.getDocumentAsTxt();
            String docURI = src.getKey().toString();
            int hashCode = src.getDocumentTextHashCode();
            String wordCountMap = src.getWordMap().toString();
            object.addProperty("text", docText);
            object.addProperty("uri", docURI);
            object.addProperty("hashCodeOfText", hashCode);
            object.addProperty("docTextHashTable", wordCountMap);
            jsonStorage.put(src.getKey(), object);
            return object;
        }
    };

    JsonDeserializer<Document> deserializer = new JsonDeserializer<Document>() {

        @Override
        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {

                Map<String,Integer> wordMap = new HashMap<>();
                String str = json.getAsJsonObject().get("text").getAsString();
                URI uri = new URI(json.getAsJsonObject().get("uri").getAsString());
                int hash = json.getAsJsonObject().get("hashCodeOfText").getAsInt();
                String table = json.getAsJsonObject().get("docTextHashTable").getAsString();

                table = table.replaceAll("[{}=,]", " ");
                StringTokenizer st = new StringTokenizer(table);

                while(st.hasMoreTokens()){
                    String word = st.nextToken();
                    int count = Integer.parseInt(st.nextToken());
                    wordMap.put(word, count);
                }
                
                DocumentImpl doc = new DocumentImpl(uri, str, hash, true);
                doc.setWordMap(wordMap);
    
                return doc;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;      
        }
    };
}