package com.hermes.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.hermes.app.data.local.entity.TaskEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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
public final class TaskDao_Impl implements TaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TaskEntity> __insertionAdapterOfTaskEntity;

  private final EntityDeletionOrUpdateAdapter<TaskEntity> __deletionAdapterOfTaskEntity;

  private final EntityDeletionOrUpdateAdapter<TaskEntity> __updateAdapterOfTaskEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTaskStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTaskSchedule;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePreGeneratedMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTaskById;

  public TaskDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTaskEntity = new EntityInsertionAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tasks` (`id`,`title`,`isFixed`,`durationMinutes`,`reminderLeadMinutes`,`deadline`,`scheduledStart`,`scheduledEnd`,`isAutoScheduled`,`priority`,`isCompleted`,`createdAt`,`createdRole`,`preGeneratedMessage`,`description`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        final int _tmp = entity.isFixed() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getDurationMinutes() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDurationMinutes());
        }
        statement.bindLong(5, entity.getReminderLeadMinutes());
        if (entity.getDeadline() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDeadline());
        }
        if (entity.getScheduledStart() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getScheduledStart());
        }
        if (entity.getScheduledEnd() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getScheduledEnd());
        }
        final int _tmp_1 = entity.isAutoScheduled() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getPriority());
        final int _tmp_2 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindString(13, entity.getCreatedRole());
        if (entity.getPreGeneratedMessage() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getPreGeneratedMessage());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDescription());
        }
      }
    };
    this.__deletionAdapterOfTaskEntity = new EntityDeletionOrUpdateAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `tasks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTaskEntity = new EntityDeletionOrUpdateAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `tasks` SET `id` = ?,`title` = ?,`isFixed` = ?,`durationMinutes` = ?,`reminderLeadMinutes` = ?,`deadline` = ?,`scheduledStart` = ?,`scheduledEnd` = ?,`isAutoScheduled` = ?,`priority` = ?,`isCompleted` = ?,`createdAt` = ?,`createdRole` = ?,`preGeneratedMessage` = ?,`description` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        final int _tmp = entity.isFixed() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getDurationMinutes() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDurationMinutes());
        }
        statement.bindLong(5, entity.getReminderLeadMinutes());
        if (entity.getDeadline() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDeadline());
        }
        if (entity.getScheduledStart() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getScheduledStart());
        }
        if (entity.getScheduledEnd() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getScheduledEnd());
        }
        final int _tmp_1 = entity.isAutoScheduled() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        statement.bindLong(10, entity.getPriority());
        final int _tmp_2 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindString(13, entity.getCreatedRole());
        if (entity.getPreGeneratedMessage() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getPreGeneratedMessage());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDescription());
        }
        statement.bindLong(16, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateTaskStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET isCompleted = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTaskSchedule = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET scheduledStart = ?, scheduledEnd = ?, isAutoScheduled = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePreGeneratedMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET preGeneratedMessage = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteTaskById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM tasks WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTask(final TaskEntity task, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTaskEntity.insertAndReturnId(task);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTask(final TaskEntity task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTaskEntity.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTask(final TaskEntity task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTaskEntity.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTaskStatus(final long id, final boolean isCompleted,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTaskStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isCompleted ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateTaskStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTaskSchedule(final long id, final Long start, final Long end,
      final boolean isAutoScheduled, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTaskSchedule.acquire();
        int _argIndex = 1;
        if (start == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, start);
        }
        _argIndex = 2;
        if (end == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, end);
        }
        _argIndex = 3;
        final int _tmp = isAutoScheduled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateTaskSchedule.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePreGeneratedMessage(final long id, final String message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePreGeneratedMessage.acquire();
        int _argIndex = 1;
        if (message == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, message);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdatePreGeneratedMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTaskById(final long taskId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTaskById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, taskId);
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
          __preparedStmtOfDeleteTaskById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TaskEntity>> getAllTasks() {
    final String _sql = "SELECT * FROM tasks ORDER BY isCompleted ASC, scheduledStart ASC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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
  public Flow<List<TaskEntity>> getTasksForDateRange(final long startOfDay, final long endOfDay) {
    final String _sql = "\n"
            + "        SELECT * FROM tasks \n"
            + "        WHERE (\n"
            + "            (scheduledStart IS NOT NULL AND scheduledEnd IS NOT NULL AND scheduledStart <= ? AND scheduledEnd >= ?)\n"
            + "            OR (scheduledStart IS NOT NULL AND scheduledEnd IS NULL AND scheduledStart >= ? AND scheduledStart <= ?)\n"
            + "        ) \n"
            + "        OR (scheduledStart IS NULL AND createdAt >= ? AND createdAt <= ?)\n"
            + "        ORDER BY isCompleted ASC, scheduledStart ASC, priority DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, endOfDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 3;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 4;
    _statement.bindLong(_argIndex, endOfDay);
    _argIndex = 5;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 6;
    _statement.bindLong(_argIndex, endOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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
  public Object getFixedTasks(final Continuation<? super List<TaskEntity>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE isFixed = 1 AND isCompleted = 0 ORDER BY scheduledStart ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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
  public Object getUnscheduledFlexibleTasks(
      final Continuation<? super List<TaskEntity>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE isFixed = 0 AND isCompleted = 0 AND scheduledStart IS NULL ORDER BY priority DESC, deadline ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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
  public Object getAllFlexibleTasks(final Continuation<? super List<TaskEntity>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE isFixed = 0 AND isCompleted = 0 ORDER BY priority DESC, deadline ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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
  public Object getTaskById(final long id, final Continuation<? super TaskEntity> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaskEntity>() {
      @Override
      @Nullable
      public TaskEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final TaskEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _result = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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

  @Override
  public Object getActiveScheduledTasks(final Continuation<? super List<TaskEntity>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE isCompleted = 0 AND scheduledStart IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsFixed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFixed");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfReminderLeadMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderLeadMinutes");
          final int _cursorIndexOfDeadline = CursorUtil.getColumnIndexOrThrow(_cursor, "deadline");
          final int _cursorIndexOfScheduledStart = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledStart");
          final int _cursorIndexOfScheduledEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledEnd");
          final int _cursorIndexOfIsAutoScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoScheduled");
          final int _cursorIndexOfPriority = CursorUtil.getColumnIndexOrThrow(_cursor, "priority");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCreatedRole = CursorUtil.getColumnIndexOrThrow(_cursor, "createdRole");
          final int _cursorIndexOfPreGeneratedMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "preGeneratedMessage");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsFixed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFixed);
            _tmpIsFixed = _tmp != 0;
            final Integer _tmpDurationMinutes;
            if (_cursor.isNull(_cursorIndexOfDurationMinutes)) {
              _tmpDurationMinutes = null;
            } else {
              _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            }
            final int _tmpReminderLeadMinutes;
            _tmpReminderLeadMinutes = _cursor.getInt(_cursorIndexOfReminderLeadMinutes);
            final Long _tmpDeadline;
            if (_cursor.isNull(_cursorIndexOfDeadline)) {
              _tmpDeadline = null;
            } else {
              _tmpDeadline = _cursor.getLong(_cursorIndexOfDeadline);
            }
            final Long _tmpScheduledStart;
            if (_cursor.isNull(_cursorIndexOfScheduledStart)) {
              _tmpScheduledStart = null;
            } else {
              _tmpScheduledStart = _cursor.getLong(_cursorIndexOfScheduledStart);
            }
            final Long _tmpScheduledEnd;
            if (_cursor.isNull(_cursorIndexOfScheduledEnd)) {
              _tmpScheduledEnd = null;
            } else {
              _tmpScheduledEnd = _cursor.getLong(_cursorIndexOfScheduledEnd);
            }
            final boolean _tmpIsAutoScheduled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsAutoScheduled);
            _tmpIsAutoScheduled = _tmp_1 != 0;
            final int _tmpPriority;
            _tmpPriority = _cursor.getInt(_cursorIndexOfPriority);
            final boolean _tmpIsCompleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpCreatedRole;
            _tmpCreatedRole = _cursor.getString(_cursorIndexOfCreatedRole);
            final String _tmpPreGeneratedMessage;
            if (_cursor.isNull(_cursorIndexOfPreGeneratedMessage)) {
              _tmpPreGeneratedMessage = null;
            } else {
              _tmpPreGeneratedMessage = _cursor.getString(_cursorIndexOfPreGeneratedMessage);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _item = new TaskEntity(_tmpId,_tmpTitle,_tmpIsFixed,_tmpDurationMinutes,_tmpReminderLeadMinutes,_tmpDeadline,_tmpScheduledStart,_tmpScheduledEnd,_tmpIsAutoScheduled,_tmpPriority,_tmpIsCompleted,_tmpCreatedAt,_tmpCreatedRole,_tmpPreGeneratedMessage,_tmpDescription);
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
