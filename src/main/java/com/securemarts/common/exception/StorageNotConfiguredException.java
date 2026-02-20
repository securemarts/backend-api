package com.securemarts.common.exception;

/**
 * Thrown when file storage (e.g. DigitalOcean Spaces) is not configured and an upload was attempted.
 * Typically in local dev when APP_STORAGE_SPACES_* are unset.
 */
public class StorageNotConfiguredException extends RuntimeException {

    public StorageNotConfiguredException(String message) {
        super(message);
    }
}
