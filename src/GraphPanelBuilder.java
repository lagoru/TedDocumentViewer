import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;


/**
 * Klasa służąca tworzeniu grafu
 * @author root
 *
 */
public class GraphPanelBuilder {

    private static mxGraphComponent sGraphComponent;
    private static byte[] sMap;
    private static GraphPanelBuilderListener sListener;
    private static Thread sWorkerThread = null;
    public static void setListener(GraphPanelBuilderListener listener){
        sListener = listener;
    }

    /**
     * Asynchroniczna funkcja do generowania grafu i mapy
     */
    static void createGraphAndMap(final String company, final int width_of_map, final int height_of_map){
        if(sWorkerThread != null && sWorkerThread.isAlive()){
            sWorkerThread.stop();
        }
        sWorkerThread = new Thread(){
            public void run(){
                createGraph(company,DataParser.getCompaniesData().get(company).getAllServices());
                createMap(company,DataParser.getCompaniesData().get(company).getAllServices(),
                        width_of_map, height_of_map);
                sListener.workIsDone();
            }
        };
        sWorkerThread.start();
    }

    /**
     * Tworzy graf prac firmy 
     * @param company
     * @param all_services
     */
    private static void createGraph(String company, Map<String, 
            ArrayList<CompanyServices.Triple<String,String,String> >> all_services){
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.setCellsDisconnectable(false);
        graph.setCellsEditable(false);
        graph.setCellsDeletable(false);
        graph.setKeepEdgesInBackground(true);
        graph.getModel().beginUpdate();


        try{
            Object[] keys = all_services.keySet().toArray();

            Map<String,Object> tmp_cities = new HashMap<String, Object>();
            Map<String,Object> tmp_countries = new HashMap<String, Object>();
            int amount_cities = 0, amount_countries = 0, amount_exact_names = 0;
            for(int i = 0; i < keys.length; ++i){
                Object name_of_service_vertex = graph.insertVertex(parent, null, (String)keys[i], 25, 20 + 30*i, 300,20);

                ArrayList<CompanyServices.Triple<String,String,String> > exact_names_of_works = all_services.get((String)keys[i]);
                for(int j =0 ; j < exact_names_of_works.size(); ++j){
                    String tmp_enofg = new String((String)exact_names_of_works.get(j).first);
                    //dzielimy jezeli za duzo
                    if(tmp_enofg.length() > 50){
                        int index_to_split = tmp_enofg.indexOf(' ', 45);
                        if(index_to_split != -1){
                            tmp_enofg = tmp_enofg.substring(0,index_to_split) +'\n' + 
                                    tmp_enofg.substring(index_to_split+1);
                        }
                    }
                    if(tmp_enofg.length() > 100){
                        int index_to_split = tmp_enofg.indexOf(' ', 95);
                        if(index_to_split != -1){
                            tmp_enofg = tmp_enofg.substring(0,index_to_split) +'\n' + 
                                    tmp_enofg.substring(index_to_split+1);
                        }
                    }
                    if(tmp_enofg.length() > 150){
                        int index_to_split = tmp_enofg.indexOf(' ', 145);
                        if(index_to_split != -1){
                            tmp_enofg = tmp_enofg.substring(0,index_to_split) +'\n' + 
                                    tmp_enofg.substring(index_to_split+1);
                        }
                    }
                    Object exact_name_of_service_vertex = graph.insertVertex(parent, null, 
                            tmp_enofg, 400, 20 + 50*(amount_exact_names), 350,45);
                    graph.insertEdge(parent, null, null, name_of_service_vertex, exact_name_of_service_vertex);
                    amount_exact_names++;

                    if(!tmp_cities.containsKey(exact_names_of_works.get(j).second)){
                        Object city_vertex = graph.insertVertex(parent, null, 
                                (String)exact_names_of_works.get(j).second, 800, 20 + 30*(amount_cities), 100,20);
                        tmp_cities.put(exact_names_of_works.get(j).second, city_vertex);
                        amount_cities++;
                    }
                    graph.insertEdge(parent, null, null, exact_name_of_service_vertex, 
                            tmp_cities.get(exact_names_of_works.get(j).second));

                    if(!tmp_countries.containsKey(exact_names_of_works.get(j).third)){
                        Object country_vertex = graph.insertVertex(parent, null, 
                                (String)exact_names_of_works.get(j).third, 950, 20 + 30*(amount_countries), 100,20);
                        tmp_countries.put(exact_names_of_works.get(j).third, country_vertex);
                        amount_countries++;
                    }
                    graph.insertEdge(parent, null, null, tmp_cities.get(exact_names_of_works.get(j).second), 
                            tmp_countries.get(exact_names_of_works.get(j).third));
                }
            }
        }
        finally{
            graph.getModel().endUpdate();
        }

        sGraphComponent = new mxGraphComponent(graph);
    }

    private static void createMap(String company,
            Map<String, ArrayList<CompanyServices.Triple<String, String,String>>> allServices,
            int width_of_map, int height_of_map) {
        //final Geocoder geocoder = new Geocoder();
        /*GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress("Paris, France").setLanguage("en").getGeocoderRequest();
        try {
            GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String url_string = new String();
        url_string += "http://maps.google.com/maps/api/staticmap?";

        Set<String> tmp_citiesAndCountries = new TreeSet<String>();
        for(String tt : allServices.keySet()){
            ArrayList<CompanyServices.Triple<String, String,String>> mm= allServices.get(tt);
            for(CompanyServices.Triple<String, String,String> triple : mm){
                String text = Normalizer
                        .normalize(triple.second, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "").replace(' ', '+')+","
                        + Normalizer
                        .normalize(triple.third, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "").replace(' ', '+');
                if(!tmp_citiesAndCountries.contains(text)){
                    tmp_citiesAndCountries.add(text);
                }
            }
        }

        for(String text :tmp_citiesAndCountries){
            url_string += "&markers=color:blue%7Clabel:C%7C" + text;
        }

        url_string += "&size="+ width_of_map +"x"+ height_of_map +"&maptype=roadmap&zoom=7";
        //wczytanie mapy z adresu 
        URLConnection con;
        try {
            con = new URL(url_string).openConnection();
            InputStream is = con.getInputStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1!=(n=is.read(buf))){
                out.write(buf, 0, n);
            }
            out.close();
            is.close();
            sMap= out.toByteArray();
        } catch (MalformedURLException e) {
            System.out.println("Cannot download picture");
        } catch (IOException e) {
            System.out.println("Cannot download picture");
        }
    }

    public static Component getGraphPanel() {
        return sGraphComponent;
    }  

    public static byte[] getMap(){
        return sMap;
    }
}
