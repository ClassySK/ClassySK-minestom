package com.novystxr.classysk.api.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.*;
import com.novystxr.classysk.api.classes.ClassInstance;
import com.novystxr.classysk.main.elements.Types;

import java.lang.reflect.Field;
import java.util.Map;

public class ReflectUtils {
    private static final Field exactClassInfos;
    private static final Field superClassInfos;
    private static final Field localizedLanguage;

    static {
        try {
            exactClassInfos = Classes.class.getDeclaredField("exactClassInfos");
            exactClassInfos.setAccessible(true);

            superClassInfos = Classes.class.getDeclaredField("superClassInfos");
            superClassInfos.setAccessible(true);

            localizedLanguage = Language.class.getDeclaredField("localizedLanguage");
            localizedLanguage.setAccessible(true);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ClassInstance> void registerClassInfo(String name, Class<T> clazz) {
        try {
            var exactClassInfosMap = (Map<Class<?>, ClassInfo<?>>) exactClassInfos.get(null);
            if (exactClassInfosMap.containsKey(clazz)) {
                return;
            }
            name = StringUtils.getLowerCase(name);
            String codename = name+"classinstance";

            var localizedLanguageMap = (Map<String, String>) localizedLanguage.get(null);
            localizedLanguageMap.put("types."+codename, StringUtils.titleCase(name) + " instance");

            ClassInfo<?> info = new ClassInfo<>(clazz, codename)
                .serializeAs(ClassInstance.class)
                .parser((Parser<? extends T>) Types.classParser);

            var superClassInfoMap = (Map<Class<?>, ClassInfo<?>>) superClassInfos.get(null);
            exactClassInfosMap.put(clazz, info);
            superClassInfoMap.put(clazz, info);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
