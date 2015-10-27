import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CompanyServices {
    public class Triple<F,S,T>{
        F first;
        S second;
        T third;
        public Triple(F _first, S _second, T _third){
            first = _first;
            second = _second;
            third = _third;
        }
    }
    //ogolny typ pracy, dokładna nazwa pracy, miasto
    private Map<String, ArrayList<Triple<String,String,String>>> mTypesOfServices= new HashMap<String,ArrayList<Triple<String,String,String> >>();


    public void addTypeOfService(String name_of_service, String exact_name_of_work, String city, String country){
        if(mTypesOfServices.containsKey(name_of_service)){
            mTypesOfServices.get(name_of_service).add( new Triple<String,String,String>(exact_name_of_work, city, country));
        }else{
            ArrayList<Triple<String,String,String>> tmp = new ArrayList<Triple<String,String,String>>();
            tmp.add(new Triple<String,String,String>(exact_name_of_work, city,country));
            mTypesOfServices.put(name_of_service, tmp);
        }
    }

    Map<String, ArrayList<Triple<String,String,String>>> getAllServices(){
        return mTypesOfServices;
    }

}
