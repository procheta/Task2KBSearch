/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package task2kb.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author prochetasen
 */
public class Retriever {
    
    IndexReader reader;
    IndexSearcher searcher;
    
    public Retriever(String indexPath) throws IOException {
        File indexDir = new File(indexPath);
        reader = DirectoryReader.open(FSDirectory.open(indexDir.toPath()));
        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(0.8f, 0.5f));
        System.out.println("Index Loaded...");
        
    }
    
        public static String analyze(Analyzer analyzer, String query) {

        StringBuffer buff = new StringBuffer();
        try {
            TokenStream stream = analyzer.tokenStream("dummy", new StringReader(query));
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                String term = termAtt.toString();
                buff.append(term).append(" ");
            }
            stream.end();
            stream.close();

            if (buff.length() > 0) {
                buff.deleteCharAt(buff.length() - 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return buff.toString();
    }
    
    public TopDocs retrieve(String qryString) throws ParseException, IOException, Exception {
        
        Analyzer analyzer = new EnglishAnalyzer();
        qryString = qryString.replace("_", " ");
        String words[] = analyze(analyzer, qryString).split("\\s+");
        BooleanQuery bq = new BooleanQuery();
        for (String s : words) {
            Term term1 = new Term("title", s);
            //create the term query object
            Query query1 = new TermQuery(term1);
            //query1.setBoost(1.2f);
            bq.add(query1, BooleanClause.Occur.SHOULD);
            
            term1 = new Term("steps", s);
            //create the term query object
            query1 = new TermQuery(term1);
            //query1.setBoost(1.2f);
            bq.add(query1, BooleanClause.Occur.SHOULD);
            
            term1 = new Term("intro", s);
            //create the term query object
            query1 = new TermQuery(term1);
            //query1.setBoost(1.2f);
            bq.add(query1, BooleanClause.Occur.SHOULD);
        }

        System.out.println("Query: " + bq);
        TopDocs tdocs = searcher.search(bq, 1000);
        return tdocs;
    }
    
    public void getTaskDetails(TopDocs tdocs) throws IOException {
        for (int i = 0; i < tdocs.scoreDocs.length; i++) {
            Document doc = reader.document(tdocs.scoreDocs[i].doc);
            System.out.println(doc.get("title"));
            
        }
    }

    public static void main(String[] args) throws Exception {
        //args[0] index path
        System.out.println(args[0]);
        Retriever tr = new Retriever(args[0]);
        //args[1] Query String
        TopDocs tdocs = tr.retrieve(args[1]);
        tr.getTaskDetails(tdocs);
        
    }
}
