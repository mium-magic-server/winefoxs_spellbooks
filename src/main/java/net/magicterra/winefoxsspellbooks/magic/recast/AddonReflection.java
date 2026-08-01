package net.magicterra.winefoxsspellbooks.magic.recast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 附属模组的反射访问层。所有目标须先 {@link #declareClass} / {@link #declareMethod} / {@link #declareField} 声明，
 * {@link #selfCheck()} 在服务器启动时校验：模组已安装但成员缺失才 WARN，模组未安装则静默。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-07-29
 */
public final class AddonReflection {
    private AddonReflection() {
    }

    /**
     * 声明目标的种类，决定 {@link #selfCheck()} 用哪种方式去查。
     */
    private enum Kind {
        CLASS, METHOD, FIELD
    }

    /**
     * 一个声明过的反射目标，供 {@link #selfCheck()} 逐个验证。
     */
    private record Declaration(String modId, String className, Kind kind, String memberName, Class<?>[] paramTypes) {
    }

    private static final List<Declaration> DECLARATIONS = new ArrayList<>();

    private static final Map<String, Optional<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();

    // ==================== 声明（在各 handler 的静态初始化里调用） ====================

    /**
     * 声明一个要反射访问的附属模组类。
     *
     * @param modId     该类所属模组的 id；模组未安装时自检跳过，不报错
     * @param className 全限定类名
     */
    public static void declareClass(String modId, String className) {
        DECLARATIONS.add(new Declaration(modId, className, Kind.CLASS, null, null));
    }

    /**
     * 声明一个要反射调用的方法。构造器用 {@code "<init>"} 作为 {@code memberName}。
     *
     * @param modId      该类所属模组的 id
     * @param className  全限定类名
     * @param memberName 方法名，构造器传 {@code "<init>"}
     * @param paramTypes 参数类型，用于精确定位重载
     */
    public static void declareMethod(String modId, String className, String memberName, Class<?>... paramTypes) {
        DECLARATIONS.add(new Declaration(modId, className, Kind.METHOD, memberName, paramTypes));
    }

    /**
     * 声明一个要反射读取的 public 静态字段。只声明类查不出字段改名——类还在，字段没了，
     * {@link #staticField} 只会静默返回 empty。
     *
     * @param modId     该类所属模组的 id
     * @param className 全限定类名
     * @param fieldName 字段名
     */
    public static void declareField(String modId, String className, String fieldName) {
        DECLARATIONS.add(new Declaration(modId, className, Kind.FIELD, fieldName, null));
    }

    // ==================== 访问 ====================

    /**
     * 解析类，结果缓存。模组未安装时返回 empty，不抛异常。
     */
    public static Optional<Class<?>> clazz(String className) {
        return CLASS_CACHE.computeIfAbsent(className, name -> {
            try {
                return Optional.of(Class.forName(name));
            } catch (ClassNotFoundException | LinkageError e) {
                return Optional.empty();
            }
        });
    }

    /**
     * 反射构造一个对象。任何一步失败都返回 empty 并留一条 debug 日志—— 真正的告警由 {@link #selfCheck()} 在启动时发出，运行时不刷屏。
     */
    public static Optional<Object> construct(String className, Class<?>[] paramTypes, Object... args) {
        Optional<Class<?>> type = clazz(className);
        if (type.isEmpty()) {
            return Optional.empty();
        }
        try {
            Constructor<?> ctor = type.get().getConstructor(paramTypes);
            return Optional.of(ctor.newInstance(args));
        } catch (ReflectiveOperationException | RuntimeException e) {
            WinefoxsSpellbooks.LOGGER.debug("Reflective construction of {} failed: {}", className, e.toString());
            return Optional.empty();
        }
    }

    /**
     * 反射调用一个方法。
     *
     * @return 调用是否真的发生了；调用方应检查返回值。
     */
    public static boolean invoke(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        if (target == null) {
            return false;
        }
        try {
            Method method = target.getClass().getMethod(methodName, paramTypes);
            method.invoke(target, args);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            WinefoxsSpellbooks.LOGGER.debug("Reflective call {}#{} failed: {}",
                target.getClass().getName(), methodName, e.toString());
            return false;
        }
    }

    /**
     * 读一个 public 静态字段（附属模组的注册表 Holder 多是这种形状）。
     */
    public static Optional<Object> staticField(String className, String fieldName) {
        Optional<Class<?>> type = clazz(className);
        if (type.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(type.get().getField(fieldName).get(null));
        } catch (ReflectiveOperationException | RuntimeException e) {
            WinefoxsSpellbooks.LOGGER.debug("Reflective read of {}#{} failed: {}", className, fieldName, e.toString());
            return Optional.empty();
        }
    }

    /**
     * 读一个 public 静态 {@link DeferredHolder} 字段并解出它持有的对象。
     * <p>
     * 附属模组的注册表基本都是这个形状；字段不在、或持有的不是 Holder 时返回 empty。
     */
    public static Optional<Object> holderValue(String className, String fieldName) {
        return staticField(className, fieldName)
            .filter(DeferredHolder.class::isInstance)
            .map(holder -> ((DeferredHolder<?, ?>) holder).get());
    }

    /**
     * 同上，并把结果收窄到期望类型。字段不在、不是 Holder、或持有的类型不符时一律返回 empty， 调用方直接 {@code orElse} 一个兜底值即可，不必自己再 instanceof 一遍。
     */
    public static <T> Optional<T> holderValue(String className, String fieldName, Class<T> expectedType) {
        return holderValue(className, fieldName)
            .filter(expectedType::isInstance)
            .map(expectedType::cast);
    }

    // ==================== 自检 ====================

    /**
     * 校验所有声明过的反射目标。在 {@code ServerStartingEvent} 上调用一次。
     * <p>
     * 只对<b>已安装</b>的模组报告缺失——模组没装本来就找不到，不是问题。
     */
    public static void selfCheck() {
        int missing = 0;
        for (Declaration declaration : DECLARATIONS) {
            if (!ModList.get().isLoaded(declaration.modId())) {
                continue;
            }
            Optional<Class<?>> type = clazz(declaration.className());
            if (type.isEmpty()) {
                WinefoxsSpellbooks.LOGGER.warn(
                    "[{}] is installed but class '{}' was not found. A recast effect will silently do nothing. "
                        + "The addon most likely renamed or moved it; update the reference in magic/recast/addon/.",
                    declaration.modId(), declaration.className());
                missing++;
                continue;
            }
            if (declaration.kind() == Kind.CLASS) {
                continue;
            }
            if (!memberExists(type.get(), declaration)) {
                WinefoxsSpellbooks.LOGGER.warn(
                    "[{}] class '{}' exists but member '{}' does not. That reference is skipped at runtime "
                        + "(e.g. a projectile spawns with default damage, or a visual effect never appears). "
                        + "Update magic/recast/addon/.",
                    declaration.modId(), declaration.className(), declaration.memberName());
                missing++;
            }
        }
        if (missing == 0) {
            WinefoxsSpellbooks.LOGGER.debug("Addon recast reflection self-check passed ({} declarations).",
                DECLARATIONS.size());
        } else {
            WinefoxsSpellbooks.LOGGER.warn("Addon recast reflection self-check found {} broken reference(s); "
                + "the affected recast effects are disabled.", missing);
        }
    }

    private static boolean memberExists(Class<?> type, Declaration declaration) {
        try {
            if (declaration.kind() == Kind.FIELD) {
                type.getField(declaration.memberName());
            } else if ("<init>".equals(declaration.memberName())) {
                type.getConstructor(declaration.paramTypes());
            } else {
                type.getMethod(declaration.memberName(), declaration.paramTypes());
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
