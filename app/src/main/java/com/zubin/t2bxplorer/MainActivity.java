package com.zubin.t2bxplorer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String backupImagesName = "88722462437", backupCacheName = "8872464222243";
    private String backupPath, imagesBackupPath, cacheBackupPath;
    private TextView result;

    final String backupDBName = "8872232822273", backupPrefName = "88722773336237",
            backupDir = ".982465238288782";
    String[] files;
    String complete;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("TUPB2 Backup Xplorer");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        result = (TextView) findViewById(R.id.result);

        backupPath = Environment.getExternalStorageDirectory() + File.separator + "Android" +
                File.separator + "data" + File.separator + backupDir + File.separator;
        imagesBackupPath = backupPath + File.separator + backupImagesName + File.separator;
        cacheBackupPath = backupPath + File.separator + backupCacheName + File.separator;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_in_the_backup) {
            complete = "";
            file = new File(backupPath);
            if (!file.exists()) {
                result.setText("NO BACKUP EXISTS!");
                return true;
            } else {
                files = file.list();
                if (files.length > 0) {
                    for (String str : files) {
                        switch (str) {
                            case backupDBName:
                                complete += str + " [BACKUP DB]\n";
                                break;
                            case backupPrefName:
                                complete += str + " [BACKUP PREF]\n";
                                break;
                            case backupImagesName:
                                complete += str + " [BACKUP IMAGES] [D]\n";
                                break;
                            case backupCacheName:
                                complete += str + " [BACKUP CACHE] [D]\n";
                                break;
                        }
                    }
                } else {
                    complete += "NO FILES IN DIRECTORY";
                }
                result.setText(complete);
            }
        } else if (id == R.id.nav_in_the_images) {
            complete = "";
            file = new File(imagesBackupPath);
            if (!file.exists()) {
                result.setText("NO BACKUP EXISTS!");
                return true;
            } else {
                files = file.list();
                if (files.length > 0) {
                    complete += "Files in directory: " + files.length + "\n";
                    for (String str : files) {
                        complete += str + "\n";
                    }
                } else {
                    complete += "NO FILES IN DIRECTORY";
                }
            }
            result.setText(complete);
        } else if (id == R.id.nav_in_the_cache) {
            complete = "";
            file = new File(cacheBackupPath);
            if (!file.exists()) {
                result.setText("NO BACKUP EXISTS!");
                return true;
            } else {
                files = file.list();
                if (files.length > 0) {
                    complete += "Files in directory: " + files.length + "\n";
                    for (String str : files) {
                        complete += str + "\n";
                    }
                } else {
                    complete += "NO FILES IN DIRECTORY";
                }
            }
            result.setText(complete);
        } else if (id == R.id.nav_properties) {
            complete = "";
            try {
                file = new File(backupPath);
                if (file.exists()) {
                    complete += "Created: " + new Date(file.lastModified()).toString() + "\n";
                    complete += "Size: " + readableFileSize(FileUtils.sizeOfDirectory(file));
                } else
                    complete += "NO BACKUP EXISTS!";
                result.setText(complete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_zip) {
            complete = "";
            try {
                file = new File(backupPath);
                if (file.exists()) {
                    zip(file, new File(Environment.getExternalStorageDirectory() + File.separator +
                            "TUPB2B.zip"));
                    complete += "ZIP CREATED!";
                } else
                    complete += "NO BACKUP EXISTS!";
                result.setText(complete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_delete_backup) {
            complete = "";
            try {
                file = new File(backupPath);
                if (file.exists()) {
                    FileUtils.deleteDirectory(file);
                    complete += "BACKUP DELETED!";
                } else
                    complete += "NO BACKUP EXISTS!";
                result.setText(complete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void zip(File directory, File zipfile) throws IOException {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<>();
        queue.push(directory);
        OutputStream out = new FileOutputStream(zipfile);
        Closeable res = out;
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            //zout.setMethod(ZipOutputStream.STORED);
            zout.setLevel(Deflater.BEST_COMPRESSION);
            res = zout;
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File kid : directory.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
        } finally {
            res.close();
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

    private String readableFileSize(long size) {
        String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        if (size <= 0) return "0";
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }
}
