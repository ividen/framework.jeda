package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import ru.kwanza.jeda.context.dictionary.dbinteractor.JDBCDictionaryDbInteractor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JDBCBlobContextControllerWithDictionary extends JDBCBlobContextController {

    private ContextDictionaryController dictionaryController;

    public JDBCBlobContextControllerWithDictionary() {
    }

    public JDBCBlobContextControllerWithDictionary(JDBCDictionaryDbInteractor dbInteractor,
                                                   String dictionaryTableName,
                                                   String dictionaryPropertyColumnName,
                                                   String dictionaryIdColumnName) {
        dictionaryController =
                new ContextDictionaryController(dbInteractor, dictionaryTableName, dictionaryPropertyColumnName, dictionaryIdColumnName);
    }

    protected byte[] serializeContextMap(Map<String, Object> ctxMap) throws IOException {
        Map<String, Long> propIdByName = dictionaryController.getPropertyIds(ctxMap.keySet());

        Map<Long, Object> newInnerMap = new HashMap<Long, Object>();
        for (Map.Entry<String, Object> entry : ctxMap.entrySet()) {
            Long id = propIdByName.get(entry.getKey());
            newInnerMap.put(id, entry.getValue());
        }

        return serializeObject(newInnerMap);
    }

    protected Map<String, Object> deserializeContextMap(byte[] data) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Map<Long, Object> rawMap = (Map<Long, Object>) deserializeObject(data);

        Map<String, Object> newInnerMap = new HashMap<String, Object>();
        Map<Long, String> nameById = dictionaryController.getPropertyNames(rawMap.keySet());

        for (Map.Entry<Long, Object> entry : rawMap.entrySet()) {
            newInnerMap.put(nameById.get(entry.getKey()), entry.getValue());
        }

        return newInnerMap;
    }

}
