package ch.epfl.sweng.erpa.operations;

import android.content.Intent;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.testing.ToothPickRule;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

// Workarounds over type erasure, fields are used later via Reflection.
@SuppressWarnings({"unused", "SuspiciousMethodCalls"})
@RunWith(MockitoJUnitRunner.class)
public class DependencyConfigurationHelperTest {
    @Rule public final ToothPickRule toothPickRule = new ToothPickRule(this, "dchTest");

    @Inject Scope scope;

    @Before
    public void prepare() {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        scope = toothPickRule.getScope();
        scope.installModules(new Module() {{
            bind(Scope.class).withName(RES_APPLICATION_SCOPE).toInstance(scope);
            bind(DependencyConfigurationHelper.class).to(DependencyConfigurationHelper.class);
            bind(Map.class).withName(RES_DEPENDENCY_COORDINATORS)
                    .toInstance(new HashMap<Class, DependencyCoordinator>());
        }});
    }

    @Test
    public void testFlatDependencyResolution() {
        // @formatter:off
        class D1 {} class D2 {} class D3 {} class D4 {} class D5 {}
        // @formatter:on
        DchTestHelper testHelper = new DchTestHelper(new D1(), new D2(), new D3(), new D4(), new D5()) {
            @Inject public D1 f1;
            @Inject public D2 f2;
            @Inject public D3 f3;
            @Inject public D4 f4;
            @Inject public D5 f5;
        };
        DependencyConfigurationHelper underTest = scope.getInstance(DependencyConfigurationHelper.class);

        // Assert all classes have been resolved
        Set<Class> expectedDependencyClasses = testHelper.dependencyConfigurators.keySet();
        assertEquals(expectedDependencyClasses,
                new HashSet<>(underTest.getNotConfiguredDependenciesForInstance(testHelper)));

        Stream.of(expectedDependencyClasses)
                .forEach(cls -> {
                    Optional<DependencyCoordinator<?>> dc = underTest.getDependencyConfiguratorForClass(cls);
                    assertTrue(dc.isPresent());
                    assertEquals(testHelper.dependencyConfigurators.get(cls), dc.get());
                });
    }

    @Test
    public void testRecursiveDependencyResolution() {
        DependencyConfigurationHelper underTest = scope.getInstance(DependencyConfigurationHelper.class);

        // The order in the constructor is important due to the way the DependencyConfigurationHelper initializes.
        DchTestHelper testHelper = new DchTestHelper(new D4(), new D3(), new D2(), new D1()) {
            @Inject public D1 f1;
            @Inject public D2 f2;
            @Inject public D3 f3;
            @Inject public D4 f4;
        };

        //noinspection unchecked -- Raw Classes.
        List<Class> expected = Stream.of(D1.class, D2.class, D3.class, D4.class).collect(Collectors.toList());
        Collections.reverse(expected);
        assertEquals(expected, underTest.getNotConfiguredDependenciesForInstance(testHelper));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDfsSolverTopologicalTreeSortExceptsOnCycle() {
        Tree<Integer> t1 = new Tree<>(1);
        Tree<Integer> t2 = new Tree<>(2);
        t1.getChildren().add(t2);
        t2.getChildren().add(t1);

        DfsTreeSolver.topologicalSort(t1);
    }

    @Test
    public void testDfsSolverTopologicalTreeSort() {
        int maxTrees = 1000;
        int nChilds = 300;

        Map<Integer, Tree<Integer>> treesPool = new HashMap<>();

        Tree<Integer> testTree = new Tree<>(Integer.class, 0, Stream.of(mkBoundedArray(0, maxTrees / 2, nChilds))
                .map(i -> mkTreeFromSuites(i, mkBoundedArray(i + 1, maxTrees, nChilds), treesPool))
                .collect(Collectors.toSet()));

        List<Tree<Integer>> sortedTrees = Stream.of(DfsTreeSolver.topologicalSort(testTree))
                .skip(1) // The first node it's always present
                .collect(Collectors.toList());

        // Verify that by removing the left-most node each time, we never leave orphan children
        sortedTrees.forEach(tree -> {
            treesPool.remove(tree.getValue());
            Stream.of(tree.getChildren()).forEach(child -> assertTrue(treesPool.containsKey(child.getValue())));
        });
    }

    private Set<Integer> mkBoundedArray(int min, int max, int count) {
        return Stream.generate(() -> min + (int) (new Random().nextDouble() * (max - min))).limit(1000)
                .distinct().limit(count).sorted().collect(Collectors.toSet());
    }

    private Tree<Integer> mkTreeFromSuites(int value, Set<Integer> children, Map<Integer, Tree<Integer>> pool) {
        Stream.of(children).forEach(childValue -> {
            Tree<Integer> node = pool.getOrDefault(value, new Tree<>(value));
            Tree<Integer> child = pool.getOrDefault(childValue, new Tree<>(childValue));
            Objects.requireNonNull(node).getChildren().add(child);
            pool.put(childValue, child);
            pool.put(value, node);
        });
        return pool.getOrDefault(value, new Tree<>(value));
    }

    // @formatter:off
    public static class D4 { @Inject public D4() {} }
    public static class D3 { @Inject public D4 f; }
    public static class D2 { @Inject public D3 f; }
    public static class D1 { @Inject public D2 f; }
    // @formatter:on

    class Pair {
        public Object first;
        public Object second;

        public Pair(Object first, Object second) {
            this.first = first;
            this.second = second;
        }
    }

    @SuppressWarnings("unchecked")
    abstract class DchTestHelper {
        Map<Class, DependencyCoordinator<?>> dependencyConfigurators;

        DchTestHelper(Object... fieldValues) {
            Map<Object, Class> objectClassMap = new HashMap<>();
            // Set fields and associate dependencyConfigurators
            dependencyConfigurators = Stream.of(this.getClass().getFields())
                    .map(f -> Stream.of(fieldValues)
                            .filter(fv -> f.getType().isAssignableFrom(fv.getClass()))
                            .findFirst()
                            .map(Function.Util.safe((fv -> {
                                f.set(this, fv);
                                objectClassMap.put(fv, f.getType());
                                return f.getType();
                            })))
                            .orElse(null))
                    .collect(Collectors.toMap(ft -> ft, this::mkDependencyConfigurator));
            DchTestHelper dchTestHelper = this;

            scope.installModules(new Module() {{
                bind(DchTestHelper.class).toInstance(dchTestHelper);
            }});

            Toothpick.inject(this, scope);

            Stream.of(fieldValues).map(v -> new Pair(v, objectClassMap.get(v))).forEach(ocPair -> {
                Toothpick.inject(ocPair.first, scope);
                scope.installModules(new Module() {{
                    bind((Class) ocPair.second).toInstance(ocPair.first);
                }});
            });

            scope.getInstance(Map.class, RES_DEPENDENCY_COORDINATORS).putAll(dependencyConfigurators);
        }

        private DependencyCoordinator<?> mkDependencyConfigurator(Class<?> forClass) {
            return new DependencyCoordinator<Object>() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return null;
                }

                @Override public boolean dependencyIsConfigured() {
                    return false;
                }

                @Override public Intent dependencyConfigurationIntent() {
                    return null;
                }

                @Override public Class<Object> configuredDependencyClass() {
                    return (Class<Object>) forClass;
                }
            };
        }
    }
}
