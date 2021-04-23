package com.ontimize.jee.core.common.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;

/**
 * This class is used to implement a login lock to users who have inserted their credentials wrong a
 * specific number of times. This is used if in the locator.properties exist the property
 * "ControlAccessAttempts = true"
 *
 * This object stores a Map whose keys are the usernames and the values the AccesObject associated
 * with these users.
 *
 * @since 5.6.0
 * @author Imatia Innovation SL
 */
public class ErrorAccessControl {

    private static final Logger logger = LoggerFactory.getLogger(ErrorAccessControl.class);

    public int numMaxErrorAccess;

    protected Map<String, AccessObject> hAccessControl = new HashMap<String, AccessObject>();

    public ErrorAccessControl(int numberMaxAcces) {
        this.numMaxErrorAccess = numberMaxAcces;
    }

    public int getNumMaxErrorAccess() {
        return this.numMaxErrorAccess;
    }

    /**
     * Check if the user which username is passed by param is blocked.
     * @param user The username to check
     * @return True if the user is blocked. False otherwise
     * @throws UserBlockedException
     */
    public boolean checkAccessControl(String user) throws UserBlockedException {
        if (!this.hAccessControl.containsKey(user)) {
            this.hAccessControl.put(user, new AccessObject(user));
        }

        AccessObject oAccessUser = this.getUserAccessObject(user);
        if (oAccessUser.isUserBlock()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unlocks a blocked user which username is passed by param.
     * @param user The user who will be allowed access
     */
    public void allowLogin(String user) {
        AccessObject oAccessUser = this.getUserAccessObject(user);
        if (oAccessUser != null) {
            oAccessUser.setUserBlock(false);
            oAccessUser.setNumAccess(0);
        }

    }

    /**
     * Return the AccessObject of the user which username is passed by param.
     * @param user The username of the AccessObject to be recovered.
     * @return The AccessObject of user or {@code null} if no exists.
     */
    public AccessObject getUserAccessObject(String user) {
        return this.hAccessControl.get(user);
    }

    /**
     * Add an access attempt to the user which username is passed by param. If the attempts is more than
     * numMaxErrorAccess in ErrorAccessControl, the user will be blocked.
     * @param user The user of the AccessObject to which you will add a new attempt.
     */
    public void addAccessAttempToUser(String user) {
        AccessObject oAccessUser = this.getUserAccessObject(user);
        oAccessUser.setLastAccess(new Timestamp(System.currentTimeMillis()));
        int currentAccesAttempt = oAccessUser.getNumAccess();
        if (currentAccesAttempt < this.getNumMaxErrorAccess()) {
            oAccessUser.setNumAccess(currentAccesAttempt + 1);
        }

        if ((currentAccesAttempt + 1) == this.getNumMaxErrorAccess()) {
            oAccessUser.setUserBlock(true);
        }
    }

    /**
     * Check if the last attempt to login is more or equals to the max number of allowed attempts
     * @param user The user of the object to which you will retrieve the last attempt
     * @return True is the user number of attempts is more than numMaxErrorAcces in ErrorAccesControl,
     *         false otherwise
     */
    public boolean checkLastAttempt(String user) {
        AccessObject oAccessUser = this.getUserAccessObject(user);
        if (oAccessUser.getNumAccess() >= this.getNumMaxErrorAccess()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This object is the logic lock. This object contains a reference to the user, the last access
     * attempt, the number of attempts and if the user is blocked or not.
     *
     * @author Imatia Innovation SL
     * @since 5.6.0
     */
    public class AccessObject {

        protected String loginUser;

        protected Timestamp lastAccess;

        protected boolean block;

        protected int numAccess;

        public AccessObject(String user) throws UserBlockedException {
            if (user == null) {
                ErrorAccessControl.logger.error("USER_{}_NOT_EXIST", user);
                throw new UserBlockedException("M_ERROR_USER_NULL");
            }

            this.loginUser = user;
            this.numAccess = 0;
        }

        /**
         * Retrieve the username associated to this AccessObject
         * @return The username of this AccesObject
         */
        public String getUser() {
            return this.loginUser;
        }

        /**
         * The actual user attempts to login without success
         * @return The number of attempts
         */
        public int getNumAccess() {
            return this.numAccess;
        }

        /**
         * Set the number of attempts to login
         * @param numAccess Number of access to set
         */
        public void setNumAccess(int numAccess) {
            this.numAccess = numAccess;
        }

        /**
         * Return the last user attempt without success
         * @return The last access unsuccessful
         */
        public Timestamp getLastAccess() {
            return this.lastAccess;
        }

        /**
         * Set the last unsuccessful access.
         * @param lastAccess The timestamps of the last unsuccessful access
         */
        public void setLastAccess(Timestamp lastAccess) {
            this.lastAccess = lastAccess;
        }

        /**
         * Check if the user of this AccesObject is blocked
         * @return True is the user is blocked, false otherwise.
         */
        public boolean isUserBlock() {
            return this.block;
        }

        /**
         * Set the block (blocked/unblocked) state to the user of this AccessObject
         * @param block A boolean to set the block state to user of this AccessObject
         */
        public void setUserBlock(boolean block) {
            this.block = block;
        }

    }

}
