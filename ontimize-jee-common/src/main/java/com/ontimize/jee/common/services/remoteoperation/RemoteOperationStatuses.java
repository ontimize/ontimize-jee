package com.ontimize.jee.common.services.remoteoperation;

import org.apache.commons.lang3.Range;

public class RemoteOperationStatuses {

    public final static Range<Integer> REMOTE_OPERATION_MESSAGE_TYPE_RANGE = Range.between(-50, -1);

    public static final Integer WEBSOCKET_MESSAGE_TYPE_REQUEST = -1;

    public static final Integer WEBSOCKET_MESSAGE_TYPE_CANCEL = -2;

    public static final Integer WEBSOCKET_MESSAGE_TYPE_ERROR = -3;

    public static final Integer WEBSOCKET_MESSAGE_TYPE_FINISH = -4;

    public static final Integer WEBSOCKET_MESSAGE_TYPE_STATUS = -5;

    public static enum RemoteOperationStatus {

        WAITING(0), RUNNING(1), FINISHED(3);

        RemoteOperationStatus(int code) {
            this.code = code;
        }

        /**
         * Return the code number of this status code.
         * @return the code.
         */
        public int getCode() {
            return this.code;
        }

        private final int code;

    }

}
