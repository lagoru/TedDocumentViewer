import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;




/**
 * Klasa służąca wczytywaniu danych, dane o kontraktach firm 
 * @author root
 *
 */
public class DataParser {

    static private GraphLoadingListener mListener;
    //pierwszy - nazwa kraju, w drugim jego miasta
    static private Map<String,ArrayList<String>> mCountries = new HashMap<String, ArrayList<String>>();
    //pierwszy to nazwa firmy, drugi to dane o jego pracy
    static private Map<String,CompanyServices> mCompanies = new HashMap<String,CompanyServices>();

    static{
        mListener = null;
    }
    
    /**Wczytuje dane z pliku file_name - linia po linii
     * @param file_name
     */
    static void loadGraph(final String file_name){

        new Thread(){

            public void run(){
                try {
                    File tt = new File(file_name);
                    double current_pos=0,file_length = tt.length();

                    BufferedReader br = new BufferedReader(new FileReader(
                            file_name));
                    String line = new String(), part_of_line;

                    part_of_line = br.readLine(); //niepotrzebna linia podpisow


                    int skipped = 0, amount_of_all = 0;
                    while ((part_of_line = br.readLine()) != null) {
                        if(part_of_line.endsWith(String.valueOf('"'))){
                            if(line.equals("")){
                                line += part_of_line;
                            }
                            String[] l = line.split("\",\"");
                            current_pos += line.length(); //zakładam, że utf-8
                            if(l[12].length() < 4){ // pomijam nazwy krajow ktore sa mniejsze niz 3 - nie chce pisac konwersji skrotow na slugie nazwy
                                skipped++;
                                line = "";
                                continue;
                            }
                            amount_of_all++;
                            //System.out.println(l[12]);
                            //System.out.println(l[28]);
                            //System.out.println(l[30]);
                            //System.out.println(l[31]);
                            //System.out.println(l[38]);

                            //dodajemy kraje i miasta do hash mapy
                            if(mCountries.containsKey(l[12])){
                                if(!mCountries.get(l[12]).contains(l[28]))
                                    mCountries.get(l[12]).add(l[28]);
                            }else{
                                ArrayList<String> tmp = new ArrayList<String>();
                                tmp.add(l[28]);
                                mCountries.put(l[12], tmp);
                            }

                            //dodajemy firmy i ich prace
                            if(mCompanies.containsKey(l[31])){
                                mCompanies.get(l[31]).addTypeOfService(l[38], l[30], l[28],l[12]);
                            }else{
                                CompanyServices services = new CompanyServices();
                                services.addTypeOfService(l[38], l[30], l[28],l[12]);
                                mCompanies.put(l[31], services);
                            }

                            if(mListener != null){
                                mListener.setLevelReady((int)((current_pos/file_length)*100.0));
                            }

                            line = "";
                        }else{
                            line += part_of_line;
                        }
                    }

                    System.out.println("Skipped datas: " + String.valueOf(skipped));
                    System.out.println("Datas in graph: " + String.valueOf(amount_of_all));
                    System.out.println("Companies in graph: " + String.valueOf(mCompanies.size()));
                    System.out.println("Amount of countries: " + String.valueOf(mCountries.size()));
                    
                    br.close();
                    int amount_of_cities=0;
                    Object[] cit = mCountries.keySet().toArray();
                    for(int k =0; k < mCountries.size(); k++){
                        amount_of_cities+= mCountries.get(cit[k]).size();
                    }
                    System.out.println("Amount of cities: " + String.valueOf(amount_of_cities));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                mListener.loadingReady();
            }
        }.start();
        
    }

    static public void setLoadingListener(GraphLoadingListener listener) {
        mListener = listener;
    }
    static public Map<String, CompanyServices> getCompaniesData() {
        return mCompanies;
    }
    
    static public Map<String,ArrayList<String>> getCountriesAndCitiesData(){
        return mCountries;
    }
}
