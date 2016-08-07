package com.dl.cynh.lemasque;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DownloadActivity extends Activity {
    private ArrayList<String> titles;
    private ArrayList<String> podcasts;
    private ProgressDialog progress;
    private ArrayList<HashMap<String, String>> listItem;
    private com.dl.cynh.lemasque.Utils utils;
    private ListView podcastList;

    public ListView getpodcastList() {
        return podcastList;
    }

    public ArrayList<HashMap<String, String>> getListItem(){
        return listItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        // Look up the AdView as a resource and load a request.
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-9891261141906247/5874151219");
        LinearLayout adContainer = (LinearLayout)this.findViewById(R.id.adsContainer);

        AdRequest adRequest = new AdRequest.Builder().build();

        adContainer.addView(adView);
        adView.loadAd(adRequest);
        utils = new Utils(this);
        startDl();

        if(!isStoragePermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }


    }
    @Override
    public void onRestart() {
        super.onRestart();
        // To dismiss the loading dialog
        progress.dismiss();
        startDl();
        if(!isStoragePermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    /**
     * Launches the progress dialog and the downloader.
     */
    private void startDl() {
        progress = new ProgressDialog(this);
        progress.setTitle("Récupération des podcasts");
        progress.setMessage("Patientez pendant la vérification des podcasts disponibles...");
        progress.show();
        new RequestTask()
                .execute("http://radiofrance-podcast.net/podcast09/rss_14007.xml");
    }

    /**
     * AsyncTask to get the podcast page, parse it and display the list on the
     * screen
     */
    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpURLConnection urlConnection = null;
            String responseString = null;
            try {
                URL url = new URL(uri[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // read it with BufferedReader
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                responseString=sb.toString();
                br.close();
            } catch (Exception e) {
                //do nothing
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // To dismiss the dialog
            progress.dismiss();
            if (result == null) {
                connectionProblem();
                return;
            }
            listItem = new ArrayList<>();
            podcasts = new ArrayList<>();
            titles = new ArrayList<>();
            Utils.parsePage(result, podcasts, titles, listItem);

            podcastList = (ListView) findViewById(R.id.list_podcast);
            // set the list of titles in listView
            Utils.updateLayout();
            // for each title, set the download when clicked (with an alertbox
            // to verify)
            podcastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> l, View v,
                                        final int position, long id) {

                    @SuppressWarnings("unchecked")
                    HashMap<String, String> item = (HashMap<String, String>) l.getItemAtPosition(position);
                    final String title = item.get("description");
                    final String day = item.get("day");
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            DownloadActivity.this);
                    alertDialogBuilder.setTitle("Télécharger");
                    alertDialogBuilder
                            .setMessage(
                                    "Voulez vous télécharger \""
                                            + title+"\"")
                            .setCancelable(false)
                            .setPositiveButton("Oui",
                                    new DialogInterface.OnClickListener() {
                                        // start the download manager
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            if(!isStoragePermissionGranted()) {
                                                AlertDialog alertDialog = new AlertDialog.Builder(DownloadActivity.this).create();
                                                alertDialog.setTitle("Problème");
                                                alertDialog.setMessage("Vous devez autoriser l'application à accéder aux fichiers pour télécharger le podcast. Relancez l'application pour autoriser l'accès.");
                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                alertDialog.show();
                                                return;
                                            }
                                            String url = podcasts.get(titles.indexOf(title));
                                            DownloadManager.Request request = new DownloadManager.Request(
                                                    Uri.parse(url));
                                            request.setDescription("podcast");
                                            String finalTitle = title + " " + day;
                                            request.setTitle(finalTitle + ".mp3");
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                request.allowScanningByMediaScanner();
                                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            }
                                            try {
                                                request.setDestinationInExternalPublicDir(
                                                        Environment.DIRECTORY_DOWNLOADS,
                                                        finalTitle + ".mp3");
                                            } catch (IllegalStateException e) {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                        DownloadActivity.this);
                                                alertDialogBuilder.setTitle("Problème de stockage");
                                                alertDialogBuilder
                                                        .setMessage(
                                                                "Impossible d'écrire sur la mémoire externe.")
                                                        .setCancelable(false)
                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                DownloadActivity.this.finish();
                                                            }
                                                        });

                                                // create alert dialog
                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                                return;
                                            }
                                            // get download service and enqueue
                                            // file
                                            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                            manager.enqueue(request);
                                            utils.addSeen(finalTitle);
                                        }
                                    })
                            .setNegativeButton("Non",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });
        }
    }

    /**
     * Closes the connection.
     */
    private void connectionProblem() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                DownloadActivity.this);
        alertDialogBuilder.setTitle("Problème de connexion");
        alertDialogBuilder
                .setMessage(
                        "Impossible de contacter le serveur, veuillez réessayer plus tard.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DownloadActivity.this.finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
