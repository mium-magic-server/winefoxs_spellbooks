package net.magicterra.winefoxsspellbooks.entity.loadout.data;

/**
 * 扫帚女仆模式
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2025-01-20
 */
public enum BroomMode {
    /**
     * 强制使用扫帚（100% 概率）
     */
    ALWAYS("always"),

    /**
     * 永不使用扫帚（0% 概率）
     */
    NEVER("never"),

    /**
     * 使用 Config 中的概率配置（默认行为）
     */
    DEFAULT("default");

    private final String name;

    BroomMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 从字符串解析 BroomMode
     */
    public static BroomMode fromString(String name) {
        for (BroomMode mode : values()) {
            if (mode.name.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return DEFAULT;
    }
}
