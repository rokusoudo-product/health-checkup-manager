package com.rokusodo.healthcheckup.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rokusodo.healthcheckup.data.db.entity.ExaminationItem;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExaminationItemDao_Impl implements ExaminationItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExaminationItem> __insertionAdapterOfExaminationItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByRecordId;

  public ExaminationItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExaminationItem = new EntityInsertionAdapter<ExaminationItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `examination_items` (`id`,`recordId`,`itemName`,`value`,`unit`,`referenceMin`,`referenceMax`,`isAbnormal`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExaminationItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecordId());
        if (entity.getItemName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getItemName());
        }
        if (entity.getValue() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getValue());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUnit());
        }
        if (entity.getReferenceMin() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getReferenceMin());
        }
        if (entity.getReferenceMax() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getReferenceMax());
        }
        final int _tmp = entity.isAbnormal() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__preparedStmtOfDeleteByRecordId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM examination_items WHERE recordId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<ExaminationItem> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExaminationItem.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByRecordId(final long recordId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByRecordId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, recordId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByRecordId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExaminationItem>> getByRecordId(final long recordId) {
    final String _sql = "SELECT * FROM examination_items WHERE recordId = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"examination_items"}, new Callable<List<ExaminationItem>>() {
      @Override
      @NonNull
      public List<ExaminationItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfReferenceMin = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMin");
          final int _cursorIndexOfReferenceMax = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMax");
          final int _cursorIndexOfIsAbnormal = CursorUtil.getColumnIndexOrThrow(_cursor, "isAbnormal");
          final List<ExaminationItem> _result = new ArrayList<ExaminationItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExaminationItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordId;
            _tmpRecordId = _cursor.getLong(_cursorIndexOfRecordId);
            final String _tmpItemName;
            if (_cursor.isNull(_cursorIndexOfItemName)) {
              _tmpItemName = null;
            } else {
              _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
            }
            final String _tmpValue;
            if (_cursor.isNull(_cursorIndexOfValue)) {
              _tmpValue = null;
            } else {
              _tmpValue = _cursor.getString(_cursorIndexOfValue);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final Double _tmpReferenceMin;
            if (_cursor.isNull(_cursorIndexOfReferenceMin)) {
              _tmpReferenceMin = null;
            } else {
              _tmpReferenceMin = _cursor.getDouble(_cursorIndexOfReferenceMin);
            }
            final Double _tmpReferenceMax;
            if (_cursor.isNull(_cursorIndexOfReferenceMax)) {
              _tmpReferenceMax = null;
            } else {
              _tmpReferenceMax = _cursor.getDouble(_cursorIndexOfReferenceMax);
            }
            final boolean _tmpIsAbnormal;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAbnormal);
            _tmpIsAbnormal = _tmp != 0;
            _item = new ExaminationItem(_tmpId,_tmpRecordId,_tmpItemName,_tmpValue,_tmpUnit,_tmpReferenceMin,_tmpReferenceMax,_tmpIsAbnormal);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getItemTrendByName(final String itemName,
      final Continuation<? super List<ItemTrend>> $completion) {
    final String _sql = "\n"
            + "        SELECT ei.id, ei.recordId, ei.itemName, ei.value, ei.unit,\n"
            + "               ei.referenceMin, ei.referenceMax, ei.isAbnormal,\n"
            + "               er.date as recordDate\n"
            + "        FROM examination_items ei\n"
            + "        INNER JOIN examination_records er ON ei.recordId = er.id\n"
            + "        WHERE ei.itemName = ?\n"
            + "        ORDER BY er.date ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (itemName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, itemName);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ItemTrend>>() {
      @Override
      @NonNull
      public List<ItemTrend> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfRecordId = 1;
          final int _cursorIndexOfItemName = 2;
          final int _cursorIndexOfValue = 3;
          final int _cursorIndexOfUnit = 4;
          final int _cursorIndexOfReferenceMin = 5;
          final int _cursorIndexOfReferenceMax = 6;
          final int _cursorIndexOfIsAbnormal = 7;
          final int _cursorIndexOfRecordDate = 8;
          final List<ItemTrend> _result = new ArrayList<ItemTrend>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemTrend _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordId;
            _tmpRecordId = _cursor.getLong(_cursorIndexOfRecordId);
            final String _tmpItemName;
            if (_cursor.isNull(_cursorIndexOfItemName)) {
              _tmpItemName = null;
            } else {
              _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
            }
            final String _tmpValue;
            if (_cursor.isNull(_cursorIndexOfValue)) {
              _tmpValue = null;
            } else {
              _tmpValue = _cursor.getString(_cursorIndexOfValue);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final Double _tmpReferenceMin;
            if (_cursor.isNull(_cursorIndexOfReferenceMin)) {
              _tmpReferenceMin = null;
            } else {
              _tmpReferenceMin = _cursor.getDouble(_cursorIndexOfReferenceMin);
            }
            final Double _tmpReferenceMax;
            if (_cursor.isNull(_cursorIndexOfReferenceMax)) {
              _tmpReferenceMax = null;
            } else {
              _tmpReferenceMax = _cursor.getDouble(_cursorIndexOfReferenceMax);
            }
            final boolean _tmpIsAbnormal;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAbnormal);
            _tmpIsAbnormal = _tmp != 0;
            final String _tmpRecordDate;
            if (_cursor.isNull(_cursorIndexOfRecordDate)) {
              _tmpRecordDate = null;
            } else {
              _tmpRecordDate = _cursor.getString(_cursorIndexOfRecordDate);
            }
            _item = new ItemTrend(_tmpId,_tmpRecordId,_tmpItemName,_tmpValue,_tmpUnit,_tmpReferenceMin,_tmpReferenceMax,_tmpIsAbnormal,_tmpRecordDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExaminationItem>> getAllAbnormalItems() {
    final String _sql = "SELECT * FROM examination_items WHERE isAbnormal = 1 ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"examination_items"}, new Callable<List<ExaminationItem>>() {
      @Override
      @NonNull
      public List<ExaminationItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfReferenceMin = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMin");
          final int _cursorIndexOfReferenceMax = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMax");
          final int _cursorIndexOfIsAbnormal = CursorUtil.getColumnIndexOrThrow(_cursor, "isAbnormal");
          final List<ExaminationItem> _result = new ArrayList<ExaminationItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExaminationItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordId;
            _tmpRecordId = _cursor.getLong(_cursorIndexOfRecordId);
            final String _tmpItemName;
            if (_cursor.isNull(_cursorIndexOfItemName)) {
              _tmpItemName = null;
            } else {
              _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
            }
            final String _tmpValue;
            if (_cursor.isNull(_cursorIndexOfValue)) {
              _tmpValue = null;
            } else {
              _tmpValue = _cursor.getString(_cursorIndexOfValue);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final Double _tmpReferenceMin;
            if (_cursor.isNull(_cursorIndexOfReferenceMin)) {
              _tmpReferenceMin = null;
            } else {
              _tmpReferenceMin = _cursor.getDouble(_cursorIndexOfReferenceMin);
            }
            final Double _tmpReferenceMax;
            if (_cursor.isNull(_cursorIndexOfReferenceMax)) {
              _tmpReferenceMax = null;
            } else {
              _tmpReferenceMax = _cursor.getDouble(_cursorIndexOfReferenceMax);
            }
            final boolean _tmpIsAbnormal;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAbnormal);
            _tmpIsAbnormal = _tmp != 0;
            _item = new ExaminationItem(_tmpId,_tmpRecordId,_tmpItemName,_tmpValue,_tmpUnit,_tmpReferenceMin,_tmpReferenceMax,_tmpIsAbnormal);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByRecordIdOnce(final long recordId,
      final Continuation<? super List<ExaminationItem>> $completion) {
    final String _sql = "SELECT * FROM examination_items WHERE recordId = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExaminationItem>>() {
      @Override
      @NonNull
      public List<ExaminationItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfReferenceMin = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMin");
          final int _cursorIndexOfReferenceMax = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMax");
          final int _cursorIndexOfIsAbnormal = CursorUtil.getColumnIndexOrThrow(_cursor, "isAbnormal");
          final List<ExaminationItem> _result = new ArrayList<ExaminationItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExaminationItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordId;
            _tmpRecordId = _cursor.getLong(_cursorIndexOfRecordId);
            final String _tmpItemName;
            if (_cursor.isNull(_cursorIndexOfItemName)) {
              _tmpItemName = null;
            } else {
              _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
            }
            final String _tmpValue;
            if (_cursor.isNull(_cursorIndexOfValue)) {
              _tmpValue = null;
            } else {
              _tmpValue = _cursor.getString(_cursorIndexOfValue);
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final Double _tmpReferenceMin;
            if (_cursor.isNull(_cursorIndexOfReferenceMin)) {
              _tmpReferenceMin = null;
            } else {
              _tmpReferenceMin = _cursor.getDouble(_cursorIndexOfReferenceMin);
            }
            final Double _tmpReferenceMax;
            if (_cursor.isNull(_cursorIndexOfReferenceMax)) {
              _tmpReferenceMax = null;
            } else {
              _tmpReferenceMax = _cursor.getDouble(_cursorIndexOfReferenceMax);
            }
            final boolean _tmpIsAbnormal;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAbnormal);
            _tmpIsAbnormal = _tmp != 0;
            _item = new ExaminationItem(_tmpId,_tmpRecordId,_tmpItemName,_tmpValue,_tmpUnit,_tmpReferenceMin,_tmpReferenceMax,_tmpIsAbnormal);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
