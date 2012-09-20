package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import ru.kwanza.jeda.context.dictionary.dbinteractor.BerkeleyDictionaryDbInteractor;

import java.util.HashMap;
import java.util.Map;

import static ru.kwanza.toolbox.SerializationHelper.bytesToObject;
import static ru.kwanza.toolbox.SerializationHelper.objectToBytes;

/**
 * @author Dmitry Zagorovsky
 */
public class BerkeleyBlobContextControllerWithDictionary extends BerkeleyBlobContextController {

    private ContextDictionaryController dictionaryController;

    public BerkeleyBlobContextControllerWithDictionary() {
    }

    public BerkeleyBlobContextControllerWithDictionary(BerkeleyDictionaryDbInteractor dbInteractor, String dictionaryTableName) {
        dictionaryController = new ContextDictionaryController(dbInteractor, dictionaryTableName, null, null);
    }

    protected byte[] packContextInnerMap(Map<String, Object> innerMap) throws Exception {
        Map<String, Long> propIdByName = dictionaryController.getPropertyIds(innerMap.keySet());

        Map<Long, Object> newInnerMap = new HashMap<Long, Object>();
        for (Map.Entry<String, Object> entry : innerMap.entrySet()) {
            Long id = propIdByName.get(entry.getKey());
            newInnerMap.put(id, entry.getValue());
        }

        return objectToBytes(newInnerMap);
    }

    protected Map<String, Object> unpackContextInnerMap(byte[] innerMapBytes) throws Exception {
        @SuppressWarnings("unchecked")
        Map<Long, Object> rawMap = (Map<Long, Object>) bytesToObject(innerMapBytes);

        Map<String, Object> newInnerMap = new HashMap<String, Object>();
        Map<Long, String> nameById = dictionaryController.getPropertyNames(rawMap.keySet());

        for (Map.Entry<Long, Object> entry : rawMap.entrySet()) {
            newInnerMap.put(nameById.get(entry.getKey()), entry.getValue());
        }

        return newInnerMap;
    }

}
