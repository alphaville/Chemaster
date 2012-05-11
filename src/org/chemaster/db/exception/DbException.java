package org.chemaster.db.exception;

/**
 *
 * @author Pantelis Sopasakis
 */
public class DbException extends Exception {

    public DbException() {
    }

    public DbException(String message) {
        super(message);
    }

    public DbException(String message, Throwable cause) {
        super(message, cause);
    }

    public DbException(Throwable cause) {
        super(cause);
    }
}
