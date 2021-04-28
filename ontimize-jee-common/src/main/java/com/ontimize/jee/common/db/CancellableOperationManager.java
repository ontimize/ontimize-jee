package com.ontimize.jee.common.db;

import com.ontimize.jee.common.gui.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class CancellableOperationManager {

    private static final Logger logger = LoggerFactory.getLogger(CancellableOperationManager.class);

    protected static class IdentifierGenerator {

        protected List identifierUsed = null;

        protected int length = 10;

        public IdentifierGenerator(int longitud) {
            this.identifierUsed = new ArrayList();
            this.length = longitud;
            if (this.length < 5) {
                this.length = 5;
                CancellableOperationManager.logger.debug("MINIMUN LENGTH OF OPERATION IDENTIFIERS IS 5");
            }
        }

        public synchronized String getUniqueIdentifier() {
            String id = RandomStringGenerator.generate(this.length);
            while (this.identifierUsed.contains(id)) {
                id = RandomStringGenerator.generate(this.length);
            }
            this.identifierUsed.add(id);
            return id;
        }

    };

    protected static class CancellationRequestQueue {

        protected List cancellationRequest = new ArrayList(5);

        public CancellationRequestQueue() {
        }

        public boolean existCancellationRequest(String s) {
            return this.cancellationRequest.contains(s);
        }

        public void addCancellationRequest(String s) {
            this.cancellationRequest.add(s);
        }

        public void deleteCancellationRequest(String s) {
            this.cancellationRequest.remove(s);
        }

    }

    protected static CancellationRequestQueue cancellationRequestQueue = new CancellationRequestQueue();

    protected static IdentifierGenerator identifierGenerator = new IdentifierGenerator(8);

    public static boolean existCancellationRequest(String s) {
        return CancellableOperationManager.cancellationRequestQueue.existCancellationRequest(s);
    }

    public static void addCancellationRequest(String s) {
        CancellableOperationManager.cancellationRequestQueue.addCancellationRequest(s);
    }

    public static void deleteCancellationRequest(String s) {
        CancellableOperationManager.cancellationRequestQueue.deleteCancellationRequest(s);
    }

    public static String getOperationUniqueIdentifier() {
        return CancellableOperationManager.identifierGenerator.getUniqueIdentifier();
    }

}
