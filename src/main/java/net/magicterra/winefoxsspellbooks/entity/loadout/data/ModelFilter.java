package net.magicterra.winefoxsspellbooks.entity.loadout.data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

/**
 * 模型过滤器，控制女仆可用模型
 *
 * @param mode   过滤模式: all/allowlist/denylist
 * @param models 模型 ID 列表（allowlist/denylist 时使用）
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public record ModelFilter(Mode mode, List<ResourceLocation> models) {
    /**
     * 默认过滤器，允许所有模型
     */
    public static final ModelFilter ALL = new ModelFilter(Mode.ALL, List.of());

    /**
     * 过滤模式枚举
     */
    public enum Mode {
        /** 允许所有模型 */
        ALL("all"),
        /** 仅允许列表中的模型 */
        ALLOWLIST("allowlist"),
        /** 排除列表中的模型 */
        DENYLIST("denylist");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Mode fromString(String name) {
            for (Mode mode : values()) {
                if (mode.name.equals(name)) {
                    return mode;
                }
            }
            return ALL;
        }
    }

    /**
     * 创建允许列表过滤器
     *
     * @param models 允许的模型列表
     * @return ModelFilter
     */
    public static ModelFilter allowlist(List<ResourceLocation> models) {
        return new ModelFilter(Mode.ALLOWLIST, models);
    }

    /**
     * 创建拒绝列表过滤器
     *
     * @param models 拒绝的模型列表
     * @return ModelFilter
     */
    public static ModelFilter denylist(List<ResourceLocation> models) {
        return new ModelFilter(Mode.DENYLIST, models);
    }

    /**
     * 过滤可用模型集合
     *
     * @param availableModels 可用模型集合
     * @return 过滤后的模型集合
     */
    public Set<ResourceLocation> filterModels(Set<ResourceLocation> availableModels) {
        return switch (mode) {
            case ALL -> availableModels;
            case ALLOWLIST -> availableModels.stream()
                .filter(models::contains)
                .collect(Collectors.toSet());
            case DENYLIST -> availableModels.stream()
                .filter(model -> !models.contains(model))
                .collect(Collectors.toSet());
        };
    }
}
