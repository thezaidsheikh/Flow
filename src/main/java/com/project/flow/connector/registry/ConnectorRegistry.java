package com.project.flow.connector.registry;

import com.project.flow.connector.provider.IConnectorAdapter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Registry to discover and cache all active IConnectorAdapter beans.
 */
@Component
public class ConnectorRegistry {

    private final Map<String, IConnectorAdapter> adapters = new ConcurrentHashMap<>();

    public ConnectorRegistry(List<IConnectorAdapter> connectorAdapters) {
        for (IConnectorAdapter adapter : connectorAdapters) {
            adapters.put(adapter.getProvider().toLowerCase(), adapter);
        }
    }

    /**
     * Resolves an adapter by its provider identifier.
     *
     * @param provider unique provider identifier (e.g. github)
     * @return optional containing the adapter if registered, empty otherwise
     */
    public Optional<IConnectorAdapter> getAdapter(String provider) {
        if (provider == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(adapters.get(provider.toLowerCase()));
    }
}
