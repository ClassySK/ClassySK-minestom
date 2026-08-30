package com.novystxr.classysk.api.classes;

import ch.njol.skript.classes.ClassInfo;
import net.bytebuddy.asm.Advice;

import java.util.regex.Matcher;

public class TypedClassAdvice {

    @Advice.OnMethodExit
    static void onExit(@Advice.Argument(0) String input, @Advice.Return(readOnly = false) ClassInfo<?> result) {
        if (result != null) return;
        Matcher matcher = AdviceBridge.pattern.matcher(input);

        if (matcher.matches()) {
            result = (ClassInfo<?>) AdviceBridge.processClassInfoResult.apply(matcher.group(1));
        }
    }
}
