package net.magicterra.winefoxsspellbooks.compat.jech;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Locale;
import javax.annotation.Nullable;
import net.magicterra.winefoxsspellbooks.WinefoxsSpellbooks;
import net.neoforged.fml.ModList;

/**
 * 搜索框的文本匹配：先按小写子串匹配；装了 Just Enough Characters 时再交给它做拼音匹配，
 * 沿用该模组自身的设置（全拼、简拼、模糊音、键盘方案）。
 * <p>
 * 反射调用 {@code me.towdium.jecharacters.utils.Match#contains(String, CharSequence)}，不引入编译依赖。
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-10-24
 */
public final class PinyinMatch {
    private static final String MOD_ID = "jecharacters";

    private static final String MATCH_CLASS = "me.towdium.jecharacters.utils.Match";

    @Nullable
    private static MethodHandle contains;

    private static boolean resolved;

    private PinyinMatch() {
    }

    /**
     * 判断 {@code text} 是否包含 {@code query}。
     *
     * @param text  被搜索的文本，如法术名、id
     * @param query 已去除首尾空白并转为小写的搜索词，空串恒为真
     */
    public static boolean contains(String text, String query) {
        if (query.isEmpty() || text.toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        MethodHandle handle = handle();
        if (handle == null) {
            return false;
        }
        try {
            return (boolean) handle.invokeExact(text, (CharSequence) query);
        } catch (Throwable e) {
            // 调用失败后不再尝试，免得每帧刷日志
            WinefoxsSpellbooks.LOGGER.warn("Just Enough Characters 拼音匹配调用失败，已停用拼音搜索：", e);
            contains = null;
            return false;
        }
    }

    @Nullable
    private static MethodHandle handle() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded(MOD_ID)) {
                try {
                    Class<?> match = Class.forName(MATCH_CLASS);
                    contains = MethodHandles.publicLookup().findStatic(match, "contains",
                        MethodType.methodType(boolean.class, String.class, CharSequence.class));
                } catch (ReflectiveOperationException e) {
                    WinefoxsSpellbooks.LOGGER.warn("已安装 Just Enough Characters，但未找到其拼音匹配方法，拼音搜索不会生效：", e);
                }
            }
        }
        return contains;
    }
}
