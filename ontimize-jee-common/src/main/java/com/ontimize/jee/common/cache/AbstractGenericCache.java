/**
 * GenericCache.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.cache;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class GenericCache.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author <a href="user@email.com">Author</a>
 */
public abstract class AbstractGenericCache<K, V> {
    private static final long DEFAULT_TTL = 30 * 60 * 1000; // 30 minutes
    private static final Logger logger = LoggerFactory.getLogger(AbstractGenericCache.class);

	protected long					ttl;
	protected Map<K, CachedItem<V>>	cache;

    /**
     * Instantiates a new generic cache.
     */
    public AbstractGenericCache() {
        this(AbstractGenericCache.DEFAULT_TTL);
    }

    /**
     * Instantiates a new generic cache.
     *
     * @param ttl the ttl
     */
    public AbstractGenericCache(final long ttl) {
        this.ttl = ttl;
        this.cache = new Hashtable<K, CachedItem<V>>();
    }

    /**
     * Obtiene.
     *
     * @param key the key
     * @return the v
     */
    public V get(final K key) {
        if ((this.ttl > 0) && this.cache.containsKey(key) &&
                ((this.cache.get(key).getTimestamp() + this.ttl) < System.currentTimeMillis())) {
            this.cache.remove(key);
            AbstractGenericCache.logger.debug("Cache descartada para la clave: " + key);
        }
        if (!this.cache.containsKey(key)) {
            final CachedItem<V> cachedItem = new CachedItem<V>();
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
            AbstractGenericCache.logger.debug("Acierto de cache para la clave: " + key +
                    ". Valor: " + this.cache.get(key).getValue());
        }
        return this.cache.get(key).getValue();
    }

    /**
     * Sets the ttl.
     *
     * @param ttl the new ttl
     */
    public void setTtl(final long ttl) {
        this.ttl = ttl;
    }

    /**
     * Gets the ttl.
     *
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
     *
     * @param key the key
     */
    public void invalidateCache(final K key) {
        this.cache.remove(key);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     */
    public void put(final K key, final V value) {
        final CachedItem<V> item = new CachedItem<V>();
        item.setTimestamp(System.currentTimeMillis());
        item.setValue(value);
        this.cache.put(key, item);
    }

    /**
     * Request data.
     *
     * @param key the key
     * @return the v
     */
    protected abstract V requestData(K key);
}
