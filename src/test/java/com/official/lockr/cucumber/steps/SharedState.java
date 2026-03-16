package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.users.domain.Users;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cucumber Step Definition들 간에 공유되는 상태
 */
public class SharedState {

    private static final SharedState INSTANCE = new SharedState();

    private final Map<String, Users> usersMap = new HashMap<>();
    private final Set<String> userClubMemberships = new HashSet<>();
    private Exception caughtException;
    private String lastErrorMessage;

    private SharedState() {}

    public static SharedState getInstance() {
        return INSTANCE;
    }

    public void clear() {
        usersMap.clear();
        userClubMemberships.clear();
        caughtException = null;
        lastErrorMessage = null;
    }

    public Map<String, Users> getUsersMap() {
        return usersMap;
    }

    public Set<String> getUserClubMemberships() {
        return userClubMemberships;
    }

    public Exception getCaughtException() {
        return caughtException;
    }

    public void setCaughtException(Exception caughtException) {
        this.caughtException = caughtException;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
