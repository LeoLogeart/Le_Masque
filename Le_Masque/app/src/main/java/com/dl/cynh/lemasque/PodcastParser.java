package com.dl.cynh.lemasque;

import android.support.annotation.Nullable;

import com.cynh.podcastdownloader.model.Podcast;
import com.cynh.podcastdownloader.utils.PodcastParserInterface;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PodcastParser implements PodcastParserInterface {

    @Override
    public ArrayList<Podcast> parsePage(String htmlPage) {
        ArrayList<Podcast> podcasts = new ArrayList<>();
        Podcast pod;
        String[] items = htmlPage.split("<item>");
        for (int i = 1; i < items.length; i++) {
            pod = parseItem(items[i]);
            if (pod != null) {
                podcasts.add(pod);
            }
        }
        return podcasts;
    }


    @Nullable
    private String findUrl(String item) {
        int i = item.indexOf("enclosure url");
        if (i == 0) {
            return null;
        }
        // url
        int start = item.indexOf("http", i);
        int end = item.indexOf("\"", start);
        return item.substring(start, end);
    }

    private Podcast parseItem(String item) {


        String url = findUrl(item);
        if (url == null) return null;

        String desc = findDescription(item);
        String date = findDate(item);
        int image = R.mipmap.ic_launcher;
        String type = "Intégrales";
        return new Podcast(date, desc, image, url, type);
    }

    private String findDescription(String item) {
        int start;
        int end;
        String title;
        start = item.indexOf("<title>") + 7;
        end = item.indexOf("</title>", start);
        if (start == 0 || end == 0) {
            title = "";
        } else {
            title = stripTitle(item.substring(start, end));
        }
        return title;
    }


    private String findDate(String item) {
        int start;
        int end;
        start = item.indexOf("<pubDate>") + 9;
        end = item.indexOf("</pubDate>", start);
        String date;
        if (start <= 0 || end <= 0) {
            date = "";
        } else {
            date = item.substring(start, end);
            String res = "";
            for (int i = 0; i < date.length(); i++) {
                if (Character.isDigit(date.charAt(i))) {
                    if (i + 1 < date.length() && Character.isDigit(date.charAt(i + 1))) {
                        res += date.substring(i, i + 2);
                    } else {
                        res += date.charAt(i);
                    }
                    break;
                }
            }
            return res + " " + getMonthFromStr(date);
        }
        return date;
    }


    private String stripTitle(String title) {
        title = title.replaceAll("&quot;", "\"");
        String pattern = "\\d{2}.\\d{2}.\\d{4}";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(title);
        if (matcher.find()) {
            // if the title contains 15.10.2015
            return title.substring(0, matcher.start() - 1);
        }
        return title;
    }

    private String getMonthFromStr(String tmp) {
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


}
