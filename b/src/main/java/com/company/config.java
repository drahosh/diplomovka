package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public  class config{
    //vsetko je static, zmeni sa pri nacitani, vsetky nastavenia sa z tohto tahaju
    public static String reasonerType = "Hermit";
    //String reasonerType = "Pellet";
    public static Boolean loopsAllowed = false;
    public static String[] reasonerTypeListArray = {"Hermit","Pellet"};
    public static List<String> reasonerTypeList = Arrays.asList(reasonerTypeListArray);
}
