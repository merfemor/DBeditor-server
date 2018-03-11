package models.entity;

public enum SqlRight {
    READ_ONLY(0),
    DML(1),
    DDL(2),
    DCL(3);

    final int priority;

    SqlRight(int priority) {
        this.priority = priority;
    }

    public static boolean isIncludes(SqlRight right, SqlRight includes) {
        return right.priority >= includes.priority;
    }
}
