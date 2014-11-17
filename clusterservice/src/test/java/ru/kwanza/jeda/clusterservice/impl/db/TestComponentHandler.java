package ru.kwanza.jeda.clusterservice.impl.db;

import mockit.Expectations;
import mockit.Mocked;
import mockit.VerificationsInOrder;
import org.junit.Test;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.AlienComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.BaseComponentEntity;

import java.util.Collections;

/**
 * @author Alexander Guzanov
 */
public class TestComponentHandler {

    @Test
    public void testDelegate(@Mocked final ComponentRepository repository,
                             @Mocked final IClusteredComponent cmp,
                             @Mocked final Node node,
                             @Mocked final AlienComponent alienComponent){
        new Expectations(){{
            cmp.getName();result="test";times=1;
            BaseComponentEntity.createId(node, (IClusteredComponent) any);result = "1_test";
            repository.getAlienEntities();result = Collections.singletonMap("1_test",alienComponent);
        }};

        final ComponentHandler componentHandler = new ComponentHandler(repository, cmp);

        componentHandler.getName();
        componentHandler.handleStart();
        componentHandler.handleStop();
        componentHandler.handleStartRepair(node);
        componentHandler.handleStopRepair(node);

        new VerificationsInOrder(){{
            cmp.getName();times=1;
            cmp.handleStart();times=1;
            cmp.handleStop();times=1;
            cmp.handleStartRepair(node);times=1;
            cmp.handleStopRepair(node);times = 1;
            repository.getAlienEntities();times=1;
            repository.addStopRepair(alienComponent);times=1;
        }};
    }

    @Test
    public void testDelegate_2(@Mocked final ComponentRepository repository,
                             @Mocked final IClusteredComponent cmp,
                             @Mocked final Node node,
                             @Mocked final AlienComponent alienComponent){
        new Expectations(){{
            cmp.getName();result="test";times=1;
            BaseComponentEntity.createId(node, (IClusteredComponent) any);result = "1_test";
            repository.getAlienEntities();result = Collections.emptyMap();
        }};

        final ComponentHandler componentHandler = new ComponentHandler(repository, cmp);

        componentHandler.getName();
        componentHandler.handleStart();
        componentHandler.handleStop();
        componentHandler.handleStartRepair(node);
        componentHandler.handleStopRepair(node);

        new VerificationsInOrder(){{
            cmp.getName();times=1;
            cmp.handleStart();times=1;
            cmp.handleStop();times=1;
            cmp.handleStartRepair(node);times=1;
            cmp.handleStopRepair(node);times = 1;
            repository.getAlienEntities();times=1;
        }};
    }
}
