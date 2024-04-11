package net.hearnsoft.gensokyoradio.trd.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.hearnsoft.gensokyoradio.trd.beans.SongHistoryBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SongHistoryDbHelper extends SQLiteOpenHelper {

    // 全局变量
    private static final String TAG = SongHistoryDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "SongHistoryDb.db";
    private static final int DATABASE_VERSION = 1;
    private static volatile SongHistoryDbHelper instance;

    // 创建SongTable
    private static final String TABLE_SONG = "SongTable";
    private static final String COLUMN_SONG_ID = "song_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_ARTIST = "artist";
    private static final String COLUMN_ALBUM = "album";
    private static final String COLUMN_CIRCLE = "circle";
    private static final String COLUMN_COVER_URL = "cover_url";
    private static final String COLUMN_ALBUM_ID = "album_id";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_SONG_TABLE =
            "CREATE TABLE " + TABLE_SONG + "(" +
                    COLUMN_SONG_ID + " INTEGER NOT NULL," +
                    COLUMN_TITLE + " TEXT NOT NULL," +
                    COLUMN_ARTIST + " TEXT NOT NULL," +
                    COLUMN_ALBUM + " TEXT NOT NULL," +
                    COLUMN_CIRCLE + " TEXT NOT NULL," +
                    COLUMN_COVER_URL + " TEXT NOT NULL," +
                    COLUMN_ALBUM_ID + " INTEGER NOT NULL," +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                    "PRIMARY KEY (" + COLUMN_SONG_ID + ", " + COLUMN_TIMESTAMP + "))";

    public SongHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SongHistoryDbHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (SongHistoryDbHelper.class) {
                if (instance == null) {
                    instance = new SongHistoryDbHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }

    public long insertSong(int songId, String title, String artist, String album, String circle, String coverUrl, int albumId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SONG_ID, songId);
        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_ARTIST, artist);
        contentValues.put(COLUMN_ALBUM, album);
        contentValues.put(COLUMN_CIRCLE, circle);
        contentValues.put(COLUMN_COVER_URL, coverUrl);
        contentValues.put(COLUMN_ALBUM_ID, albumId);
        contentValues.put(COLUMN_TIMESTAMP, System.currentTimeMillis()); // 设置当前时间戳

        long rowId = db.insert(TABLE_SONG, null, contentValues);
        db.close();

        return rowId;
    }

    public List<SongHistoryBean> getAllSongs() {
        List<SongHistoryBean> songList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_SONG + " ORDER BY " + COLUMN_TIMESTAMP + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                List<Integer> columnIndices = Arrays.asList(
                        cursor.getColumnIndex(COLUMN_SONG_ID),
                        cursor.getColumnIndex(COLUMN_TITLE),
                        cursor.getColumnIndex(COLUMN_ARTIST),
                        cursor.getColumnIndex(COLUMN_ALBUM),
                        cursor.getColumnIndex(COLUMN_CIRCLE),
                        cursor.getColumnIndex(COLUMN_COVER_URL),
                        cursor.getColumnIndex(COLUMN_ALBUM_ID),
                        cursor.getColumnIndex(COLUMN_TIMESTAMP)
                );

                if (columnIndices.stream().allMatch(index -> index >= 0)) {
                    SongHistoryBean songBean = new SongHistoryBean(
                            cursor.getInt(columnIndices.get(0)),
                            cursor.getString(columnIndices.get(1)),
                            cursor.getString(columnIndices.get(2)),
                            cursor.getString(columnIndices.get(3)),
                            cursor.getString(columnIndices.get(4)),
                            cursor.getString(columnIndices.get(5)),
                            cursor.getInt(columnIndices.get(6)),
                            cursor.getLong(columnIndices.get(7))
                    );
                    songList.add(songBean);
                } else {
                    Log.e(TAG, "index <= -1 error");
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return songList;
    }

    /**
     * 清空数据库，删除所有表
     */
    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        List<String> tables = getAllTables(db);

        for (String table : tables) {
            String deleteTableQuery = "DROP TABLE IF EXISTS " + table;
            db.execSQL(deleteTableQuery);
        }
        // new an empty tables
        db.execSQL(CREATE_SONG_TABLE);
        db.close();
    }

    /**
     * 获取数据库中所有的表名
     *
     * @param db writable SQLiteDatabase实例
     * @return 所有表名的列表
     */
    private List<String> getAllTables(SQLiteDatabase db) {
        List<String> tables = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0));
        }
        cursor.close();

        return tables;
    }

}
