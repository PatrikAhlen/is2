/**
 * Copyright 2019 ISTAT
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 *
 * @author Francesco Amato <framato @ istat.it>
 * @author Mauro Bruno <mbruno @ istat.it>
 * @author Paolo Francescangeli  <pafrance @ istat.it>
 * @author Renzo Iannacone <iannacone @ istat.it>
 * @author Stefano Macone <macone @ istat.it>
 * @version 1.0
 */
package it.istat.is2.app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.web.multipart.MultipartFile;

import it.istat.is2.app.bean.ColonnaJson;
import it.istat.is2.app.bean.DatoJson;

public class FileHandler {

    public static void loadFile(RConnection connection, String inputTable, String nomeFile) {
        try {
            connection.eval(inputTable + " <- read.csv(file='" + nomeFile + "', header=TRUE, sep=',', dec='.')");
        } catch (Exception ex) {
            Logger.getRootLogger().error("Errore: ", ex);
        }
    }

    public static void readFile(String nomeFile) {
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(nomeFile);
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                Logger.getRootLogger().info(sCurrentLine);
            }
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
                Logger.getRootLogger().error("Errore: ", ex);
            }
        }
    }

    public static void writeFile(String nomeFile) {
        String path = "input/" + nomeFile;
        try {
            File file = new File(path);
            FileWriter fw = new FileWriter(file);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
    }

    // Ritorna ArrayList con i campi dell'header
    public static ArrayList<String> getCampiHeader(String urlFile, char delimiter) {
        ArrayList<String> campiHeader = new ArrayList<String>();
        Reader in = null;

        try {
            in = new FileReader(urlFile);
            delimiter = checkDelimiter(delimiter);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.RFC4180.withDelimiter(delimiter).parse(in);
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        int i = 0;
        int j = 0;
        while (i < 1) {
            CSVRecord rec = records.iterator().next();
            rec.size();
            while (rec.iterator().hasNext() && j < rec.size()) {
                String field = rec.get(j);
                j++;
                campiHeader.add(field.toUpperCase());
            }
            i++;
        }
        return campiHeader;
    }

    // Ritorna HashMap con i campi dell'header come chiave e l'indice corrispondente
    public static HashMap<String, Integer> getCampiHeaderNameIndex(String urlFile, char delimiter) {
        HashMap<String, Integer> campiHeader = new HashMap<String, Integer>();
        Reader in = null;

        try {
            in = new FileReader(urlFile);
            delimiter = checkDelimiter(delimiter);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        Iterable<CSVRecord> records = null;

        try {
            // CSVFormat format = aFormat.withHeader().withSkipHeaderRecord();
            // records = CSVFormat.RFC4180.parse(in);
            records = CSVFormat.RFC4180.withDelimiter(delimiter).parse(in);
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        int i = 0;
        int j = 0;

        while (i < 1) {
            CSVRecord rec = records.iterator().next();
            rec.size();
            while (rec.iterator().hasNext() && j < rec.size()) {
                String field = rec.get(j);
                campiHeader.put(field.toUpperCase(), j);
                j++;
            }
            i++;
        }
        return campiHeader;

    }

    // Ritorna HashMap con indice numerico come chiave e nome dell'header
    // corrispondente
    public static HashMap<Integer, String> getCampiHeaderNumIndex(String urlFile, char delimiter) throws Exception {
        HashMap<Integer, String> campiHeader = new HashMap<Integer, String>();
        Reader in = null;

        try {
            in = new FileReader(urlFile);
            delimiter = checkDelimiter(delimiter);

            Iterable<CSVRecord> records = null;
            records = CSVFormat.RFC4180.withDelimiter(delimiter).parse(in);
            int i = 0;
            int j = 0;

            while (i < 1) {
                CSVRecord rec = records.iterator().next();
                rec.size();
                while (rec.iterator().hasNext() && j < rec.size()) {
                    String field = rec.get(j);
                    campiHeader.put(j, field.toUpperCase());
                    j++;
                }
                i++;
            }
        } catch (Exception e) {
            throw e;
        }
        return campiHeader;
    }

    public static HashMap<Integer, ArrayList<String>> getArrayListFromCsv(String urlFile, int sizeHeader,
            char delimiter) {

        Reader in = null;
        try {
            in = new FileReader(urlFile);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        try {
            in = new FileReader(urlFile);
            delimiter = checkDelimiter(delimiter);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.RFC4180.withDelimiter(delimiter).parse(in);
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        Iterator<CSVRecord> itr = records.iterator();
        Iterator<CSVRecord> itr2 = records.iterator();

        CSVRecord riga = itr2.next();
        HashMap<Integer, ArrayList<String>> campiMap = new HashMap<Integer, ArrayList<String>>();
        for (int i = 0; i < riga.size(); i++) {
            campiMap.put(Integer.valueOf(i), new ArrayList<String>());
        }
        if (itr.hasNext()) {
            // Salta l'intestazione
            // itr.next();
        }
        // Cicla le righe del csv
        while (itr.hasNext()) {
            // Cicla le colonne del csv
            CSVRecord rec = itr.next();
            for (int i = 0; i < rec.size(); i++) {
                String valore = rec.get(i);
                // Double valore = Double.parseDouble(field);
                // Popola l'hashmap usando come chiave l'indice della colonna del csv
                campiMap.get(i).add(valore);
            }
        }
        return campiMap;
    }

    // Ritorna HashMap di campi popolato con i dati del csv (Indice HashMap = nome
    // campo)
    public static HashMap<String, ArrayList<String>> getArrayListFromCsv2(String urlFile, int sizeHeader, char delimiter, HashMap<Integer, String> valoriHeaderNum) {

        Reader in = null;
        try {
            in = new FileReader(urlFile);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        try {
            in = new FileReader(urlFile);
            delimiter = checkDelimiter(delimiter);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        Iterable<CSVRecord> records = null;
        try {
            // records = CSVFormat.RFC4180.parse(in);
            records = CSVFormat.RFC4180.withDelimiter(delimiter).parse(in);
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        Iterator<CSVRecord> itr = records.iterator();
        Iterator<CSVRecord> itr2 = records.iterator();
        CSVRecord riga = itr2.next();
        HashMap<String, ArrayList<String>> campiMap = new HashMap<String, ArrayList<String>>();
        String nameKeyHash = "";

        for (int i = 0; i < riga.size(); i++) {
            nameKeyHash = valoriHeaderNum.get(i);
            // Inizializzo l'hasmap con chiave il nome del campo e tanti ArrayList(String)
            // quanti sono i campi di intestazione
            campiMap.put(nameKeyHash, new ArrayList<String>());
        }

        if (itr.hasNext()) {
            // Salta l'intestazione
            // itr.next();
        }

        // Cicla le righe del csv
        while (itr.hasNext()) {
            // Cicla le colonne del csv
            CSVRecord rec = itr.next();
            for (int i = 0; i < rec.size(); i++) {
                String valore = rec.get(i);
                // Double valore = Double.parseDouble(field);
                // Popola l'hashmap usando come chiave l'indice della colonna del csv
                String nameIndex = valoriHeaderNum.get(i);
                campiMap.get(nameIndex).add(valore);
            }
        }
        return campiMap;
    }

    public static HashMap<String, ColonnaJson> getColonnaJsonFromCsv(String urlFile, int sizeHeader, char delimiter, HashMap<Integer, String> valoriHeaderNum) {

        Reader in = null;
        try {
            in = new FileReader(urlFile);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        try {
            in = new FileReader(urlFile);
            delimiter = checkDelimiter(delimiter);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.RFC4180.withDelimiter(delimiter).parse(in);

        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }

        Iterator<CSVRecord> itr = records.iterator();
        Iterator<CSVRecord> itr2 = records.iterator();

        CSVRecord riga = itr2.next();
        HashMap<String, ColonnaJson> campiMap = new HashMap<String, ColonnaJson>();
        String nameKeyHash = "";

        for (int i = 0; i < riga.size(); i++) {
            nameKeyHash = valoriHeaderNum.get(i);
            // Inizializzo l'hasmap con chiave il nome del campo e tanti ArrayList(String)
            // quanti sono i campi di intestazione
            campiMap.put(nameKeyHash, new ColonnaJson());
        }

        if (itr.hasNext()) {
            // Salta l'intestazione
            // itr.next();
        }
        int counter = 0;
        // Cicla le righe del csv
        while (itr.hasNext()) {
            // Cicla le colonne del csv
            CSVRecord rec = itr.next();
            ++counter;
            for (int i = 0; i < rec.size(); i++) {
                DatoJson valore = new DatoJson();
                valore.setValore(rec.get(i));
                valore.setRiga(counter);
                // Double valore = Double.parseDouble(field);
                // Popola l'hashmap usando come chiave l'indice della colonna del csv
                String nameIndex = valoriHeaderNum.get(i);
                campiMap.get(nameIndex).add(valore);
            }
        }
        return campiMap;
    }

    public static String[][] convertHashMapToDoubleMatrix(HashMap<String, ArrayList<String>> hash, HashMap<Integer, String> valoriHeaderNum) {
        // fornire l'indice giusto
        String chiave0 = valoriHeaderNum.get(0);
        String key = "";
        String[][] target = new String[hash.get(chiave0).size()][hash.size()];
        for (int i = 0; i < hash.get(chiave0).size(); i++) {
            for (int j = 0; j < hash.size(); j++) {
                key = valoriHeaderNum.get(j);
                target[i][j] = hash.get(key).get(i);
            }
        }
        return target;
    }

    public static void getHeader(String urlFile) {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(urlFile);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(urlFile);
        } catch (FileNotFoundException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        CSVParser csvFileParser = null;
        try {
            csvFileParser = new CSVParser(fileReader, csvFileFormat);
        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
        try {
            List<CSVRecord> csvRecords = csvFileParser.getRecords();

            Logger.getRootLogger().info("Stamp l'header del csv: " + csvRecords.get(0));

        } catch (IOException e) {
            Logger.getRootLogger().error("Errore: ", e);
        }
    }

    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("temp", ".csv");
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    // Inposta il tab (\t) come delimiter se da form arriva il valore 0
    public static char checkDelimiter(char delimiter) {
        if (delimiter == '0') {
            delimiter = '\t';
        }
        return delimiter;
    }
}
