package ru.kwanza.jeda.api;

import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public interface IMapContext<ID, VER, K, V> extends IContext<ID, VER>, Map<K, V> {
}
