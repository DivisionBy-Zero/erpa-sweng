package ch.epfl.sweng.erpa.operations;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import toothpick.Scope;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;

@Singleton
public class DependencyConfigurationHelper {
    @Inject @Named(RES_DEPENDENCY_COORDINATORS) Set<DependencyCoordinator<?>> coordinators;
    @Inject @Named(RES_APPLICATION_SCOPE) Scope scope;

    @Inject public DependencyConfigurationHelper() {
    }

    /**
     * Calculates a list of not configured dependencies required by the given instance.
     * <p>
     * This method also resolves nested dependencies.
     *
     * @param instance the object to scan for injected but not configured dependencies.
     * @return A sorted list with required dependency classes in an optimal order to be resolved.
     */
    public List<Class> getNotConfiguredDependenciesForInstance(Object instance) {
        List<Class> sortedNotConfiguredDependencies = Stream.of(makeInstanceDependencyTree(instance.getClass(), instance, new HashMap<>())
                .map(DfsTreeSolver::topologicalSort)
                .orElse(new ArrayList<>()))
                .map(t -> t.getSupportingType())
                .filterNot(instance.getClass()::equals)
                .collect(Collectors.toList());
        // DfsTreeSolver::topologicalSort returns a list sorted from the oldest parent
        Collections.reverse(sortedNotConfiguredDependencies);
        return sortedNotConfiguredDependencies;
    }

    public Optional<DependencyCoordinator<?>> getDependencyConfiguratorForClass(Class<?> cls) {
        return Stream.of(coordinators)
                .filter(coordinator -> coordinator.configuredDependencyClass().isAssignableFrom(cls))
                .findFirst();
    }

    @SuppressWarnings("unchecked") // Horrible casts due to Java Generics partial support for type variance.
    private <T> Optional<Tree<Class<T>>> makeInstanceDependencyTree(Class<? extends T> type, T rootInstance, Map<Class, Tree> treeCache) {
        if (treeCache.containsKey(type))
            return Optional.of(Objects.requireNonNull(treeCache.get(type)));

        Set<Class> instanceInjectedTypes = Stream.of(rootInstance.getClass().getFields())
                .filter(f -> f.isAnnotationPresent(Inject.class))
                .map(Field::getType)
                .collect(Collectors.toSet());

        // Claim responsibility for this tree
        treeCache.put(type, new Tree<>(type, rootInstance.getClass()));

        // If it's a terminal tree, just exit
        if (instanceInjectedTypes.isEmpty())
            return Optional.empty();

        Set<Tree> instanceDependencies = Stream.of(coordinators)
                .filter(cc -> instanceInjectedTypes.contains(cc.configuredDependencyClass()))
                // Coordinators with non-configured dependencies
                .filterNot(DependencyCoordinator::dependencyIsConfigured)
                .map(DependencyCoordinator::configuredDependencyClass)
                // Examine dependency
                .map(embeddedDependencyClass -> {
                    Object instance = scope.getInstance(embeddedDependencyClass);
                    return ((Optional<Tree>) (Object) makeInstanceDependencyTree(embeddedDependencyClass, instance, treeCache))
                            // The tree was nonetheless claimed, retrieve the terminal tree.
                            .orElse(treeCache.get(embeddedDependencyClass));
                })
                .collect(Collectors.toSet());

        Tree dependencyTree = treeCache.get(type);
        // Integrate recursion results.
        dependencyTree.setChildren(instanceDependencies);
        return Optional.of(dependencyTree);
    }
}

@Data
@RequiredArgsConstructor
class Tree<T> {
    @NonNull Class supportingType;
    @NonNull T value;
    @NonNull Set<Tree> children;
    Mark mark = Mark.NONE;

    public Tree(T value) {
        this(value.getClass(), value);
    }

    public Tree(Class supportingType, T value) {
        this(supportingType, value, new HashSet<>());
    }

    enum Mark {NONE, TEMPORARY, PERMANENT}
}

class DfsTreeSolver {
    /**
     * Performs topological sort on a tree
     *
     * @param tree Tree to sort
     * @param <T>  Type of the tree
     * @return List of trees such that the first element is a parent of every other element
     */
    public static <T> List<Tree<T>> topologicalSort(Tree<T> tree) {
        LinkedList<Tree<T>> sorted = new LinkedList<>();
        visit(tree, sorted);
        return sorted;
    }

    private static <T> void visit(Tree<T> node, LinkedList<Tree<T>> accumulator) {
        if (node.mark == Tree.Mark.PERMANENT) return;
        if (node.mark == Tree.Mark.TEMPORARY) throw new IllegalArgumentException("Cyclic tree");
        node.mark = Tree.Mark.TEMPORARY;
        Stream.of(node.children).forEach(child -> visit(child, accumulator));
        node.mark = Tree.Mark.PERMANENT;
        accumulator.addFirst(node);
    }
}
