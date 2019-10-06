/*
 * Copyright (C)  2019 Domjos
 * This file is part of UniTrackerMobile <https://github.com/domjos1994/UniTrackerMobile>.
 *
 * UniTrackerMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UniTrackerMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UniTrackerMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.unitrackermobile.helper;

import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteStatement;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.domjos.unitrackerlibrary.services.engine.Authentication;
import de.domjos.unitrackerlibrary.utils.Crypto;
import de.domjos.unitrackerlibrary.utils.MessageHelper;
import de.domjos.unitrackermobile.R;

public class SQLiteGeneral extends SQLiteOpenHelper {
    static final String NO_PASS = "noPassword";
    private Context context;
    private String password;
    private Crypto crypto;

    SQLiteGeneral(Context context, String password) throws Exception {
        super(context, "general.db", null, Helper.getVersionCode(context));
        this.context = context;
        this.password = password;

        this.crypto = new Crypto(context, this.password);
    }

    public void changePassword(String newPassword) throws Exception {
        List<Authentication> accounts = this.getAccounts("");
        if(newPassword.isEmpty()) {
            newPassword = SQLiteGeneral.NO_PASS;
        }
        this.crypto = new Crypto(context, newPassword);
        for(Authentication authentication : accounts) {
            this.insertOrUpdateAccount(authentication);
        }
        this.getWritableDatabase().rawExecSQL("PRAGMA key = '" + this.password + "';");
        this.getWritableDatabase().rawExecSQL("PRAGMA rekey = '" + newPassword + "';");
        this.password = newPassword;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.initDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.initDatabase(db);
        this.updateDatabase(db);
    }

    private SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase(this.password);
    }

    private SQLiteDatabase getReadableDatabase(boolean onlyCheck) {
        try {
            if(onlyCheck) {
                return super.getReadableDatabase(this.password);
            }
        } catch (Exception ignored){}
        return null;
    }

    private SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase(this.password);
    }

    public List<Authentication> getAccounts(String where) {
        return this.getAccounts(where, false);
    }

    List<Authentication> getAccounts(String where, boolean onlyCheck) {
        List<Authentication> authentications = new LinkedList<>();
        try {
            Cursor cursor;
            String sql = "SELECT * FROM accounts" + (!where.trim().equals("") ? " WHERE " + where : "");
            if(onlyCheck) {
                SQLiteDatabase database = this.getReadableDatabase(true);
                if(database!=null) {
                    cursor = database.rawQuery(sql, null);
                } else {
                    return null;
                }
            } else {
                cursor = this.getReadableDatabase().rawQuery(sql, null);
            }
            if(cursor!=null) {
                while (cursor.moveToNext()) {
                    Authentication authentication = new Authentication();
                    authentication.setServer(this.crypto.decryptString(this.getString(cursor, "serverName")));
                    authentication.setUserName(this.crypto.decryptString(this.getString(cursor, "userName")));
                    authentication.setPassword(this.crypto.decryptString(this.getString(cursor, "password")));
                    authentication.setAPIKey(this.getString(cursor, "apiKey"));
                    authentication.setTitle(this.getString(cursor, "title"));
                    authentication.setDescription(this.getString(cursor, "description"));
                    authentication.setCover(cursor.getBlob(cursor.getColumnIndex("cover")));
                    authentication.setTracker(Authentication.Tracker.valueOf(this.getString(cursor, "tracker")));
                    authentication.setId(cursor.getLong(cursor.getColumnIndex("ID")));
                    authentication.setGuest(cursor.getLong(cursor.getColumnIndex("guest")) == 1);
                    authentications.add(authentication);
                }
                cursor.close();

                for(int i = 0; i<=authentications.size()-1; i++) {
                    authentications.get(i).setHints(this.getHints(authentications.get(i).getId()));
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            if(onlyCheck) {
                return null;
            } else {
                MessageHelper.printException(ex, this.context);
            }
        }
        return authentications;
    }

    public void insertOrUpdateAccount(Authentication authentication) {
        try {
            if (authentication != null) {
                SQLiteDatabase db = this.getWritableDatabase();
                SQLiteStatement stmt;
                if (authentication.getId() != null) {
                    stmt = db.compileStatement("UPDATE accounts SET title=?, serverName=?, apiKey=?, userName=?, password=?, description=?, cover=?, tracker=?, guest=? WHERE ID=?");
                    stmt.bindLong(10, authentication.getId());
                } else {
                    stmt = db.compileStatement("INSERT INTO accounts(title, serverName, apiKey, userName, password, description, cover, tracker, guest) VALUES(?,?,?,?,?,?,?,?,?)");
                }
                stmt.bindString(1, authentication.getTitle());
                stmt.bindString(3, authentication.getAPIKey());
                stmt.bindString(4, this.crypto.encryptString(authentication.getUserName()));
                stmt.bindString(5, this.crypto.encryptString(authentication.getPassword()));
                stmt.bindString(2, this.crypto.encryptString(authentication.getServer()));
                stmt.bindString(6, authentication.getDescription());
                if (authentication.getCover() != null) {
                    stmt.bindBlob(7, authentication.getCover());
                } else {
                    stmt.bindNull(7);
                }
                if (authentication.getTracker() != null) {
                    stmt.bindString(8, authentication.getTracker().name());
                } else {
                    stmt.bindString(8, Authentication.Tracker.Local.name());
                }
                stmt.bindLong(9, authentication.isGuest() ? 1 : 0);

                if(authentication.getId() == null) {
                    authentication.setId(stmt.executeInsert());
                } else {
                    stmt.execute();
                }
                this.setHints(authentication);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }

    private Map<String, String> getHints(long id) {
        Map<String, String> hints = new LinkedHashMap<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM hints WHERE account=" + id, new String[]{});
            while (cursor.moveToNext()) {
                hints.put(this.getString(cursor, "hint_key"), this.getString(cursor, "hint_value"));
            }
            cursor.close();
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
        return hints;
    }

    private void setHints(Authentication authentication) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM hints WHERE account=" + authentication.getId());
        for(Map.Entry<String, String> entry : authentication.getHints().entrySet()) {
            SQLiteStatement preparedStatement = db.compileStatement("INSERT INTO hints(hint_key, hint_value, account) VALUES(?, ?, ?)");
            preparedStatement.bindString(1, entry.getKey());
            preparedStatement.bindString(2, entry.getValue());
            preparedStatement.bindLong(3, authentication.getId());
            preparedStatement.executeInsert();
            preparedStatement.close();
        }
    }

    boolean duplicated(String table, String column, String value, String where) {
        boolean duplicated = false;
        SQLiteDatabase db = this.getReadableDatabase();
        where = where.trim().isEmpty() ? "" : " AND " + where;
        Cursor cursor = db.rawQuery((String.format("SELECT %s FROM %s WHERE %s=?", column, table, column) + where), new String[]{value});
        while (cursor.moveToNext()) {
            duplicated = true;
        }
        cursor.close();
        return duplicated;
    }

    public void delete(String table, String column, Object value) {
        if (value == null) {
            this.getWritableDatabase().execSQL("DELETE FROM " + table);
        } else {
            if (value instanceof Integer) {
                this.getWritableDatabase().execSQL("DELETE FROM " + table + " WHERE " + column + "=" + value.toString() + "");
            } else {
                this.getWritableDatabase().execSQL("DELETE FROM " + table + " WHERE " + column + "='" + value.toString() + "'");
            }
        }
    }

    private String getString(Cursor cursor, String key) {
        if (cursor.getColumnIndex(key) == -1) {
            return "";
        } else {
            return cursor.getString(cursor.getColumnIndex(key));
        }
    }

    private void initDatabase(SQLiteDatabase db) {
        try {
            String queries = Helper.readStringFromRaw(R.raw.init, context);
            for (String query : queries.split(";")) {
                db.execSQL(query.trim());
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }

    private void updateDatabase(SQLiteDatabase db) {
        try {
            String queries = Helper.readStringFromRaw(R.raw.update, context);
            for (String query : queries.split(";")) {
                if (!query.trim().isEmpty()) {
                    db.execSQL(query.trim());
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, this.context);
        }
    }
}
