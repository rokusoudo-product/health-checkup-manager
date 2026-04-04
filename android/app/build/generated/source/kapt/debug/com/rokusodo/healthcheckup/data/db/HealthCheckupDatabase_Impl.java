package com.rokusodo.healthcheckup.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.rokusodo.healthcheckup.data.db.dao.ExaminationItemDao;
import com.rokusodo.healthcheckup.data.db.dao.ExaminationItemDao_Impl;
import com.rokusodo.healthcheckup.data.db.dao.ExaminationRecordDao;
import com.rokusodo.healthcheckup.data.db.dao.ExaminationRecordDao_Impl;
import com.rokusodo.healthcheckup.data.db.dao.ItemMasterDao;
import com.rokusodo.healthcheckup.data.db.dao.ItemMasterDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HealthCheckupDatabase_Impl extends HealthCheckupDatabase {
  private volatile ExaminationRecordDao _examinationRecordDao;

  private volatile ExaminationItemDao _examinationItemDao;

  private volatile ItemMasterDao _itemMasterDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `examination_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `facility` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `examination_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordId` INTEGER NOT NULL, `itemName` TEXT NOT NULL, `value` TEXT NOT NULL, `unit` TEXT NOT NULL, `referenceMin` REAL, `referenceMax` REAL, `isAbnormal` INTEGER NOT NULL, FOREIGN KEY(`recordId`) REFERENCES `examination_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_examination_items_recordId` ON `examination_items` (`recordId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `item_masters` (`itemName` TEXT NOT NULL, `unit` TEXT NOT NULL, `referenceMin` REAL, `referenceMax` REAL, PRIMARY KEY(`itemName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0eb0e781994c4d1c4baed85ee5db5728')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `examination_records`");
        db.execSQL("DROP TABLE IF EXISTS `examination_items`");
        db.execSQL("DROP TABLE IF EXISTS `item_masters`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsExaminationRecords = new HashMap<String, TableInfo.Column>(4);
        _columnsExaminationRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationRecords.put("date", new TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationRecords.put("facility", new TableInfo.Column("facility", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationRecords.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExaminationRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExaminationRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExaminationRecords = new TableInfo("examination_records", _columnsExaminationRecords, _foreignKeysExaminationRecords, _indicesExaminationRecords);
        final TableInfo _existingExaminationRecords = TableInfo.read(db, "examination_records");
        if (!_infoExaminationRecords.equals(_existingExaminationRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "examination_records(com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord).\n"
                  + " Expected:\n" + _infoExaminationRecords + "\n"
                  + " Found:\n" + _existingExaminationRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsExaminationItems = new HashMap<String, TableInfo.Column>(8);
        _columnsExaminationItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("recordId", new TableInfo.Column("recordId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("itemName", new TableInfo.Column("itemName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("value", new TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("referenceMin", new TableInfo.Column("referenceMin", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("referenceMax", new TableInfo.Column("referenceMax", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExaminationItems.put("isAbnormal", new TableInfo.Column("isAbnormal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExaminationItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysExaminationItems.add(new TableInfo.ForeignKey("examination_records", "CASCADE", "NO ACTION", Arrays.asList("recordId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExaminationItems = new HashSet<TableInfo.Index>(1);
        _indicesExaminationItems.add(new TableInfo.Index("index_examination_items_recordId", false, Arrays.asList("recordId"), Arrays.asList("ASC")));
        final TableInfo _infoExaminationItems = new TableInfo("examination_items", _columnsExaminationItems, _foreignKeysExaminationItems, _indicesExaminationItems);
        final TableInfo _existingExaminationItems = TableInfo.read(db, "examination_items");
        if (!_infoExaminationItems.equals(_existingExaminationItems)) {
          return new RoomOpenHelper.ValidationResult(false, "examination_items(com.rokusodo.healthcheckup.data.db.entity.ExaminationItem).\n"
                  + " Expected:\n" + _infoExaminationItems + "\n"
                  + " Found:\n" + _existingExaminationItems);
        }
        final HashMap<String, TableInfo.Column> _columnsItemMasters = new HashMap<String, TableInfo.Column>(4);
        _columnsItemMasters.put("itemName", new TableInfo.Column("itemName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItemMasters.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItemMasters.put("referenceMin", new TableInfo.Column("referenceMin", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsItemMasters.put("referenceMax", new TableInfo.Column("referenceMax", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysItemMasters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesItemMasters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoItemMasters = new TableInfo("item_masters", _columnsItemMasters, _foreignKeysItemMasters, _indicesItemMasters);
        final TableInfo _existingItemMasters = TableInfo.read(db, "item_masters");
        if (!_infoItemMasters.equals(_existingItemMasters)) {
          return new RoomOpenHelper.ValidationResult(false, "item_masters(com.rokusodo.healthcheckup.data.db.entity.ItemMaster).\n"
                  + " Expected:\n" + _infoItemMasters + "\n"
                  + " Found:\n" + _existingItemMasters);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "0eb0e781994c4d1c4baed85ee5db5728", "c0d41e5931d6df613249e545e869f9f7");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "examination_records","examination_items","item_masters");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `examination_records`");
      _db.execSQL("DELETE FROM `examination_items`");
      _db.execSQL("DELETE FROM `item_masters`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ExaminationRecordDao.class, ExaminationRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExaminationItemDao.class, ExaminationItemDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ItemMasterDao.class, ItemMasterDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ExaminationRecordDao recordDao() {
    if (_examinationRecordDao != null) {
      return _examinationRecordDao;
    } else {
      synchronized(this) {
        if(_examinationRecordDao == null) {
          _examinationRecordDao = new ExaminationRecordDao_Impl(this);
        }
        return _examinationRecordDao;
      }
    }
  }

  @Override
  public ExaminationItemDao itemDao() {
    if (_examinationItemDao != null) {
      return _examinationItemDao;
    } else {
      synchronized(this) {
        if(_examinationItemDao == null) {
          _examinationItemDao = new ExaminationItemDao_Impl(this);
        }
        return _examinationItemDao;
      }
    }
  }

  @Override
  public ItemMasterDao masterDao() {
    if (_itemMasterDao != null) {
      return _itemMasterDao;
    } else {
      synchronized(this) {
        if(_itemMasterDao == null) {
          _itemMasterDao = new ItemMasterDao_Impl(this);
        }
        return _itemMasterDao;
      }
    }
  }
}
