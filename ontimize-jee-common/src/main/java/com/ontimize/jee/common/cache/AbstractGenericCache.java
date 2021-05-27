/**
 * GenericCache.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.cache;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class GenericCache.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author <a href="user@email.com">Author</a>
 */
public abstract class AbstractGenericCache<K, V> {

    private static final long DEFAULT_TTL = 30l * 60l * 1000l; // 30 minutes

    private static final Logger logger = LoggerFactory.getLogger(AbstractGenericCache.class);

    protected long ttl;

    protected Map<K, CachedItem<V>> cache;

    /**
     * Instantiates a new generic cache.
     */
    public AbstractGenericCache() {
        this(AbstractGenericCache.DEFAULT_TTL);
    }

    /**
     * Instantiates a new generic cache.
     * @param ttl the ttl
     */
    public AbstractGenericCache(final long ttl) {
        this.ttl = ttl;
        this.cache = new HashMap();
    }

    /**
     * Obtiene.
     * @param key the key
     * @return the v
     */
    public V get(final K key) {

        try {
            CachedItem<V> cachedItem = this.cache.get(key);
            if ((this.ttl > 0) && (cachedItem != null)
                    && ((cachedItem.getTimestamp() + this.ttl) < System.currentTimeMillis())) {
                this.cache.remove(key);
                cachedItem = null;
                AbstractGenericCache.logger.debug("Cache descartada para la clave: " + key);
            }
            if (cachedItem == null) {
                // TODO hacer que el valor del cached item sea un future
                cachedItem = new CachedItem<>();
                final V result = this.requestData(key);
                if (result != null) {
                    cachedItem.setValue(result);
                    cachedItem.setTimestamp(System.currentTimeMillis());
                    this.cache.put(key, cachedItem);
                    AbstractGenericCache.logger.debug("Cache cargada para clave: " + key + ". Valor: " + result);
                } else {
                    return null;
                }
            } else {
                AbstractGenericCache.logger
                    .debug("Acierto de cache para la clave: " + key + ". Valor: " + cachedItem.getValue());
            }
            return cachedItem.getValue();
        } catch (Exception ex) {
            throw new OntimizeJEERuntimeException(ex);
        }
    }

    /**
     * Sets the ttl.
     * @param ttl the new ttl
     */
    public void setTtl(final long ttl) {
        this.ttl = ttl;
    }

    /**
     * Gets the ttl.
     * @return the ttl
     */
    public long getTtl() {
        return this.ttl;
    }

    /**
     * Invalidate cache.
     */
    public void invalidateCache() {
        this.cache.clear();
    }

    /**
     * Invalidate cache.
     * @param key the key
     */
    public void invalidateCache(final K key) {
        this.cache.remove(key);
    }

    /**
     * Put.
     * @param key the key
     * @param value the value
     */
    public void put(final K key, final V value) {
        final CachedItem<V> item = new CachedItem<>();
        item.setTimestamp(System.currentTimeMillis());
        item.setValue(value);
        this.cache.put(key, item);
    }

    /**
     * Check key existence.
     * @param key
     * @return
     */
    public boolean containsKey(final K key) {
        return this.cache.containsKey(key);
    }

    /**
     * Request data.
     * @param key the key
     * @return the v
     */
    protected abstract V requestData(K key) throws OntimizeJEEException;

}
