package com.novystxr.classysk;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import com.novystxr.classysk.api.classes.*;
import com.novystxr.classysk.api.fields.SerializableField;
import com.novystxr.classysk.api.util.Logger;
import com.novystxr.classysk.api.util.StringUtils;
import com.novystxr.classysk.api.util.TypedInstanceParser;
import com.novystxr.classysk.main.MainModule;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingInstrumentation.Target;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.util.Priority;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class Classysk extends JavaPlugin {

    public static final String NAME_PATTERN = "[\\w_]+";
    public static final String CLASSNAME_PATTERN = "[A-Z]\\w*";

    public static final Priority SHADOW_REALM = Priority.after(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

    public static boolean TYPES_ALLOWED = false;

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onEnable() {
        // skript-minestom's class loading differs from bukkit in that we can't reference anything classysk related from the advice itself
        // so we need to use a bridge that handles the internal stuff and inject it into the bootstrap classloader to ensure skript-minestom knows about it
        try {
            Instrumentation agent = ByteBuddyAgent.install();
            ClassInjector.UsingInstrumentation
                .of(new File("."), Target.BOOTSTRAP, agent)
                .inject(Collections.singletonMap(
                    new TypeDescription.ForLoadedType(AdviceBridge.class),
                    ClassFileLocator.ForClassLoader.read(AdviceBridge.class)
                ));

            Class<?> bridge = Class.forName("com.novystxr.classysk.api.classes.AdviceBridge", true, null);
            bridge.getDeclaredField("pattern").set(null, Pattern.compile("("+CLASSNAME_PATTERN+") instances?"));
            bridge.getDeclaredField("processClassInfoResult").set(null, (Function<String, Object>) matched -> {
                matched = StringUtils.getLowerCase(matched);
                Class<? extends TypedInstanceWrapper> subclass = ClassManager.getSubclass(matched);

                return new ClassInfo<>(subclass, "typedinstance")
                    .name("Typed Instance Wrapper")
                    .serializeAs(ClassInstance.class)
                    .parser(new TypedInstanceParser<>());
            });

            new ByteBuddy()
                .redefine(Classes.class)
                .visit(Advice.to(TypedClassAdvice.class).on(ElementMatchers.named("getClassInfoFromUserInput")))
                .make()
                .load(Classes.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            TYPES_ALLOWED = true;
        } catch (IllegalStateException e) {
            Logger.log("<RED>The ByteBuddy agent failed to install, dynamic agent loading has likely been disabled for this JVM.",
            "The plugin will operate as normal but class-specific types will not be available.",
            "To enable this feature, add <YELLOW>-XX:+EnableDynamicAgentLoading</YELLOW> to your JVM startup flags.<BR>");
            Logger.warning(e.toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Variables.yggdrasil.registerSingleClass(SerializableField.class, "SerializableField");
        SkriptAddon addon = Skript.instance().registerAddon(Classysk.class, "ClassySK");

        addon.localizer().setSourceDirectories("lang", null);
        addon.loadModules(new MainModule());
    }
}
