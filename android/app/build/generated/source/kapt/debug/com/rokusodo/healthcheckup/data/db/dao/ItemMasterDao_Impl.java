package com.rokusodo.healthcheckup.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rokusodo.healthcheckup.data.db.entity.ItemMaster;
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
public final class ItemMasterDao_Impl implements ItemMasterDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<ItemMaster> __upsertionAdapterOfItemMaster;

  public ItemMasterDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfItemMaster = new EntityUpsertionAdapter<ItemMaster>(new EntityInsertionAdapter<ItemMaster>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `item_masters` (`itemName`,`unit`,`referenceMin`,`referenceMax`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ItemMaster entity) {
        if (entity.getItemName() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getItemName());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUnit());
        }
        if (entity.getReferenceMin() == null) {
          statement.bindNull(3);
        } else {
          statement.bindDouble(3, entity.getReferenceMin());
        }
        if (entity.getReferenceMax() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getReferenceMax());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<ItemMaster>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `item_masters` SET `itemName` = ?,`unit` = ?,`referenceMin` = ?,`referenceMax` = ? WHERE `itemName` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ItemMaster entity) {
        if (entity.getItemName() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getItemName());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUnit());
        }
        if (entity.getReferenceMin() == null) {
          statement.bindNull(3);
        } else {
          statement.bindDouble(3, entity.getReferenceMin());
        }
        if (entity.getReferenceMax() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getReferenceMax());
        }
        if (entity.getItemName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getItemName());
        }
      }
    });
  }

  @Override
  public Object upsert(final ItemMaster master, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfItemMaster.upsert(master);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ItemMaster>> getAll() {
    final String _sql = "SELECT * FROM item_masters ORDER BY itemName ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"item_masters"}, new Callable<List<ItemMaster>>() {
      @Override
      @NonNull
      public List<ItemMaster> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfReferenceMin = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMin");
          final int _cursorIndexOfReferenceMax = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMax");
          final List<ItemMaster> _result = new ArrayList<ItemMaster>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ItemMaster _item;
            final String _tmpItemName;
            if (_cursor.isNull(_cursorIndexOfItemName)) {
              _tmpItemName = null;
            } else {
              _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
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
            _item = new ItemMaster(_tmpItemName,_tmpUnit,_tmpReferenceMin,_tmpReferenceMax);
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
  public Object getByName(final String itemName,
      final Continuation<? super ItemMaster> $completion) {
    final String _sql = "SELECT * FROM item_masters WHERE itemName = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (itemName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, itemName);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ItemMaster>() {
      @Override
      @Nullable
      public ItemMaster call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfReferenceMin = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMin");
          final int _cursorIndexOfReferenceMax = CursorUtil.getColumnIndexOrThrow(_cursor, "referenceMax");
          final ItemMaster _result;
          if (_cursor.moveToFirst()) {
            final String _tmpItemName;
            if (_cursor.isNull(_cursorIndexOfItemName)) {
              _tmpItemName = null;
            } else {
              _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
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
            _result = new ItemMaster(_tmpItemName,_tmpUnit,_tmpReferenceMin,_tmpReferenceMax);
          } else {
            _result = null;
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
