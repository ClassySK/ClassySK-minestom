package com.novystxr.classysk.api.classes;

import ch.njol.skript.classes.ClassInfo;
import net.bytebuddy.asm.Advice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypedClassAdvice {

    public static final Pattern pattern = Pattern.compile("(\\w*) instances?");

    @Advice.OnMethodExit
    static void onExit(@Advice.Argument(0) String input, @Advice.Return(readOnly = false) ClassInfo<?> result) {
        if (result != null) return;
        Matcher matcher = AdviceBridge.pattern.matcher(input);

        if (matcher.matches()) {
            result = (ClassInfo<?>) AdviceBridge.processClassInfoResult.apply(matcher.group(1));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ClassInstance> ClassInfo<T> getClassInfo(Class<T> subclass) {
        return new ClassInfo<>(subclass, "classinstance")
            .name("Class Instance")
            .serializeAs(ClassInstance.class)
            .parser((Parser<T>) Types.classParser);
    }
}
