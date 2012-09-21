package ru.kwanza.jeda.api;

import ru.kwanza.toolbox.attribute.AttributeFactory;
import ru.kwanza.toolbox.attribute.AttributeField;
import ru.kwanza.toolbox.attribute.IAttribute;
import ru.kwanza.toolbox.fieldhelper.FieldHelper.Field;

import java.util.Collection;

public interface IPendingStore {

    public static final IAttribute<Long> SUSPEND_ID_ATTR = AttributeFactory.create();
    public static final IAttribute<String> SUSPEND_SINK_NAME_ATTR = AttributeFactory.create();
    public static final Field<IEvent, Long> SUSPEND_ID_FIELD = new AttributeField<IEvent, Long>(SUSPEND_ID_ATTR);
    public static final Field<IEvent, String> SUSPEND_SINK_NAME_FIELD = new AttributeField<IEvent, String>(SUSPEND_SINK_NAME_ATTR);

    public <E extends IEvent> ISuspender<E> getSuspender();

    public void resume(Collection<Long> suspendItemsIds) throws ResumeException;

    public void tryResume(Collection<Long> suspendItemsIds) throws ResumeException;

    public void remove(Collection<Long> suspendItemsIds);

}