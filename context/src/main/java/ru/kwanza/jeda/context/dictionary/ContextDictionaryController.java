package ru.kwanza.jeda.context.dictionary;

import ru.kwanza.jeda.context.dictionary.dbinteractor.DictionaryDbInteractor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Zagorovsky
 */
public class ContextDictionaryController {

    public DictionaryDbInteractor dbInteractor;
    private ContextDictionaryCache dictionaryCache = new ContextDictionaryCache();

    private String dictionaryTableName = "ctx_dictionary";
    private String dictionaryPropertyColumnName = "name";
    private String dictionaryIdColumnName = "id";

    public ContextDictionaryController() {
    }

    public ContextDictionaryController(DictionaryDbInteractor dbInteractor,
                                       String dictionaryTableName,
                                       String dictionaryPropertyColumnName,
                                       String dictionaryIdColumnName) {

        this.dbInteractor = dbInteractor;
        this.dictionaryTableName = dictionaryTableName;
        this.dictionaryPropertyColumnName = dictionaryPropertyColumnName;
        this.dictionaryIdColumnName = dictionaryIdColumnName;
        dictionaryCache.putIdByName(dbInteractor.readAllDictionary(this));
    }

    public Map<String, Long> getPropertyIds(final Collection<String> propertyNames) {
        Map<String, Long> idByName = new HashMap<String, Long>();

        for (String propName : propertyNames) {
            Long id = dictionaryCache.getPropertyId(propName);
            if (id == null) {
                id = readIdFromDb(propName);

                if (id == null) {
                    id = storeNewProperty(propName);
                }
            }
            idByName.put(propName, id);
        }
        return idByName;
    }

    public Map<Long, String> getPropertyNames(Collection<Long> propertyIds) {
        Map<Long, String> nameById = new HashMap<Long, String>();

        for (Long id : propertyIds) {
            String name = dictionaryCache.getPropertyName(id);
            if (name == null) {
                name = readNameFromDb(id);
            }

            if (name != null) {
                nameById.put(id, name);
            }
        }

        return nameById;
    }

    private Long readIdFromDb(String propertyName) {
        Long id = dbInteractor.readIdFromDb(propertyName, this);
        if (id != null) {
            dictionaryCache.put(propertyName, id);
        }
        return id;
    }

    private String readNameFromDb(Long propertyId) {
        String name = dbInteractor.readNameFromDb(propertyId, this);
        if (name != null) {
            dictionaryCache.put(name, propertyId);
        }
        return name;
    }

    public Long storeNewProperty(String propertyName) {
        Long id = dbInteractor.storeNewProperty(propertyName, this);
        if (id != null) {
            dictionaryCache.put(propertyName, id);
        }
        return id;
    }

    public String getDictionaryTableName() {
        return dictionaryTableName;
    }

    public String getDictionaryPropertyColumnName() {
        return dictionaryPropertyColumnName;
    }

    public String getDictionaryIdColumnName() {
        return dictionaryIdColumnName;
    }

    //  For tests.
    ContextDictionaryCache getDictionaryCache() {
        return dictionaryCache;
    }

}
