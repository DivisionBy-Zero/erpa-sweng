package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Optional;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.operations.DependencyConfigurationHelper;
import ch.epfl.sweng.erpa.operations.DependencyCoordinator;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.testing.ToothPickRule;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AppCompatActivity.class)
public class DependencyConfigurationAgnosticActivityTest {
    @Rule public final ToothPickRule toothPickRule = new ToothPickRule(this, "dcaTest");

    @Mock DependencyConfigurationHelper dependencyConfigurationHelper;
    @Mock ErpaApplication application;

    @Mock Class c1 = Float.class; // Unfortunately, we need a base to mock classes...
    @Mock Class c2 = Double.class; // Unfortunately, we need a base to mock classes...
    @Mock DependencyCoordinator d1;
    @Mock DependencyCoordinator d2;
    Intent i1 = new Intent("i1") { public Intent addFlags(int flags) { return this; } };
    Intent i2 = new Intent("i2") { public Intent addFlags(int flags) { return this; } };

    SyntethicActivity underTest;
    Scope scope;

    @Before
    public void prepare() {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        scope = toothPickRule.getScope();
        scope.installModules(new Module() {{
            bind(DependencyConfigurationHelper.class).toInstance(dependencyConfigurationHelper);
            bind(ErpaApplication.class).toInstance(application);
            bind(Map.class).withName(RES_DEPENDENCY_COORDINATORS)
                .toInstance(new HashMap<Class, DependencyCoordinator>());
            bind(Scope.class).withName(RES_APPLICATION_SCOPE).toInstance(scope);
        }});
        underTest = new SyntethicActivity(scope);
        MockitoAnnotations.initMocks(underTest);
        PowerMockito.suppress(MemberMatcher.methodsDeclaredIn(AppCompatActivity.class));
    }

    @Test
    public void noUnconfiguredDependenciesDoNotQuit() {
        when(dependencyConfigurationHelper.getNotConfiguredDependenciesForInstance(any())).thenReturn(new ArrayList<>());
        underTest.onCreate(null);
        assertFalse(underTest.isFinishing);
        assertTrue(underTest.intentStack.isEmpty());
    }

    @Test
    public void unconfiguredDependenciesQuitAndStartConfigurationIntents() {
        when(dependencyConfigurationHelper.getNotConfiguredDependenciesForInstance(any()))
            .thenReturn(Lists.newArrayList(c1, c2));
        when(dependencyConfigurationHelper.getDependencyConfiguratorForClass(eq(c1))).thenReturn(Optional.of(d1));
        when(dependencyConfigurationHelper.getDependencyConfiguratorForClass(eq(c2))).thenReturn(Optional.of(d2));
        when(d1.dependencyConfigurationIntent()).thenReturn(i1);
        when(d2.dependencyConfigurationIntent()).thenReturn(i2);

        underTest.onCreate(null);

        assertTrue(underTest.isFinishing);
        assertFalse(underTest.intentStack.isEmpty());
        LinkedList<Intent> expected = new LinkedList<>();
        expected.push(underTest.thisIntent);
        expected.push(i1);
        expected.push(i2);
        assertEquals(expected, underTest.intentStack);
    }

    static class SyntethicActivity extends DependencyConfigurationAgnosticActivity {
        LinkedList<Intent> intentStack = new LinkedList<>();
        boolean isFinishing = false;
        Intent thisIntent = new Intent("activity");
        Scope scope;

        public SyntethicActivity(Scope scope) {
            this.scope = scope;
        }

        @Override public void startActivity(Intent intent) {
            intentStack.push(intent);
        }

        @Override public void startActivities(Intent[] intents) {
            for (Intent intent : intents)
                intentStack.push(intent);
        }

        @Override public void finish() {
            isFinishing = true;
        }

        @Override public Intent getIntent() {
            return thisIntent;
        }

        @Override protected Scope getScope() {
            return scope;
        }
    }
}