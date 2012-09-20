package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;

import java.util.Map;

/**
 * @author Dmitry Zagorovsky
 */
public interface DictionaryDbInteractor {

    Long storeNewProperty(String propertyName, ContextDictionaryController dictCtrl);

    Long readIdFromDb(String propertyName, ContextDictionaryController dictCtrl);

    Map<String, Long> readAllDictionary(ContextDictionaryController dictCtrl);

    String readNameFromDb(Long propertyId, ContextDictionaryController dictCtrl);

}
