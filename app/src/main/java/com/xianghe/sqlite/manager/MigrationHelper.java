package com.xianghe.sqlite.manager;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;


import com.xianghe.sqlite.db.dao.DaoMaster;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MigrationHelper {

    private static final String CONVERSION_CLASS_NOT_FOUND_EXCEPTION = "MIGRATION HELPER - CLASS DOESN'T MATCH WITH THE CURRENT PARAMETERS";
    private static MigrationHelper instance;
    private static final String TAG = MigrationHelper.class.getSimpleName();

    static MigrationHelper getInstance() {
        if (instance == null) {
            instance = new MigrationHelper();
        }
        return instance;
    }

    @SafeVarargs
    final void migrate(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        generateTempTables(db, daoClasses);
        DaoMaster.dropAllTables(db, true);
        DaoMaster.createAllTables(db, false);
        restoreData(db, daoClasses);
    }

    @SafeVarargs
    private final void generateTempTables(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
            DaoConfig daoConfig = new DaoConfig(db, daoClass);

            String divider = "";
            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            ArrayList<String> properties = new ArrayList<>();

            if (!tableIsExist(db, tableName)) continue;

            StringBuilder createTableStringBuilder = new StringBuilder();

            createTableStringBuilder.append("CREATE TABLE ").append(tempTableName).append(" (");

            for (int j = 0; j < daoConfig.properties.length; j++) {
                String columnName = daoConfig.properties[j].columnName;

                if (getColumns(db, tableName).contains(columnName)) {
                    properties.add(columnName);

                    String type = null;

                    try {
                        type = getTypeByClass(daoConfig.properties[j].type);
                    } catch (Exception exception) {
//                        Crashlytics.logException(exception);
                    }

                    createTableStringBuilder.append(divider).append(columnName).append(" ").append(type);

                    if (daoConfig.properties[j].primaryKey) {
                        createTableStringBuilder.append(" PRIMARY KEY");
                    }

                    divider = ",";
                }
            }
            createTableStringBuilder.append(");");

            Log.e(TAG, "1-generateTempTables: "+createTableStringBuilder.toString());
            db.execSQL(createTableStringBuilder.toString());

            String insertTableStringBuilder = "INSERT INTO " + tempTableName + " (" +
                    TextUtils.join(",", properties) +
                    ") SELECT " +
                    TextUtils.join(",", properties) +
                    " FROM " + tableName + ";";

            Log.e(TAG, "2-generateTempTables: "+insertTableStringBuilder);
            db.execSQL(insertTableStringBuilder);
        }
    }

    @SafeVarargs
    private final void restoreData(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
            DaoConfig daoConfig = new DaoConfig(db, daoClass);

            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            List<String> properties = new ArrayList<>();
            String insertTableStringBuilder;
            if (!tableIsExist(db, tempTableName)) continue;

            for (int j = 0; j < daoConfig.properties.length; j++) {
                String columnName = daoConfig.properties[j].columnName;

                if (!getColumns(db,tempTableName).contains(columnName)) {
                    insertTableStringBuilder = "ALTER TABLE " + tempTableName + " ADD COLUMN " +
                            columnName + getTypeByClass(daoConfig.properties[j].type);
                    Log.e(TAG, "restoreData: "+insertTableStringBuilder);
                    db.execSQL(insertTableStringBuilder);
                }
//                properties.add(columnName);

            if (getColumns(db, tempTableName).contains(columnName)) {
                properties.add(columnName);
            }
        }

        insertTableStringBuilder = "INSERT INTO " + tableName + " (" +
                TextUtils.join(",", properties) +
                ") SELECT " +
                TextUtils.join(",", properties) +
                " FROM " + tempTableName + ";";
            Log.e(TAG, "restoreData: "+insertTableStringBuilder);
        db.execSQL(insertTableStringBuilder);
        db.execSQL("DROP TABLE " + tempTableName);
    }

}

    /**
     * 判断表是否存在
     *
     * @param db
     * @param tableName 表名
     * @return true-存在，false-不存在
     */
    private boolean tableIsExist(Database db, String tableName) {
        String isExistTableString = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        Cursor cursor = db.rawQuery(isExistTableString, null);
        if (cursor.moveToNext() && cursor.getInt(0) == 1) {
            return true;
        } else {
            return false;
        }
    }

    private String getTypeByClass(Class<?> type) {
        if (type.equals(String.class)) {
            return "TEXT";
        }
        if (type.equals(Long.class) || type.equals(Integer.class) || type.equals(long.class)) {
            return "INTEGER DEFAULT 0";
        }
        if (type.equals(Boolean.class)) {
            return "NUMERIC DEFAULT 0";
        }
        return "TEXT";

        //        Crashlytics.logException(exception);
//        throw new Exception(CONVERSION_CLASS_NOT_FOUND_EXCEPTION.concat(" - Class: ").concat(type.toString()));
    }

    private static List<String> getColumns(Database db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return columns;
    }
}