package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Guzanov
 */
public class ComponentRepositoryTest {

    @Test
    public void testRegisterComponent(@Mocked final IClusteredComponent component1,
                                      @Mocked final IClusteredComponent component2,
                                      @Mocked final IClusteredComponent component3) {

        new Expectations() {{
            component1.getName();
            result = "component_1";
            component2.getName();
            result = "component_2";
            component3.getName();
            result = "component_1";
        }};

        final ComponentRepository repository = new ComponentRepository();

        repository.registerComponent(component1);
        repository.registerComponent(component2);
        try {
            repository.registerComponent(component3);
            Assert.fail("Expected " + IllegalStateException.class);
        } catch (IllegalStateException e) {
        }


        ConcurrentHashMap<String,IClusteredComponent> map = Deencapsulation.getField(repository, "components");

        Assert.assertEquals(map.size(), 2);
        Assert.assertEquals(map.get("component_1"), component1);
        Assert.assertEquals(map.get("component_2"), component2);

        Assert.assertEquals(repository.getComponent("component_1"), component1);
        Assert.assertEquals(repository.getComponent("component_2"), component2);

    }

    @Test
    public void testActiveComponent(@Mocked final IClusteredComponent component1,
                                    @Mocked final IClusteredComponent component2,
                                    @Mocked final ComponentEntity entity1,
                                    @Mocked final ComponentEntity entity2) {

        new Expectations() {{
            entity1.getName();result = "component_1";
            component1.getName();result = "component_1";
            entity2.getName();result = "component_2";
            component2.getName();result = "component_2";
        }};

        final ComponentRepository repository = new ComponentRepository();

        repository.registerComponent(component1);
        repository.registerComponent(component2);

        repository.addActiveComponent(entity1);


        Assert.assertEquals(1,repository.getActiveEntities().size());
        Assert.assertEquals(entity1,repository.getActiveEntities().iterator().next());
        Assert.assertEquals(1,repository.getActiveComponents().size());
        Assert.assertEquals(component1,repository.getActiveComponents().get("component_1"));
        Assert.assertTrue(repository.isActive("component_1"));


        repository.addActiveComponent(entity2);
        Assert.assertEquals(2, repository.getActiveEntities().size());
        final Iterator<ComponentEntity> i = repository.getActiveEntities().iterator();
        Assert.assertEquals(2,repository.getActiveComponents().size());
        Assert.assertEquals(component1,repository.getActiveComponents().get("component_1"));
        Assert.assertEquals(component2,repository.getActiveComponents().get("component_2"));
        Assert.assertTrue(repository.isActive("component_1"));
        Assert.assertTrue(repository.isActive("component_2"));

        repository.removeActiveComponent("component_1");
        Assert.assertEquals(1,repository.getActiveEntities().size());
        Assert.assertEquals(entity2,repository.getActiveEntities().iterator().next());
        Assert.assertEquals(1,repository.getActiveComponents().size());
        Assert.assertEquals(component2,repository.getActiveComponents().get("component_2"));
        Assert.assertFalse(repository.isActive("component_1"));
        Assert.assertTrue(repository.isActive("component_2"));
    }

    @Test
    public void testPassiveComponent(@Mocked final IClusteredComponent component1,
                                    @Mocked final IClusteredComponent component2,
                                    @Mocked final ComponentEntity entity1,
                                    @Mocked final ComponentEntity entity2) {

        new Expectations() {{
            entity1.getName();result = "component_1";
            component1.getName();result = "component_1";
            entity2.getName();result = "component_2";
            component2.getName();result = "component_2";
        }};

        final ComponentRepository repository = new ComponentRepository();

        repository.registerComponent(component1);
        repository.registerComponent(component2);

        repository.addPassiveComponent(entity1);


        Assert.assertEquals(1,repository.getPassiveEntities().size());
        Assert.assertEquals(entity1,repository.getPassiveEntities().iterator().next());
        Assert.assertEquals(1,repository.getPassiveComponents().size());
        Assert.assertEquals(component1,repository.getPassiveComponents().get("component_1"));



        repository.addPassiveComponent(entity2);
        Assert.assertEquals(2, repository.getPassiveEntities().size());
        final Iterator<ComponentEntity> i = repository.getPassiveEntities().iterator();
        Assert.assertEquals(2,repository.getPassiveComponents().size());
        Assert.assertEquals(component1,repository.getPassiveComponents().get("component_1"));
        Assert.assertEquals(component2,repository.getPassiveComponents().get("component_2"));

        repository.removePassiveComponent("component_1");
        Assert.assertEquals(1,repository.getPassiveEntities().size());
        Assert.assertEquals(entity2,repository.getPassiveEntities().iterator().next());
        Assert.assertEquals(1,repository.getPassiveComponents().size());
        Assert.assertEquals(component2,repository.getPassiveComponents().get("component_2"));
    }



    @Test
    public void tesAlienComponent(@Mocked final IClusteredComponent component1,
                                     @Mocked final IClusteredComponent component2,
                                     @Mocked final ComponentEntity entity1,
                                     @Mocked final ComponentEntity entity2) {

        new Expectations() {{
            component1.getName();result = "component_1";
            entity1.getId();result="1_component_1";
            entity2.getId();result="1_component_2";
            component2.getName();result = "component_2";
        }};

        final ComponentRepository repository = new ComponentRepository();

        repository.registerComponent(component1);
        repository.registerComponent(component2);

        repository.addAlientComponent(entity1);
        Assert.assertEquals(1,repository.getAlienEntities().size());
        Assert.assertEquals(entity1,repository.getAlienEntities().get("1_component_1"));


        repository.addAlientComponent(entity2);
        Assert.assertEquals(2,repository.getAlienEntities().size());
        Assert.assertEquals(entity1,repository.getAlienEntities().get("1_component_1"));
        Assert.assertEquals(entity2,repository.getAlienEntities().get("1_component_2"));

        Assert.assertTrue(repository.removeAlienComponent("1_component_1"));
        Assert.assertEquals(1,repository.getAlienEntities().size());
        Assert.assertEquals(entity2,repository.getAlienEntities().get("1_component_2"));
    }



}
