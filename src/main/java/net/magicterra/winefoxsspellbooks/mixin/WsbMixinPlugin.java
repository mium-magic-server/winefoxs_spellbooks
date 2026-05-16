package net.magicterra.winefoxsspellbooks.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * 通用软依赖 mixin 开关。
 * <p>
 * 约定：把每个软依赖目标 mod 的 mixin 放在
 * {@code net.magicterra.winefoxsspellbooks.mixin.<modid>.*} 子包下，并照常登记进
 * {@code winefoxs_spellbooks.mixins.json}；插件自动从 mixin 类名解析出 {@code <modid>}
 * 并查 {@link LoadingModList} 判断是否应用。
 * <p>
 * 示例：{@code net.magicterra.winefoxsspellbooks.mixin.ali.ConfigUtilsMixin}
 * → 解析出 mod id {@code "ali"} → ALI 已加载才应用。
 *
 * @author Gardel
 * @since 2026-05-16
 */
public class WsbMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_MARKER = ".mixin.";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String modId = extractRequiredModId(mixinClassName);
        if (modId == null) {
            // 类名不符合"mixin.<modid>.*"约定（应不存在）：保守起见放行，由 @Pseudo 兜底
            return true;
        }
        LoadingModList list = LoadingModList.get();
        return list != null && list.getModFileById(modId) != null;
    }

    /**
     * 从 mixin 全限定类名里截出"mixin"包后的第一段当成 mod id。
     * 返回 {@code null} 表示类名直接位于 {@code mixin} 包根下、没有子包。
     */
    private static String extractRequiredModId(String mixinClassName) {
        int markerIdx = mixinClassName.indexOf(MIXIN_PACKAGE_MARKER);
        if (markerIdx < 0) {
            return null;
        }
        int subPkgStart = markerIdx + MIXIN_PACKAGE_MARKER.length();
        int subPkgEnd = mixinClassName.indexOf('.', subPkgStart);
        if (subPkgEnd < 0) {
            // 形如 ...mixin.SomeMixin，没有子包 → 不是软依赖 mixin
            return null;
        }
        return mixinClassName.substring(subPkgStart, subPkgEnd);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
