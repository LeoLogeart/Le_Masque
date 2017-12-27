package com.dl.cynh.lemasque;

import android.os.Bundle;

import com.cynh.podcastdownloader.context.DownloadActivity;
import com.cynh.podcastdownloader.model.Podcast;
import com.google.firebase.analytics.FirebaseAnalytics;

public class LeMasqueActivity extends DownloadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String[] types = new String[]{"Intégrales"};
        setAdId("ca-app-pub-9891261141906247/3222872418");
        Podcast.setPodcastTypes(types);
        setParser(new PodcastParser());
        setRssUrl("http://radiofrance-podcast.net/podcast09/rss_14007.xml");
        setFirebase(FirebaseAnalytics.getInstance(this));
        super.onCreate(savedInstanceState);
    }
}
