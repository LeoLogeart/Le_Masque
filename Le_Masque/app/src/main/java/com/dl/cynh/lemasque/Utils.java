package com.dl.cynh.lemasque;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SimpleAdapter;

public class Utils {

    private static DownloadActivity act;


    public Utils(DownloadActivity activity) {
        act=activity;
    }


    /**
     * Add a value to the list of downloaded podcasts
     * @param seenPodcast string id of the podcast to add to the "seen" list
     */
    public void addSeen(String seenPodcast){
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        String downloaded = sharedPref.getString("Downloaded",null);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(downloaded==null){
            editor.putString("Downloaded",seenPodcast);
        } else {
            editor.putString("Downloaded", downloaded + "," + seenPodcast);
        }
        editor.apply();

        ArrayList<HashMap<String, String>> list = act.getListItem();
        for(HashMap<String,String> item : list){
            if((item.get("description")+ " " + item.get("day")).equals(seenPodcast) && !item.get("day").contains(" ( Téléchargé )")){
                item.put("day", item.get("day")+" ( Téléchargé )");
            }
        }
        updateLayout();
    }

    /**
     * Retrieve the list of downloaded podcasts
     * @return list of downloaded podcasts
     */
    public static List<String> getDownloaded(){
        SharedPreferences sharedPref = act.getPreferences(Context.MODE_PRIVATE);
        String downloaded = sharedPref.getString("Downloaded", "");
        ArrayList<String> seen = new ArrayList<>(Arrays.asList(downloaded.split(",")));
        if(seen.size()>30){
            seen.remove(0);
        }
        return seen;
    }


    /**
     * Parses the html page to get podcast titles and urls
     *
     * @param responseString the whole html page
     *
     */
    public static void parsePage(String responseString, List<String> podcasts, List<String> titles, List<HashMap<String, String>> listItem) {
        String[] items = responseString.split("<item>");
        for(int i=1;i<items.length;i++){
            parseItem(items[i],podcasts,titles,listItem);
        }
    }


    public static void parseItem(String item, List<String> podcasts, List<String> titles, List<HashMap<String, String>> listItem) {
        int i = item.indexOf("enclosure url");
        if(i==0){
            return;
        }
        // url
        int start = item.indexOf("http",i);
        int end = item.indexOf("\"", start);
        podcasts.add(item.substring(start,end));

        //title
        String title;
        start = item.indexOf("<title>")+7;
        end = item.indexOf("</title>", start);
        if(start==0 || end==0){
            title = "?";
        } else {
            title = stripTitle(item.substring(start,end));
        }

        //date
        start = item.indexOf("<pubDate>")+9;
        end = item.indexOf("</pubDate>", start);
        String date;
        if(start<=0 || end<=0){
            date = "?";
        } else {
            date = item.substring(start,end);
        }
        titles.add(title);
        HashMap<String, String> map = new HashMap<>();
        if(Utils.getDownloaded().contains(title + " " + getDate(date))){
            map.put("day", getDate(date)+" ( Téléchargé )");
        } else {
            map.put("day", getDate(date));
        }

        map.put("description", title);
        map.put("img",String.valueOf(R.drawable.plume));
        listItem.add(map);

    }

    public static String getDate(String date) {
        if(date.equals("?"))
            return "";
        String res="";
        for(int i=0;i<date.length();i++){
            if(Character.isDigit(date.charAt(i))){
                if(i+1<date.length() && Character.isDigit(date.charAt(i+1))){
                    res+=date.substring(i,i+2);
                } else {
                    res+=date.charAt(i);
                }
                break;
            }
        }
        return res+" "+getMonthFromStr(date);
    }

    public static String stripTitle(String title) {
        title=title.replaceAll("&quot;","\"");
        String pattern = "\\d{2}.\\d{2}.\\d{4}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(title);
        if (matcher.find()) {
            // if the title contains 15.10.2015
            return title.substring(0,matcher.start()-1);
        }
        return title;
    }


    /**
     *
     * @param tmp the podcast title
     * @return the number of the month contained in the string
     */
    public static String getMonthFromStr(String tmp) {
        String month;
        if (tmp.contains("Jan")) {
            month = "Janvier";
        } else if (tmp.contains("Feb")) {
            month = "Février";
        } else if (tmp.contains("Mar")) {
            month = "Mars";
        } else if (tmp.contains("Apr")) {
            month = "Avril";
        } else if (tmp.contains("May")) {
            month = "Mai";
        } else if (tmp.contains("Jun")) {
            month = "Juin";
        } else if (tmp.contains("Jul")) {
            month = "Juillet";
        } else if (tmp.contains("Aug")) {
            month = "Août";
        } else if (tmp.contains("Sep")) {
            month = "Septembre";
        } else if (tmp.contains("Oct")) {
            month = "Octobre";
        } else if (tmp.contains("Nov")) {
            month = "Novembre";
        } else if (tmp.contains("Dec")) {
            month = "Décembre";
        } else {
            return "";
        }
        return month;
    }

    /**
     * Update the layout with values selected by the user
     */
    public static void updateLayout() {
        ArrayList<HashMap<String, String>> list = act.getListItem();
        SimpleAdapter adapt = new SimpleAdapter(
                act.getBaseContext(), list,
                R.layout.print_item, new String[] { "img", "day",
                "description" }, new int[] { R.id.img, R.id.title,
                R.id.description });
        act.getpodcastList().setAdapter(adapt);
    }

}